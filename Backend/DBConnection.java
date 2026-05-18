import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnection {

    public static Connection getConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            String dbHost = getEnvOrDefault("DB_HOST", "localhost");
            String dbPort = getEnvOrDefault("DB_PORT", "3306");
            String dbName = getEnvOrDefault("DB_NAME", "studentbridge");
            String dbUser = getEnvOrDefault("DB_USER", "root");
            String dbPassword = getEnvOrDefault("DB_PASSWORD", "mezbah143");
            String dbUrl = System.getenv("DB_URL");

            String jdbcUrl = (dbUrl != null && !dbUrl.isBlank())
                ? dbUrl
                : "jdbc:mysql://" + dbHost + ":" + dbPort + "/" + dbName
                    + "?serverTimezone=UTC&useUnicode=true&characterEncoding=UTF-8"
                    + "&allowPublicKeyRetrieval=true&useSSL=false";

            Connection con = DriverManager.getConnection(jdbcUrl, dbUser, dbPassword);

            System.out.println("Connected to MySQL.");
            return con;

        } catch (Exception e) {
            System.out.println("Connection to MySQL failed.");
            e.printStackTrace();
            return null;
        }
    }

    private static String getEnvOrDefault(String key, String fallback) {
        String value = System.getenv(key);
        return (value == null || value.isBlank()) ? fallback : value;
    }
}
