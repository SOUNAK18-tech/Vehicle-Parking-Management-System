import java.sql.SQLException;
import java.util.List;

// ==========================================
// CONTROLLER LAYER: Coordinates between ParkingModel and ParkingView
// ==========================================
public class ParkingController {
    private final ParkingModel model;
    private final ParkingView view;

    public ParkingController(ParkingModel model, ParkingView view) {
        this.model = model;
        this.view = view;
        bindEvents();
    }

    // Default constructor for convenience
    public ParkingController() {
        this(new ParkingModel(), new ParkingView());
    }

    private void bindEvents() {
        view.addCheckInListener(e -> handleVehicleEntry());
        view.addCheckOutListener(e -> handleVehicleExit());
        view.addPrintReceiptListener(e -> view.printReceiptContent());
        view.addClearListener(e -> {
            view.clearInput();
            view.setReceiptText("");
            view.setStatusMessage("Form cleared.");
        });
    }

    public void startApp() {
        try {
            model.createTable();
            refreshActiveLots();
            view.setStatusMessage("Database connected • System ready");
            view.setReceiptText(
                    "────────────────────────────────────────\n"
                    + "               PARKFLOW                 \n"
                    + "    Automated Slot & Billing System     \n"
                    + "────────────────────────────────────────\n\n"
                    + " QUICK START GUIDE\n\n"
                    + " 1. Enter vehicle registration number\n"
                    + "    (auto-formatted as you type).\n\n"
                    + " 2. Click 'Allocate Slot' to park.\n\n"
                    + " 3. Click any active row or slot chip\n"
                    + "    to check out & calculate fees.\n\n"
                    + " 4. Click 'Print Receipt' for print copy.\n\n"
                    + "────────────────────────────────────────\n"
                    + "     Standard Hourly Rate: Rs. 20.00    \n"
                    + "────────────────────────────────────────\n"
            );
            view.setVisible(true);
        } catch (SQLException ex) {
            view.showErrorDialog("Database initialization error: " + ex.getMessage(), "Database Error");
        }
    }

    private void handleVehicleEntry() {
        String vehicleNo = view.getVehicleNoInput();
        String validationError = VehicleNumberFormatter.getValidationError(vehicleNo);
        if (validationError != null) {
            view.highlightInputError();
            view.showErrorDialog(validationError, "Invalid Vehicle Number");
            view.setStatusMessage("Entry rejected: Invalid vehicle registration number.");
            return;
        }

        view.setVehicleNoText(vehicleNo);

        try {
            if (model.isVehicleCurrentlyParked(vehicleNo)) {
                view.highlightInputError();
                view.showErrorDialog("Vehicle '" + vehicleNo + "' is already parked in the lot!",
                        "Duplicate Entry");
                view.setStatusMessage("Entry rejected: Vehicle " + vehicleNo + " is already parked.");
                return;
            }

            ParkingRecord record = model.allocateSlotAndCheckIn(vehicleNo);
            if (record == null) {
                view.highlightInputError();
                view.showErrorDialog("Parking Lot is FULL! All " + ParkingModel.TOTAL_SLOTS + " slots are occupied.",
                        "Lot Full");
                view.setStatusMessage("Entry rejected: Parking lot is full.");
                return;
            }

            view.displayEntrySlip(record);
            view.clearInput();
            refreshActiveLots();
            view.setStatusMessage("Allocated Slot #" + record.slotNumber + " to vehicle " + record.vehicleNo
                    + " at " + record.entryTime);
        } catch (SQLException ex) {
            view.showErrorDialog("Database error during check-in: " + ex.getMessage(), "Database Error");
        }
    }

    private void handleVehicleExit() {
        String vehicleNo = view.getVehicleNoInput();
        if (vehicleNo.isEmpty()) {
            view.highlightInputError();
            view.showErrorDialog("Please enter or select a parked Vehicle Registration Number to check out.",
                    "Validation Error");
            return;
        }

        String validationError = VehicleNumberFormatter.getValidationError(vehicleNo);
        if (validationError != null) {
            view.highlightInputError();
            view.showErrorDialog(validationError, "Invalid Vehicle Number");
            view.setStatusMessage("Checkout rejected: Invalid vehicle registration number.");
            return;
        }

        try {
            ParkingRecord record = model.checkOutAndCalculateFee(vehicleNo);
            if (record == null) {
                view.highlightInputError();
                view.showErrorDialog("No currently parked record found for vehicle '" + vehicleNo + "'.\nPlease check the registration number or select a parked vehicle from the table.",
                        "Vehicle Not Found");
                view.setStatusMessage("Checkout failed: Vehicle " + vehicleNo + " is not currently parked.");
                return;
            }

            view.displayExitReceipt(record);
            view.clearInput();
            refreshActiveLots();
            view.setStatusMessage(String.format("Checked out %s from Slot #%d | Fee: Rs. %.2f",
                    record.vehicleNo, record.slotNumber, record.fee));
        } catch (SQLException ex) {
            view.showErrorDialog("Database error during check-out: " + ex.getMessage(), "Database Error");
        }
    }

    private void refreshActiveLots() throws SQLException {
        List<ParkingRecord> activeList = model.getActiveParkedVehicles();
        view.updateParkedVehiclesTable(activeList, ParkingModel.TOTAL_SLOTS);
    }
}
