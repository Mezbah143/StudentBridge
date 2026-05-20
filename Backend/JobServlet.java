import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/JobServlet")
public class JobServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try (Connection con = DBConnection.getConnection()) {

            if (con == null) {
                sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "Database connection failed");
                return;
            }

            try {
                writeJobsJson(con, response, true);

            } catch (SQLException e) {

                // If columns do not exist then use old query
                if (isMissingLocationColumn(e)) {
                    writeJobsJson(con, response, false);
                } else {
                    throw e;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();

            sendError(response,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Unable to load jobs");
        }
    }

    private void writeJobsJson(Connection con,
                               HttpServletResponse response,
                               boolean includeMapColumns)
            throws SQLException, IOException {

        String sql;

        if (includeMapColumns) {

            sql = "SELECT id, title, company, location, category, type, " +
                    "salary, description, address, latitude, longitude " +
                    "FROM jobs ORDER BY id DESC";

        } else {

            sql = "SELECT id, title, company, location, category, type, " +
                    "salary, description FROM jobs ORDER BY id DESC";
        }

        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery();
             PrintWriter out = response.getWriter()) {

            StringBuilder json = new StringBuilder();
            json.append("[");

            boolean first = true;

            while (rs.next()) {

                if (!first) {
                    json.append(",");
                }

                json.append("{");

                json.append("\"id\":").append(rs.getInt("id")).append(",");
                json.append("\"title\":\"").append(escapeJson(rs.getString("title"))).append("\",");
                json.append("\"company\":\"").append(escapeJson(rs.getString("company"))).append("\",");
                json.append("\"location\":\"").append(escapeJson(rs.getString("location"))).append("\",");
                json.append("\"category\":\"").append(escapeJson(rs.getString("category"))).append("\",");
                json.append("\"type\":\"").append(escapeJson(rs.getString("type"))).append("\",");
                json.append("\"salary\":\"").append(escapeJson(rs.getString("salary"))).append("\",");
                json.append("\"description\":\"").append(escapeJson(rs.getString("description"))).append("\",");

                // Optional map columns
                if (includeMapColumns) {

                    json.append("\"address\":\"")
                            .append(escapeJson(rs.getString("address")))
                            .append("\",");

                    json.append("\"latitude\":")
                            .append(decimalOrNull(rs.getString("latitude")))
                            .append(",");

                    json.append("\"longitude\":")
                            .append(decimalOrNull(rs.getString("longitude")));

                } else {

                    json.append("\"address\":\"\",");
                    json.append("\"latitude\":null,");
                    json.append("\"longitude\":null");
                }

                json.append("}");

                first = false;
            }

            json.append("]");

            out.write(json.toString());
        }
    }

    private void sendError(HttpServletResponse response,
                           int statusCode,
                           String message) throws IOException {

        response.setStatus(statusCode);

        response.getWriter().write(
                "{\"error\":\"" + escapeJson(message) + "\"}"
        );
    }

    private boolean isMissingLocationColumn(SQLException e) {

        String message = e.getMessage() == null
                ? ""
                : e.getMessage().toLowerCase();

        return "42S22".equals(e.getSQLState())
                || message.contains("address")
                || message.contains("latitude")
                || message.contains("longitude");
    }

    private String decimalOrNull(String value) {

        if (value == null || value.trim().isEmpty()) {
            return "null";
        }

        try {
            Double.parseDouble(value);
            return value;

        } catch (NumberFormatException e) {
            return "null";
        }
    }

    private String escapeJson(String value) {

        if (value == null) {
            return "";
        }

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
