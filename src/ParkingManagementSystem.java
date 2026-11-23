import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;
import java.util.regex.Pattern;

public class ParkingManagementSystem {

    // ===== Components from clone.form =====
    private JPanel mainPnl;
    private JPanel headerPnl;
    private JLabel titleLbl;
    private JLabel dateLbl;
    private JLabel clockLbl;
    private JPanel navPnl;
    private JButton entryBtn;
    private JButton slotBtn;
    private JButton exitBtn;
    private JPanel entryPnl;
    private JPanel entryLeft;
    private JLabel entryTitleLbl;
    private JTextField platenumTF;
    private JComboBox<String> vehicleTypeCb;
    private JButton parkBtn;
    private JTextField slotNum;
    private JScrollPane ticketScroll;
    private JTextArea ticketArea;
    private JPanel slotPnl;

    // ===== Colors =====
    private static final Color NAVY = new Color(22, 43, 72);
    private static final Color BLUE = new Color(0, 122, 204);
    private static final Color RED = new Color(204, 62, 62);
    private static final Color GREEN = new Color(46, 160, 67);
    private static final Color PAGE = new Color(243, 245, 248);
    private static final Color GRAY = new Color(224, 228, 234);
    private static final Color PAPER = new Color(255, 253, 240);

    // ===== Features not finished yet =====
    private static final boolean EXIT_ENABLED = false;      // set to true when the Exit feature is ready

    // ===== Data =====
    private static final Pattern PLATE_FORMAT = Pattern.compile("^[A-Z0-9][A-Z0-9 -]{3,9}$");
    private static final SimpleDateFormat TIME = new SimpleDateFormat("hh:mm a");

    private static class Vehicle {
        String ticketNo, plate, type, slot;
        Date timeIn;
    }

    private JButton[] slots;                          // the 16 slot buttons from the form
    private String[] slotPlates;                      // plate parked in each slot (null = free)
    private final Map<String, Vehicle> parked = new HashMap<>();
    private int ticketCount = 0;

    // ===================================================================
    // Setup
    // ===================================================================
    private ParkingManagementSystem() {
        vehicleTypeCb.addItem("Car");
        vehicleTypeCb.addItem("Motorcycle");
        vehicleTypeCb.addItem("SUV / Van");
        vehicleTypeCb.addItem("Truck");

        setupSlots();
        applyColors();
        addListeners();
        startClock();

        ticketArea.setText("\n  No ticket yet.\n\n  Enter a plate number and\n  press Park Vehicle.");
        showPage(true);
    }

    /** The 16 slot buttons were drawn in the designer; we just collect them. */
    private void setupSlots() {
        Component[] parts = slotPnl.getComponents();
        slots = new JButton[parts.length];
        slotPlates = new String[parts.length];

        for (int i = 0; i < parts.length; i++) {
            final int index = i;
            slots[i] = (JButton) parts[i];
            slots[i].setOpaque(true);
            slots[i].setFocusPainted(false);
            slots[i].setForeground(Color.WHITE);
            slots[i].setFont(slots[i].getFont().deriveFont(Font.BOLD, 14f));
            slots[i].addActionListener(e -> showSlotInfo(index));
            paintSlot(i);
        }
    }

    private void addListeners() {
        entryBtn.addActionListener(e -> showPage(true));
        slotBtn.addActionListener(e -> showPage(false));
        exitBtn.addActionListener(e -> exitVehicle());
        parkBtn.addActionListener(e -> parkVehicle());
        platenumTF.addActionListener(e -> parkVehicle());      // Enter key
    }

    // ===================================================================
    // Navigation
    // ===================================================================
    /** true = Entry page (form + ticket), false = Slot Map page. */
    private void showPage(boolean entryPage) {
        entryPnl.setVisible(entryPage);
        slotPnl.setVisible(!entryPage);
        colorNavButton(entryBtn, entryPage);
        colorNavButton(slotBtn, !entryPage);
        mainPnl.revalidate();
        mainPnl.repaint();
    }

