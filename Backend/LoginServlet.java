import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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

@WebServlet("/LoginServlet")
public class LoginServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String email = req.getParameter("email");
        String password = req.getParameter("password");

        if (email == null || email.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            resp.sendRedirect("frontend/login.html?error=1");
            return;
        }

        try {
            Connection con = DBConnection.getConnection();

            if (con == null) {
                resp.getWriter().println("Database connection failed.");
                return;
            }

            String sql = "SELECT * FROM users WHERE email = ? AND password = ?";
            PreparedStatement ps = con.prepareStatement(sql);

            ps.setString(1, email);
            ps.setString(2, password);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                HttpSession session = req.getSession();
                session.setAttribute("user", rs.getString("name"));
                session.setAttribute("userEmail", rs.getString("email"));
                session.setAttribute("accountType", safeGetString(rs, "account_type"));

                String encodedEmail = URLEncoder.encode(rs.getString("email"), StandardCharsets.UTF_8);
                resp.sendRedirect("index.html?login=success&email=" + encodedEmail);
            } else {
                resp.sendRedirect("frontend/login.html?error=1");
            }

            rs.close();
            ps.close();
            con.close();

        } catch (Exception e) {
            e.printStackTrace();
            resp.getWriter().println("Error: " + e.getMessage());
        }
    }

    private String safeGetString(ResultSet rs, String columnName) {
        try {
            String value = rs.getString(columnName);
            return value == null ? "" : value;
        } catch (SQLException e) {
            return "";
        }
    }
}
