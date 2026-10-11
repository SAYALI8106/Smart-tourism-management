package smarttourism.presentation;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import smarttourism.model.Attraction;
import smarttourism.model.Booking;
import smarttourism.model.Destination;
import smarttourism.model.Payment;
import smarttourism.model.TourPackage;
import smarttourism.model.User;
import smarttourism.service.AttractionService;
import smarttourism.service.AuthService;
import smarttourism.service.BookingService;
import smarttourism.service.DestinationService;
import smarttourism.service.PackageService;
import smarttourism.service.PaymentService;
import smarttourism.service.SessionManager;
import smarttourism.service.UserService;
import smarttourism.service.ValidationException;

/**
 * Tier 1 - Presentation Layer: Tourist Portal Dashboard.
 * Clean, modern interface designed exclusively for tourists.
 * Contains browsing, booking, personalized booking/payment tracking, and profile management.
 * Does not contain administrative CRUD controls.
 */
public class TouristDashboard extends JFrame {

    private final User activeTourist;
    private final DestinationService destinationService = new DestinationService();
    private final AttractionService attractionService = new AttractionService();
    private final PackageService packageService = new PackageService();
    private final BookingService bookingService = new BookingService();
    private final PaymentService paymentService = new PaymentService();
    private final UserService userService = new UserService();

    private JTabbedPane tabbedPane;

    // Destinations Tab
    private JTextField txtSearchDest;
    private JTable tblDestinations;
    private DefaultTableModel modelDestinations;

    // Attractions Tab
    private JComboBox<Destination> cmbFilterDest;
    private JTable tblAttractions;
    private DefaultTableModel modelAttractions;

    // Header
    private JLabel lblHeaderSub;

    // Packages Tab
    private JTable tblPackages;
    private DefaultTableModel modelPackages;

    // Booking Tab
    private JComboBox<TourPackage> cmbBookingPackage;
    private JTextField txtTravelDate;
    private JSpinner spnPeople;
    private JLabel lblTotalCost;

    // My Bookings Tab
    private JTable tblMyBookings;
    private DefaultTableModel modelMyBookings;

    // My Payments Tab
    private JTable tblMyPayments;
    private DefaultTableModel modelMyPayments;

    // Profile Tab
    private JTextField txtProfFirstName;
    private JTextField txtProfLastName;
    private JTextField txtProfPhone;
    private JTextField txtProfStreet;
    private JTextField txtProfCity;
    private JTextField txtProfState;
    private JTextField txtProfCountry;

    public TouristDashboard(User tourist) {
        super("Smart Tourism Management System — Tourist Portal");
        this.activeTourist = tourist;
        initUI();
        loadAllData();
    }

