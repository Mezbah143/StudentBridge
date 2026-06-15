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

@WebServlet("/ProfileServlet")
public class ProfileServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        prepareJsonResponse(response);

        HttpSession session = request.getSession(false);

        if (!hasLoggedInUser(session)) {
            sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Login required");
            return;
        }

        String userEmail = clean((String) session.getAttribute("userEmail"));
        String userName = clean((String) session.getAttribute("user"));
        String accountType = normalizeAccountType((String) session.getAttribute("accountType"));

        try (Connection con = DBConnection.getConnection()) {
            if (con == null) {
                sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "Database connection failed");
                return;
            }

            DatabaseSchemaManager.ensureProfileFeatureTables(con);
            writeProfile(con, response, userEmail, userName, accountType);
        } catch (SQLException e) {
            e.printStackTrace();
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Unable to load profile");
        }
    }

    private void writeProfile(Connection con,
                              HttpServletResponse response,
                              String userEmail,
                              String userName,
                              String accountType)
            throws SQLException, IOException {

        UserRecord user = loadUser(con, userEmail, userName, accountType);
        RoleProfile roleProfile = "Employer".equals(user.accountType)
                ? loadEmployerProfile(con, user.email)
                : loadStudentProfile(con, user.email);
        Stats stats = "Employer".equals(user.accountType)
                ? loadEmployerStats(con, user.email)
                : loadStudentStats(con, user.email);

        try (PrintWriter out = response.getWriter()) {
            out.write("{");
            out.write("\"loggedIn\":true,");
            out.write("\"name\":\"" + escapeJson(user.name) + "\",");
            out.write("\"email\":\"" + escapeJson(user.email) + "\",");
            out.write("\"phone\":\"" + escapeJson(user.phone) + "\",");
            out.write("\"accountType\":\"" + escapeJson(user.accountType) + "\",");
            out.write("\"subtitle\":\"" + escapeJson(roleProfile.subtitle) + "\",");
            out.write("\"address\":\"" + escapeJson(roleProfile.address) + "\",");
            out.write("\"stats\":{");
            out.write("\"primary\":" + stats.primary + ",");
            out.write("\"secondary\":" + stats.secondary + ",");
            out.write("\"messages\":" + stats.messages);
            out.write("},");
            out.write("\"documents\":" + listDocuments(con, user.email, user.accountType));
            out.write("}");
        }
    }

    private UserRecord loadUser(Connection con,
                                String userEmail,
                                String sessionName,
                                String sessionAccountType) throws SQLException {

        String sql = "SELECT name, email, phone, account_type FROM users WHERE email = ? LIMIT 1";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, userEmail);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new UserRecord(
                            fallback(clean(rs.getString("name")), sessionName),
                            clean(rs.getString("email")),
                            clean(rs.getString("phone")),
                            normalizeAccountType(rs.getString("account_type"))
                    );
                }
            }
        }

        return new UserRecord(
                fallback(sessionName, userEmail),
                userEmail,
                "",
                normalizeAccountType(sessionAccountType)
        );
    }

    private RoleProfile loadStudentProfile(Connection con,
                                           String userEmail) throws SQLException {

        String sql = "SELECT university_name, major, address FROM student_profiles " +
                "WHERE user_email = ? ORDER BY id DESC LIMIT 1";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, userEmail);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String university = fallback(clean(rs.getString("university_name")),
                            "Ulsan College");
                    String major = fallback(clean(rs.getString("major")), "IT");
                    String subtitle = major + " Student | " + university;
                    return new RoleProfile(subtitle, clean(rs.getString("address")));
                }
            }
        }

        return new RoleProfile("IT Student | Ulsan College", "");
    }

    private RoleProfile loadEmployerProfile(Connection con,
                                            String userEmail) throws SQLException {

        String sql = "SELECT business_name, business_location, business_type FROM employer_profiles " +
                "WHERE user_email = ? ORDER BY id DESC LIMIT 1";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, userEmail);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String business = fallback(clean(rs.getString("business_name")),
                            "StudentBridge Employer");
                    String type = fallback(clean(rs.getString("business_type")), "Employer");
                    String subtitle = type + " | " + business;
                    return new RoleProfile(subtitle, clean(rs.getString("business_location")));
                }
            }
        }

        return new RoleProfile("Employer | StudentBridge", "");
    }

    private Stats loadStudentStats(Connection con,
                                   String userEmail) throws SQLException {

        return new Stats(
                count(con, "SELECT COUNT(*) FROM applications WHERE student_email = ?", userEmail),
                count(con, "SELECT COUNT(*) FROM saved_jobs WHERE student_email = ?", userEmail),
                countUnreadMessages(con, userEmail, "Student")
        );
    }

    private Stats loadEmployerStats(Connection con,
                                    String userEmail) throws SQLException {

        int postedJobs = count(con, "SELECT COUNT(*) FROM jobs WHERE employer_email = ?", userEmail);
        int applicants = count(con,
                "SELECT COUNT(*) FROM applications a JOIN jobs j ON j.id = a.job_id " +
                        "WHERE j.employer_email = ?",
                userEmail);

        return new Stats(postedJobs, applicants, countUnreadMessages(con, userEmail, "Employer"));
    }

    private int countUnreadMessages(Connection con,
                                    String userEmail,
                                    String accountType) throws SQLException {

        String ownerColumn = "Employer".equals(accountType) ? "c.employer_email" : "c.student_email";
        String sql = "SELECT COUNT(*) FROM messages m " +
                "JOIN conversations c ON c.id = m.conversation_id " +
                "WHERE " + ownerColumn + " = ? " +
                "AND m.sender_email <> ? AND m.read_at IS NULL";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, userEmail);
            ps.setString(2, userEmail);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private int count(Connection con,
                      String sql,
                      String userEmail) throws SQLException {

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, userEmail);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private String listDocuments(Connection con,
                                 String userEmail,
                                 String accountType) throws SQLException {

        String sql = "SELECT id, document_type, file_name, content_type, file_size, created_at " +
                "FROM profile_documents WHERE user_email = ? AND account_type = ? " +
                "ORDER BY created_at DESC, id DESC";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, userEmail);
            ps.setString(2, accountType);

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
                    json.append("\"documentType\":\"").append(escapeJson(rs.getString("document_type"))).append("\",");
                    json.append("\"fileName\":\"").append(escapeJson(rs.getString("file_name"))).append("\",");
                    json.append("\"contentType\":\"").append(escapeJson(rs.getString("content_type"))).append("\",");
                    json.append("\"fileSize\":").append(rs.getLong("file_size")).append(",");
                    json.append("\"createdAt\":\"").append(escapeJson(rs.getString("created_at"))).append("\"");
                    json.append("}");

                    first = false;
                }

                json.append("]");
                return json.toString();
            }
        }
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

    private String fallback(String value,
                            String fallback) {
        return clean(value).isEmpty() ? clean(fallback) : clean(value);
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

    private static class UserRecord {
        private final String name;
        private final String email;
        private final String phone;
        private final String accountType;

        private UserRecord(String name,
                           String email,
                           String phone,
                           String accountType) {
            this.name = name;
            this.email = email;
            this.phone = phone;
            this.accountType = accountType;
        }
    }

    private static class RoleProfile {
        private final String subtitle;
        private final String address;

        private RoleProfile(String subtitle,
                            String address) {
            this.subtitle = subtitle;
            this.address = address;
        }
    }

    private static class Stats {
        private final int primary;
        private final int secondary;
        private final int messages;

        private Stats(int primary,
                      int secondary,
                      int messages) {
            this.primary = primary;
            this.secondary = secondary;
            this.messages = messages;
        }
    }
}
