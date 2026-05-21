import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/StudentProfileServlet")
public class StudentProfileServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-store");

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("userEmail") == null) {
            sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Login required");
            return;
        }

        String accountType = clean((String) session.getAttribute("accountType"));

        if (!"Student".equalsIgnoreCase(accountType)) {
            sendError(response, HttpServletResponse.SC_FORBIDDEN, "Student account required");
            return;
        }

        String userEmail = clean((String) session.getAttribute("userEmail"));
        String userName = clean((String) session.getAttribute("user"));

        try (Connection con = DBConnection.getConnection()) {
            if (con == null) {
                sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "Database connection failed");
                return;
            }

            writeProfile(con, response, userEmail, userName, accountType);
        } catch (SQLException e) {
            e.printStackTrace();
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Unable to load student profile");
        }
    }

    private void writeProfile(Connection con,
                              HttpServletResponse response,
                              String userEmail,
                              String userName,
                              String accountType)
            throws SQLException, IOException {

        String sql = "SELECT university_name, major, student_id, preferred_job_category, " +
                "available_working_time, korean_language_level, address, latitude, longitude, " +
                "created_at FROM student_profiles WHERE user_email = ? ORDER BY id DESC LIMIT 1";

        try (PreparedStatement ps = con.prepareStatement(sql);
             PrintWriter out = response.getWriter()) {

            ps.setString(1, userEmail);

            try (ResultSet rs = ps.executeQuery()) {
                StringBuilder json = new StringBuilder();
                json.append("{");
                json.append("\"name\":\"").append(escapeJson(userName)).append("\",");
                json.append("\"email\":\"").append(escapeJson(userEmail)).append("\",");
                json.append("\"accountType\":\"").append(escapeJson(accountType)).append("\",");

                if (rs.next()) {
                    json.append("\"universityName\":\"").append(escapeJson(rs.getString("university_name"))).append("\",");
                    json.append("\"major\":\"").append(escapeJson(rs.getString("major"))).append("\",");
                    json.append("\"studentId\":\"").append(escapeJson(rs.getString("student_id"))).append("\",");
                    json.append("\"preferredJobCategory\":\"").append(escapeJson(rs.getString("preferred_job_category"))).append("\",");
                    json.append("\"availableWorkingTime\":\"").append(escapeJson(rs.getString("available_working_time"))).append("\",");
                    json.append("\"koreanLanguageLevel\":\"").append(escapeJson(rs.getString("korean_language_level"))).append("\",");
                    json.append("\"address\":\"").append(escapeJson(rs.getString("address"))).append("\",");
                    json.append("\"latitude\":").append(decimalOrNull(rs.getString("latitude"))).append(",");
                    json.append("\"longitude\":").append(decimalOrNull(rs.getString("longitude"))).append(",");
                    json.append("\"createdAt\":\"").append(escapeJson(rs.getString("created_at"))).append("\"");
                } else {
                    json.append("\"universityName\":\"\",");
                    json.append("\"major\":\"\",");
                    json.append("\"studentId\":\"\",");
                    json.append("\"preferredJobCategory\":\"\",");
                    json.append("\"availableWorkingTime\":\"\",");
                    json.append("\"koreanLanguageLevel\":\"\",");
                    json.append("\"address\":\"\",");
                    json.append("\"latitude\":null,");
                    json.append("\"longitude\":null,");
                    json.append("\"createdAt\":\"\"");
                }

                json.append("}");
                out.write(json.toString());
            }
        }
    }

    private void sendError(HttpServletResponse response,
                           int statusCode,
                           String message) throws IOException {

        response.setStatus(statusCode);
        response.getWriter().write("{\"error\":\"" + escapeJson(message) + "\"}");
    }

    private String decimalOrNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "null";
        }

        try {
            Double.parseDouble(value);
            return value;
        } catch (NumberFormatException e) {
            return "null";
        }
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
