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

@WebServlet("/EmployerJobsServlet")
public class EmployerJobsServlet extends HttpServlet {

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

        if (!"Employer".equalsIgnoreCase(accountType)) {
            sendError(response, HttpServletResponse.SC_FORBIDDEN, "Employer account required");
            return;
        }

        String employerEmail = clean((String) session.getAttribute("userEmail"));

        try (Connection con = DBConnection.getConnection()) {
            if (con == null) {
                sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "Database connection failed");
                return;
            }

            try {
                writeJobs(con, response, employerEmail, true);
            } catch (SQLException e) {
                if (isMissingOptionalColumn(e)) {
                    writeJobs(con, response, employerEmail, false);
                } else {
                    throw e;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Unable to load employer jobs");
        }
    }

    private void writeJobs(Connection con,
                           HttpServletResponse response,
                           String employerEmail,
                           boolean includeExtendedColumns)
            throws SQLException, IOException {

        String sql;

        if (includeExtendedColumns) {
            sql = "SELECT id, title, company, location, category, type, salary, " +
                    "description, working_hours, requirements, contact_email, contact_phone, " +
                    "company_details, application_deadline, logo_url, address, latitude, " +
                    "longitude, created_at FROM jobs WHERE employer_email = ? ORDER BY id DESC";
        } else {
            sql = "SELECT id, title, company, location, category, type, salary, " +
                    "description FROM jobs WHERE employer_email = ? ORDER BY id DESC";
        }

        try (PreparedStatement ps = con.prepareStatement(sql);
             PrintWriter out = response.getWriter()) {

            ps.setString(1, employerEmail);

            try (ResultSet rs = ps.executeQuery()) {
                StringBuilder json = new StringBuilder();
                json.append("[");

                boolean first = true;

                while (rs.next()) {
                    if (!first) {
                        json.append(",");
                    }

                    json.append("{");
                    json.append("\"id\":").append(rs.getInt("id")).append(",");
                    json.append("\"title\":\"").append(escapeJson(rs.getString("title"))).append("\",");
                    json.append("\"company\":\"").append(escapeJson(rs.getString("company"))).append("\",");
                    json.append("\"location\":\"").append(escapeJson(rs.getString("location"))).append("\",");
                    json.append("\"category\":\"").append(escapeJson(rs.getString("category"))).append("\",");
                    json.append("\"type\":\"").append(escapeJson(rs.getString("type"))).append("\",");
                    json.append("\"salary\":\"").append(escapeJson(rs.getString("salary"))).append("\",");
                    json.append("\"description\":\"").append(escapeJson(rs.getString("description"))).append("\",");

                    if (includeExtendedColumns) {
                        json.append("\"workingHours\":\"").append(escapeJson(rs.getString("working_hours"))).append("\",");
                        json.append("\"requirements\":\"").append(escapeJson(rs.getString("requirements"))).append("\",");
                        json.append("\"contactEmail\":\"").append(escapeJson(rs.getString("contact_email"))).append("\",");
                        json.append("\"contactPhone\":\"").append(escapeJson(rs.getString("contact_phone"))).append("\",");
                        json.append("\"companyDetails\":\"").append(escapeJson(rs.getString("company_details"))).append("\",");
                        json.append("\"applicationDeadline\":\"").append(escapeJson(rs.getString("application_deadline"))).append("\",");
                        json.append("\"logoUrl\":\"").append(escapeJson(rs.getString("logo_url"))).append("\",");
                        json.append("\"address\":\"").append(escapeJson(rs.getString("address"))).append("\",");
                        json.append("\"latitude\":").append(decimalOrNull(rs.getString("latitude"))).append(",");
                        json.append("\"longitude\":").append(decimalOrNull(rs.getString("longitude"))).append(",");
                        json.append("\"createdAt\":\"").append(escapeJson(rs.getString("created_at"))).append("\"");
                    } else {
                        json.append("\"workingHours\":\"\",");
                        json.append("\"requirements\":\"\",");
                        json.append("\"contactEmail\":\"\",");
                        json.append("\"contactPhone\":\"\",");
                        json.append("\"companyDetails\":\"\",");
                        json.append("\"applicationDeadline\":\"\",");
                        json.append("\"logoUrl\":\"\",");
                        json.append("\"address\":\"\",");
                        json.append("\"latitude\":null,");
                        json.append("\"longitude\":null,");
                        json.append("\"createdAt\":\"\"");
                    }

                    json.append("}");
                    first = false;
                }

                json.append("]");
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

    private boolean isMissingOptionalColumn(SQLException e) {
        String message = e.getMessage() == null ? "" : e.getMessage().toLowerCase();

        return "42S22".equals(e.getSQLState())
                || message.contains("working_hours")
                || message.contains("requirements")
                || message.contains("contact_email")
                || message.contains("contact_phone")
                || message.contains("company_details")
                || message.contains("application_deadline")
                || message.contains("logo_url")
                || message.contains("address")
                || message.contains("latitude")
                || message.contains("longitude")
                || message.contains("created_at");
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
