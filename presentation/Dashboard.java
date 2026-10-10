package smarttourism.presentation;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.sql.SQLException;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import smarttourism.model.Booking;
import smarttourism.model.DashboardStats;
import smarttourism.model.User;
import smarttourism.service.AuthService;
import smarttourism.service.DashboardService;

/**
 * Tier 1 - Presentation Layer: Main Dashboard.
 * Displays KPI statistics, recent bookings, and launches all management modules.
 */
public class Dashboard extends JFrame {

    private final DashboardService dashboardService = new DashboardService();
    private final User activeUser;

    // KPI Labels
    private JLabel lblDestinations;
    private JLabel lblAttractions;
    private JLabel lblPackages;
    private JLabel lblBookings;
    private JLabel lblPayments;
    private JLabel lblRevenue;

    // Recent Bookings Table
    private JTable tblRecentBookings;
    private DefaultTableModel tableModel;

    public Dashboard() {
        this(AuthService.getCurrentUser());
    }

    public Dashboard(User user) {
        super("Smart Tourism Management System — Dashboard");
        this.activeUser = user;
        initUI();
        loadDashboardData();
    }

    private void initUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 720);
        setMinimumSize(new Dimension(950, 600));
        setLocationRelativeTo(null);
        getContentPane().setBackground(UITheme.COLOR_BG);
        setLayout(new BorderLayout(0, 12));

        // Top Header
        JPanel topPanel = createTopPanel();
        add(topPanel, BorderLayout.NORTH);

        // Center Content (KPIs + Nav + Recent Bookings Table)
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBackground(UITheme.COLOR_BG);
        centerPanel.setBorder(new EmptyBorder(0, 20, 16, 20));

        // 1. KPI Cards Grid
        JPanel kpiGrid = createKpiGrid();
        centerPanel.add(kpiGrid);
        centerPanel.add(Box.createVerticalStrut(16));

        // 2. Navigation Module Bar
        JPanel navBar = createModuleNavigationBar();
        centerPanel.add(navBar);
        centerPanel.add(Box.createVerticalStrut(16));

        // 3. Recent Bookings Card with Table
        JPanel tableCard = createRecentBookingsCard();
        centerPanel.add(tableCard);

        add(centerPanel, BorderLayout.CENTER);
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UITheme.COLOR_PRIMARY_DARK);
        panel.setBorder(new EmptyBorder(16, 24, 16, 24));

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setBackground(UITheme.COLOR_PRIMARY_DARK);

        JLabel title = new JLabel("Smart Tourism Management System");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(Color.WHITE);

        JLabel sub = new JLabel("Overview, Key Metrics & Management");
        sub.setFont(UITheme.FONT_SUBTITLE);
        sub.setForeground(new Color(204, 251, 241));

        left.add(title);
        left.add(Box.createVerticalStrut(4));
        left.add(sub);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setBackground(UITheme.COLOR_PRIMARY_DARK);

        String userName = activeUser != null ? activeUser.getFullName() + " (" + activeUser.getRole() + ")" : "Administrator";
        JLabel userBadge = new JLabel("User: " + userName);
        userBadge.setFont(UITheme.FONT_LABEL);
        userBadge.setForeground(Color.WHITE);

        JButton btnChangePass = UITheme.createSecondaryButton("Change Password");
        btnChangePass.addActionListener(e -> new ChangePasswordDialog(this).setVisible(true));

        JButton btnRefresh = UITheme.createSecondaryButton("Refresh Data");
        btnRefresh.addActionListener(e -> loadDashboardData());

        JButton btnLogout = UITheme.createSecondaryButton("Logout");
        btnLogout.addActionListener(e -> {
            AuthService.logout();
            dispose();
            SwingUtilities.invokeLater(() -> new LoginForm().setVisible(true));
        });

        right.add(userBadge);
        right.add(Box.createHorizontalStrut(8));
        right.add(btnChangePass);
        right.add(btnRefresh);
        right.add(btnLogout);

        panel.add(left, BorderLayout.WEST);
        panel.add(right, BorderLayout.EAST);
        return panel;
    }

    private JPanel createKpiGrid() {
        JPanel grid = new JPanel(new GridLayout(1, 6, 12, 0));
        grid.setBackground(UITheme.COLOR_BG);
        grid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        lblDestinations = new JLabel("0", SwingConstants.CENTER);
        lblAttractions = new JLabel("0", SwingConstants.CENTER);
        lblPackages = new JLabel("0", SwingConstants.CENTER);
        lblBookings = new JLabel("0", SwingConstants.CENTER);
        lblPayments = new JLabel("0", SwingConstants.CENTER);
        lblRevenue = new JLabel("₹0", SwingConstants.CENTER);

        grid.add(createKpiCard("Destinations", lblDestinations, new Color(13, 148, 136)));
        grid.add(createKpiCard("Attractions", lblAttractions, new Color(2, 132, 199)));
        grid.add(createKpiCard("Tour Packages", lblPackages, new Color(124, 58, 237)));
        grid.add(createKpiCard("Bookings", lblBookings, new Color(234, 88, 12)));
        grid.add(createKpiCard("Payments", lblPayments, new Color(16, 185, 129)));
        grid.add(createKpiCard("Completed Revenue", lblRevenue, new Color(217, 119, 6)));

        return grid;
    }

    private JPanel createKpiCard(String labelText, JLabel valueLabel, Color accentColor) {
        JPanel card = UITheme.createCardPanel();
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 4, 0, 0, accentColor),
            new EmptyBorder(10, 12, 10, 12)
        ));

        valueLabel.setFont(UITheme.FONT_STAT_NUM);
        valueLabel.setForeground(UITheme.COLOR_TEXT_MAIN);

        JLabel lbl = new JLabel(labelText, SwingConstants.CENTER);
        lbl.setFont(UITheme.FONT_STAT_LBL);
        lbl.setForeground(UITheme.COLOR_TEXT_MUTED);

        card.add(valueLabel, BorderLayout.CENTER);
        card.add(lbl, BorderLayout.SOUTH);
        return card;
    }

    private JPanel createModuleNavigationBar() {
        JPanel panel = UITheme.createCardPanel();
        panel.setLayout(new BorderLayout());
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));

        JLabel navTitle = new JLabel("Management Modules:");
        navTitle.setFont(UITheme.FONT_LABEL);
        navTitle.setForeground(UITheme.COLOR_TEXT_MUTED);
        panel.add(navTitle, BorderLayout.WEST);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnRow.setBackground(UITheme.COLOR_SURFACE);

        JButton btnDest = UITheme.createPrimaryButton("Destinations");
        btnDest.addActionListener(e -> new DestinationForm().setVisible(true));

        JButton btnAttr = UITheme.createPrimaryButton("Attractions");
        btnAttr.addActionListener(e -> new AttractionForm().setVisible(true));

        JButton btnPkg = UITheme.createPrimaryButton("Tour Packages");
        btnPkg.addActionListener(e -> new PackageForm().setVisible(true));

        JButton btnBook = UITheme.createPrimaryButton("Bookings");
        btnBook.addActionListener(e -> new BookingForm().setVisible(true));

        JButton btnPay = UITheme.createPrimaryButton("Payments");
        btnPay.addActionListener(e -> new PaymentForm().setVisible(true));

        JButton btnUser = UITheme.createSecondaryButton("Users & Tourists");
        btnUser.addActionListener(e -> new UserForm().setVisible(true));

        btnRow.add(btnDest);
        btnRow.add(btnAttr);
        btnRow.add(btnPkg);
        btnRow.add(btnBook);
        btnRow.add(btnPay);
        btnRow.add(btnUser);

        panel.add(btnRow, BorderLayout.EAST);
        return panel;
    }

    private JPanel createRecentBookingsCard() {
        JPanel card = UITheme.createCardPanel();
        card.setLayout(new BorderLayout(0, 10));

        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setBackground(UITheme.COLOR_SURFACE);

        JLabel cardTitle = new JLabel("Recent Bookings");
        cardTitle.setFont(UITheme.FONT_HEADER);
        cardTitle.setForeground(UITheme.COLOR_TEXT_MAIN);

        JLabel cardSub = new JLabel("Latest tourist reservations and trip details");
        cardSub.setFont(UITheme.FONT_SUBTITLE);
        cardSub.setForeground(UITheme.COLOR_TEXT_MUTED);

        headerRow.add(cardTitle, BorderLayout.WEST);
        headerRow.add(cardSub, BorderLayout.EAST);
        card.add(headerRow, BorderLayout.NORTH);

        String[] columns = {"Booking ID", "Tourist Name", "Tour Package", "Travel Date", "People", "Status", "Estimated Total (₹)"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        tblRecentBookings = new JTable(tableModel);
        UITheme.formatTable(tblRecentBookings);

        JScrollPane sp = new JScrollPane(tblRecentBookings);
        sp.setBorder(BorderFactory.createLineBorder(UITheme.COLOR_BORDER, 1));
        sp.getViewport().setBackground(Color.WHITE);
        card.add(sp, BorderLayout.CENTER);

        return card;
    }

    public void loadDashboardData() {
        new Thread(() -> {
            try {
                DashboardStats stats = dashboardService.getStats();
                List<Booking> bookings = dashboardService.getRecentBookings(15);

                SwingUtilities.invokeLater(() -> {
                    lblDestinations.setText(String.valueOf(stats.getTotalDestinations()));
                    lblAttractions.setText(String.valueOf(stats.getTotalAttractions()));
                    lblPackages.setText(String.valueOf(stats.getTotalPackages()));
                    lblBookings.setText(String.valueOf(stats.getTotalBookings()));
                    lblPayments.setText(String.valueOf(stats.getTotalPayments()));
                    lblRevenue.setText("₹" + String.format("%,.0f", stats.getTotalRevenue()));

                    tableModel.setRowCount(0);
                    for (Booking b : bookings) {
                        tableModel.addRow(new Object[]{
                            b.getBookingId(),
                            b.getTouristName(),
                            b.getPackageName(),
                            b.getTravelDate(),
                            b.getNumberOfPeople(),
                            b.getBookingStatus(),
                            String.format("₹%,.2f", b.getEstimatedAmount())
                        });
                    }
                });
            } catch (SQLException ex) {
                SwingUtilities.invokeLater(() -> UITheme.showError(this, "Could not load dashboard data:\n" + ex.getMessage()));
            }
        }).start();
    }
}
