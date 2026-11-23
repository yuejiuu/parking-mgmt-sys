import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * CRA2 Parking Lot - main window (GUI only).
 *
 * Design patterns used:
 *   Factory Method (creational) - VehicleCreator creates Car / Motorcycle / SUV / Truck objects
 *   Facade         (structural) - ParkingFacade hides the parking rules, slots and fees
 *   Decorator      (structural) - TicketText builds the ticket and the receipt
 *
 * Plate checker: each Vehicle knows its own Philippine plate rule
 * (Motorcycle = 3 letters + 3 digits, Car / SUV / Truck = 3 letters + 4 digits).
 * The slot map shows a picture of the parked vehicle (VehicleIcon).
 *
 * The layout is in ParkingManagementSystem.form. The field names below must match the form.
 */
public class ParkingManagementSystem {

    // ===== Components from ParkingManagementSystem.form =====
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

    // ===== Patterns =====
    private final ParkingFacade lot = new ParkingFacade();             // Facade
    private final VehicleCreator[] creators = {                         // Factory Method
            new CarCreator(), new MotorcycleCreator(), new SuvVanCreator(), new TruckCreator()
    };

    private JButton[] slotButtons;                                      // the 16 buttons from the form

    // ===================================================================
    // Setup
    // ===================================================================
    private ParkingManagementSystem() {
        for (VehicleCreator creator : creators) {
            vehicleTypeCb.addItem(creator.getTypeName());
        }

        setupSlotButtons();
        applyColors();
        addListeners();
        startClock();
        checkPlateLive();

        ticketArea.setText("\n  No ticket yet.\n\n  Enter a plate number and\n  press Park Vehicle.");
        showPage(true);
    }

    /** The 16 slot buttons were drawn in the designer; we just collect them. */
    private void setupSlotButtons() {
        Component[] parts = slotPnl.getComponents();
        slotButtons = new JButton[parts.length];

        for (int i = 0; i < parts.length; i++) {
            final int index = i;
            slotButtons[i] = (JButton) parts[i];
            slotButtons[i].setOpaque(true);
            slotButtons[i].setFocusPainted(false);
            slotButtons[i].setForeground(Color.WHITE);
            slotButtons[i].setFont(slotButtons[i].getFont().deriveFont(Font.BOLD, 13f));
            slotButtons[i].setVerticalTextPosition(SwingConstants.BOTTOM);       // picture above the text
            slotButtons[i].setHorizontalTextPosition(SwingConstants.CENTER);
            slotButtons[i].addActionListener(e -> showSlotInfo(index));
        }
        refreshSlotMap();
    }

