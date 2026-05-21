import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public final class DatabaseSchemaManager {

    private DatabaseSchemaManager() {
    }

    public static void ensureJobsTable(Connection con) throws SQLException {
        try (Statement statement = con.createStatement()) {
            statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS jobs (" +
                            "id INT AUTO_INCREMENT PRIMARY KEY, " +
                            "title VARCHAR(150) NOT NULL, " +
                            "company VARCHAR(150) NOT NULL, " +
                            "location VARCHAR(100) NOT NULL, " +
                            "category VARCHAR(100) NOT NULL, " +
                            "type VARCHAR(50) NOT NULL, " +
                            "salary VARCHAR(100) NOT NULL, " +
                            "description TEXT NOT NULL, " +
                            "employer_email VARCHAR(255), " +
                            "working_hours VARCHAR(150), " +
                            "requirements TEXT, " +
                            "contact_email VARCHAR(255), " +
                            "contact_phone VARCHAR(50), " +
                            "company_details TEXT, " +
                            "application_deadline DATE, " +
                            "logo_url VARCHAR(500), " +
                            "address VARCHAR(255), " +
                            "latitude DECIMAL(10, 7), " +
                            "longitude DECIMAL(10, 7), " +
                            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                            ")"
            );
        }

        ensureColumn(con, "employer_email", "VARCHAR(255)");
        ensureColumn(con, "working_hours", "VARCHAR(150)");
        ensureColumn(con, "requirements", "TEXT");
        ensureColumn(con, "contact_email", "VARCHAR(255)");
        ensureColumn(con, "contact_phone", "VARCHAR(50)");
        ensureColumn(con, "company_details", "TEXT");
        ensureColumn(con, "application_deadline", "DATE");
        ensureColumn(con, "logo_url", "VARCHAR(500)");
        ensureColumn(con, "address", "VARCHAR(255)");
        ensureColumn(con, "latitude", "DECIMAL(10, 7)");
        ensureColumn(con, "longitude", "DECIMAL(10, 7)");
        ensureColumn(con, "created_at", "TIMESTAMP DEFAULT CURRENT_TIMESTAMP");

        ensureIndex(con, "idx_jobs_employer_email", "employer_email");
        ensureIndex(con, "idx_jobs_coordinates", "latitude, longitude");
        ensureIndex(con, "idx_jobs_location", "location");
        ensureIndex(con, "idx_jobs_category", "category");
        ensureIndex(con, "idx_jobs_type", "type");
    }

    private static void ensureColumn(Connection con,
                                     String columnName,
                                     String definition) throws SQLException {
        if (columnExists(con, columnName)) {
            return;
        }

        try (Statement statement = con.createStatement()) {
            statement.executeUpdate(
                    "ALTER TABLE jobs ADD COLUMN " + columnName + " " + definition
            );
        } catch (SQLException e) {
            if (!isDuplicateColumn(e)) {
                throw e;
            }
        }
    }

    private static boolean columnExists(Connection con,
                                        String columnName) throws SQLException {
        String sql = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS " +
                "WHERE TABLE_SCHEMA = DATABASE() " +
                "AND TABLE_NAME = 'jobs' " +
                "AND COLUMN_NAME = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, columnName);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    private static void ensureIndex(Connection con,
                                    String indexName,
                                    String columns) throws SQLException {
        if (indexExists(con, indexName)) {
            return;
        }

        try (Statement statement = con.createStatement()) {
            statement.executeUpdate(
                    "CREATE INDEX " + indexName + " ON jobs (" + columns + ")"
            );
        } catch (SQLException e) {
            if (!isDuplicateIndex(e)) {
                throw e;
            }
        }
    }

    private static boolean indexExists(Connection con,
                                       String indexName) throws SQLException {
        String sql = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS " +
                "WHERE TABLE_SCHEMA = DATABASE() " +
                "AND TABLE_NAME = 'jobs' " +
                "AND INDEX_NAME = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, indexName);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    private static boolean isDuplicateColumn(SQLException e) {
        return "42S21".equals(e.getSQLState()) || hasMessage(e, "duplicate column");
    }

    private static boolean isDuplicateIndex(SQLException e) {
        return "42000".equals(e.getSQLState()) && hasMessage(e, "duplicate key name");
    }

    private static boolean hasMessage(SQLException e, String text) {
        return e.getMessage() != null
                && e.getMessage().toLowerCase().contains(text);
    }
}
