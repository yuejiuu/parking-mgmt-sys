import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * STRUCTURAL & CREATIONAL PATTERNS IN GUI:
 * 1. Abstract Factory (UIComponentFactory) - creates themed component families
 * 2. Builder (SlotButtonBuilder, PanelBuilder) - constructs complex components step by step
 * 3. Composite (SlotComponent, SlotGrid) - treats individual slots and groups uniformly
 * 4. Observer (LotObserver, UIUpdater) - decouples lot changes from UI refresh
 * 5. Strategy (LayoutStrategy) - swappable layout algorithms
 */
public class ParkingManagementSystem {

    // ===== Components =====
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
    private JCheckBox twoWheelerCb;
    private JCheckBox fourWheelerCb;
    private ButtonGroup vehicleTypeGroup;
    private JButton parkBtn;
    private JTextField slotNum;
    private JScrollPane ticketScroll;
    private JTextArea ticketArea;
    private JPanel slotPnl;
    private JPanel timeControlPnl;
    private JLabel timeOffsetLbl;

    // ===== Colors =====
    private static final Color NAVY = new Color(22, 43, 72);
    private static final Color HEADER_CARD = new Color(30, 56, 92);
    private static final Color HEADER_CARD_BORDER = new Color(56, 92, 138);
    private static final Color HEADER_SUBTEXT = new Color(184, 206, 232);
    private static final Color BLUE = new Color(0, 122, 204);
    private static final Color RED = new Color(204, 62, 62);
    private static final Color GREEN = new Color(46, 160, 67);
    private static final Color PAGE = new Color(243, 245, 248);
    private static final Color GRAY = new Color(224, 228, 234);
    private static final Color PAPER = new Color(255, 253, 240);

    // ===== Patterns =====
    private final ParkingFacade lot = new ParkingFacade();
    private final VehicleCreator carCreator = new CarCreator();
    private final VehicleCreator motorcycleCreator = new MotorcycleCreator();

    // UI Pattern components
    private UIComponentFactory uiFactory;
    private SlotGrid slotGrid;
    private List<LotObserver> lotObservers = new ArrayList<>();
    private LayoutStrategy currentLayout;

    private JButton[] slotButtons;

    // ===================================================================
    // Setup
    // ===================================================================
    private ParkingManagementSystem() {
        // Initialize pattern components
        uiFactory = new DefaultUIComponentFactory();
        currentLayout = new BorderLayoutStrategy();

        initComponents();
        setupSlotGrid();  // Uses Composite + Builder
        applyColors();
        addListeners();
        setupTimeControlShortcut();
        startClock();
        registerLotObservers();  // Observer pattern

        ticketArea.setText("\n  No ticket yet.\n\n  Enter a plate number and\n  press Park Vehicle.");
        showPage(true);
    }

