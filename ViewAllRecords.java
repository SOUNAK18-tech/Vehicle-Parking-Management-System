import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * =========================================================================================
 * DATABASE INSPECTION UTILITY (ViewAllRecords.java)
 * =========================================================================================
 * Displays all records (active and exited) from parking_database.db in a formatted console table.
 *
 * Compilation & Run:
 *   Windows:
 *     javac -cp ".;sqlite-jdbc-3.53.2.1.jar" ViewAllRecords.java
 *     java -cp ".;sqlite-jdbc-3.53.2.1.jar" ViewAllRecords
 *
 *   Linux / macOS:
 *     javac -cp ".:sqlite-jdbc-3.53.2.1.jar" ViewAllRecords.java
 *     java -cp ".:sqlite-jdbc-3.53.2.1.jar" ViewAllRecords
 * =========================================================================================
 */
public class ViewAllRecords {
    public static void main(String[] args) {
        String url = "jdbc:sqlite:parking_database.db";
        String sql = "SELECT ticket_id, vehicle_no, slot_number, entry_time, exit_time, billable_hours, fee, status "
                   + "FROM parking_records ORDER BY ticket_id ASC;";

        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("=======================================================================================================");
            System.out.printf("%-8s %-15s %-8s %-22s %-22s %-8s %-10s %-10s%n",
                    "TicketID", "Vehicle No", "Slot No", "Entry Timestamp", "Exit Timestamp", "Hours", "Fee (Rs)", "Status");
            System.out.println("=======================================================================================================");

            int count = 0;
            while (rs.next()) {
                count++;
                int id = rs.getInt("ticket_id");
                String veh = rs.getString("vehicle_no");
                int slot = rs.getInt("slot_number");
                String entry = rs.getString("entry_time");
                String exit = rs.getString("exit_time");
                int hours = rs.getInt("billable_hours");
                double fee = rs.getDouble("fee");
                String status = rs.getString("status");

                System.out.printf("#%-7d %-15s Slot #%-3d %-22s %-22s %-8d Rs.%-7.2f %-10s%n",
                        id, veh, slot, entry, (exit != null ? exit : "STILL PARKED"), hours, fee, status);
            }

            System.out.println("=======================================================================================================");
            System.out.println("Total Records Found: " + count);
            System.out.println("=======================================================================================================");

        } catch (Exception ex) {
            System.err.println("Error reading parking_database.db: " + ex.getMessage());
        }
    }
}