    private void initUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1120, 720);
        setMinimumSize(new Dimension(960, 600));
        setLocationRelativeTo(null);
        getContentPane().setBackground(UITheme.COLOR_BG);
        setLayout(new BorderLayout());

        // Header Panel
        add(createHeaderPanel(), BorderLayout.NORTH);

        // Center Tabs
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(UITheme.FONT_LABEL);
        tabbedPane.setBackground(UITheme.COLOR_SURFACE);

        tabbedPane.addTab("Browse Destinations", createDestinationsTab());
        tabbedPane.addTab("View Attractions", createAttractionsTab());
        tabbedPane.addTab("Explore Tour Packages", createPackagesTab());
        tabbedPane.addTab("Book a Package", createBookPackageTab());
        tabbedPane.addTab("My Bookings", createMyBookingsTab());
        tabbedPane.addTab("My Payments", createMyPaymentsTab());
        tabbedPane.addTab("My Profile", createMyProfileTab());

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UITheme.COLOR_PRIMARY_DARK);
        panel.setBorder(new EmptyBorder(16, 24, 16, 24));

        JPanel left = new JPanel(new GridLayout(2, 1, 0, 4));
        left.setOpaque(false);

        JLabel title = new JLabel("Smart Tourism Management System");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(Color.WHITE);

        String greeting = "Welcome, " + (activeTourist != null ? activeTourist.getFullName() : "Tourist")
                        + " (" + (activeTourist != null ? activeTourist.getEmail() : "") + ")";
        lblHeaderSub = new JLabel(greeting);
        lblHeaderSub.setFont(UITheme.FONT_SUBTITLE);
        lblHeaderSub.setForeground(new Color(204, 251, 241));

        left.add(title);
        left.add(lblHeaderSub);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setBackground(UITheme.COLOR_PRIMARY_DARK);

        JButton btnChangePass = UITheme.createSecondaryButton("Change Password");
        btnChangePass.addActionListener(e -> new ChangePasswordDialog(this).setVisible(true));

        JButton btnLogout = UITheme.createSecondaryButton("Logout");
        btnLogout.addActionListener(e -> {
            AuthService.logout();
            dispose();
            SwingUtilities.invokeLater(() -> new LoginForm().setVisible(true));
        });

        right.add(btnChangePass);
        right.add(btnLogout);

        panel.add(left, BorderLayout.CENTER);
        panel.add(right, BorderLayout.EAST);
        return panel;
    }

    // --- Tab 1: Browse Destinations ---
    private JPanel createDestinationsTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(UITheme.COLOR_BG);
        panel.setBorder(new EmptyBorder(14, 16, 14, 16));

        // Filter / Search Toolbar
        JPanel bar = UITheme.createCardPanel();
        bar.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 6));

        JLabel lblSearch = new JLabel("Search Destinations:");
        lblSearch.setFont(UITheme.FONT_LABEL);
        txtSearchDest = UITheme.createTextField();
        txtSearchDest.setPreferredSize(new Dimension(220, 32));

        JButton btnSearch = UITheme.createPrimaryButton("Search");
        btnSearch.addActionListener(e -> searchDestinations());

        JButton btnReset = UITheme.createSecondaryButton("Show All");
        btnReset.addActionListener(e -> {
            txtSearchDest.setText("");
            loadDestinations();
        });

        JButton btnViewAttr = UITheme.createSecondaryButton("View Attractions for Selected");
        btnViewAttr.addActionListener(e -> viewAttractionsForSelectedDestination());

        bar.add(lblSearch);
        bar.add(txtSearchDest);
        bar.add(btnSearch);
        bar.add(btnReset);
        bar.add(Box.createHorizontalStrut(20));
        bar.add(btnViewAttr);

        // Table
        String[] cols = {"Destination ID", "Name", "Location", "Description"};
        modelDestinations = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblDestinations = new JTable(modelDestinations);
        UITheme.formatTable(tblDestinations);

        JScrollPane sp = new JScrollPane(tblDestinations);
        sp.setBorder(BorderFactory.createLineBorder(UITheme.COLOR_BORDER, 1));
        sp.getViewport().setBackground(Color.WHITE);

        panel.add(bar, BorderLayout.NORTH);
        panel.add(sp, BorderLayout.CENTER);
        return panel;
    }

    // --- Tab 2: View Attractions ---
    private JPanel createAttractionsTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(UITheme.COLOR_BG);
        panel.setBorder(new EmptyBorder(14, 16, 14, 16));

        JPanel bar = UITheme.createCardPanel();
        bar.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 6));

        JLabel lblFilter = new JLabel("Filter by Destination:");
        lblFilter.setFont(UITheme.FONT_LABEL);
        cmbFilterDest = new JComboBox<>();
        cmbFilterDest.setFont(UITheme.FONT_REGULAR);
        cmbFilterDest.setPreferredSize(new Dimension(240, 32));
        cmbFilterDest.addActionListener(e -> filterAttractions());

        JButton btnAll = UITheme.createSecondaryButton("Show All Attractions");
        btnAll.addActionListener(e -> loadAllAttractions());

        bar.add(lblFilter);
        bar.add(cmbFilterDest);
        bar.add(btnAll);

        String[] cols = {"Destination", "Attraction Name", "Type", "Entry Fee (₹)", "Closing Time", "Operating Hours", "Description"};
        modelAttractions = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblAttractions = new JTable(modelAttractions);
        UITheme.formatTable(tblAttractions);

        JScrollPane sp = new JScrollPane(tblAttractions);
        sp.setBorder(BorderFactory.createLineBorder(UITheme.COLOR_BORDER, 1));
        sp.getViewport().setBackground(Color.WHITE);

        panel.add(bar, BorderLayout.NORTH);
        panel.add(sp, BorderLayout.CENTER);
        return panel;
    }

    // --- Tab 3: Explore Tour Packages ---
    private JPanel createPackagesTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(UITheme.COLOR_BG);
        panel.setBorder(new EmptyBorder(14, 16, 14, 16));

        JPanel bar = UITheme.createCardPanel();
        bar.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 6));

        JButton btnBook = UITheme.createPrimaryButton("Book Selected Package");
        btnBook.addActionListener(e -> bookSelectedPackageFromTable());

        JButton btnRefresh = UITheme.createSecondaryButton("Refresh Packages");
        btnRefresh.addActionListener(e -> loadPackages());

        bar.add(btnBook);
        bar.add(btnRefresh);

        String[] cols = {"Package ID", "Tour Package Name", "Duration (Days)", "Cost Per Person (₹)", "Included Destinations"};
        modelPackages = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblPackages = new JTable(modelPackages);
        UITheme.formatTable(tblPackages);

        JScrollPane sp = new JScrollPane(tblPackages);
        sp.setBorder(BorderFactory.createLineBorder(UITheme.COLOR_BORDER, 1));
        sp.getViewport().setBackground(Color.WHITE);

        panel.add(bar, BorderLayout.NORTH);
        panel.add(sp, BorderLayout.CENTER);
        return panel;
    }

    // --- Tab 4: Book a Package ---
    private JPanel createBookPackageTab() {
        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        wrapper.setBackground(UITheme.COLOR_BG);

        JPanel card = UITheme.createCardPanel();
        card.setLayout(new GridBagLayout());
        card.setPreferredSize(new Dimension(540, 440));
        card.setBorder(new EmptyBorder(20, 24, 20, 24));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.weightx = 1.0;
        gbc.gridx = 0;

        JLabel title = new JLabel("Reserve a Tour Package", SwingConstants.CENTER);
        title.setFont(UITheme.FONT_HEADER);
        title.setForeground(UITheme.COLOR_PRIMARY_DARK);

        cmbBookingPackage = new JComboBox<>();
        cmbBookingPackage.setFont(UITheme.FONT_REGULAR);
        cmbBookingPackage.setPreferredSize(new Dimension(300, 34));
        cmbBookingPackage.addActionListener(e -> updateDynamicCostCalculation());

        txtTravelDate = UITheme.createTextField();
        txtTravelDate.setText(LocalDate.now().plusDays(7).toString());
        txtTravelDate.setPreferredSize(new Dimension(300, 34));

        spnPeople = new JSpinner(new SpinnerNumberModel(1, 1, 50, 1));
        spnPeople.setFont(UITheme.FONT_REGULAR);
        spnPeople.setPreferredSize(new Dimension(300, 34));
        spnPeople.addChangeListener(e -> updateDynamicCostCalculation());

        lblTotalCost = new JLabel("Calculated Total: ₹0.00", SwingConstants.CENTER);
        lblTotalCost.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTotalCost.setForeground(new Color(13, 148, 136));

        JButton btnConfirm = UITheme.createPrimaryButton("Confirm & Submit Booking");
        btnConfirm.setPreferredSize(new Dimension(300, 38));
        btnConfirm.addActionListener(e -> performBookingSubmission());

        int row = 0;
        gbc.gridy = row++; card.add(title, gbc);
        gbc.gridy = row++; card.add(createFieldPanel("Select Tour Package *", cmbBookingPackage), gbc);
        gbc.gridy = row++; card.add(createFieldPanel("Travel Date (YYYY-MM-DD) *", txtTravelDate), gbc);
        gbc.gridy = row++; card.add(createFieldPanel("Number of People *", spnPeople), gbc);
        gbc.gridy = row++; card.add(lblTotalCost, gbc);
        gbc.gridy = row++; card.add(btnConfirm, gbc);

        wrapper.add(card);
        return wrapper;
    }

    // --- Tab 5: My Bookings ---
    private JPanel createMyBookingsTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(UITheme.COLOR_BG);
        panel.setBorder(new EmptyBorder(14, 16, 14, 16));

        JPanel bar = UITheme.createCardPanel();
        bar.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 6));

        JButton btnPayBooking = UITheme.createPrimaryButton("Make Payment / Pay Now");
        btnPayBooking.addActionListener(e -> payForSelectedBooking());

        JButton btnCancel = UITheme.createSecondaryButton("Cancel Selected Booking");
        btnCancel.addActionListener(e -> cancelSelectedBooking());

        JButton btnRefresh = UITheme.createSecondaryButton("Refresh My Bookings");
        btnRefresh.addActionListener(e -> loadMyBookings());

        bar.add(btnPayBooking);
        bar.add(btnCancel);
        bar.add(btnRefresh);

        String[] cols = {"Booking ID", "Tour Package", "Travel Date", "People", "Total Amount (₹)", "Paid (₹)", "Balance Due (₹)", "Status"};
        modelMyBookings = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblMyBookings = new JTable(modelMyBookings);
        UITheme.formatTable(tblMyBookings);

        JScrollPane sp = new JScrollPane(tblMyBookings);
        sp.setBorder(BorderFactory.createLineBorder(UITheme.COLOR_BORDER, 1));
        sp.getViewport().setBackground(Color.WHITE);

        panel.add(bar, BorderLayout.NORTH);
        panel.add(sp, BorderLayout.CENTER);
        return panel;
    }

    // --- Tab 6: My Payments ---
    private JPanel createMyPaymentsTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(UITheme.COLOR_BG);
        panel.setBorder(new EmptyBorder(14, 16, 14, 16));

        JPanel bar = UITheme.createCardPanel();
        bar.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 6));

        JButton btnMakePayment = UITheme.createPrimaryButton("Make a Payment");
        btnMakePayment.addActionListener(e -> showMakePaymentDialog(null));

        JButton btnRefresh = UITheme.createSecondaryButton("Refresh My Payments");
        btnRefresh.addActionListener(e -> loadMyPayments());

        bar.add(btnMakePayment);
        bar.add(btnRefresh);

        String[] cols = {"Payment ID", "Booking ID", "Amount (₹)", "Payment Date", "Payment Method", "Status", "Refund Details"};
        modelMyPayments = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblMyPayments = new JTable(modelMyPayments);
        UITheme.formatTable(tblMyPayments);

        JScrollPane sp = new JScrollPane(tblMyPayments);
        sp.setBorder(BorderFactory.createLineBorder(UITheme.COLOR_BORDER, 1));
        sp.getViewport().setBackground(Color.WHITE);

        panel.add(bar, BorderLayout.NORTH);
        panel.add(sp, BorderLayout.CENTER);
        return panel;
    }

    // --- Tab 7: My Profile ---
    private JPanel createMyProfileTab() {
        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        wrapper.setBackground(UITheme.COLOR_BG);

        JPanel card = UITheme.createCardPanel();
        card.setLayout(new GridBagLayout());
        card.setPreferredSize(new Dimension(560, 480));
        card.setBorder(new EmptyBorder(20, 24, 20, 24));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.weightx = 1.0;

        txtProfFirstName = UITheme.createTextField();
        txtProfLastName = UITheme.createTextField();
        txtProfPhone = UITheme.createTextField();
        txtProfStreet = UITheme.createTextField();
        txtProfCity = UITheme.createTextField();
        txtProfState = UITheme.createTextField();
        txtProfCountry = UITheme.createTextField();

        if (activeTourist != null) {
            txtProfFirstName.setText(activeTourist.getFirstName());
            txtProfLastName.setText(activeTourist.getLastName());
            txtProfPhone.setText(activeTourist.getPhone());
            txtProfStreet.setText(activeTourist.getStreet());
            txtProfCity.setText(activeTourist.getCity());
            txtProfState.setText(activeTourist.getState());
            txtProfCountry.setText(activeTourist.getCountry());
        }

        int row = 0;
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1;
        card.add(createFieldPanel("First Name *", txtProfFirstName), gbc);
        gbc.gridx = 1; gbc.gridy = row++;
        card.add(createFieldPanel("Last Name *", txtProfLastName), gbc);

        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        card.add(createFieldPanel("Phone Number *", txtProfPhone), gbc);

        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        card.add(createFieldPanel("Street Address", txtProfStreet), gbc);

        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = row;
        card.add(createFieldPanel("City", txtProfCity), gbc);
        gbc.gridx = 1; gbc.gridy = row++;
        card.add(createFieldPanel("State", txtProfState), gbc);

        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        card.add(createFieldPanel("Country", txtProfCountry), gbc);

        JButton btnSaveProfile = UITheme.createPrimaryButton("Save Profile Details");
        btnSaveProfile.addActionListener(e -> performSaveProfile());
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        card.add(btnSaveProfile, gbc);

        wrapper.add(card);
        return wrapper;
    }

    private JPanel createFieldPanel(String label, javax.swing.JComponent comp) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(UITheme.COLOR_SURFACE);

        JLabel lbl = new JLabel(label);
        lbl.setFont(UITheme.FONT_LABEL);
        lbl.setForeground(UITheme.COLOR_TEXT_MAIN);

        p.add(lbl);
        p.add(Box.createVerticalStrut(3));
        p.add(comp);
        return p;
    }

    // --- Data Loading & Action Handlers ---

    private void loadAllData() {
        loadDestinations();
        loadAllAttractions();
        loadPackages();
        loadMyBookings();
        loadMyPayments();
        updateHeaderWelcome();
    }

    private void loadDestinations() {
        new Thread(() -> {
            try {
                List<Destination> list = destinationService.getAllDestinations();
                SwingUtilities.invokeLater(() -> {
                    modelDestinations.setRowCount(0);
                    cmbFilterDest.removeAllItems();
                    for (Destination d : list) {
                        modelDestinations.addRow(new Object[]{
                            d.getDestinationId(), d.getName(), d.getLocation(), d.getDescription()
                        });
                        cmbFilterDest.addItem(d);
                    }
                });
            } catch (SQLException ex) {
                SwingUtilities.invokeLater(() -> UITheme.showError(this, "Could not load destinations: " + ex.getMessage()));
            }
        }).start();
    }

    private void searchDestinations() {
        String q = txtSearchDest.getText().trim();
        new Thread(() -> {
            try {
                List<Destination> list = destinationService.searchDestinations(q);
                SwingUtilities.invokeLater(() -> {
                    modelDestinations.setRowCount(0);
                    for (Destination d : list) {
                        modelDestinations.addRow(new Object[]{
                            d.getDestinationId(), d.getName(), d.getLocation(), d.getDescription()
                        });
                    }
                });
            } catch (SQLException ex) {
                SwingUtilities.invokeLater(() -> UITheme.showError(this, "Search error: " + ex.getMessage()));
            }
        }).start();
    }

    private void viewAttractionsForSelectedDestination() {
        int row = tblDestinations.getSelectedRow();
        if (row < 0) {
            UITheme.showError(this, "Please select a destination from the table first.");
            return;
        }
        int destId = (int) modelDestinations.getValueAt(row, 0);
        tabbedPane.setSelectedIndex(1); // Switch to Attractions tab
        for (int i = 0; i < cmbFilterDest.getItemCount(); i++) {
            Destination d = cmbFilterDest.getItemAt(i);
            if (d.getDestinationId() == destId) {
                cmbFilterDest.setSelectedIndex(i);
                break;
            }
        }
    }

    private void loadAllAttractions() {
        new Thread(() -> {
            try {
                List<Attraction> list = attractionService.getAllAttractions();
                SwingUtilities.invokeLater(() -> populateAttractionsTable(list));
            } catch (SQLException ex) {
                SwingUtilities.invokeLater(() -> UITheme.showError(this, "Could not load attractions: " + ex.getMessage()));
            }
        }).start();
    }

    private void filterAttractions() {
        Destination sel = (Destination) cmbFilterDest.getSelectedItem();
        if (sel == null) return;
        new Thread(() -> {
            try {
                List<Attraction> list = attractionService.getAttractionsByDestination(sel.getDestinationId());
                SwingUtilities.invokeLater(() -> populateAttractionsTable(list));
            } catch (SQLException ex) {
                SwingUtilities.invokeLater(() -> UITheme.showError(this, "Could not filter attractions: " + ex.getMessage()));
            }
        }).start();
    }

    private void populateAttractionsTable(List<Attraction> list) {
        modelAttractions.setRowCount(0);
        for (Attraction a : list) {
            modelAttractions.addRow(new Object[]{
                a.getDestinationName(),
                a.getName(),
                a.getType(),
                String.format("₹%,.2f", a.getEntryFee()),
                a.getClosingTime() != null ? a.getClosingTime() : "N/A",
                a.getOperatingTime() != null ? a.getOperatingTime() : "Standard",
                a.getDescription()
            });
        }
    }

    private void loadPackages() {
        new Thread(() -> {
            try {
                List<TourPackage> list = packageService.getAllPackages();
                SwingUtilities.invokeLater(() -> {
                    modelPackages.setRowCount(0);
                    cmbBookingPackage.removeAllItems();
                    for (TourPackage p : list) {
                        try {
                            List<Destination> dests = packageService.getDestinationsForPackage(p.getPackageId());
                            StringBuilder sb = new StringBuilder();
                            for (Destination d : dests) {
                                if (sb.length() > 0) sb.append(", ");
                                sb.append(d.getName());
                            }
                            modelPackages.addRow(new Object[]{
                                p.getPackageId(),
                                p.getPackageName(),
                                p.getDuration(),
                                String.format("₹%,.2f", p.getCost()),
                                sb.length() > 0 ? sb.toString() : "None"
                            });
                        } catch (Exception ignored) {}
                        cmbBookingPackage.addItem(p);
                    }
                    updateDynamicCostCalculation();
                });
            } catch (SQLException ex) {
                SwingUtilities.invokeLater(() -> UITheme.showError(this, "Could not load packages: " + ex.getMessage()));
            }
        }).start();
    }

    private void bookSelectedPackageFromTable() {
        int row = tblPackages.getSelectedRow();
        if (row < 0) {
            UITheme.showError(this, "Please select a package from the table to book.");
            return;
        }
        int pkgId = (int) modelPackages.getValueAt(row, 0);
        tabbedPane.setSelectedIndex(3); // Switch to Book a Package tab
        for (int i = 0; i < cmbBookingPackage.getItemCount(); i++) {
            TourPackage p = cmbBookingPackage.getItemAt(i);
            if (p.getPackageId() == pkgId) {
                cmbBookingPackage.setSelectedIndex(i);
                break;
            }
        }
    }

    private void updateDynamicCostCalculation() {
        TourPackage p = (TourPackage) cmbBookingPackage.getSelectedItem();
        int people = (int) spnPeople.getValue();
        if (p != null && people > 0) {
            double total = p.getCost() * people;
            lblTotalCost.setText(String.format("Calculated Total: ₹%,.2f  (%d × ₹%,.2f)", total, people, p.getCost()));
        } else {
            lblTotalCost.setText("Calculated Total: ₹0.00");
        }
    }

    private void performBookingSubmission() {
        TourPackage p = (TourPackage) cmbBookingPackage.getSelectedItem();
        if (p == null) {
            UITheme.showError(this, "Please select a Tour Package.");
            return;
        }

        String travelDate = txtTravelDate.getText().trim();
        int people = (int) spnPeople.getValue();
        double estimatedTotal = p.getCost() * people;

        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Confirm booking for " + p.getPackageName() + "?\n"
            + "Travel Date: " + travelDate + "\n"
            + "People: " + people + "\n"
            + "Calculated Total Amount: ₹" + String.format("%,.2f", estimatedTotal),
            "Confirm Booking",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );

        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            Booking b = bookingService.bookForCurrentTourist(p.getPackageId(), travelDate, people);
            int choice = JOptionPane.showConfirmDialog(
                this,
                "Booking Confirmed!\n"
                + "Booking Reference #" + b.getBookingId() + " for " + p.getPackageName() + " has been recorded.\n\n"
                + "Calculated Amount: ₹" + String.format("%,.2f", b.getEstimatedAmount()) + "\n\n"
                + "Would you like to make a payment now?",
                "Booking Confirmed — Proceed to Payment",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );
            loadMyBookings();
            loadMyPayments();
            updateHeaderWelcome();
            if (choice == JOptionPane.YES_OPTION) {
                showMakePaymentDialog(b);
            } else {
                tabbedPane.setSelectedIndex(4); // Switch to My Bookings tab
            }
        } catch (ValidationException ve) {
            UITheme.showError(this, ve.getMessage());
        } catch (SQLException sqle) {
            String msg = sqle.getMessage();
            if (msg != null && (msg.contains("Travel date cannot be before booking date") || msg.contains("Number of people must be greater than zero"))) {
                UITheme.showError(this, "Database Trigger Violation: " + msg);
            } else {
                UITheme.showError(this, "Database Error: " + sqle.getMessage());
            }
        } catch (Exception ex) {
            UITheme.showError(this, "Booking Error: " + ex.getMessage());
        }
    }

    private void updateHeaderWelcome() {
        if (activeTourist == null || lblHeaderSub == null) return;
        new Thread(() -> {
            int count = 0;
            try {
                count = bookingService.getTouristBookingCount(activeTourist.getUserId());
            } catch (Exception ignored) {}
            final int totalBookings = count;
            SwingUtilities.invokeLater(() -> {
                lblHeaderSub.setText("Welcome, " + activeTourist.getFullName()
                    + " (" + activeTourist.getEmail() + ")  |  Total Bookings: " + totalBookings);
            });
        }).start();
    }

    private void loadMyBookings() {
        new Thread(() -> {
            try {
                List<Booking> list = bookingService.getMyBookings();
                SwingUtilities.invokeLater(() -> {
                    modelMyBookings.setRowCount(0);
                    for (Booking b : list) {
                        double total = b.getEstimatedAmount();
                        double paid = paymentService.getCompletedPaymentAmountForBooking(b.getBookingId());
                        double balance = Math.max(0.0, total - paid);
                        modelMyBookings.addRow(new Object[]{
                            b.getBookingId(),
                            b.getPackageName(),
                            b.getTravelDate(),
                            b.getNumberOfPeople(),
                            String.format("₹%,.2f", total),
                            String.format("₹%,.2f", paid),
                            String.format("₹%,.2f", balance),
                            b.getBookingStatus()
                        });
                    }
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> UITheme.showError(this, "Could not load bookings: " + ex.getMessage()));
            }
        }).start();
    }

    private void payForSelectedBooking() {
        int row = tblMyBookings.getSelectedRow();
        if (row < 0) {
            UITheme.showError(this, "Please select a booking from the table to make a payment.");
            return;
        }

        int bookingId = (int) modelMyBookings.getValueAt(row, 0);
        String status = (String) modelMyBookings.getValueAt(row, 7);

        if ("Cancelled".equalsIgnoreCase(status)) {
            UITheme.showError(this, "Cannot pay for a cancelled booking.");
            return;
        }

        try {
            Booking b = bookingService.getBookingById(bookingId);
            if (b == null) {
                UITheme.showError(this, "Booking #" + bookingId + " not found.");
                return;
            }
            double paid = paymentService.getCompletedPaymentAmountForBooking(bookingId);
            double total = b.getEstimatedAmount();
            if (total > 0 && (total - paid) <= 0.01) {
                UITheme.showSuccess(this, "Booking #" + bookingId + " is already fully paid!\nTotal Paid: ₹" + String.format("%,.2f", paid));
                return;
            }
            showMakePaymentDialog(b);
        } catch (Exception ex) {
            UITheme.showError(this, "Error: " + ex.getMessage());
        }
    }

    private void cancelSelectedBooking() {
        int row = tblMyBookings.getSelectedRow();
        if (row < 0) {
            UITheme.showError(this, "Please select a booking from the table to cancel.");
            return;
        }

        int bookingId = (int) modelMyBookings.getValueAt(row, 0);
        String status = (String) modelMyBookings.getValueAt(row, 7);

        if ("Cancelled".equalsIgnoreCase(status)) {
            UITheme.showError(this, "This booking is already cancelled.");
            return;
        }

        int opt = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to cancel Booking #" + bookingId + "?",
            "Confirm Cancellation",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (opt != JOptionPane.YES_OPTION) return;

        try {
            bookingService.cancelBooking(bookingId);
            UITheme.showSuccess(this, "Booking #" + bookingId + " has been cancelled.");
            loadMyBookings();
            updateHeaderWelcome();
        } catch (ValidationException ve) {
            UITheme.showError(this, ve.getMessage());
        } catch (Exception ex) {
            UITheme.showError(this, "Cancellation failed: " + ex.getMessage());
        }
    }

    private void loadMyPayments() {
        new Thread(() -> {
            try {
                List<Payment> list = paymentService.getMyPayments();
                SwingUtilities.invokeLater(() -> {
                    modelMyPayments.setRowCount(0);
                    for (Payment p : list) {
                        modelMyPayments.addRow(new Object[]{
                            p.getPaymentId(),
                            p.getBookingId(),
                            String.format("₹%,.2f", p.getAmount()),
                            p.getPaymentDate(),
                            p.getPaymentMethod(),
                            p.getPaymentStatus(),
                            p.getRefundInfo() != null ? p.getRefundInfo() : "N/A"
                        });
                    }
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> UITheme.showError(this, "Could not load payments: " + ex.getMessage()));
            }
        }).start();
    }

    private void showMakePaymentDialog(Booking defaultBooking) {
        List<Booking> bookings;
        try {
            bookings = bookingService.getMyBookings();
        } catch (Exception ex) {
            UITheme.showError(this, "Could not load bookings: " + ex.getMessage());
            return;
        }

        // Filter out Cancelled bookings
        List<Booking> activeBookings = new ArrayList<>();
        for (Booking b : bookings) {
            if (!"Cancelled".equalsIgnoreCase(b.getBookingStatus())) {
                activeBookings.add(b);
            }
        }

        if (activeBookings.isEmpty()) {
            UITheme.showError(this, "You have no active bookings available for payment.\nPlease book a tour package first.");
            tabbedPane.setSelectedIndex(3); // Switch to Book a Package
            return;
        }

        JDialog dlg = new JDialog(this, "Make Payment — Smart Tourism Portal", true);
        dlg.setSize(520, 600);
        dlg.setLocationRelativeTo(this);
        dlg.setResizable(false);
        dlg.getContentPane().setBackground(UITheme.COLOR_BG);
        dlg.setLayout(new BorderLayout());

        // Header Banner
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(UITheme.COLOR_PRIMARY_DARK);
        headerPanel.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel titleLbl = new JLabel("Make Tour Payment");
        titleLbl.setFont(UITheme.FONT_TITLE);
        titleLbl.setForeground(Color.WHITE);

        JLabel subLbl = new JLabel("Secure Payment Processing — MySQL Smart Tourism DB");
        subLbl.setFont(UITheme.FONT_SUBTITLE);
        subLbl.setForeground(new Color(204, 251, 241));

        headerPanel.add(titleLbl);
        headerPanel.add(Box.createVerticalStrut(4));
        headerPanel.add(subLbl);
        dlg.add(headerPanel, BorderLayout.NORTH);

        // Center Form Panel
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(UITheme.COLOR_BG);
        centerPanel.setBorder(new EmptyBorder(14, 18, 14, 18));

        JPanel card = UITheme.createCardPanel();
        card.setLayout(new GridBagLayout());
        card.setPreferredSize(new Dimension(460, 480));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 6, 5, 6);
        gbc.weightx = 1.0;
        gbc.gridx = 0;

        // 1. Select Booking
        JComboBox<Booking> cmbBookings = new JComboBox<>();
        cmbBookings.setFont(UITheme.FONT_REGULAR);
        for (Booking b : activeBookings) {
            cmbBookings.addItem(b);
        }
        cmbBookings.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Booking) {
                    Booking b = (Booking) value;
                    setText("Booking #" + b.getBookingId() + " - " + b.getPackageName() + " (" + b.getTravelDate() + ")");
                }
                return this;
            }
        });

        // 2. Summary details panel
        JPanel summaryPanel = new JPanel(new GridLayout(4, 2, 8, 4));
        summaryPanel.setBackground(new Color(241, 245, 249));
        summaryPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.COLOR_BORDER, 1),
            new EmptyBorder(8, 10, 8, 10)
        ));

        JLabel lblPkg = new JLabel("-");
        JLabel lblDatePeople = new JLabel("-");
        JLabel lblTotal = new JLabel("₹0.00");
        JLabel lblPaidBal = new JLabel("₹0.00");
        lblPaidBal.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblPaidBal.setForeground(new Color(15, 118, 110));

        summaryPanel.add(new JLabel("Tour Package:"));
        summaryPanel.add(lblPkg);
        summaryPanel.add(new JLabel("Travel Date & People:"));
        summaryPanel.add(lblDatePeople);
        summaryPanel.add(new JLabel("Total Booking Amount:"));
        summaryPanel.add(lblTotal);
        summaryPanel.add(new JLabel("Remaining Balance Due:"));
        summaryPanel.add(lblPaidBal);

        // 3. Amount Field
        JTextField txtAmount = UITheme.createTextField();

        // 4. Payment Method (strictly following payment_chk_2 in MySQL schema)
        String[] methods = {"UPI", "Credit Card", "Debit Card", "Net Banking", "Cash"};
        JComboBox<String> cmbMethod = new JComboBox<>(methods);
        cmbMethod.setFont(UITheme.FONT_REGULAR);

        // 5. Method Details Panel
        JPanel methodDetailsPanel = new JPanel(new BorderLayout(5, 5));
        methodDetailsPanel.setOpaque(false);
        JLabel lblDetailPrompt = new JLabel("UPI ID / Virtual Payment Address:");
        lblDetailPrompt.setFont(UITheme.FONT_LABEL);
        JTextField txtDetail = UITheme.createTextField();
        txtDetail.setText((activeTourist != null ? activeTourist.getFirstName().toLowerCase() : "tourist") + "@okhdfcbank");
        methodDetailsPanel.add(lblDetailPrompt, BorderLayout.NORTH);
        methodDetailsPanel.add(txtDetail, BorderLayout.CENTER);

        cmbMethod.addActionListener(e -> {
            String m = (String) cmbMethod.getSelectedItem();
            if ("UPI".equals(m)) {
                lblDetailPrompt.setText("Enter UPI ID (e.g. tourist@upi, Google Pay, PhonePe):");
                txtDetail.setText((activeTourist != null ? activeTourist.getFirstName().toLowerCase() : "tourist") + "@okhdfcbank");
                txtDetail.setEnabled(true);
            } else if ("Credit Card".equals(m) || "Debit Card".equals(m)) {
                lblDetailPrompt.setText("Card Number (Simulation: 16 digits):");
                txtDetail.setText("4532 8821 9912 3014");
                txtDetail.setEnabled(true);
            } else if ("Net Banking".equals(m)) {
                lblDetailPrompt.setText("Bank Name / Portal:");
                txtDetail.setText("State Bank of India (Net Banking)");
                txtDetail.setEnabled(true);
            } else {
                lblDetailPrompt.setText("Counter Payment:");
                txtDetail.setText("Payable at Smart Tourism Desk (Cash Receipt)");
                txtDetail.setEnabled(false);
            }
        });

        // Update function for selected booking
        Runnable updateBookingSummary = () -> {
            Booking sel = (Booking) cmbBookings.getSelectedItem();
            if (sel != null) {
                lblPkg.setText(sel.getPackageName());
                lblDatePeople.setText(sel.getTravelDate() + " (" + sel.getNumberOfPeople() + " travelers)");
                double total = sel.getEstimatedAmount();
                double paid = paymentService.getCompletedPaymentAmountForBooking(sel.getBookingId());
                double balance = Math.max(0.0, total - paid);
                lblTotal.setText(String.format("₹%,.2f", total));
                lblPaidBal.setText(String.format("₹%,.2f  (Paid: ₹%,.2f)", balance, paid));
                txtAmount.setText(String.format("%.2f", balance > 0 ? balance : total));
            }
        };

        cmbBookings.addActionListener(e -> updateBookingSummary.run());

        // Preselect default booking if provided
        if (defaultBooking != null) {
            for (int i = 0; i < cmbBookings.getItemCount(); i++) {
                if (cmbBookings.getItemAt(i).getBookingId() == defaultBooking.getBookingId()) {
                    cmbBookings.setSelectedIndex(i);
                    break;
                }
            }
        }
        updateBookingSummary.run();

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);
        JButton btnPay = UITheme.createPrimaryButton("Confirm & Pay");
        JButton btnCancel = UITheme.createSecondaryButton("Cancel");
        btnCancel.addActionListener(e -> dlg.dispose());

        btnPay.addActionListener(e -> {
            Booking sel = (Booking) cmbBookings.getSelectedItem();
            if (sel == null) {
                UITheme.showError(dlg, "Please select a booking to pay for.");
                return;
            }

            double amt;
            try {
                amt = Double.parseDouble(txtAmount.getText().trim());
            } catch (NumberFormatException nfe) {
                UITheme.showError(dlg, "Please enter a valid numeric payment amount.");
                return;
            }

            if (amt <= 0) {
                UITheme.showError(dlg, "Payment amount must be greater than zero (Trigger: BeforePaymentInsert).");
                return;
            }

            String selectedMethod = (String) cmbMethod.getSelectedItem();

            try {
                Payment p = paymentService.makeTouristPayment(sel.getBookingId(), amt, selectedMethod);
                UITheme.showSuccess(dlg,
                    "Payment Successful!\n\n"
                    + "Payment Reference: #" + p.getPaymentId() + "\n"
                    + "Booking Reference: #" + p.getBookingId() + " (" + sel.getPackageName() + ")\n"
                    + "Amount Paid: ₹" + String.format("%,.2f", p.getAmount()) + "\n"
                    + "Payment Method: " + p.getPaymentMethod() + "\n"
                    + "Status: " + p.getPaymentStatus() + "\n\n"
                    + "Your booking status has been updated to Confirmed!");
                dlg.dispose();
                loadMyBookings();
                loadMyPayments();
                updateHeaderWelcome();
                tabbedPane.setSelectedIndex(5); // Switch to My Payments tab
            } catch (ValidationException ve) {
                UITheme.showError(dlg, ve.getMessage());
            } catch (SQLException sqle) {
                String msg = sqle.getMessage();
                if (msg != null && msg.contains("Payment amount must be greater than zero")) {
                    UITheme.showError(dlg, "Database Trigger Violation: Payment amount must be greater than zero.");
                } else {
                    UITheme.showError(dlg, "Database Error: " + sqle.getMessage());
                }
            } catch (Exception ex) {
                UITheme.showError(dlg, "Error processing payment: " + ex.getMessage());
            }
        });

        btnPanel.add(btnCancel);
        btnPanel.add(btnPay);

        // Layout rows in card
        int r = 0;
        gbc.gridy = r++; card.add(createFieldPanel("Select Booking *", cmbBookings), gbc);
        gbc.gridy = r++; card.add(summaryPanel, gbc);
        gbc.gridy = r++; card.add(createFieldPanel("Payment Amount (₹) *", txtAmount), gbc);
        gbc.gridy = r++; card.add(createFieldPanel("Payment Method *", cmbMethod), gbc);
        gbc.gridy = r++; card.add(methodDetailsPanel, gbc);
        gbc.gridy = r++; card.add(Box.createVerticalStrut(6), gbc);
        gbc.gridy = r++; card.add(btnPanel, gbc);

        centerPanel.add(card);
        dlg.add(centerPanel, BorderLayout.CENTER);
        dlg.setVisible(true);
    }

    private void performSaveProfile() {
        String fn = txtProfFirstName.getText();
        String ln = txtProfLastName.getText();
        String ph = txtProfPhone.getText();
        String st = txtProfStreet.getText();
        String ci = txtProfCity.getText();
        String sta = txtProfState.getText();
        String co = txtProfCountry.getText();

        try {
            userService.updateMyProfile(fn, ln, ph, st, ci, sta, co);
            UITheme.showSuccess(this, "Profile updated successfully!");
        } catch (ValidationException ve) {
            UITheme.showError(this, ve.getMessage());
        } catch (Exception ex) {
            UITheme.showError(this, "Profile update error: " + ex.getMessage());
        }
    }

    public JTabbedPane getTabbedPane() {
        return tabbedPane;
    }
}
