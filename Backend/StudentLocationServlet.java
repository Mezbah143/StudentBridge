import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/StudentLocationServlet")
public class StudentLocationServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        // UTF-8 support
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        // Get session
        HttpSession session = request.getSession(false);

        // Check login
        if (session == null
                || session.getAttribute("userEmail") == null) {

            response.sendRedirect(
                    "frontend/login.html?error=loginRequired"
            );

            return;
        }

        // Check account type
        String accountType = clean(
                (String) session.getAttribute("accountType")
        );

        if (!"Student".equalsIgnoreCase(accountType)) {

            response.sendRedirect(
                    "frontend/student-profile.html?error=notStudent"
            );

            return;
        }

        // Get form data
        String userEmail = clean(
                (String) session.getAttribute("userEmail")
        );

        String address = clean(
                request.getParameter("studentAddress")
        );

        BigDecimal latitude = parseDecimal(
                request.getParameter("studentLatitude")
        );

        BigDecimal longitude = parseDecimal(
                request.getParameter("studentLongitude")
        );

        // Validation
        if (address.isEmpty()) {

            response.sendRedirect(
                    "frontend/student-profile.html?error=address"
            );

            return;
        }

        if (latitude == null || longitude == null) {

            response.sendRedirect(
                    "frontend/student-profile.html?error=map"
            );

            return;
        }

        try (Connection con = DBConnection.getConnection()) {

            if (con == null) {

                response.sendRedirect(
                        "frontend/student-profile.html?error=db"
                );

                return;
            }

            con.setAutoCommit(false);

            saveStudentLocation(
                    con,
                    userEmail,
                    address,
                    latitude,
                    longitude
            );

            con.commit();

            response.sendRedirect(
                    "frontend/student-profile.html?success=1"
            );

        } catch (SQLException e) {

            e.printStackTrace();

            response.sendRedirect(
                    "frontend/student-profile.html?error=database"
            );
        }
    }

    private void saveStudentLocation(
            Connection con,
            String userEmail,
            String address,
            BigDecimal latitude,
            BigDecimal longitude
    ) throws SQLException {

        // Try update first
        String updateSql =
                "UPDATE student_profiles " +
                "SET address = ?, latitude = ?, longitude = ? " +
                "WHERE user_email = ?";

        try (PreparedStatement ps =
                     con.prepareStatement(updateSql)) {

            ps.setString(1, address);

            setNullableDecimal(ps, 2, latitude);
            setNullableDecimal(ps, 3, longitude);

            ps.setString(4, userEmail);

            int rows = ps.executeUpdate();

            // Already updated
            if (rows > 0) {
                return;
            }
        }

        // Insert if profile does not exist
        String insertSql =
                "INSERT INTO student_profiles " +
                "(user_email, address, latitude, longitude) " +
                "VALUES (?, ?, ?, ?)";

        try (PreparedStatement ps =
                     con.prepareStatement(insertSql)) {

            ps.setString(1, userEmail);
            ps.setString(2, address);

            setNullableDecimal(ps, 3, latitude);
            setNullableDecimal(ps, 4, longitude);

            ps.executeUpdate();
        }
    }

    private String clean(String value) {

        return value == null
                ? ""
                : value.trim();
    }

    private BigDecimal parseDecimal(String value) {

        String cleanedValue = clean(value);

        if (cleanedValue.isEmpty()) {
            return null;
        }

        try {

            return new BigDecimal(cleanedValue);

        } catch (NumberFormatException e) {

            return null;
        }
    }

    private void setNullableDecimal(
            PreparedStatement ps,
            int index,
            BigDecimal value
    ) throws SQLException {

        if (value == null) {

            ps.setNull(index, Types.DECIMAL);

        } else {

            ps.setBigDecimal(index, value);
        }
    }
}
