import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.print.PrinterException;
import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;

/**
 * =========================================================================================
 * VIEW LAYER: Java Swing GUI (Premium Minimalist Apple Aesthetic)
 * =========================================================================================
 * Designed following Apple Human Interface Guidelines:
 * - Refined neutral palette (#F5F5F7 canvas, #FFFFFF card containers, #E5E5EA borders)
 * - Typography hierarchy using SF Pro / Segoe UI with crisp text antialiasing
 * - Rounded cards, pill badges, and smooth action buttons
 * - Live interactive slot allocation matrix and Apple Store style digital pass slips
 * - Full MVC contract compliance
 * =========================================================================================
 */
public class ParkingView extends JFrame {

    // --- Apple Palette Constants ---
    public static final Color COLOR_CANVAS      = new Color(245, 245, 247); // #F5F5F7
    public static final Color COLOR_CARD        = new Color(255, 255, 255); // #FFFFFF
    public static final Color COLOR_BORDER      = new Color(229, 229, 234); // #E5E5EA
    public static final Color COLOR_BORDER_SOFT = new Color(238, 238, 242);

    public static final Color TEXT_PRIMARY      = new Color(29, 29, 31);    // #1D1D1F
    public static final Color TEXT_SECONDARY    = new Color(134, 134, 139); // #86868B
    public static final Color TEXT_MUTED        = new Color(174, 174, 178); // #AEAEB2

    public static final Color APPLE_BLUE        = new Color(0, 113, 227);   // #0071E3
    public static final Color APPLE_BLUE_HOVER  = new Color(0, 122, 255);   // #007AFF
    public static final Color APPLE_BLUE_PRESS  = new Color(0, 91, 181);
    public static final Color APPLE_BLUE_BG     = new Color(235, 243, 254); // #EBF3FE

    public static final Color APPLE_RED         = new Color(255, 59, 48);   // #FF3B30
    public static final Color APPLE_RED_HOVER   = new Color(255, 69, 58);
    public static final Color APPLE_RED_PRESS   = new Color(215, 0, 21);
    public static final Color APPLE_RED_BG      = new Color(255, 235, 238);

    public static final Color APPLE_GREEN       = new Color(52, 199, 89);   // #34C759
    public static final Color APPLE_GREEN_BG    = new Color(232, 245, 233);

    public static final Color APPLE_GRAY_BTN    = new Color(242, 242, 247); // #F2F2F7
    public static final Color APPLE_GRAY_HOVER  = new Color(229, 229, 234);
    public static final Color APPLE_GRAY_PRESS  = new Color(215, 215, 220);

    // --- Interactive Components ---
    private final AppleTextField txtVehicleNo;
    private final AppleButton btnCheckIn;
    private final AppleButton btnCheckOut;
    private final AppleButton btnPrintReceipt;
    private final AppleButton btnClear;

    private final JLabel lblOccupancyPill = new JLabel("● 0 / 10 Occupied  •  10 Available");
    private final PillBadge occupancyBadge;
    private final JLabel lblStatusMessage = new JLabel("System ready.");
    private final JPanel slotMatrixPanel = new JPanel(new GridLayout(1, 10, 6, 0));
    private final Map<Integer, SlotPill> slotPillMap = new HashMap<>();

    private final DefaultTableModel tableModel;
    private final JTable parkedTable;
    private final JTextArea txtReceipt = new JTextArea();

