import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnection {

    public static Connection getConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            String host = getEnv("DB_HOST", "localhost");
            String port = getEnv("DB_PORT", "3306");
            String database = getEnv("DB_NAME", "studentbridge");
            String user = getEnv("DB_USER", "root");
            String password = getEnv("DB_PASSWORD", "");

            String url = "jdbc:mysql://" + host + ":" + port + "/" + database
                    + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8";

            Connection con = DriverManager.getConnection(url, user, password);

            System.out.println("Connected to MySQL database: " + database + " at " + host + ":" + port);
            return con;

        } catch (Exception e) {
            System.out.println("Database connection failed.");
            e.printStackTrace();
            return null;
        }
    }

    private static String getEnv(String key, String defaultValue) {
        String value = System.getenv(key);
        return value == null || value.trim().isEmpty() ? defaultValue : value.trim();
    }
}
