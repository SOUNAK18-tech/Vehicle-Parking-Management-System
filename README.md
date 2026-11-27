# Vehicle Parking Management System (ParkFlow)

A desktop vehicle parking lot allocation and billing system built with **Java Swing (GUI)** and **SQLite JDBC**, adhering strictly to the **Model-View-Controller (MVC)** architectural design pattern.

## 1. System Overview & Core Features

* **Parking Lot Allocation**: Automatically allocates the lowest-numbered available parking bay (`Slot 1` to `Slot 10`) from a fixed 10-slot capacity, and dynamically reuses released slots immediately upon vehicle departure.

* **Entry & Exit Timestamping**: Captures live system timestamps formatted as `yyyy-MM-dd HH:mm:ss` upon entry and checkout.

* **Hourly Fee Calculation**: Computes duration elapsed between entry and exit, rounding fractional hours up to the next full hour (`Math.ceil`), charging a standard rate of **Rs. 20.00 / hour** (minimum charge: 1 hour).

* **Digital Receipt & System Printing**: Renders structured parking entry passes and final fee receipts in a dedicated monospace text view with one-click native OS printer integration (`PrinterJob` / Save to PDF).

* **Interactive Top-Down Parking Map**: Replaces static lists with a visual architectural layout featuring:

  * Two back-to-back parking islands: Upper Island (`A1`–`A6` / Slots 1–6) and Lower Island (`B1`–`B4` / Slots 7–10).

  * Top-down vector car illustrations rendered inside occupied bays in distinct colorways (Crimson Red, Pearl White, Royal Blue, Charcoal Black).

  * Floor selector chips (`B1`–`B5`), directional lane arrows (`←`, `↔`, `→`, `↓`), and Entry/Exit gate bars.

  * Click-to-select interaction (clicking any parked car populates the vehicle number box for checkout).

* **Strict Vehicle Registration Validation**:

  * First 2 characters must strictly be uppercase alphabets (`A`–`Z`).

  * No spaces, hyphens (`-`), lowercase characters (`a`–`z`), or special characters permitted anywhere in the registration string.

  * Valid Sample: `WB18J3935`.

  * Triggers an explicit **"Validation error"** window stating:

    ```
    Invalid registration no
    . Sample registration :  WB18J3935
    
    ```

* **Persistent Relational Database**: Uses an embedded SQLite database (`parking_database.db`) storing all transactions in the `parking_records` table with complete historical audit capability.

## 2. Project File Structure

```
VehicleParkingProject/
│
├── Main.java                   # Application Entry Point & Driver (Initializes MVC components)
├── ParkingModel.java           # Model Layer (SQLite JDBC connection, SQL statements, business logic)
├── ParkingRecord.java          # Data Transfer Object (DTO representing a parking transaction)
├── ParkingView.java            # View Layer (Java Swing GUI with top-down visual parking canvas)
├── ParkingController.java      # Controller Layer (Event coordination, validation, model-view binding)
├── VehicleNumberFormatter.java # Validation Engine (Enforces registration rules and exceptions)
├── ViewAllRecords.java         # CLI Utility to inspect all historical database records
│
├── parking_database.db         # SQLite Relational Database file
├── sqlite-jdbc-3.53.2.1.jar    # SQLite JDBC Driver archive
├── Manrope.ttf                 # Custom sans-serif typography asset
├── logo.png / logo.gif         # Branding icons and animated header assets
├── parking.gif                 # Supplemental animated parking graphic
├── run.bat                     # Windows one-click batch launcher
└── README.md                   # System documentation and execution guide

```

## 3. Compilation & Execution Commands

### Prerequisites

* **Java Development Kit (JDK)**: JDK 8, 11, 17, or 21 installed and added to your system `PATH`.

* Ensure `sqlite-jdbc-3.53.2.1.jar` is located in the same directory as the `.java` files.

### A. Windows (Command Prompt)

1. Open **Command Prompt** (`cmd.exe`) and navigate to the project directory:

   ```
   cd /d "C:\path\to\your\project_folder"
   
   ```

2. Compile all Java source files with the SQLite JDBC library in classpath:

   ```
   javac -cp ".;sqlite-jdbc-3.53.2.1.jar" *.java
   
   ```

3. Run the application:

   ```
   java -cp ".;sqlite-jdbc-3.53.2.1.jar" Main
   
   ```

### B. Windows (PowerShell)

1. Open **PowerShell** and navigate to your project directory:

   ```
   cd "C:\path\to\your\project_folder"
   
   ```

2. Compile all source files:

   ```
   javac -cp ".;sqlite-jdbc-3.53.2.1.jar" *.java
   
   ```

