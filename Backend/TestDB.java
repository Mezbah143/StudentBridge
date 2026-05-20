import java.sql.Connection;

public class TestDB {

    public static void main(String[] args) {

        Connection con = null;

        try {

            con = DBConnection.getConnection();

            if (con != null) {

                System.out.println("Database connected successfully!");

            } else {

                System.out.println("Database connection failed!");
            }

        } catch (Exception e) {

            System.out.println("Error while connecting to database:");
            e.printStackTrace();

        } finally {

            try {

                if (con != null) {
                    con.close();
                    System.out.println("Database connection closed.");
                }

            } catch (Exception e) {

                e.printStackTrace();
            }
        }
    }
}