    public ParkingView() {
        // Enable system font anti-aliasing
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        setTitle("ParkFlow • Vehicle Parking Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1080, 720);
        setMinimumSize(new Dimension(960, 620));
        setLocationRelativeTo(null);
        getContentPane().setBackground(COLOR_CANVAS);
        setLayout(new BorderLayout(0, 0));

        // Set Window & Taskbar Icon
        try {
            File iconFile = new File("assets/logo.png");
            if (!iconFile.exists()) iconFile = new File("logo.png");
            if (iconFile.exists()) {
                setIconImage(Toolkit.getDefaultToolkit().getImage(iconFile.getAbsolutePath()));
            }
        } catch (Exception ignored) {}

        // Instantiate Controls
        txtVehicleNo = new AppleTextField("e.g. WB02AB1234", 14);
        btnCheckIn = new AppleButton("Allocate Slot (Entry)", AppleButton.Style.PRIMARY);
        btnCheckOut = new AppleButton("Exit & Calculate Fee", AppleButton.Style.DESTRUCTIVE);
        btnPrintReceipt = new AppleButton("Print Receipt", AppleButton.Style.SECONDARY);
        btnClear = new AppleButton("Clear", AppleButton.Style.GHOST);
        occupancyBadge = new PillBadge(lblOccupancyPill);

        // ==========================================
        // 1. TOP HEADER APP BAR (Apple Navigation Bar)
        // ==========================================
        JPanel headerBar = new JPanel(new BorderLayout(16, 0));
        headerBar.setBackground(COLOR_CARD);
        headerBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BORDER),
                BorderFactory.createEmptyBorder(14, 24, 14, 24)));

        // Left Branding Block: Animated Icon + Title + Subtitle
        JPanel brandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        brandPanel.setOpaque(false);

        AnimatedLogoView logoIcon = new AnimatedLogoView();
        JPanel titleGroup = new JPanel(new GridLayout(2, 1, 0, 2));
        titleGroup.setOpaque(false);

        JLabel lblAppTitle = new JLabel("ParkFlow");
        lblAppTitle.setFont(getAppleFont(Font.BOLD, 17f));
        lblAppTitle.setForeground(TEXT_PRIMARY);

        JLabel lblAppSubtitle = new JLabel(String.format("Capacity: %d Slots   •   Rate: Rs. %.2f / hr",
                ParkingModel.TOTAL_SLOTS, ParkingModel.HOURLY_RATE));
        lblAppSubtitle.setFont(getAppleFont(Font.PLAIN, 12f));
        lblAppSubtitle.setForeground(TEXT_SECONDARY);

        titleGroup.add(lblAppTitle);
        titleGroup.add(lblAppSubtitle);
        brandPanel.add(logoIcon);
        brandPanel.add(titleGroup);

        // Right Occupancy Pill Badge (Wrapped in centering panel to avoid vertical stretch)
        JPanel badgeWrapper = new JPanel(new GridBagLayout());
        badgeWrapper.setOpaque(false);
        badgeWrapper.add(occupancyBadge);

        headerBar.add(brandPanel, BorderLayout.WEST);
        headerBar.add(badgeWrapper, BorderLayout.EAST);
        add(headerBar, BorderLayout.NORTH);

        // ==========================================
        // 2. MAIN WORKSPACE CONTAINER
        // ==========================================
        JPanel mainContent = new JPanel(new BorderLayout(0, 12));
        mainContent.setOpaque(false);
        mainContent.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));

        // Top Section: Slot Visualizer Strip + Input Controls Card
        JPanel topSection = new JPanel(new BorderLayout(0, 10));
        topSection.setOpaque(false);

        // A. Slot Matrix Strip Card
        AppleCard slotCard = new AppleCard(14);
        slotCard.setLayout(new BorderLayout(8, 8));
        slotCard.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));

        JPanel slotHeader = new JPanel(new BorderLayout());
        slotHeader.setOpaque(false);
        JLabel lblSlotMatrixTitle = new JLabel("LIVE LOT CAPACITY OVERVIEW (CLICK OCCUPIED SLOT TO SELECT)");
        lblSlotMatrixTitle.setFont(getAppleFont(Font.BOLD, 10.5f));
        lblSlotMatrixTitle.setForeground(TEXT_SECONDARY);
        slotHeader.add(lblSlotMatrixTitle, BorderLayout.WEST);

        slotMatrixPanel.setOpaque(false);
        buildSlotMatrix();

        slotCard.add(slotHeader, BorderLayout.NORTH);
        slotCard.add(slotMatrixPanel, BorderLayout.CENTER);
        topSection.add(slotCard, BorderLayout.NORTH);

        // B. Vehicle Action Controls Card
        AppleCard actionCard = new AppleCard(14);
        actionCard.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 10));
        actionCard.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));

        JLabel lblVeh = new JLabel("Vehicle Reg. No:");
        lblVeh.setFont(getAppleFont(Font.BOLD, 12.5f));
        lblVeh.setForeground(TEXT_PRIMARY);

        actionCard.add(lblVeh);
        actionCard.add(txtVehicleNo);
        actionCard.add(btnCheckIn);
        actionCard.add(btnCheckOut);
        actionCard.add(btnPrintReceipt);
        actionCard.add(btnClear);

        topSection.add(actionCard, BorderLayout.CENTER);
        mainContent.add(topSection, BorderLayout.NORTH);

        // ==========================================
        // 3. CENTER SPLIT PANE: ACTIVE LOT TABLE + DIGITAL RECEIPT PASS
        // ==========================================
        // Left: Active Allocation Table Card
        AppleCard tableCard = new AppleCard(14);
        tableCard.setLayout(new BorderLayout(0, 8));
        tableCard.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        JPanel tableCardHeader = new JPanel(new BorderLayout());
        tableCardHeader.setOpaque(false);
        JLabel lblTableTitle = new JLabel("Active Parked Vehicles");
        lblTableTitle.setFont(getAppleFont(Font.BOLD, 14f));
        lblTableTitle.setForeground(TEXT_PRIMARY);
        JLabel lblTableHint = new JLabel("Select row to check out");
        lblTableHint.setFont(getAppleFont(Font.PLAIN, 12f));
        lblTableHint.setForeground(TEXT_SECONDARY);
        tableCardHeader.add(lblTableTitle, BorderLayout.WEST);
        tableCardHeader.add(lblTableHint, BorderLayout.EAST);
        tableCard.add(tableCardHeader, BorderLayout.NORTH);

        String[] columns = {"Ticket ID", "Slot No", "Vehicle Registration", "Entry Timestamp", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        parkedTable = new JTable(tableModel);
        parkedTable.setRowHeight(36);
        parkedTable.setFont(getAppleFont(Font.PLAIN, 13f));
        parkedTable.setForeground(TEXT_PRIMARY);
        parkedTable.setSelectionBackground(APPLE_BLUE_BG);
        parkedTable.setSelectionForeground(APPLE_BLUE);
        parkedTable.setGridColor(COLOR_BORDER_SOFT);
        parkedTable.setShowHorizontalLines(true);
        parkedTable.setShowVerticalLines(false);
        parkedTable.setIntercellSpacing(new Dimension(0, 1));
        parkedTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Table Header Styling
        parkedTable.getTableHeader().setDefaultRenderer(new AppleTableHeaderRenderer());
        parkedTable.getTableHeader().setPreferredSize(new Dimension(0, 34));
        parkedTable.getTableHeader().setReorderingAllowed(false);

        // Custom Cell Renderers for Badges
        parkedTable.getColumnModel().getColumn(0).setCellRenderer(new TicketIdCellRenderer());
        parkedTable.getColumnModel().getColumn(1).setCellRenderer(new SlotCellRenderer());
        parkedTable.getColumnModel().getColumn(2).setCellRenderer(new VehicleNoCellRenderer());
        parkedTable.getColumnModel().getColumn(3).setCellRenderer(new StandardCellRenderer());
        parkedTable.getColumnModel().getColumn(4).setCellRenderer(new StatusBadgeCellRenderer());

        // Column widths
        parkedTable.getColumnModel().getColumn(0).setPreferredWidth(75);
        parkedTable.getColumnModel().getColumn(1).setPreferredWidth(85);
        parkedTable.getColumnModel().getColumn(2).setPreferredWidth(160);
        parkedTable.getColumnModel().getColumn(3).setPreferredWidth(150);
        parkedTable.getColumnModel().getColumn(4).setPreferredWidth(100);

        parkedTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = parkedTable.getSelectedRow();
                if (row >= 0) {
                    String vehNo = String.valueOf(tableModel.getValueAt(row, 2));
                    txtVehicleNo.setText(vehNo);
                }
            }
        });

        JScrollPane tableScroll = new JScrollPane(parkedTable);
        tableScroll.setBorder(BorderFactory.createEmptyBorder());
        tableScroll.getViewport().setBackground(COLOR_CARD);
        tableScroll.getVerticalScrollBar().setUI(new AppleScrollBarUI());
        tableScroll.getHorizontalScrollBar().setUI(new AppleScrollBarUI());
        tableCard.add(tableScroll, BorderLayout.CENTER);

        // Right: Digital Receipt / Pass Card
        AppleCard receiptCard = new AppleCard(14);
        receiptCard.setLayout(new BorderLayout(0, 8));
        receiptCard.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        JPanel receiptCardHeader = new JPanel(new BorderLayout());
        receiptCardHeader.setOpaque(false);
        JLabel lblReceiptTitle = new JLabel("Digital Receipt & Ticket Pass");
        lblReceiptTitle.setFont(getAppleFont(Font.BOLD, 14f));
        lblReceiptTitle.setForeground(TEXT_PRIMARY);

        JLabel lblReceiptBadge = new JLabel("● Live Output");
        lblReceiptBadge.setFont(getAppleFont(Font.BOLD, 11f));
        lblReceiptBadge.setForeground(APPLE_BLUE);
        receiptCardHeader.add(lblReceiptTitle, BorderLayout.WEST);
        receiptCardHeader.add(lblReceiptBadge, BorderLayout.EAST);
        receiptCard.add(receiptCardHeader, BorderLayout.NORTH);

        txtReceipt.setFont(getAppleMonoFont(Font.PLAIN, 12.5f));
        txtReceipt.setEditable(false);
        txtReceipt.setBackground(new Color(250, 250, 252));
        txtReceipt.setForeground(TEXT_PRIMARY);
        txtReceipt.setLineWrap(true);
        txtReceipt.setWrapStyleWord(true);
        txtReceipt.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));

        JScrollPane receiptScroll = new JScrollPane(txtReceipt);
        receiptScroll.setBorder(BorderFactory.createLineBorder(COLOR_BORDER, 1, true));
        receiptScroll.getViewport().setBackground(new Color(250, 250, 252));
        receiptScroll.getVerticalScrollBar().setUI(new AppleScrollBarUI());
        receiptScroll.getHorizontalScrollBar().setUI(new AppleScrollBarUI());
        receiptCard.add(receiptScroll, BorderLayout.CENTER);

        // Split Pane combining Table and Receipt
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tableCard, receiptCard);
        splitPane.setResizeWeight(0.62);
        splitPane.setBorder(BorderFactory.createEmptyBorder());
        splitPane.setOpaque(false);
        splitPane.setDividerSize(8);
        splitPane.setUI(new BasicSplitPaneUI() {
            @Override
            public BasicSplitPaneDivider createDefaultDivider() {
                return new BasicSplitPaneDivider(this) {
                    @Override
                    public void paint(Graphics g) {
                        g.setColor(COLOR_CANVAS);
                        g.fillRect(0, 0, getWidth(), getHeight());
                    }
                };
            }
        });

        mainContent.add(splitPane, BorderLayout.CENTER);
        add(mainContent, BorderLayout.CENTER);

        // ==========================================
        // 4. BOTTOM STATUS BAR FOOTER
        // ==========================================
        JPanel statusFooter = new JPanel(new BorderLayout(12, 0));
        statusFooter.setBackground(COLOR_CARD);
        statusFooter.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, COLOR_BORDER),
                BorderFactory.createEmptyBorder(7, 24, 7, 24)));

        lblStatusMessage.setFont(getAppleFont(Font.PLAIN, 12f));
        lblStatusMessage.setForeground(TEXT_SECONDARY);

        JLabel lblSystemBadge = new JLabel("● SQLite Ready");
        lblSystemBadge.setFont(getAppleFont(Font.BOLD, 11f));
        lblSystemBadge.setForeground(APPLE_GREEN);

        statusFooter.add(lblStatusMessage, BorderLayout.WEST);
        statusFooter.add(lblSystemBadge, BorderLayout.EAST);
        add(statusFooter, BorderLayout.SOUTH);

        // Attach live automatic Indian vehicle number formatter
        setupVehicleNumberFormatter();
    }

    /**
     * Builds interactive slot pills for visual overview
     */
    private void buildSlotMatrix() {
        slotMatrixPanel.removeAll();
        slotPillMap.clear();
        for (int i = 1; i <= ParkingModel.TOTAL_SLOTS; i++) {
            SlotPill pill = new SlotPill(i);
            final int slotNum = i;
            pill.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (pill.isOccupied && pill.vehicleNo != null) {
                        txtVehicleNo.setText(pill.vehicleNo);
                    }
                }
            });
            slotPillMap.put(i, pill);
            slotMatrixPanel.add(pill);
        }
        slotMatrixPanel.revalidate();
        slotMatrixPanel.repaint();
    }

    /**
     * Live interactive DocumentFilter for automatic Indian vehicle number formatting
     */
    private void setupVehicleNumberFormatter() {
        ((AbstractDocument) txtVehicleNo.getDocument()).setDocumentFilter(new DocumentFilter() {
            private boolean updating = false;

            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
                if (string == null || string.isEmpty()) return;
                replace(fb, offset, 0, string, attr);
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                if (updating) {
                    super.replace(fb, offset, length, text, attrs);
                    return;
                }
                txtVehicleNo.setError(false);
                Document d = fb.getDocument();
                String cur = d.getText(0, d.getLength());
                String candidate = cur.substring(0, offset) + (text != null ? text : "") + cur.substring(offset + length);
                String formatted = VehicleNumberFormatter.format(candidate);

                int targetAlpha = VehicleNumberFormatter.countAlphanumeric(candidate.substring(0, offset + (text != null ? text.length() : 0)));
                int newCaret = Math.min(targetAlpha, formatted.length());

                updating = true;
                fb.replace(0, d.getLength(), formatted, attrs);
                updating = false;

                SwingUtilities.invokeLater(() -> {
                    txtVehicleNo.setCaretPosition(Math.min(newCaret, txtVehicleNo.getText().length()));
                });
            }

            @Override
            public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
                if (updating) {
                    super.remove(fb, offset, length);
                    return;
                }
                txtVehicleNo.setError(false);
                Document d = fb.getDocument();
                String cur = d.getText(0, d.getLength());
                String candidate = cur.substring(0, Math.max(0, offset))
                        + cur.substring(Math.min(cur.length(), offset + length));
                String formatted = VehicleNumberFormatter.format(candidate);

                int targetAlpha = VehicleNumberFormatter.countAlphanumeric(cur.substring(0, offset));
                int newCaret = Math.min(targetAlpha, formatted.length());

                updating = true;
                fb.replace(0, d.getLength(), formatted, null);
                updating = false;

                SwingUtilities.invokeLater(() -> {
                    txtVehicleNo.setCaretPosition(Math.min(newCaret, txtVehicleNo.getText().length()));
                });
            }
        });
    }

    public String getVehicleNoInput() {
        return VehicleNumberFormatter.padRegistrationNumber(txtVehicleNo.getText().trim());
    }

    public void setVehicleNoText(String text) {
        txtVehicleNo.setText(text != null ? text : "");
    }

    public void highlightInputError() {
        txtVehicleNo.setError(true);
        txtVehicleNo.requestFocusInWindow();
    }

    public void clearInput() {
        txtVehicleNo.setText("");
        parkedTable.clearSelection();
        txtVehicleNo.requestFocusInWindow();
    }

    public void setStatusMessage(String message) {
        lblStatusMessage.setText(message);
    }

    public void showInfoDialog(String message, String title) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.INFORMATION_MESSAGE);
    }

    public void showErrorDialog(String message, String title) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
    }

    public void updateParkedVehiclesTable(List<ParkingRecord> activeRecords, int totalSlots) {
        tableModel.setRowCount(0);
        Set<Integer> occupiedSlotSet = new HashSet<>();

        // Reset visual slot pills
        for (SlotPill pill : slotPillMap.values()) {
            pill.setFree();
        }

        for (ParkingRecord r : activeRecords) {
            tableModel.addRow(new Object[]{
                    r.ticketId,
                    "Slot #" + r.slotNumber,
                    r.vehicleNo,
                    r.entryTime,
                    r.status
            });
            occupiedSlotSet.add(r.slotNumber);
            SlotPill pill = slotPillMap.get(r.slotNumber);
            if (pill != null) {
                pill.setOccupied(r.vehicleNo);
            }
        }

        int occupied = activeRecords.size();
        int available = Math.max(0, totalSlots - occupied);
        lblOccupancyPill.setText(String.format("● %d / %d Occupied  •  %d Available", occupied, totalSlots, available));
        occupancyBadge.updateState(occupied, totalSlots);
        slotMatrixPanel.repaint();
    }

    public void displayEntrySlip(ParkingRecord r) {
        StringBuilder sb = new StringBuilder();
        sb.append("────────────────────────────────────────\n");
        sb.append("               PARKFLOW                 \n");
        sb.append("         Vehicle Parking Pass           \n");
        sb.append("────────────────────────────────────────\n\n");
        sb.append(String.format(" Ticket ID        : #%04d\n", r.ticketId));
        sb.append(String.format(" Vehicle Reg No   : %s\n", r.vehicleNo));
        sb.append(String.format(" Allocated Slot   : Slot #%d\n", r.slotNumber));
        sb.append(String.format(" Entry Time       : %s\n", r.entryTime));
        sb.append(String.format(" Hourly Rate      : Rs. %.2f / hr\n", ParkingModel.HOURLY_RATE));
        sb.append(" Parking Status   : ACTIVE (PARKED)\n\n");
        sb.append("────────────────────────────────────────\n");
        sb.append(" Please retain this digital pass for exit\n");
        sb.append("────────────────────────────────────────\n");
        txtReceipt.setText(sb.toString());
    }

    public void displayExitReceipt(ParkingRecord r) {
        StringBuilder sb = new StringBuilder();
        sb.append("────────────────────────────────────────\n");
        sb.append("               PARKFLOW                 \n");
        sb.append("         Official Fee Receipt           \n");
        sb.append("────────────────────────────────────────\n\n");
        sb.append(String.format(" Ticket ID        : #%04d\n", r.ticketId));
        sb.append(String.format(" Vehicle Reg No   : %s\n", r.vehicleNo));
        sb.append(String.format(" Released Slot    : Slot #%d\n", r.slotNumber));
        sb.append("────────────────────────────────────────\n");
        sb.append(String.format(" Entry Timestamp  : %s\n", r.entryTime));
        sb.append(String.format(" Exit Timestamp   : %s\n", r.exitTime));
        sb.append(String.format(" Total Duration   : %d min(s)\n", r.durationMinutes));
        sb.append(String.format(" Billable Hours   : %d hr(s)\n", r.billableHours));
        sb.append(String.format(" Hourly Rate      : Rs. %.2f / hr\n", ParkingModel.HOURLY_RATE));
        sb.append("────────────────────────────────────────\n");
        sb.append(String.format(" TOTAL FEE PAID   : Rs. %.2f\n", r.fee));
        sb.append(" Payment Status   : PAID (COMPLETED)\n\n");
        sb.append("────────────────────────────────────────\n");
        sb.append("       Thank you for parking with us!   \n");
        sb.append("────────────────────────────────────────\n");
        txtReceipt.setText(sb.toString());
    }

    public void printReceiptContent() {
        if (txtReceipt.getText().trim().isEmpty()) {
            showErrorDialog("No receipt generated to print yet.", "Print Error");
            return;
        }
        try {
            boolean complete = txtReceipt.print();
            if (complete) {
                setStatusMessage("Receipt sent to printer successfully.");
            } else {
                setStatusMessage("Receipt printing cancelled by user.");
            }
        } catch (PrinterException ex) {
            showErrorDialog("Printer error: " + ex.getMessage(), "Print Failed");
        }
    }

    public void setReceiptText(String text) {
        txtReceipt.setText(text);
    }

    // Event listener registration methods for Controller
    public void addCheckInListener(ActionListener listener) {
        btnCheckIn.addActionListener(listener);
    }

    public void addCheckOutListener(ActionListener listener) {
        btnCheckOut.addActionListener(listener);
    }

    public void addPrintReceiptListener(ActionListener listener) {
        btnPrintReceipt.addActionListener(listener);
    }

    public void addClearListener(ActionListener listener) {
        btnClear.addActionListener(listener);
    }

    // =========================================================================================
    // CUSTOM DESIGN SYSTEM COMPONENTS & TYPOGRAPHY
    // =========================================================================================

    static {
        // Automatically load and register Manrope & Google Sans font families from fonts directory
        try {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            File dir = new File("fonts");
            if (!dir.exists()) dir = new File("c:/Users/hp/Desktop/mvc/fonts");
            if (dir.exists() && dir.isDirectory()) {
                File[] fontFiles = dir.listFiles();
                if (fontFiles != null) {
                    for (File f : fontFiles) {
                        if (f.getName().toLowerCase().endsWith(".ttf")) {
                            try {
                                Font customFont = Font.createFont(Font.TRUETYPE_FONT, f);
                                ge.registerFont(customFont);
                            } catch (Exception ignored) {}
                        }
                    }
                }
            }
        } catch (Exception ignored) {}
    }

    /**
     * Resolves Manrope / Google Sans typography with modern sans-serif fallbacks.
     */
    public static Font getManropeFont(int style, float size) {
        String[] preferred = {"Manrope", "Google Sans", "Product Sans", "Plus Jakarta Sans", "SF Pro Display", "Segoe UI", "SansSerif"};
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String[] available = ge.getAvailableFontFamilyNames();
        Set<String> set = new HashSet<>(Arrays.asList(available));
        for (String f : preferred) {
            if (set.contains(f)) {
                return new Font(f, style, (int) size).deriveFont(size);
            }
        }
        return new Font("SansSerif", style, (int) size).deriveFont(size);
    }

    public static Font getAppleFont(int style, float size) {
        return getManropeFont(style, size);
    }

    public static Font getAppleMonoFont(int style, float size) {
        String[] preferred = {"SF Mono", "Menlo", "Consolas", "Courier New", "Monospaced"};
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String[] available = ge.getAvailableFontFamilyNames();
        Set<String> set = new HashSet<>(Arrays.asList(available));
        for (String f : preferred) {
            if (set.contains(f)) {
                return new Font(f, style, (int) size).deriveFont(size);
            }
        }
        return new Font("Monospaced", style, (int) size).deriveFont(size);
    }

    /**
     * Apple-style Card Panel: Pure white container with soft rounded corners and subtle border.
     */
    public static class AppleCard extends JPanel {
        private final int cornerRadius;

        public AppleCard(int radius) {
            this.cornerRadius = radius;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(COLOR_CARD);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius);

            g2.setColor(COLOR_BORDER);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius);

            g2.dispose();
            super.paintComponent(g);
        }
    }

    /**
     * Apple-style Button with rounded pill geometry and subtle hover dynamics.
     */
    public static class AppleButton extends JButton {
        public enum Style { PRIMARY, DESTRUCTIVE, SECONDARY, GHOST }
        private final Style style;
        private boolean isHovered = false;
        private boolean isPressed = false;
        private final int radius = 9;

        public AppleButton(String text, Style style) {
            super(text);
            this.style = style;
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setFont(getAppleFont(Font.BOLD, 12.5f));
            setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) { isHovered = true; repaint(); }
                @Override
                public void mouseExited(MouseEvent e) { isHovered = false; repaint(); }
                @Override
                public void mousePressed(MouseEvent e) { isPressed = true; repaint(); }
                @Override
                public void mouseReleased(MouseEvent e) { isPressed = false; repaint(); }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Color bg;
            Color fg;
            Color border = null;

            switch (style) {
                case PRIMARY:
                    bg = isPressed ? APPLE_BLUE_PRESS : (isHovered ? APPLE_BLUE_HOVER : APPLE_BLUE);
                    fg = Color.WHITE;
                    break;
                case DESTRUCTIVE:
                    bg = isPressed ? APPLE_RED_PRESS : (isHovered ? APPLE_RED_HOVER : APPLE_RED);
                    fg = Color.WHITE;
                    break;
                case SECONDARY:
                    bg = isPressed ? APPLE_GRAY_PRESS : (isHovered ? APPLE_GRAY_HOVER : APPLE_GRAY_BTN);
                    fg = TEXT_PRIMARY;
                    border = COLOR_BORDER;
                    break;
                case GHOST:
                default:
                    bg = isPressed ? new Color(225, 225, 230) : (isHovered ? new Color(238, 238, 242) : new Color(0, 0, 0, 0));
                    fg = TEXT_SECONDARY;
                    border = isHovered ? COLOR_BORDER : new Color(230, 230, 235);
                    break;
            }

            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius * 2, radius * 2);

            if (border != null) {
                g2.setColor(border);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius * 2, radius * 2);
            }

            g2.dispose();
            setForeground(fg);
            super.paintComponent(g);
        }
    }

    /**
     * Apple-style TextField: Smooth rounded field with active blue focus ring and watermark placeholder.
     */
    public static class AppleTextField extends JTextField {
        private boolean isFocused = false;
        private boolean isError = false;
        private final String placeholder;
        private final int radius = 9;

        public AppleTextField(String placeholder, int columns) {
            super(columns);
            this.placeholder = placeholder;
            setOpaque(false);
            setFont(getAppleMonoFont(Font.BOLD, 13.5f));
            setForeground(TEXT_PRIMARY);
            setCaretColor(APPLE_BLUE);
            setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

            addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    isFocused = true;
                    isError = false;
                    repaint();
                }
                @Override
                public void focusLost(FocusEvent e) {
                    isFocused = false;
                    repaint();
                }
            });
        }

        public void setError(boolean error) {
            this.isError = error;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(Color.WHITE);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius * 2, radius * 2);

            if (isError) {
                // Apple Red error ring
                g2.setColor(new Color(255, 59, 48, 45));
                g2.setStroke(new BasicStroke(2.0f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius * 2, radius * 2);

                g2.setColor(APPLE_RED);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, radius * 2 - 2, radius * 2 - 2);
            } else if (isFocused) {
                g2.setColor(new Color(0, 113, 227, 45));
                g2.setStroke(new BasicStroke(2.0f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius * 2, radius * 2);

                g2.setColor(APPLE_BLUE);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, radius * 2 - 2, radius * 2 - 2);
            } else {
                g2.setColor(new Color(212, 212, 218));
                g2.setStroke(new BasicStroke(1.0f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius * 2, radius * 2);
            }

            g2.dispose();
            super.paintComponent(g);

            if (getText().isEmpty() && !isFocused && placeholder != null) {
                Graphics2D gText = (Graphics2D) g.create();
                gText.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
                gText.setColor(TEXT_MUTED);
                gText.setFont(getAppleFont(Font.PLAIN, 12.5f));
                FontMetrics fm = gText.getFontMetrics();
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                gText.drawString(placeholder, 12, y);
                gText.dispose();
            }
        }
    }

    /**
     * Minimalist Apple Pill Badge for occupancy metrics:
     * Compact capsule shape with tailored internal padding, adaptive status colors,
     * and crisp anti-aliased border.
     */
    public static class PillBadge extends JPanel {
        private final JLabel label;
        private Color bgColor = new Color(236, 248, 239);
        private Color borderColor = new Color(195, 232, 206);
        private Color textColor = new Color(28, 115, 60);

        public PillBadge(JLabel label) {
            super(new BorderLayout());
            this.label = label;
            setOpaque(false);

            label.setFont(getAppleFont(Font.BOLD, 12f));
            label.setForeground(textColor);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setVerticalAlignment(SwingConstants.CENTER);

            // Snug, proportional padding around the text
            setBorder(BorderFactory.createEmptyBorder(5, 14, 5, 14));
            add(label, BorderLayout.CENTER);
        }

        public void updateState(int occupied, int total) {
            int available = Math.max(0, total - occupied);
            if (available == 0) {
                // Lot is FULL
                bgColor = new Color(254, 240, 240);
                borderColor = new Color(252, 205, 205);
                textColor = new Color(195, 40, 40);
            } else if (available <= 2) {
                // Almost full warning
                bgColor = new Color(254, 247, 235);
                borderColor = new Color(250, 222, 175);
                textColor = new Color(175, 100, 20);
            } else {
                // Normal
                bgColor = new Color(236, 248, 239);
                borderColor = new Color(195, 232, 206);
                textColor = new Color(28, 115, 60);
            }
            label.setForeground(textColor);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int arc = h; // Exact pill / capsule geometry

            g2.setColor(bgColor);
            g2.fillRoundRect(0, 0, w - 1, h - 1, arc, arc);

            g2.setColor(borderColor);
            g2.setStroke(new BasicStroke(1.0f));
            g2.drawRoundRect(0, 0, w - 1, h - 1, arc, arc);

            g2.dispose();
            super.paintComponent(g);
        }
    }

    /**
     * Individual visual slot pill in the Live Slot Matrix
     */
    public static class SlotPill extends JPanel {
        public final int slotNumber;
        public boolean isOccupied = false;
        public String vehicleNo = null;

        private final JLabel lblNumber = new JLabel();
        private final JLabel lblVehDesc = new JLabel();

        public SlotPill(int slotNumber) {
            super(new GridLayout(2, 1, 0, 1));
            this.slotNumber = slotNumber;
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

            lblNumber.setHorizontalAlignment(SwingConstants.CENTER);
            lblNumber.setFont(getAppleFont(Font.BOLD, 11f));

            lblVehDesc.setHorizontalAlignment(SwingConstants.CENTER);
            lblVehDesc.setFont(getAppleFont(Font.PLAIN, 10f));

            add(lblNumber);
            add(lblVehDesc);
            setFree();
        }

        public void setFree() {
            this.isOccupied = false;
            this.vehicleNo = null;
            lblNumber.setText("Slot " + slotNumber);
            lblNumber.setForeground(new Color(35, 130, 65));
            lblVehDesc.setText("Free");
            lblVehDesc.setForeground(TEXT_SECONDARY);
            setToolTipText("Slot #" + slotNumber + " is Available");
            repaint();
        }

        public void setOccupied(String vehicleNo) {
            this.isOccupied = true;
            this.vehicleNo = vehicleNo;
            lblNumber.setText("Slot " + slotNumber);
            lblNumber.setForeground(APPLE_BLUE);
            String displayVeh = vehicleNo.length() > 6 ? vehicleNo.substring(vehicleNo.length() - 6) : vehicleNo;
            lblVehDesc.setText(displayVeh);
            lblVehDesc.setForeground(TEXT_PRIMARY);
            setToolTipText("Slot #" + slotNumber + " Occupied by " + vehicleNo + " (Click to select)");
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (isOccupied) {
                g2.setColor(APPLE_BLUE_BG);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.setColor(new Color(195, 220, 252));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
            } else {
                g2.setColor(new Color(248, 250, 248));
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.setColor(new Color(225, 235, 225));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }

    /**
     * Animated Logo Icon: Displays the authentic original multi-frame animated parking GIF
     * with its original motion: moving car body, shining windshield, and shifting P parking sign.
     */
    public static class AnimatedLogoView extends JPanel {
        private final Image gifImage;

        public AnimatedLogoView() {
            setOpaque(false);
            setPreferredSize(new Dimension(48, 48));
            setMinimumSize(new Dimension(48, 48));
            setMaximumSize(new Dimension(48, 48));

            File gifFile = new File("assets/logo.gif");
            if (!gifFile.exists()) gifFile = new File("assets/parking.gif");
            if (!gifFile.exists()) gifFile = new File("logo.gif");
            if (!gifFile.exists()) gifFile = new File("parking.gif");

            if (gifFile.exists()) {
                this.gifImage = new ImageIcon(gifFile.getAbsolutePath()).getImage();
            } else {
                File png = new File("assets/logo.png");
                if (!png.exists()) png = new File("logo.png");
                this.gifImage = png.exists() ? new ImageIcon(png.getAbsolutePath()).getImage() : null;
            }
        }

        @Override
        public boolean imageUpdate(Image img, int infoflags, int x, int y, int w, int h) {
            repaint();
            return super.imageUpdate(img, infoflags, x, y, w, h);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (gifImage != null) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.drawImage(gifImage, 0, 0, getWidth(), getHeight(), this);
                g2.dispose();
            }
        }
    }

    /**
     * Apple Minimalist ScrollBar UI
     */
    public static class AppleScrollBarUI extends BasicScrollBarUI {
        @Override
        protected void configureScrollBarColors() {
            this.thumbColor = new Color(205, 205, 210);
            this.trackColor = COLOR_CARD;
        }

        @Override
        protected JButton createDecreaseButton(int orientation) { return createZeroButton(); }
        @Override
        protected JButton createIncreaseButton(int orientation) { return createZeroButton(); }

        private JButton createZeroButton() {
            JButton btn = new JButton();
            btn.setPreferredSize(new Dimension(0, 0));
            btn.setMinimumSize(new Dimension(0, 0));
            btn.setMaximumSize(new Dimension(0, 0));
            return btn;
        }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
            if (thumbBounds.isEmpty() || !scrollbar.isEnabled()) return;
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(isThumbRollover() ? new Color(150, 150, 155) : new Color(195, 195, 200));
            int arc = Math.min(thumbBounds.width, 8);
            g2.fillRoundRect(thumbBounds.x + 2, thumbBounds.y + 2, thumbBounds.width - 4, thumbBounds.height - 4, arc, arc);
            g2.dispose();
        }

        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(COLOR_CARD);
            g2.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
            g2.dispose();
        }
    }

    /**
     * Clean table header renderer with muted uppercase typography
     */
    public static class AppleTableHeaderRenderer extends DefaultTableCellRenderer {
        public AppleTableHeaderRenderer() {
            setHorizontalAlignment(SwingConstants.LEFT);
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setBackground(COLOR_CANVAS);
            setForeground(TEXT_SECONDARY);
            setFont(getAppleFont(Font.BOLD, 10.5f));
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BORDER),
                    BorderFactory.createEmptyBorder(6, 12, 6, 12)));
            setText(value != null ? value.toString().toUpperCase() : "");
            return this;
        }
    }

    /**
     * Standard Cell Renderer with clean row padding and alternate highlights
     */
    public static class StandardCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
            setFont(getAppleFont(Font.PLAIN, 12.5f));
            if (isSelected) {
                setBackground(APPLE_BLUE_BG);
                setForeground(APPLE_BLUE);
            } else {
                setBackground(row % 2 == 0 ? COLOR_CARD : new Color(250, 250, 252));
                setForeground(TEXT_PRIMARY);
            }
            return this;
        }
    }

    public static class TicketIdCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
            setFont(getAppleMonoFont(Font.BOLD, 12f));
            if (isSelected) {
                setBackground(APPLE_BLUE_BG);
                setForeground(APPLE_BLUE);
            } else {
                setBackground(row % 2 == 0 ? COLOR_CARD : new Color(250, 250, 252));
                setForeground(TEXT_SECONDARY);
            }
            setText(value != null ? String.format("#%04d", Integer.parseInt(value.toString())) : "");
            return this;
        }
    }

    public static class VehicleNoCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
            setFont(getAppleMonoFont(Font.BOLD, 13f));
            if (isSelected) {
                setBackground(APPLE_BLUE_BG);
                setForeground(APPLE_BLUE);
            } else {
                setBackground(row % 2 == 0 ? COLOR_CARD : new Color(250, 250, 252));
                setForeground(TEXT_PRIMARY);
            }
            return this;
        }
    }

    public static class SlotCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
            setFont(getAppleFont(Font.BOLD, 12f));
            if (isSelected) {
                setBackground(APPLE_BLUE_BG);
                setForeground(APPLE_BLUE);
            } else {
                setBackground(row % 2 == 0 ? COLOR_CARD : new Color(250, 250, 252));
                setForeground(APPLE_BLUE);
            }
            return this;
        }
    }

    /**
     * Status Column Pill Badge: Renders active state as a green Apple capsule chip
     */
    public static class StatusBadgeCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
            panel.setOpaque(true);
            panel.setBackground(isSelected ? APPLE_BLUE_BG : (row % 2 == 0 ? COLOR_CARD : new Color(250, 250, 252)));

            String text = value != null ? value.toString() : "";
            JLabel badge = new JLabel("● " + text);
            badge.setFont(getAppleFont(Font.BOLD, 11f));

            if ("PARKED".equalsIgnoreCase(text)) {
                badge.setForeground(new Color(35, 130, 65));
            } else {
                badge.setForeground(TEXT_SECONDARY);
            }

            panel.add(badge);
            return panel;
        }
    }
}