3. Run the application:

   ```
   java -cp ".;sqlite-jdbc-3.53.2.1.jar" Main
   
   ```

### C. macOS & Linux (Terminal)

> **Note**: On Unix systems (Linux/macOS), the classpath delimiter is a colon (`:`) instead of a semicolon (`;`).

1. Open **Terminal** and navigate to your project directory:

   ```
   cd /path/to/your/project_folder
   
   ```

2. Compile all source files:

   ```
   javac -cp ".:sqlite-jdbc-3.53.2.1.jar" *.java
   
   ```

3. Run the application:

   ```
   java -cp ".:sqlite-jdbc-3.53.2.1.jar" Main
   
   ```

### D. One-Click Windows Launcher (`run.bat`)

Double-click `run.bat` in File Explorer, or execute it from Command Prompt:

```
run.bat

```

## 4. Commands to Check Database Records

All transactions are saved in `parking_database.db`. When vehicles exit, their status is updated to `EXITED` and the row is permanently preserved.

### Option 1: Using the Included Java CLI Utility (`ViewAllRecords.java`)

Compile and run the included inspector tool:

* **Windows**:

  ```
  javac -cp ".;sqlite-jdbc-3.53.2.1.jar" ViewAllRecords.java
  java -cp ".;sqlite-jdbc-3.53.2.1.jar" ViewAllRecords
  
  ```

* **macOS / Linux**:

  ```
  javac -cp ".:sqlite-jdbc-3.53.2.1.jar" ViewAllRecords.java
  java -cp ".:sqlite-jdbc-3.53.2.1.jar" ViewAllRecords
  
  ```

### Option 2: Using Python (One-Line Terminal Command)

```
python -c "import sqlite3; conn = sqlite3.connect('parking_database.db'); cur = conn.cursor(); cur.execute('SELECT * FROM parking_records'); print(f'{\"ID\":<6} {\"Vehicle No\":<14} {\"Slot\":<6} {\"Entry Time\":<21} {\"Exit Time\":<21} {\"Hours\":<7} {\"Fee\":<8} {\"Status\":<10}'); print('-'*93); [print(f'{r[0]:<6} {r[1]:<14} {r[2]:<6} {r[3]:<21} {str(r[4]):<21} {r[5]:<7} {r[6]:<8.2f} {r[7]:<10}') for r in cur.fetchall()]"

```

### Option 3: Using SQLite CLI

```
sqlite3 -header -column parking_database.db "SELECT * FROM parking_records;"

```

### Option 4: Graphical Inspection (DB Browser for SQLite)