    // ===================================================================
    // Park a vehicle
    // ===================================================================
    private void parkVehicle() {
        String plate = platenumTF.getText().trim().toUpperCase();

        // --- error handling ---
        if (plate.isEmpty()) {
            JOptionPane.showMessageDialog(mainPnl, "Please enter the plate number.",
                    "Missing plate number", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!PLATE_FORMAT.matcher(plate).matches()) {
            JOptionPane.showMessageDialog(mainPnl,
                    "Invalid plate number.\nUse 4 to 10 letters or numbers, for example: ABC 1234",
                    "Invalid plate number", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (parked.containsKey(plate)) {
            JOptionPane.showMessageDialog(mainPnl,
                    plate + " is already parked in slot " + parked.get(plate).slot + ".",
                    "Duplicate vehicle", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int free = findFreeSlot();
        if (free == -1) {
            JOptionPane.showMessageDialog(mainPnl, "Sorry, the parking lot is full.",
                    "No slots available", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // --- park ---
        Vehicle v = new Vehicle();
        ticketCount++;
        v.ticketNo = String.format("T-%04d", ticketCount);
        v.plate = plate;
        v.type = (String) vehicleTypeCb.getSelectedItem();
        v.slot = slotName(free);
        v.timeIn = new Date();

        parked.put(plate, v);
        slotPlates[free] = plate;
        paintSlot(free);

        slotNum.setText("Slot " + v.slot);
        ticketArea.setText(makeTicket(v));
        platenumTF.setText("");
        platenumTF.requestFocusInWindow();
    }

    private int findFreeSlot() {
        for (int i = 0; i < slotPlates.length; i++) {
            if (slotPlates[i] == null) {
                return i;
            }
        }
        return -1;
    }

    // ===================================================================
    // Exit a vehicle
    // ===================================================================
    private void exitVehicle() {
        if (!EXIT_ENABLED) {
            return;
        }
        if (parked.isEmpty()) {
            JOptionPane.showMessageDialog(mainPnl, "There are no parked vehicles.",
                    "Exit", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String[] plates = new TreeSet<>(parked.keySet()).toArray(new String[0]);
        Object choice = JOptionPane.showInputDialog(mainPnl, "Select the vehicle that is leaving:",
                "Exit vehicle", JOptionPane.QUESTION_MESSAGE, null, plates, plates[0]);
        if (choice == null) {
            return;                                           // user pressed Cancel
        }
        checkOut((String) choice);
    }

    private void checkOut(String plate) {
        Vehicle v = parked.get(plate);
        if (v == null) {
            JOptionPane.showMessageDialog(mainPnl, "No parked vehicle with plate " + plate + ".",
                    "Not found", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int sure = JOptionPane.showConfirmDialog(mainPnl,
                "Check out " + plate + " from slot " + v.slot + "?",
                "Confirm exit", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (sure != JOptionPane.YES_OPTION) {
            return;
        }

        // free the slot
        for (int i = 0; i < slotPlates.length; i++) {
            if (plate.equals(slotPlates[i])) {
                slotPlates[i] = null;
                paintSlot(i);
            }
        }
        parked.remove(plate);

        ticketArea.setText(makeReceipt(v));
        slotNum.setText("");
        showPage(true);                                       // show the receipt
    }

    // ===================================================================
    // Slot map
    // ===================================================================
    private void showSlotInfo(int index) {
        String plate = slotPlates[index];
        if (plate == null) {
            JOptionPane.showMessageDialog(mainPnl, "Slot " + slotName(index) + " is free.",
                    "Slot " + slotName(index), JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Vehicle v = parked.get(plate);
        JOptionPane.showMessageDialog(mainPnl,
                "Plate:  " + v.plate + "\nType:   " + v.type + "\nTime in:  " + TIME.format(v.timeIn),
                "Slot " + v.slot, JOptionPane.INFORMATION_MESSAGE);
    }

    /** Slot names run across the rows: A1 B1 C1 D1 / A2 B2 C2 D2 ... */
    private String slotName(int index) {
        return "" + (char) ('A' + index % 4) + (index / 4 + 1);
    }

    private void paintSlot(int i) {
        if (slotPlates[i] == null) {
            slots[i].setBackground(GREEN);
            slots[i].setText(slotName(i));
        } else {
            slots[i].setBackground(RED);
            slots[i].setText("<html><center>" + slotName(i) + "<br><small>" + slotPlates[i]
                    + "</small></center></html>");
        }
    }

    // ===================================================================
    // Ticket and receipt text
    // ===================================================================
    private String makeTicket(Vehicle v) {
        return "==============================\n"
                + "      CRA2 PARKING LOT\n"
                + "       PARKING TICKET\n"
                + "==============================\n"
                + "Ticket No : " + v.ticketNo + "\n"
                + "Plate     : " + v.plate + "\n"
                + "Type      : " + v.type + "\n"
                + "Slot      : " + v.slot + "\n"
                + "Time In   : " + TIME.format(v.timeIn) + "\n"
                + "------------------------------\n"
                + " Keep this ticket. Present it\n"
                + " when leaving the parking lot.\n"
                + "==============================\n";
    }

    private String makeReceipt(Vehicle v) {
        Date now = new Date();
        long minutes = Math.max(1, (now.getTime() - v.timeIn.getTime()) / 60000);
        return "==============================\n"
                + "      CRA2 PARKING LOT\n"
                + "       PARKING RECEIPT\n"
                + "==============================\n"
                + "Ticket No : " + v.ticketNo + "\n"
                + "Plate     : " + v.plate + "\n"
                + "Slot      : " + v.slot + "\n"
                + "Time In   : " + TIME.format(v.timeIn) + "\n"
                + "Time Out  : " + TIME.format(now) + "\n"
                + "Duration  : " + minutes + " min\n"
                + "------------------------------\n"
                + "   Thank you! Drive safely.\n"
                + "==============================\n";
    }

    // ===================================================================
    // Colors and clock
    // ===================================================================
    private void applyColors() {
        mainPnl.setBackground(PAGE);
        paintPanels(mainPnl);

        // header
        headerPnl.setBackground(NAVY);
        headerPnl.setBorder(BorderFactory.createMatteBorder(0, 0, 3, 0, BLUE));
        for (JLabel label : new JLabel[]{titleLbl, dateLbl, clockLbl}) {
            label.setForeground(Color.WHITE);
        }
        titleLbl.setFont(titleLbl.getFont().deriveFont(Font.BOLD, 18f));

        // navigation bar
        navPnl.setBackground(Color.WHITE);
        colorButton(exitBtn, EXIT_ENABLED ? RED : GRAY);
        exitBtn.setEnabled(EXIT_ENABLED);
        if (!EXIT_ENABLED) {
            exitBtn.setToolTipText("Coming soon");
        }

        // entry form
        entryLeft.setBackground(Color.WHITE);
        entryLeft.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(215, 219, 225)),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        entryTitleLbl.setFont(entryTitleLbl.getFont().deriveFont(Font.BOLD, 15f));
        entryTitleLbl.setForeground(NAVY);
        platenumTF.setFont(platenumTF.getFont().deriveFont(Font.BOLD, 14f));
        slotNum.setFont(slotNum.getFont().deriveFont(Font.BOLD, 14f));
        slotNum.setBackground(new Color(232, 244, 255));
        slotNum.setHorizontalAlignment(SwingConstants.CENTER);
        colorButton(parkBtn, BLUE);

        // ticket
        ticketArea.setFont(new Font(Font.MONOSPACED, Font.BOLD, 12));
        ticketArea.setBackground(PAPER);
        ticketScroll.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(215, 219, 225)), "Parking Ticket"));

        // slot map
        slotPnl.setBackground(Color.WHITE);
    }

    private void colorButton(JButton button, Color color) {
        button.setOpaque(true);
        button.setFocusPainted(false);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(button.getFont().deriveFont(Font.BOLD, 13f));
        button.setBorder(BorderFactory.createEmptyBorder(9, 18, 9, 18));
    }

    /** Selected navigation button is blue, the other is gray. */
    private void colorNavButton(JButton button, boolean selected) {
        colorButton(button, selected ? BLUE : GRAY);
        button.setForeground(selected ? Color.WHITE : NAVY);
    }

    private void paintPanels(Container parent) {
        for (Component child : parent.getComponents()) {
            if (child instanceof JPanel) {
                child.setBackground(PAGE);
                paintPanels((Container) child);
            }
        }
    }

    private void startClock() {
        Timer timer = new Timer(1000, e -> {
            Date now = new Date();
            dateLbl.setText("Date: " + new SimpleDateFormat("MMM d, yyyy").format(now));
            clockLbl.setText(new SimpleDateFormat("hh:mm:ss a").format(now));
        });
        timer.setInitialDelay(0);
        timer.start();
    }

    // ===================================================================
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Could not load the look and feel: " + e.getMessage(),
                        "Warning", JOptionPane.WARNING_MESSAGE);
            }
            JFrame frame = new JFrame("CRA2 Parking Lot");
            frame.setContentPane(new ParkingManagementSystem().mainPnl);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(560, 540);
            frame.setMinimumSize(new Dimension(520, 480));
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}