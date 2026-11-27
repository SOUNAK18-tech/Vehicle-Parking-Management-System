import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// ==========================================
// DATA TRANSFER OBJECT: Represents a single parking transaction
// ==========================================
class ParkingRecord {
    public int ticketId;
    public String vehicleNo;
    public int slotNumber;
    public String entryTime;
    public String exitTime;
    public long durationMinutes;
    public long billableHours;
    public double fee;
    public String status;
}

// ==========================================
// MODEL LAYER: Handles SQLite JDBC connectivity, SQL queries, and fee calculation
// ==========================================
public class ParkingModel {
    private final String url = "jdbc:sqlite:parking_database.db";
    public static final int TOTAL_SLOTS = 10;
    public static final double HOURLY_RATE = 20.0; // Rs. 20.00 per hour
    public static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Establish connection to the SQLite database
    public Connection connect() throws SQLException {
        return DriverManager.getConnection(url);
    }

    // Create the parking_records table if it does not already exist
    public void createTable() throws SQLException {
        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            String createTableSql = "CREATE TABLE IF NOT EXISTS parking_records (\n"
                    + " ticket_id INTEGER PRIMARY KEY AUTOINCREMENT,\n"
                    + " vehicle_no TEXT NOT NULL,\n"
                    + " slot_number INTEGER NOT NULL,\n"
                    + " entry_time TEXT NOT NULL,\n"
                    + " exit_time TEXT,\n"
                    + " billable_hours INTEGER DEFAULT 0,\n"
                    + " fee REAL DEFAULT 0.0,\n"
                    + " status TEXT NOT NULL\n"
                    + ");";
            stmt.execute(createTableSql);
        }
    }

    // Check if a vehicle with the given registration number is currently parked
    public boolean isVehicleCurrentlyParked(String vehicleNo) throws SQLException {
        String formattedNo = VehicleNumberFormatter.padRegistrationNumber(VehicleNumberFormatter.format(vehicleNo));
        String sql = "SELECT COUNT(*) FROM parking_records WHERE (UPPER(vehicle_no) = UPPER(?) OR REPLACE(REPLACE(UPPER(vehicle_no), '-', ''), ' ', '') = REPLACE(REPLACE(UPPER(?), '-', ''), ' ', '')) AND status = 'PARKED';";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, formattedNo);
            pstmt.setString(2, formattedNo);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    // Find the lowest-numbered available parking slot between 1 and TOTAL_SLOTS
    public int findNextAvailableSlot() throws SQLException {
        Set<Integer> occupiedSlots = new HashSet<>();
        String sql = "SELECT slot_number FROM parking_records WHERE status = 'PARKED';";
        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                occupiedSlots.add(rs.getInt("slot_number"));
            }
        }
        for (int slot = 1; slot <= TOTAL_SLOTS; slot++) {
            if (!occupiedSlots.contains(slot)) {
                return slot;
            }
        }
        return -1; // Parking lot is full
    }

    // Allocate an available parking slot and record the entry timestamp
    public ParkingRecord allocateSlotAndCheckIn(String vehicleNo) throws SQLException {
        int slot = findNextAvailableSlot();
        if (slot == -1) {
            return null; // No slot available
        }

        String formattedNo = VehicleNumberFormatter.padRegistrationNumber(VehicleNumberFormatter.format(vehicleNo));
        String nowStr = LocalDateTime.now().format(TIME_FMT);
        String insertSql = "INSERT INTO parking_records(vehicle_no, slot_number, entry_time, status) VALUES(?, ?, ?, 'PARKED');";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, formattedNo);
            pstmt.setInt(2, slot);
            pstmt.setString(3, nowStr);
            pstmt.executeUpdate();

            ParkingRecord record = new ParkingRecord();
            try (ResultSet keys = pstmt.getGeneratedKeys()) {
                if (keys.next()) {
                    record.ticketId = keys.getInt(1);
                }
            }
            record.vehicleNo = formattedNo;
            record.slotNumber = slot;
            record.entryTime = nowStr;
            record.status = "PARKED";
            return record;
        }
    }

    // Record exit timestamp, compute hourly fee, and release the parking slot
    public ParkingRecord checkOutAndCalculateFee(String vehicleNo) throws SQLException {
        String formattedNo = VehicleNumberFormatter.padRegistrationNumber(VehicleNumberFormatter.format(vehicleNo));
        String selectSql = "SELECT ticket_id, vehicle_no, slot_number, entry_time FROM parking_records "
                + "WHERE (UPPER(vehicle_no) = UPPER(?) OR REPLACE(REPLACE(UPPER(vehicle_no), '-', ''), ' ', '') = REPLACE(REPLACE(UPPER(?), '-', ''), ' ', '')) AND status = 'PARKED' LIMIT 1;";

        try (Connection conn = connect();
             PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
            selectStmt.setString(1, formattedNo);
            selectStmt.setString(2, formattedNo);
            try (ResultSet rs = selectStmt.executeQuery()) {
                if (!rs.next()) {
                    return null; // Active record not found
                }

                ParkingRecord record = new ParkingRecord();
                record.ticketId = rs.getInt("ticket_id");
                record.vehicleNo = rs.getString("vehicle_no");
                record.slotNumber = rs.getInt("slot_number");
                record.entryTime = rs.getString("entry_time");

                LocalDateTime entryDt = LocalDateTime.parse(record.entryTime, TIME_FMT);
                LocalDateTime exitDt = LocalDateTime.now();
                if (exitDt.isBefore(entryDt)) {
                    exitDt = entryDt;
                }

                record.exitTime = exitDt.format(TIME_FMT);
                long seconds = Duration.between(entryDt, exitDt).getSeconds();
                record.durationMinutes = Math.max(0, seconds / 60);

                // Minimum 1 hour charged; partial hours rounded up to next full hour
                record.billableHours = Math.max(1L, (long) Math.ceil(seconds / 3600.0));
                record.fee = record.billableHours * HOURLY_RATE;
                record.status = "EXITED";

                String updateSql = "UPDATE parking_records SET exit_time = ?, billable_hours = ?, fee = ?, status = 'EXITED' "
                        + "WHERE ticket_id = ?;";
                try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                    updateStmt.setString(1, record.exitTime);
                    updateStmt.setLong(2, record.billableHours);
                    updateStmt.setDouble(3, record.fee);
                    updateStmt.setInt(4, record.ticketId);
                    updateStmt.executeUpdate();
                }

                return record;
            }
        }
    }

    // Retrieve all currently parked vehicles ordered by slot number
    public List<ParkingRecord> getActiveParkedVehicles() throws SQLException {
        List<ParkingRecord> list = new ArrayList<>();
        String sql = "SELECT ticket_id, vehicle_no, slot_number, entry_time, status "
                + "FROM parking_records WHERE status = 'PARKED' ORDER BY slot_number ASC;";
        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                ParkingRecord r = new ParkingRecord();
                r.ticketId = rs.getInt("ticket_id");
                r.vehicleNo = rs.getString("vehicle_no");
                r.slotNumber = rs.getInt("slot_number");
                r.entryTime = rs.getString("entry_time");
                r.status = rs.getString("status");
                list.add(r);
            }
        }
        return list;
    }
}