1. Open [DB Browser for SQLite](https://sqlitebrowser.org/).

2. Click **Open Database** and choose `parking_database.db`.

3. Select the **Browse Data** tab and choose `parking_records` to view all rows.

## 5. System MVC Architecture & Class Diagram

The application strictly implements the **Model-View-Controller (MVC)** architectural pattern:

* **Model Layer (`ParkingModel`, `ParkingRecord`)**:

  * Encapsulates all data persistence, SQLite connection pooling, and business calculations.

  * Executes SQL queries (`INSERT`, `UPDATE`, `SELECT`) using `PreparedStatement` to guard against injection.

  * Never imports or references Swing GUI components.

* **View Layer (`ParkingView`, `ParkingLotCanvas`)**:

  * Manages graphical presentation using Swing components (`JFrame`, `JPanel`, `JTextField`, `JButton`, `JTextArea`).

  * Custom-renders the top-down vector car parking canvas, driving lanes, floor indicators, and entry/exit gates.

  * Dispatches user interactions via `ActionListener` hooks without containing database access code.

* **Controller Layer (`ParkingController`)**:

  * Acts as the intermediary mediator binding View events to Model operations.

  * Intercepts vehicle registration entries, invokes validation routines in `VehicleNumberFormatter`, handles error dialogs, and triggers UI updates.

* **Validator (`VehicleNumberFormatter`)**:

  * Standalone validation engine enforcing format compliance and throwing `InvalidRegistrationException`.

### Detailed UML Class Diagram (Mermaid)

```
classDiagram
    direction TB

    class Main {
        +main(args: String[])$ void
    }

    class ParkingModel {
        -url: String$
        +TOTAL_SLOTS: int$
        +HOURLY_RATE: double$
        +TIME_FMT: DateTimeFormatter$
        +connect() Connection
        +createTable() void
        +isVehicleCurrentlyParked(vehicleNo: String) boolean
        +findNextAvailableSlot() int
        +allocateSlotAndCheckIn(vehicleNo: String) ParkingRecord
        +checkOutAndCalculateFee(vehicleNo: String) ParkingRecord
        +getActiveParkedVehicles() List~ParkingRecord~
    }

    class ParkingRecord {
        +ticketId: int
        +vehicleNo: String
        +slotNumber: int
        +entryTime: String
        +exitTime: String
        +durationMinutes: long
        +billableHours: long
        +fee: double
        +status: String
    }

    class ParkingController {
        -model: ParkingModel
        -view: ParkingView
        +ParkingController(model: ParkingModel, view: ParkingView)
        +ParkingController()
        -bindEvents() void
        +startApp() void
        -handleVehicleEntry() void
        -handleVehicleExit() void
        -refreshActiveLots() void
    }

    class ParkingView {
        -txtVehicleNo: AppleTextField
        -btnCheckIn: AppleButton
        -btnCheckOut: AppleButton
        -btnPrintReceipt: AppleButton
        -btnClear: AppleButton
        -lblOccupancyPill: JLabel
        -occupancyBadge: PillBadge
        -lblFilledCount: JLabel
        -lblEmptyCount: JLabel
        -parkingLotCanvas: ParkingLotCanvas
        -txtReceipt: JTextArea
        +ParkingView()
        +getVehicleNoInput() String
        +clearInput() void
        +setStatusMessage(message: String) void
        +showInfoDialog(message: String, title: String) void
        +showErrorDialog(message: String, title: String) void
        +updateParkedVehiclesTable(activeRecords: List~ParkingRecord~, totalSlots: int) void
        +displayEntrySlip(r: ParkingRecord) void
        +displayExitReceipt(r: ParkingRecord) void
        +printReceiptContent() void
        +setReceiptText(text: String) void
        +addCheckInListener(listener: ActionListener) void
        +addCheckOutListener(listener: ActionListener) void
        +addPrintReceiptListener(listener: ActionListener) void
        +addClearListener(listener: ActionListener) void
    }

    class ParkingLotCanvas {
        -occupiedMap: Map~Integer, ParkingRecord~
        -slotBoundsMap: Map~Integer, Rectangle~
        -hoveredSlot: int
        -selectedSlot: int
        +updateSlots(activeRecords: List~ParkingRecord~) void
        +clearSelection() void
        #paintComponent(g: Graphics) void
        -drawParkingBay(g2: Graphics2D, slot: int, rect: Rectangle, isLeft: boolean) void
        -drawTopDownCar(g2: Graphics2D, x: int, y: int, w: int, h: int, styleIdx: int, facingLeft: boolean) void
    }

    class VehicleNumberFormatter {
        +VALIDATION_ERROR_TITLE: String$
        +VALIDATION_ERROR_MESSAGE: String$
        +isInvalidInsertion(offset: int, text: String)$ boolean
        +validate(input: String)$ void
        +getValidationError(input: String)$ String
    }

    class InvalidRegistrationException {
        +InvalidRegistrationException(message: String)
    }

    class SQLite_Database {
        <<Database>>
        +table: parking_records
        +ticket_id: INTEGER [PK]
        +vehicle_no: TEXT
        +slot_number: INTEGER
        +entry_time: TEXT
        +exit_time: TEXT
        +billable_hours: INTEGER
        +fee: REAL
        +status: TEXT
    }

    %% Relationships
    Main ..> ParkingModel : Instantiates
    Main ..> ParkingView : Instantiates
    Main ..> ParkingController : Wires & Starts

    ParkingController o-- ParkingModel : Aggregates / Controls
    ParkingController o-- ParkingView : Aggregates / Controls
    ParkingController ..> VehicleNumberFormatter : Uses for Validation

    ParkingView *-- ParkingLotCanvas : Contains Inner Canvas
    ParkingView ..> ParkingRecord : Renders Data

    ParkingModel ..> ParkingRecord : Creates & Returns
    ParkingModel ..> SQLite_Database : JDBC SQL Operations (INSERT/UPDATE/SELECT)
    VehicleNumberFormatter +-- InvalidRegistrationException : Declares Inner Exception

```

### High-Level Architectural Flow Diagram

```
+--------------------------------------------------------------------------------------------------+
|                                        USER / OPERATOR                                           |
+--------------------------------------------------------------------------------------------------+
         | (1. Types Reg No & Clicks Park/Exit)               ^ (4. Views Pass / Clicks Print)
         v                                                    |
+--------------------------------------------------------------------------------------------------+
|                                    VIEW LAYER (ParkingView.java)                                 |
|  - AppleTextField: Vehicle Reg Input                     - AppleButton: Entry, Exit, Print, Clear|
|  - PillBadge: Live Occupancy Metric                     - JTextArea: Digital Pass / Fee Receipt  |
|  - ParkingLotCanvas: Top-Down Bays (A1..A6, B1..B4) with Vector Cars & Directional Lanes        |
+--------------------------------------------------------------------------------------------------+
         |                                                    ^
         | (2. Fires ActionEvent)                             | (7. Refreshes UI, Canvas & Receipt)
         v                                                    |
+--------------------------------------------------------------------------------------------------+
|                              CONTROLLER LAYER (ParkingController.java)                           |
|  - ActionListeners for Entry and Exit                   - Coordinates Model & View               |
|  - Invokes VehicleNumberFormatter.validate()            - Catches InvalidRegistrationException   |
|  - Displays "Validation error" dialog                   - Dispatches updates to ParkingView      |
+--------------------------------------------------------------------------------------------------+
         |                                                    ^
         | (3. Validates Format)                              | (6. Returns Status / ParkingRecord DTO)
         v                                                    |
+----------------------------------+       +-------------------------------------------------------+
|        VALIDATION ENGINE         |       |              MODEL LAYER (ParkingModel.java)          |
|  (VehicleNumberFormatter.java)   |       |  - connect() -> DriverManager.getConnection()         |
|  - First 2 chars: Upper Alpha    |       |  - allocateSlotAndCheckIn(vehicleNo)                  |
|  - No spaces, hyphens, lowercase |       |  - checkOutAndCalculateFee(vehicleNo)                 |
|  - Throws InvalidRegException    |       |  - getActiveParkedVehicles()                          |
+----------------------------------+       |  - Hourly Fee Formula: Math.ceil(sec/3600.0) * Rate   |
                                           +-------------------------------------------------------+
                                                                      |                 ^
                                                      (4. JDBC Query) |                 | (5. Result)
                                                                      v                 |
                                           +-------------------------------------------------------+
                                           |                     SQLITE DATABASE                   |
                                           |                  (parking_database.db)                |
                                           |                 Table: parking_records                |
                                           |  ticket_id | vehicle_no | slot_number | entry_time    |
                                           |  exit_time | billable_hours | fee | status            |
                                           +-------------------------------------------------------+

```

## 6. Functional Verification & Test Cases

| Test Case | Steps to Execute | Expected Behavior | 
| ----- | ----- | ----- | 
| **TC1: Valid Check-In** | Enter `WB18J3935` and click **Allocate Slot (Entry)**. | Allocated `Slot 1` (`A1`). Vector car graphic renders in Bay A1. Live counter updates to `1 Filled • 9 Empty`. Monospace entry pass displays in receipt panel. | 
| **TC2: Lowercase Exception** | Type `wb18J3935` or `WB18j3935` in the input box. | Real-time input listener catches lowercase character and throws dialog titled `"Validation error"` stating: `Invalid registration no\n. Sample registration :  WB18J3935`. | 
| **TC3: Hyphen / Space Exception** | Type `WB-18-J-3935` or `WB 18 J 3935`. | Throws `"Validation error"` window with sample format `WB18J3935`. | 
| **TC4: Number in Leading 2 Chars** | Type `18WBJ3935` or `W118J3935`. | Throws `"Validation error"` window immediately. | 
| **TC5: Duplicate Park Check** | Type `WB18J3935` again and click **Allocate Slot (Entry)**. | Throws `"Duplicate Entry"` dialog: `Vehicle 'WB18J3935' is already parked in the lot!`. | 
| **TC6: Click-to-Checkout** | Click on the parked car in Bay `A1`. | Input field auto-populates with `WB18J3935`. | 
| **TC7: Check-Out & Fee Calculation** | Click **Exit & Calculate Fee**. | Bay `A1` is freed (car removed). Live counter updates to `0 Filled • 10 Empty`. Official fee receipt displays `Duration`, `Billable Hours: 1 hr(s)`, `TOTAL FEE PAID: Rs. 20.00`. | 
| **TC8: Slot Reuse Verification** | Enter a new vehicle `DL01AB1234` and click **Allocate Slot (Entry)**. | System re-allocates newly freed `Slot 1` instead of skipping to an arbitrary slot. | 
| **TC9: Lot Full Protection** | Park 10 distinct vehicles until capacity is reached, then attempt an 11th entry. | Throws error dialog: `Parking Lot is FULL! All 10 slots are occupied.`. | 
| **TC10: Audit Trail Verification** | Run `java -cp ".;sqlite-jdbc-3.53.2.1.jar" ViewAllRecords`. | Outputs complete historical table of all vehicles, entry timestamps, exit timestamps, and fees paid from `parking_database.db`. | 
