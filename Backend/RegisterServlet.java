import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.*;

public class RegisterServlet extends HttpServlet {

    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        String name = req.getParameter("name");
        String email = req.getParameter("email");
        String phone = req.getParameter("phone");
        String password = req.getParameter("password");

        try {
            Connection con = DBConnection.getConnection();

            PreparedStatement ps = con.prepareStatement(
                "INSERT INTO users(name,email,phone,password) VALUES(?,?,?,?)"
            );

            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, phone);
            ps.setString(4, password); // later we hash

            ps.executeUpdate();

            res.sendRedirect("login.html");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}