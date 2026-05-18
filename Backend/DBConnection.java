import java.sql.Connection;
import java.sql.DriverManager;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public class DBConnection {

    public static Connection getConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            Connection con = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/studentbridge",
                "root",
                "mezbah143"
            );

            System.out.println("✅ Connected to MySQL!");
            return con;

        } catch (Exception e) {
            System.out.println("❌ Connection Failed!");
            e.printStackTrace();
            return null;
        }
    }
}