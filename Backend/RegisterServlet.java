import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/RegisterServlet")
public class RegisterServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String name = request.getParameter("name");
        String email = request.getParameter("email");
        String phone = request.getParameter("phone");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");
        String accountType = request.getParameter("accountType");

        if (!password.equals(confirmPassword)) {
            response.getWriter().println("Passwords do not match.");
            return;
        }

        try {
            Connection con = DBConnection.getConnection();

            if (con == null) {
                response.getWriter().println("Database connection failed.");
                return;
            }

            String sql = "INSERT INTO users (name, email, phone, password) VALUES (?, ?, ?, ?)";
            PreparedStatement ps = con.prepareStatement(sql);

            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, phone);
            ps.setString(4, password);

            int rows = ps.executeUpdate();

            ps.close();
            con.close();

            if (rows > 0) {
                response.sendRedirect("login.html");
            } else {
                response.getWriter().println("Registration failed.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().println("Error: " + e.getMessage());
        }
    }
}