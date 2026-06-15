import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

@WebServlet("/ProfileDocumentServlet")
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024,
        maxFileSize = 5 * 1024 * 1024,
        maxRequestSize = 6 * 1024 * 1024
)
public class ProfileDocumentServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (!hasLoggedInUser(session)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Login required");
            return;
        }

        int documentId = parseInt(request.getParameter("id"));

        if (documentId <= 0) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Document id required");
            return;
        }

        String userEmail = clean((String) session.getAttribute("userEmail"));
        String accountType = normalizeAccountType((String) session.getAttribute("accountType"));

        try (Connection con = DBConnection.getConnection()) {
            if (con == null) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "Database connection failed");
                return;
            }

            DatabaseSchemaManager.ensureProfileFeatureTables(con);
            writeDocument(con, response, documentId, userEmail, accountType);
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Unable to download document");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        prepareJsonResponse(response);

        HttpSession session = request.getSession(false);

        if (!hasLoggedInUser(session)) {
            sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Login required");
            return;
        }

        String documentType = clean(request.getParameter("documentType"));
        Part filePart = request.getPart("documentFile");

        if (!isAllowedDocumentType(documentType) || filePart == null || filePart.getSize() <= 0) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Document file required");
            return;
        }

        String fileName = cleanSubmittedFileName(filePart.getSubmittedFileName());

        if (fileName.isEmpty()) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "File name required");
            return;
        }

        String contentType = clean(filePart.getContentType());

        if (contentType.isEmpty()) {
            contentType = "application/octet-stream";
        }

        String userEmail = clean((String) session.getAttribute("userEmail"));
        String accountType = normalizeAccountType((String) session.getAttribute("accountType"));

        try (Connection con = DBConnection.getConnection()) {
            if (con == null) {
                sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "Database connection failed");
                return;
            }

            DatabaseSchemaManager.ensureProfileFeatureTables(con);
            saveDocument(con, userEmail, accountType, documentType, fileName, contentType, filePart);
            response.getWriter().write("{\"success\":true}");
        } catch (SQLException e) {
            e.printStackTrace();
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Unable to upload document");
        }
    }

    private void writeDocument(Connection con,
                               HttpServletResponse response,
                               int documentId,
                               String userEmail,
                               String accountType) throws SQLException, IOException {

        String sql = "SELECT file_name, content_type, file_size, file_data " +
                "FROM profile_documents WHERE id = ? AND user_email = ? AND account_type = ? LIMIT 1";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, documentId);
            ps.setString(2, userEmail);
            ps.setString(3, accountType);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "Document not found");
                    return;
                }

                String fileName = clean(rs.getString("file_name"));
                String contentType = clean(rs.getString("content_type"));

                response.setContentType(contentType.isEmpty()
                        ? "application/octet-stream"
                        : contentType);
                response.setHeader("Content-Disposition",
                        "attachment; filename=\"" + safeHeaderFileName(fileName) + "\"");
                response.setContentLengthLong(rs.getLong("file_size"));

                try (InputStream in = rs.getBinaryStream("file_data")) {
                    in.transferTo(response.getOutputStream());
                }
            }
        }
    }

    private void saveDocument(Connection con,
                              String userEmail,
                              String accountType,
                              String documentType,
                              String fileName,
                              String contentType,
                              Part filePart) throws SQLException, IOException {

        try (PreparedStatement delete = con.prepareStatement(
                "DELETE FROM profile_documents WHERE user_email = ? AND account_type = ? AND document_type = ?")) {
            delete.setString(1, userEmail);
            delete.setString(2, accountType);
            delete.setString(3, documentType);
            delete.executeUpdate();
        }

        String sql = "INSERT INTO profile_documents " +
                "(user_email, account_type, document_type, file_name, content_type, file_size, file_data) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = con.prepareStatement(sql);
             InputStream in = filePart.getInputStream()) {
            ps.setString(1, userEmail);
            ps.setString(2, accountType);
            ps.setString(3, documentType);
            ps.setString(4, fileName);
            ps.setString(5, contentType);
            ps.setLong(6, filePart.getSize());
            ps.setBinaryStream(7, in, filePart.getSize());
            ps.executeUpdate();
        }
    }

    private boolean isAllowedDocumentType(String documentType) {
        return "resume".equals(documentType)
                || "arc".equals(documentType)
                || "certificates".equals(documentType)
                || "business-registration".equals(documentType)
                || "company-certificate".equals(documentType);
    }

    private boolean hasLoggedInUser(HttpSession session) {
        return session != null && session.getAttribute("userEmail") != null;
    }

    private void prepareJsonResponse(HttpServletResponse response) {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=UTF-8");
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
    }

    private void sendError(HttpServletResponse response,
                           int statusCode,
                           String message) throws IOException {

        response.setStatus(statusCode);
        response.getWriter().write("{\"error\":\"" + escapeJson(message) + "\"}");
    }

    private String normalizeAccountType(String value) {
        return "Employer".equalsIgnoreCase(clean(value)) ? "Employer" : "Student";
    }

    private int parseInt(String value) {
        try {
            return Integer.parseInt(clean(value));
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private String cleanSubmittedFileName(String value) {
        String cleanValue = clean(value).replace("\\", "/");
        int slashIndex = cleanValue.lastIndexOf("/");
        return slashIndex >= 0 ? cleanValue.substring(slashIndex + 1) : cleanValue;
    }

    private String safeHeaderFileName(String value) {
        return clean(value).replace("\"", "").replace("\r", "").replace("\n", "");
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
