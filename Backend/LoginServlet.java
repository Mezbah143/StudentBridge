import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/LoginServlet")
public class LoginServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String email = req.getParameter("email");
        String password = req.getParameter("password");

        resp.setContentType("text/html");

        resp.getWriter().println("Email from form = " + email + "<br>");
        resp.getWriter().println("Password from form = " + password + "<br><hr>");

        try {
            Connection con = DBConnection.getConnection();

            if (con == null) {
                resp.getWriter().println("Database connection failed.");
                return;
            }

            PreparedStatement ps = con.prepareStatement(
                "SELECT * FROM users WHERE email = ? AND password = ?"
            );

            ps.setString(1, email);
            ps.setString(2, password);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                resp.getWriter().println("LOGIN SUCCESS<br>");
                resp.getWriter().println("Welcome, " + rs.getString("name"));
            } else {
                resp.getWriter().println("LOGIN FAILED<br>");
                resp.getWriter().println("No matching user found in database.");
            }

            rs.close();
            ps.close();
            con.close();

        } catch (Exception e) {
            e.printStackTrace();
            resp.getWriter().println("Error: " + e.getMessage());
        }
    }
}