    private void initComponents() {
        // PATTERN: Abstract Factory - create all components via factory
        mainPnl = uiFactory.createPanel();
        mainPnl.setLayout(new BorderLayout(0, 10));
        mainPnl.setBorder(uiFactory.createEmptyBorder(10, 10, 10, 10));

        // Header - title on the left, date & time badge on the right
        headerPnl = uiFactory.createPanel();
        headerPnl.setLayout(new BorderLayout(16, 0));

        Font titleFont = new Font(Font.SANS_SERIF, Font.BOLD, 20);
        Font clockFont = new Font(Font.SANS_SERIF, Font.BOLD, 15);
        Font dateFont = new Font(Font.SANS_SERIF, Font.PLAIN, 11);

        titleLbl = uiFactory.createLabel("CRA2 PARKING LOT");
        titleLbl.setFont(titleFont);
        titleLbl.setForeground(Color.WHITE);
        headerPnl.add(titleLbl, BorderLayout.WEST);

        JPanel dateClockPnl = new JPanel(new BorderLayout(0, 1)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(HEADER_CARD);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.setColor(HEADER_CARD_BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        dateClockPnl.setOpaque(false);
        dateClockPnl.setBorder(uiFactory.createEmptyBorder(6, 14, 7, 14));

        clockLbl = uiFactory.createLabel("");
        clockLbl.setForeground(Color.WHITE);
        clockLbl.setFont(clockFont);
        clockLbl.setHorizontalAlignment(SwingConstants.CENTER);

        dateLbl = uiFactory.createLabel("");
        dateLbl.setForeground(HEADER_SUBTEXT);
        dateLbl.setFont(dateFont);
        dateLbl.setHorizontalAlignment(SwingConstants.CENTER);

        dateClockPnl.add(clockLbl, BorderLayout.NORTH);
        dateClockPnl.add(dateLbl, BorderLayout.SOUTH);
        headerPnl.add(dateClockPnl, BorderLayout.EAST);

        // Navigation - PATTERN: Strategy (FlowLayoutStrategy)
        navPnl = uiFactory.createPanel();
        new FlowLayoutStrategy(FlowLayout.LEFT, 8, 6).layoutContainer(navPnl);
        navPnl.setBackground(Color.WHITE);

        entryBtn = uiFactory.createButton("Entry");
        slotBtn = uiFactory.createButton("Slot Map");
        exitBtn = uiFactory.createButton("Exit");

        navPnl.add(entryBtn);
        navPnl.add(slotBtn);
        navPnl.add(exitBtn);

        // Entry Panel (CardLayout)
        entryPnl = uiFactory.createPanel();
        entryPnl.setLayout(new BorderLayout(8, 0));

        // Entry Left - PATTERN: Builder could be used here too
        entryLeft = uiFactory.createPanel();
        entryLeft.setLayout(new BoxLayout(entryLeft, BoxLayout.Y_AXIS));
        entryLeft.setBorder(BorderFactory.createCompoundBorder(
            uiFactory.createLineBorder(new Color(215, 219, 225)),
            uiFactory.createEmptyBorder(8, 10, 8, 10)));
        entryLeft.setBackground(Color.WHITE);

        entryTitleLbl = uiFactory.createLabel("Vehicle Entry");
        entryTitleLbl.setFont(entryTitleLbl.getFont().deriveFont(Font.BOLD, 15f));
        entryTitleLbl.setForeground(NAVY);
        entryTitleLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        entryLeft.add(entryTitleLbl);
        entryLeft.add(Box.createVerticalStrut(10));

        platenumTF = uiFactory.createTextField();
        platenumTF.setFont(platenumTF.getFont().deriveFont(Font.BOLD, 14f));
        platenumTF.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        platenumTF.setAlignmentX(Component.LEFT_ALIGNMENT);
        entryLeft.add(platenumTF);
        entryLeft.add(Box.createVerticalStrut(8));

        vehicleTypeGroup = new ButtonGroup();

        twoWheelerCb = uiFactory.createCheckBox("2 Wheeler");
        twoWheelerCb.setBackground(Color.WHITE);
        twoWheelerCb.setAlignmentX(Component.LEFT_ALIGNMENT);
        vehicleTypeGroup.add(twoWheelerCb);
        entryLeft.add(twoWheelerCb);
        entryLeft.add(Box.createVerticalStrut(4));

        fourWheelerCb = uiFactory.createCheckBox("4 Wheeler");
        fourWheelerCb.setBackground(Color.WHITE);
        fourWheelerCb.setAlignmentX(Component.LEFT_ALIGNMENT);
        vehicleTypeGroup.add(fourWheelerCb);
        entryLeft.add(fourWheelerCb);
        entryLeft.add(Box.createVerticalStrut(8));

        parkBtn = new JButton("Park Vehicle");
        parkBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        parkBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        entryLeft.add(parkBtn);
        entryLeft.add(Box.createVerticalStrut(8));

        slotNum = new JTextField();
        slotNum.setEditable(false);
        slotNum.setFont(slotNum.getFont().deriveFont(Font.BOLD, 14f));
        slotNum.setBackground(new Color(232, 244, 255));
        slotNum.setHorizontalAlignment(SwingConstants.CENTER);
        slotNum.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        slotNum.setAlignmentX(Component.LEFT_ALIGNMENT);
        entryLeft.add(slotNum);
        entryLeft.add(Box.createVerticalGlue());

        entryPnl.add(entryLeft, BorderLayout.WEST);

        // Ticket area
        ticketArea = new JTextArea();
        ticketArea.setEditable(false);
        ticketArea.setFont(new Font(Font.MONOSPACED, Font.BOLD, 12));
        ticketArea.setBackground(PAPER);
        ticketArea.setLineWrap(true);
        ticketArea.setWrapStyleWord(true);
        ticketArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        ticketArea.setMargin(new Insets(10, 10, 10, 10));

        ticketScroll = new JScrollPane(ticketArea);
        ticketScroll.setBorder(BorderFactory.createLineBorder(new Color(215, 219, 225), 1));
        ticketScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        ticketScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        ticketScroll.getViewport().setBackground(PAPER);

        entryPnl.add(ticketScroll, BorderLayout.CENTER);

        // Slot Map Panel
        slotPnl = uiFactory.createPanel();
        new GridLayoutStrategy(4, 4, 6, 6).layoutContainer(slotPnl);
        slotPnl.setBorder(uiFactory.createEmptyBorder(8, 8, 8, 8));
        slotPnl.setBackground(Color.WHITE);

        // Main layout: header at top, nav below, then card panel for entry/slot
        JPanel topPnl = new JPanel(new BorderLayout());
        topPnl.add(headerPnl, BorderLayout.NORTH);
        topPnl.add(navPnl, BorderLayout.SOUTH);

        JPanel centerPnl = new JPanel();
        cardLayout = new CardLayout();
        centerPnl.setLayout(cardLayout);
        centerPnl.add(entryPnl, "entry");
        centerPnl.add(slotPnl, "slot");
        this.centerPnl = centerPnl;

        // Hidden Time Controls bar (toggled with P or Ctrl+P for fee testing)
        timeControlPnl = uiFactory.createPanel();
        timeControlPnl.setLayout(new BorderLayout(8, 0));

        timeOffsetLbl = uiFactory.createLabel("Time Sim: Live");
        timeOffsetLbl.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        timeOffsetLbl.setForeground(HEADER_SUBTEXT);
        timeControlPnl.add(timeOffsetLbl, BorderLayout.WEST);

        JPanel timeBtnPnl = uiFactory.createPanel();
        timeBtnPnl.setLayout(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        timeBtnPnl.setOpaque(false);
        timeBtnPnl.add(createTimeButton("+1m", BLUE, () -> adjustSimulatedTime(1)));
        timeBtnPnl.add(createTimeButton("+15m", BLUE, () -> adjustSimulatedTime(15)));
        timeBtnPnl.add(createTimeButton("+1h", BLUE, () -> adjustSimulatedTime(60)));
        timeBtnPnl.add(createTimeButton("+3h", BLUE, () -> adjustSimulatedTime(180)));
        timeBtnPnl.add(createTimeButton("+1d", BLUE, () -> adjustSimulatedTime(1440)));
        timeBtnPnl.add(createTimeButton("Reset", RED, this::resetSimulatedTime));
        timeControlPnl.add(timeBtnPnl, BorderLayout.EAST);
        timeControlPnl.setVisible(false);

        mainPnl.add(topPnl, BorderLayout.NORTH);
        mainPnl.add(centerPnl, BorderLayout.CENTER);
        mainPnl.add(timeControlPnl, BorderLayout.SOUTH);
    }

    private JPanel centerPnl;
    private CardLayout cardLayout;

    private void addListeners() {
        entryBtn.addActionListener(e -> showPage(true));
        slotBtn.addActionListener(e -> showPage(false));
        exitBtn.addActionListener(e -> exitVehicle());
        parkBtn.addActionListener(e -> parkVehicle());
        platenumTF.addActionListener(e -> parkVehicle());
    }

    // ===================================================================
    // Navigation
    // ===================================================================
    /** true = Entry page (form + ticket), false = Slot Map page. */
    private void showPage(boolean entryPage) {
        cardLayout.show(centerPnl, entryPage ? "entry" : "slot");
        colorNavButton(entryBtn, entryPage);
        colorNavButton(slotBtn, !entryPage);
    }

    // ===================================================================
    // Park a vehicle
    // ===================================================================
    private void parkVehicle() {
        try {
            if (!twoWheelerCb.isSelected() && !fourWheelerCb.isSelected()) {
                throw new ParkingException("Missing vehicle type",
                        "Please select a vehicle type (2 Wheeler or 4 Wheeler).", false);
            }
            VehicleCreator creator = twoWheelerCb.isSelected() ? motorcycleCreator : carCreator;
            Vehicle vehicle = creator.createVehicle(platenumTF.getText().trim().toUpperCase());

            ParkingRecord record = lot.park(vehicle);

            int ticketWidth = calculateTicketWidth();
            ticketArea.setText(Tickets.ticketFor(record, ticketWidth).render());
            slotNum.setText("Slot " + record.getSlot());
            platenumTF.setText("");
            vehicleTypeGroup.clearSelection();
            platenumTF.requestFocusInWindow();
            refreshSlotMap();
            notifyLotObservers();  // Observer pattern
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
            return;
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

            int ticketWidth = calculateTicketWidth();
            ticketArea.setText(Tickets.receiptFor(record, ticketWidth).render());
            slotNum.setText("");
            refreshSlotMap();
            showPage(true);
            notifyLotObservers();  // Observer pattern
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

    /** Repaints every slot using Composite pattern (SlotGrid). */
    private void refreshSlotMap() {
        if (slotGrid == null) return;
        for (int i = 0; i < slotGrid.size(); i++) {
            SlotComponent slot = slotGrid.get(i);
            String plate = lot.getSlotPlate(i);
            String name = lot.getSlotName(i);

            if (plate == null) {
                slot.setFree();
                slot.getButton().setBackground(GREEN);
                slot.getButton().setIcon(null);
                slot.getButton().setText(name);
            } else {
                String type = lot.getRecord(plate).getVehicle().getType();
                slot.setOccupied(plate, type);
                slot.getButton().setBackground(RED);
                slot.getButton().setIcon(new VehicleIcon(type, Color.WHITE, RED, 48));
                slot.getButton().setText("<html><center>" + name + "<br><small>" + plate + "</small></center></html>");
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
        headerPnl.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 3, 0, BLUE),
                uiFactory.createEmptyBorder(10, 16, 10, 16)));

        // navigation bar
        navPnl.setBackground(Color.WHITE);
        colorButton(exitBtn, RED);

        // entry form
        entryLeft.setBackground(Color.WHITE);
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

        // hidden time controls bar
        timeControlPnl.setBackground(NAVY);
        timeControlPnl.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(2, 0, 0, 0, BLUE),
                uiFactory.createEmptyBorder(6, 12, 6, 12)));
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

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM d, yyyy");
    private final SimpleDateFormat clockFormat = new SimpleDateFormat("hh:mm:ss a");

    private void startClock() {
        updateClock();
        Timer timer = new Timer(1000, e -> updateClock());
        timer.start();
    }

    private void updateClock() {
        Date now = lot.getCurrentTime();
        dateLbl.setText(dateFormat.format(now));
        clockLbl.setText(clockFormat.format(now));
    }

    private JButton createTimeButton(String label, Color bg, Runnable action) {
        JButton btn = uiFactory.createButton(label);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        btn.setBorder(uiFactory.createEmptyBorder(4, 9, 4, 9));
        btn.addActionListener(e -> action.run());
        return btn;
    }

    private void setupTimeControlShortcut() {
        InputMap inputMap = mainPnl.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = mainPnl.getActionMap();

        Action toggleAlways = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toggleTimeControls();
            }
        };

        Action toggleWhenNotTyping = new AbstractAction() {
            @Override
            public boolean isEnabled() {
                Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
                return !(focusOwner instanceof JTextComponent && ((JTextComponent) focusOwner).isEditable());
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                toggleTimeControls();
            }
        };

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_P, 0), "toggleTimeControlsPlain");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.SHIFT_DOWN_MASK), "toggleTimeControlsPlain");
        actionMap.put("toggleTimeControlsPlain", toggleWhenNotTyping);

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_DOWN_MASK), "toggleTimeControlsCtrl");
        actionMap.put("toggleTimeControlsCtrl", toggleAlways);
    }

    private void toggleTimeControls() {
        timeControlPnl.setVisible(!timeControlPnl.isVisible());
        mainPnl.revalidate();
        mainPnl.repaint();
    }

    private void adjustSimulatedTime(long minutes) {
        lot.advanceTimeMinutes(minutes);
        updateClock();
        updateTimeOffsetLabel();
    }

    private void resetSimulatedTime() {
        lot.resetTime();
        updateClock();
        updateTimeOffsetLabel();
    }

    private void updateTimeOffsetLabel() {
        long offset = lot.getTimeOffsetMinutes();
        if (offset == 0) {
            timeOffsetLbl.setText("Time Sim: Live");
            timeOffsetLbl.setForeground(HEADER_SUBTEXT);
            return;
        }
        String sign = offset > 0 ? "+" : "-";
        long abs = Math.abs(offset);
        long days = abs / 1440;
        long hours = (abs % 1440) / 60;
        long mins = abs % 60;
        StringBuilder sb = new StringBuilder("Time Sim: ").append(sign);
        if (days > 0) sb.append(days).append("d ");
        if (hours > 0) sb.append(hours).append("h ");
        if (mins > 0 || (days == 0 && hours == 0)) sb.append(mins).append("m");
        timeOffsetLbl.setText(sb.toString().trim());
        timeOffsetLbl.setForeground(Color.WHITE);
    }

    /** Estimate character columns from ticketArea width (monospaced font). */
    private int calculateTicketWidth() {
        if (ticketArea == null) return 40;
        FontMetrics fm = ticketArea.getFontMetrics(ticketArea.getFont());
        if (fm == null) return 40;
        int charW = fm.charWidth('0');
        if (charW <= 0) return 40;
        int avail = ticketArea.getWidth() - ticketArea.getInsets().left - ticketArea.getInsets().right;
        return Math.max(20, avail / charW);
    }

    // ===================================================================
    // PATTERN: Observer - Lot changes notify UI
    // ===================================================================
    private void registerLotObservers() {
        lotObservers.add(this::refreshSlotMap);
        lotObservers.add(this::repaintTicketArea);
    }

    private void notifyLotObservers() {
        for (LotObserver obs : lotObservers) {
            obs.onLotChanged();
        }
    }

    private void repaintTicketArea() {
        if (ticketArea != null) {
            ticketArea.revalidate();
            ticketArea.repaint();
        }
    }

    @FunctionalInterface
    interface LotObserver {
        void onLotChanged();
    }

    // ===================================================================
    // PATTERN: Composite + Builder - Slot Grid
    // ===================================================================
    private void setupSlotGrid() {
        slotPnl.removeAll();
        SlotButtonBuilder builder = new SlotButtonBuilder(uiFactory)
                .setFont(uiFactory.createDefaultFont().deriveFont(Font.BOLD, 13f))
                .setForeground(Color.WHITE)
                .setVerticalTextPosition(SwingConstants.BOTTOM)
                .setHorizontalTextPosition(SwingConstants.CENTER);

        int slotCount = lot.getSlotCount();
        slotGrid = new SlotGrid();
        slotButtons = new JButton[slotCount];

        for (int i = 0; i < slotCount; i++) {
            final int index = i;
            SlotComponent slot = builder
                    .setName(lot.getSlotName(i))
                    .setActionListener(e -> showSlotInfo(index))
                    .build();
            slotGrid.add(slot);
            slotButtons[i] = slot.getButton();
            slotPnl.add(slot.getButton());
        }
        refreshSlotMap();
        slotPnl.revalidate();
        slotPnl.repaint();
    }

    // ===================================================================
    // PATTERN: Abstract Factory - UI Component Factory
    // ===================================================================
    interface UIComponentFactory {
        JButton createButton(String text);
        JLabel createLabel(String text);
        JTextField createTextField();
        JCheckBox createCheckBox(String text);
        JPanel createPanel();
        JScrollPane createScrollPane(JComponent view);
        Font createDefaultFont();
        Border createLineBorder(Color color);
        Border createEmptyBorder(int top, int left, int bottom, int right);
    }

    class DefaultUIComponentFactory implements UIComponentFactory {
        public JButton createButton(String text) {
            JButton btn = new JButton(text);
            btn.setOpaque(true);
            btn.setFocusPainted(false);
            return btn;
        }
        public JLabel createLabel(String text) { return new JLabel(text); }
        public JTextField createTextField() { return new JTextField(); }
        public JCheckBox createCheckBox(String text) { return new JCheckBox(text); }
        public JPanel createPanel() { return new JPanel(); }
        public JScrollPane createScrollPane(JComponent view) { return new JScrollPane(view); }
        public Font createDefaultFont() { return new Font(Font.SANS_SERIF, Font.PLAIN, 12); }
        public Border createLineBorder(Color color) { return BorderFactory.createLineBorder(color); }
        public Border createEmptyBorder(int top, int left, int bottom, int right) { return BorderFactory.createEmptyBorder(top, left, bottom, right); }
    }

    // ===================================================================
    // PATTERN: Builder - SlotButtonBuilder
    // ===================================================================
    static class SlotButtonBuilder {
        private final UIComponentFactory factory;
        private String name = "";
        private Font font;
        private Color foreground;
        private int verticalTextPos = SwingConstants.CENTER;
        private int horizontalTextPos = SwingConstants.CENTER;
        private Consumer<ActionEvent> actionListener;

        SlotButtonBuilder(UIComponentFactory factory) { this.factory = factory; }

        SlotButtonBuilder setName(String name) { this.name = name; return this; }
        SlotButtonBuilder setFont(Font font) { this.font = font; return this; }
        SlotButtonBuilder setForeground(Color c) { this.foreground = c; return this; }
        SlotButtonBuilder setVerticalTextPosition(int pos) { this.verticalTextPos = pos; return this; }
        SlotButtonBuilder setHorizontalTextPosition(int pos) { this.horizontalTextPos = pos; return this; }
        SlotButtonBuilder setActionListener(Consumer<ActionEvent> listener) { this.actionListener = listener; return this; }

        SlotComponent build() {
            JButton btn = factory.createButton(name);
            if (font != null) btn.setFont(font);
            if (foreground != null) btn.setForeground(foreground);
            btn.setVerticalTextPosition(verticalTextPos);
            btn.setHorizontalTextPosition(horizontalTextPos);
            if (actionListener != null) btn.addActionListener(actionListener::accept);
            return new SlotComponent(btn);
        }
    }

    // ===================================================================
    // PATTERN: Composite - SlotComponent & SlotGrid
    // ===================================================================
    static class SlotComponent {
        private final JButton button;
        private String plate = null;
        private String vehicleType = null;

        SlotComponent(JButton button) { this.button = button; }
        JButton getButton() { return button; }
        void setOccupied(String plate, String type) {
            this.plate = plate;
            this.vehicleType = type;
        }
        void setFree() { this.plate = null; this.vehicleType = null; }
        boolean isOccupied() { return plate != null; }
        String getPlate() { return plate; }
        String getVehicleType() { return vehicleType; }
    }

    static class SlotGrid {
        private final List<SlotComponent> slots = new ArrayList<>();
        void add(SlotComponent slot) { slots.add(slot); }
        SlotComponent get(int index) { return slots.get(index); }
        int size() { return slots.size(); }
        List<SlotComponent> getAll() { return new ArrayList<>(slots); }
        void clear() { slots.forEach(SlotComponent::setFree); }
    }

    // ===================================================================
    // PATTERN: Strategy - LayoutStrategy
    // ===================================================================
    interface LayoutStrategy {
        void layoutContainer(Container parent);
        String getName();
    }

    static class BorderLayoutStrategy implements LayoutStrategy {
        public void layoutContainer(Container parent) { parent.setLayout(new BorderLayout()); }
        public String getName() { return "BorderLayout"; }
    }

    static class GridLayoutStrategy implements LayoutStrategy {
        private final int rows, cols, hgap, vgap;
        GridLayoutStrategy(int rows, int cols, int hgap, int vgap) { this.rows = rows; this.cols = cols; this.hgap = hgap; this.vgap = vgap; }
        public void layoutContainer(Container parent) { parent.setLayout(new GridLayout(rows, cols, hgap, vgap)); }
        public String getName() { return "GridLayout(" + rows + "x" + cols + ")"; }
    }

    static class FlowLayoutStrategy implements LayoutStrategy {
        private final int align, hgap, vgap;
        FlowLayoutStrategy(int align, int hgap, int vgap) { this.align = align; this.hgap = hgap; this.vgap = vgap; }
        public void layoutContainer(Container parent) { parent.setLayout(new FlowLayout(align, hgap, vgap)); }
        public String getName() { return "FlowLayout"; }
    }

    void setLayoutStrategy(LayoutStrategy strategy) {
        this.currentLayout = strategy;
        if (mainPnl != null) {
            strategy.layoutContainer(mainPnl);
            mainPnl.revalidate();
        }
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
            frame.setResizable(false);  // Disable resizing
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}