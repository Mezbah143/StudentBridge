import java.io.IOException;
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

@WebServlet("/ApplyServlet")
public class ApplyServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("userEmail") == null) {
            response.sendRedirect("frontend/login.html?error=loginRequired&source=apply");
            return;
        }

        String accountType = clean((String) session.getAttribute("accountType"));

        if (!"Student".equalsIgnoreCase(accountType)) {
            response.sendRedirect("frontend/jobsearch.html?apply=notStudent");
            return;
        }

        int jobId = parseJobId(request.getParameter("jobId"));

        if (jobId <= 0) {
            response.sendRedirect("frontend/jobsearch.html?apply=missingJob");
            return;
        }

        String studentEmail = clean((String) session.getAttribute("userEmail"));

        try (Connection con = DBConnection.getConnection()) {
            if (con == null) {
                response.sendRedirect("frontend/jobsearch.html?apply=db");
                return;
            }

            DatabaseSchemaManager.ensureApplicationsTable(con);

            if (!jobExists(con, jobId)) {
                response.sendRedirect("frontend/jobsearch.html?apply=missingJob");
                return;
            }

            String cvLink = getStudentCvLink(con, studentEmail);

            if (cvLink.isEmpty()) {
                response.sendRedirect("frontend/student-profile.html?error=cvRequired");
                return;
            }

            if (alreadyApplied(con, jobId, studentEmail)) {
                response.sendRedirect("frontend/jobsearch.html?apply=duplicate");
                return;
            }

            saveApplication(con, jobId, studentEmail, cvLink);
            response.sendRedirect("frontend/jobsearch.html?apply=success");

        } catch (SQLException e) {
            e.printStackTrace();

            if (isDuplicateApplication(e)) {
                response.sendRedirect("frontend/jobsearch.html?apply=duplicate");
            } else {
                response.sendRedirect("frontend/jobsearch.html?apply=database");
            }
        }
    }

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        response.sendRedirect("frontend/jobsearch.html");
    }

    private boolean jobExists(Connection con,
                              int jobId) throws SQLException {

        String sql = "SELECT id FROM jobs WHERE id = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, jobId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private String getStudentCvLink(Connection con,
                                    String studentEmail) throws SQLException {

        String sql = "SELECT cv_link FROM student_profiles " +
                "WHERE user_email = ? ORDER BY id DESC LIMIT 1";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, studentEmail);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return clean(rs.getString("cv_link"));
                }
            }
        }

        return "";
    }

    private boolean alreadyApplied(Connection con,
                                   int jobId,
                                   String studentEmail) throws SQLException {

        String sql = "SELECT id FROM applications WHERE job_id = ? AND student_email = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, jobId);
            ps.setString(2, studentEmail);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private void saveApplication(Connection con,
                                 int jobId,
                                 String studentEmail,
                                 String cvLink) throws SQLException {

        String sql = "INSERT INTO applications (job_id, student_email, cv_link) " +
                "VALUES (?, ?, ?)";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, jobId);
            ps.setString(2, studentEmail);
            ps.setString(3, cvLink);
            ps.executeUpdate();
        }
    }

    private int parseJobId(String value) {
        try {
            return Integer.parseInt(clean(value));
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private boolean isDuplicateApplication(SQLException e) {
        String message = e.getMessage() == null ? "" : e.getMessage().toLowerCase();
        return "23000".equals(e.getSQLState()) && message.contains("duplicate");
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }
}
