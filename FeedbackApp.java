import java.sql.*;
import java.util.Scanner;

public class FeedbackApp {

    private static final String URL  = "jdbc:oracle:thin:@//localhost:1521/FREEPDB1";
    private static final String USER = "system";
    private static final String PASS = "MyOraclePass123";

    public static void main(String[] args) {
        try (Connection con = DriverManager.getConnection(URL, USER, PASS);
             Scanner sc = new Scanner(System.in)) {

            createTableIfMissing(con);

            while (true) {
                System.out.println("\n===== Customer Feedback System =====");
                System.out.println("1. Add feedback");
                System.out.println("2. Average rating per service");
                System.out.println("3. Show feedback rated below 3");
                System.out.println("4. Exit");
                System.out.print("Choose an option: ");

                String choice = sc.nextLine().trim();
                switch (choice) {
                    case "1": addFeedback(con, sc); break;
                    case "2": showAverageRatings(con); break;
                    case "3": showLowRatedFeedback(con); break;
                    case "4": System.out.println("Goodbye!"); return;
                    default:  System.out.println("Invalid option. Try again.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
        }
    }

    private static void createTableIfMissing(Connection con) throws SQLException {
        String sql = "CREATE TABLE feedback ("
                   + " id            NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,"
                   + " customer_name VARCHAR2(100) NOT NULL,"
                   + " service_name  VARCHAR2(100) NOT NULL,"
                   + " rating        NUMBER(1) NOT NULL CHECK (rating BETWEEN 1 AND 5),"
                   + " comments      VARCHAR2(500),"
                   + " created_at    TIMESTAMP DEFAULT SYSTIMESTAMP)";
        try (Statement st = con.createStatement()) {
            st.execute(sql);
            System.out.println("Table FEEDBACK created.");
        } catch (SQLException e) {
            if (e.getErrorCode() != 955) throw e;
        }
    }

    private static void addFeedback(Connection con, Scanner sc) {
        System.out.print("Customer name: ");
        String name = sc.nextLine().trim();
        System.out.print("Service name: ");
        String service = sc.nextLine().trim();

        if (name.isEmpty() || service.isEmpty()) {
            System.out.println("Customer name and service name are required.");
            return;
        }

        int rating;
        try {
            System.out.print("Rating (1-5): ");
            rating = Integer.parseInt(sc.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Rating must be a whole number.");
            return;
        }
        if (rating < 1 || rating > 5) {
            System.out.println("Rating must be between 1 and 5.");
            return;
        }

        System.out.print("Comments: ");
        String comments = sc.nextLine().trim();

        String sql = "INSERT INTO feedback (customer_name, service_name, rating, comments) "
                   + "VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, service);
            ps.setInt(3, rating);
            ps.setString(4, comments);
            int rows = ps.executeUpdate();   
            System.out.println(rows > 0 ? "Feedback saved successfully." : "Nothing was saved.");
        } catch (SQLException e) {
            System.err.println("Could not save feedback: " + e.getMessage());
        }
    }

    private static void showAverageRatings(Connection con) {
        String sql = "SELECT service_name, AVG(rating) AS avg_rating, COUNT(*) AS total "
                   + "FROM feedback GROUP BY service_name ORDER BY avg_rating DESC";
        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            System.out.printf("%n%-25s %-12s %-8s%n", "Service", "Avg Rating", "Reviews");
            System.out.println("-".repeat(47));
            boolean any = false;
            while (rs.next()) {
                any = true;
                System.out.printf("%-25s %-12.2f %-8d%n",
                        rs.getString("service_name"),
                        rs.getDouble("avg_rating"),
                        rs.getInt("total"));
            }
            if (!any) System.out.println("No feedback recorded yet.");
        } catch (SQLException e) {
            System.err.println("Query failed: " + e.getMessage());
        }
    }

    private static void showLowRatedFeedback(Connection con) {
        String sql = "SELECT id, customer_name, service_name, rating, comments "
                   + "FROM feedback WHERE rating < ? ORDER BY rating, id";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, 3);
            try (ResultSet rs = ps.executeQuery()) {
                System.out.printf("%n%-4s %-18s %-20s %-7s %s%n",
                        "ID", "Customer", "Service", "Rating", "Comments");
                System.out.println("-".repeat(80));
                boolean any = false;
                while (rs.next()) {
                    any = true;
                    System.out.printf("%-4d %-18s %-20s %-7d %s%n",
                            rs.getInt("id"),
                            rs.getString("customer_name"),
                            rs.getString("service_name"),
                            rs.getInt("rating"),
                            rs.getString("comments"));
                }
                if (!any) System.out.println("No feedback rated below 3.");
            }
        } catch (SQLException e) {
            System.err.println("Query failed: " + e.getMessage());
        }
    }
}