    private void addListeners() {
        entryBtn.addActionListener(e -> showPage(true));
        slotBtn.addActionListener(e -> showPage(false));
        exitBtn.addActionListener(e -> exitVehicle());
        parkBtn.addActionListener(e -> parkVehicle());
        platenumTF.addActionListener(e -> parkVehicle());              // Enter key

        // live plate checker: the box turns green when valid and red when not
        platenumTF.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { checkPlateLive(); }
            public void removeUpdate(DocumentEvent e) { checkPlateLive(); }
            public void changedUpdate(DocumentEvent e) { checkPlateLive(); }
        });
        vehicleTypeCb.addActionListener(e -> checkPlateLive());
    }

    /** Colors the plate box and shows the plate rule of the chosen vehicle type as a tooltip. */
    private void checkPlateLive() {
        String text = platenumTF.getText().trim();
        Vehicle vehicle = creators[vehicleTypeCb.getSelectedIndex()].createVehicle(text);

        platenumTF.setToolTipText(vehicle.getPlateHint());
        if (text.isEmpty()) {
            platenumTF.setBackground(Color.WHITE);
        } else if (vehicle.hasValidPlate()) {
            platenumTF.setBackground(new Color(220, 245, 226));        // valid = light green
        } else {
            platenumTF.setBackground(new Color(253, 226, 226));        // not valid = light red
        }
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
        try {
            VehicleCreator creator = creators[vehicleTypeCb.getSelectedIndex()];
            Vehicle vehicle = creator.createVehicle(platenumTF.getText().trim().toUpperCase());

            ParkingRecord record = lot.park(vehicle);

            ticketArea.setText(Tickets.ticketFor(record).render());
            slotNum.setText("Slot " + record.getSlot());
            platenumTF.setText("");
            platenumTF.requestFocusInWindow();
            refreshSlotMap();
        } catch (ParkingException ex) {
            showError(ex);
        }
    }

    // ===================================================================
    // Exit a vehicle
    // ===================================================================
    private void exitVehicle() {
        String[] plates = lot.getParkedPlates();
        if (plates.length == 0) {
            JOptionPane.showMessageDialog(mainPnl, "There are no parked vehicles.",
                    "Exit", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Object choice = JOptionPane.showInputDialog(mainPnl, "Select the vehicle that is leaving:",
                "Exit vehicle", JOptionPane.QUESTION_MESSAGE, null, plates, plates[0]);
        if (choice == null) {
            return;                                                    // user pressed Cancel
        }

        int sure = JOptionPane.showConfirmDialog(mainPnl,
                "Check out " + choice + " and compute the parking fee?",
                "Confirm exit", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (sure == JOptionPane.YES_OPTION) {
            finishCheckOut((String) choice);
        }
    }

    private void finishCheckOut(String plate) {
        try {
            ParkingRecord record = lot.checkOut(plate);

            ticketArea.setText(Tickets.receiptFor(record).render());
            slotNum.setText("");
            refreshSlotMap();
            showPage(true);                                            // show the receipt
        } catch (ParkingException ex) {
            showError(ex);
        }
    }

    // ===================================================================
    // Slot map
    // ===================================================================
    private void showSlotInfo(int index) {
        String plate = lot.getSlotPlate(index);
        String slotName = lot.getSlotName(index);

        if (plate == null) {
            JOptionPane.showMessageDialog(mainPnl, "Slot " + slotName + " is free.",
                    "Slot " + slotName, JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        ParkingRecord record = lot.getRecord(plate);
        String info = "Plate:  " + plate
                + "\nType:   " + record.getVehicle().getType()
                + "\nTime in:  " + new SimpleDateFormat("hh:mm a").format(record.getTimeIn())
                + "\n\nCheck out this vehicle now?";
        Icon picture = new VehicleIcon(record.getVehicle().getType(), NAVY, PAGE, 70);
        int answer = JOptionPane.showConfirmDialog(mainPnl, info, "Slot " + slotName,
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, picture);
        if (answer == JOptionPane.YES_OPTION) {
            finishCheckOut(plate);
        }
    }

    /** Repaints every slot: green = free, red = taken (with a picture of the vehicle and its plate). */
    private void refreshSlotMap() {
        for (int i = 0; i < slotButtons.length; i++) {
            String plate = lot.getSlotPlate(i);
            String name = lot.getSlotName(i);

            if (plate == null) {
                slotButtons[i].setBackground(GREEN);
                slotButtons[i].setIcon(null);
                slotButtons[i].setText(name);
            } else {
                String type = lot.getRecord(plate).getVehicle().getType();
                slotButtons[i].setBackground(RED);
                slotButtons[i].setIcon(new VehicleIcon(type, Color.WHITE, RED, 48));
                slotButtons[i].setText("<html><center>" + name + "<br><small>" + plate + "</small></center></html>");
            }
        }
    }

    private void showError(ParkingException ex) {
        int type = ex.isSerious() ? JOptionPane.ERROR_MESSAGE : JOptionPane.WARNING_MESSAGE;
        JOptionPane.showMessageDialog(mainPnl, ex.getMessage(), ex.getTitle(), type);
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
        colorButton(exitBtn, RED);

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
            frame.setSize(600, 620);
            frame.setMinimumSize(new Dimension(540, 540));
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
