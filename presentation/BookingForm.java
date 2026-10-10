package smarttourism.presentation;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import smarttourism.model.Booking;
import smarttourism.model.TourPackage;
import smarttourism.model.User;
import smarttourism.service.BookingService;
import smarttourism.service.ValidationException;

/**
 * Tier 1 - Presentation Layer: Booking Management Form.
 * Enforces business rules and dynamic estimated amount calculation (cost × people).
 */
public class BookingForm extends JFrame {

    private final BookingService bookingService = new BookingService();

    private JTextField txtId;
    private JComboBox<User> cmbTourist;
    private JComboBox<TourPackage> cmbPackage;
    private JTextField txtBookingDate;
    private JTextField txtTravelDate;
    private JTextField txtPeople;
    private JComboBox<String> cmbStatus;
    private JLabel lblEstimatedCost;
    private JTextField txtSearch;

    private JTable tblBookings;
    private DefaultTableModel tableModel;

    public BookingForm() {
        super("Booking Management — Smart Tourism");
        initUI();
        loadDropdowns();
        loadBookings();
    }

    private void initUI() {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1150, 700);
        setLocationRelativeTo(null);
        getContentPane().setBackground(UITheme.COLOR_BG);
        setLayout(new BorderLayout(0, 10));

        // Header Banner
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(234, 88, 12)); // Warm orange
        headerPanel.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel titleLbl = new JLabel("Booking Management");
        titleLbl.setFont(UITheme.FONT_TITLE);
        titleLbl.setForeground(Color.WHITE);

        JLabel subLbl = new JLabel("Create, view, and manage tourist trip bookings and reservations");
        subLbl.setFont(UITheme.FONT_SUBTITLE);
        subLbl.setForeground(new Color(255, 237, 213));

        headerPanel.add(titleLbl, BorderLayout.WEST);
        headerPanel.add(subLbl, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Center Content
        JPanel centerPanel = new JPanel(new BorderLayout(16, 0));
        centerPanel.setBackground(UITheme.COLOR_BG);
        centerPanel.setBorder(new EmptyBorder(0, 16, 16, 16));

        // LEFT: Form Inputs Card
        JPanel formCard = UITheme.createCardPanel();
        formCard.setLayout(new BorderLayout());
        formCard.setPreferredSize(new Dimension(390, 0));

        JPanel formFields = new JPanel(new GridBagLayout());
        formFields.setBackground(UITheme.COLOR_SURFACE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.gridx = 0;
        gbc.weightx = 1.0;

        int row = 0;
        gbc.gridy = row++;
        JLabel lblId = new JLabel("Booking ID:");
        lblId.setFont(UITheme.FONT_LABEL);
        formFields.add(lblId, gbc);

        gbc.gridy = row++;
        txtId = UITheme.createTextField();
        formFields.add(txtId, gbc);

        gbc.gridy = row++;
        JLabel lblTourist = new JLabel("Tourist:");
        lblTourist.setFont(UITheme.FONT_LABEL);
        formFields.add(lblTourist, gbc);

        gbc.gridy = row++;
        cmbTourist = new JComboBox<>();
        cmbTourist.setFont(UITheme.FONT_REGULAR);
        formFields.add(cmbTourist, gbc);

        gbc.gridy = row++;
        JLabel lblPkg = new JLabel("Tour Package:");
        lblPkg.setFont(UITheme.FONT_LABEL);
        formFields.add(lblPkg, gbc);

        gbc.gridy = row++;
        cmbPackage = new JComboBox<>();
        cmbPackage.setFont(UITheme.FONT_REGULAR);
        cmbPackage.addActionListener(e -> updateEstimatedCostDisplay());
        formFields.add(cmbPackage, gbc);

        gbc.gridy = row++;
        JLabel lblBDate = new JLabel("Booking Date (YYYY-MM-DD):");
        lblBDate.setFont(UITheme.FONT_LABEL);
        formFields.add(lblBDate, gbc);

        gbc.gridy = row++;
        JPanel bDatePanel = new JPanel(new BorderLayout(4, 0));
        bDatePanel.setBackground(UITheme.COLOR_SURFACE);
        txtBookingDate = UITheme.createTextField();
        txtBookingDate.setText(LocalDate.now().toString());
        JButton btnToday = UITheme.createSecondaryButton("Today");
        btnToday.addActionListener(e -> txtBookingDate.setText(LocalDate.now().toString()));
        bDatePanel.add(txtBookingDate, BorderLayout.CENTER);
        bDatePanel.add(btnToday, BorderLayout.EAST);
        formFields.add(bDatePanel, gbc);

        gbc.gridy = row++;
        JLabel lblTDate = new JLabel("Travel Date (YYYY-MM-DD):");
        lblTDate.setFont(UITheme.FONT_LABEL);
        formFields.add(lblTDate, gbc);

        gbc.gridy = row++;
        JPanel tDatePanel = new JPanel(new BorderLayout(4, 0));
        tDatePanel.setBackground(UITheme.COLOR_SURFACE);
        txtTravelDate = UITheme.createTextField();
        txtTravelDate.setText(LocalDate.now().plusDays(14).toString());
        JButton btnPlus14 = UITheme.createSecondaryButton("+14d");
        btnPlus14.addActionListener(e -> txtTravelDate.setText(LocalDate.now().plusDays(14).toString()));
        tDatePanel.add(txtTravelDate, BorderLayout.CENTER);
        tDatePanel.add(btnPlus14, BorderLayout.EAST);
        formFields.add(tDatePanel, gbc);

        gbc.gridy = row++;
        JLabel lblPpl = new JLabel("Number of People:");
        lblPpl.setFont(UITheme.FONT_LABEL);
        formFields.add(lblPpl, gbc);

        gbc.gridy = row++;
        txtPeople = UITheme.createTextField();
        txtPeople.setText("1");
        txtPeople.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent e) { updateEstimatedCostDisplay(); }
        });
        formFields.add(txtPeople, gbc);

        gbc.gridy = row++;
        JLabel lblSt = new JLabel("Booking Status:");
        lblSt.setFont(UITheme.FONT_LABEL);
        formFields.add(lblSt, gbc);

        gbc.gridy = row++;
        cmbStatus = new JComboBox<>(BookingService.ALLOWED_STATUSES.toArray(new String[0]));
        cmbStatus.setFont(UITheme.FONT_REGULAR);
        formFields.add(cmbStatus, gbc);

        // Highlight card for dynamic estimated cost calculation
        gbc.gridy = row++;
        JPanel costCard = new JPanel(new BorderLayout());
        costCard.setBackground(new Color(254, 243, 199)); // Amber-100
        costCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(245, 158, 11), 1),
            new EmptyBorder(8, 10, 8, 10)
        ));
        JLabel costTitle = new JLabel("Estimated Total Amount:", JLabel.LEFT);
        costTitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        costTitle.setForeground(new Color(146, 64, 14));
        lblEstimatedCost = new JLabel("₹0.00", JLabel.RIGHT);
        lblEstimatedCost.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblEstimatedCost.setForeground(new Color(180, 83, 9));
        costCard.add(costTitle, BorderLayout.WEST);
        costCard.add(lblEstimatedCost, BorderLayout.EAST);
        formFields.add(costCard, gbc);

        formCard.add(formFields, BorderLayout.CENTER);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 8));
        btnPanel.setBackground(UITheme.COLOR_SURFACE);

        JButton btnAdd = UITheme.createPrimaryButton("Create Booking");
        btnAdd.addActionListener(e -> addBooking());

        JButton btnUpdate = UITheme.createPrimaryButton("Update Status");
        btnUpdate.addActionListener(e -> updateBookingStatus());

        JButton btnDelete = UITheme.createDangerButton("Delete");
        btnDelete.addActionListener(e -> deleteBooking());

        JButton btnClear = UITheme.createSecondaryButton("Clear");
        btnClear.addActionListener(e -> clearFields());

        btnPanel.add(btnAdd);
        btnPanel.add(btnUpdate);
        btnPanel.add(btnDelete);
        btnPanel.add(btnClear);
        formCard.add(btnPanel, BorderLayout.SOUTH);

        centerPanel.add(formCard, BorderLayout.WEST);

        // RIGHT: Search & Table
        JPanel tableCard = UITheme.createCardPanel();
        tableCard.setLayout(new BorderLayout(0, 10));

        JPanel searchRow = new JPanel(new BorderLayout(8, 0));
        searchRow.setBackground(UITheme.COLOR_SURFACE);

        JLabel lblSearch = new JLabel("Search:");
        lblSearch.setFont(UITheme.FONT_LABEL);
        searchRow.add(lblSearch, BorderLayout.WEST);

        txtSearch = UITheme.createTextField();
        searchRow.add(txtSearch, BorderLayout.CENTER);

        JPanel searchBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        searchBtns.setBackground(UITheme.COLOR_SURFACE);

        JButton btnSearch = UITheme.createPrimaryButton("Search");
        btnSearch.addActionListener(e -> searchBookings());

        JButton btnRefresh = UITheme.createSecondaryButton("Refresh");
        btnRefresh.addActionListener(e -> {
            txtSearch.setText("");
            loadBookings();
        });

        searchBtns.add(btnSearch);
        searchBtns.add(btnRefresh);
        searchRow.add(searchBtns, BorderLayout.EAST);
        tableCard.add(searchRow, BorderLayout.NORTH);

        String[] cols = {"ID", "Tourist Name", "Package", "Booking Date", "Travel Date", "People", "Status", "Estimated Total (₹)"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tblBookings = new JTable(tableModel);
        UITheme.formatTable(tblBookings);

        tblBookings.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tblBookings.getSelectedRow() != -1) {
                int r = tblBookings.getSelectedRow();
                int bId = (int) tableModel.getValueAt(r, 0);
                loadSelectedBookingToForm(bId);
            }
        });

        JScrollPane sp = new JScrollPane(tblBookings);
        sp.setBorder(BorderFactory.createLineBorder(UITheme.COLOR_BORDER, 1));
        sp.getViewport().setBackground(Color.WHITE);
        tableCard.add(sp, BorderLayout.CENTER);

        centerPanel.add(tableCard, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);
    }

    private void loadDropdowns() {
        new Thread(() -> {
            try {
                List<User> tourists = bookingService.getTourists();
                List<TourPackage> packages = bookingService.getPackages();
                SwingUtilities.invokeLater(() -> {
                    cmbTourist.removeAllItems();
                    for (User t : tourists) cmbTourist.addItem(t);

                    cmbPackage.removeAllItems();
                    for (TourPackage p : packages) cmbPackage.addItem(p);

                    updateEstimatedCostDisplay();
                });
            } catch (SQLException ex) {
                SwingUtilities.invokeLater(() -> UITheme.showError(this, "Failed loading dropdowns:\n" + ex.getMessage()));
            }
        }).start();
    }

    private void updateEstimatedCostDisplay() {
        TourPackage p = (TourPackage) cmbPackage.getSelectedItem();
        if (p == null) {
            lblEstimatedCost.setText("₹0.00");
            return;
        }
        int people = 1;
        try {
            people = Integer.parseInt(txtPeople.getText().trim());
        } catch (NumberFormatException ignored) {}
        if (people <= 0) people = 1;
        double total = p.getCost() * people;
        lblEstimatedCost.setText("₹" + String.format("%,.2f", total));
    }

    private void loadBookings() {
        new Thread(() -> {
            try {
                List<Booking> list = bookingService.getAllBookings();
                SwingUtilities.invokeLater(() -> populateTable(list));
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> UITheme.showError(this, "Failed to load bookings:\n" + ex.getMessage()));
            }
        }).start();
    }

    private void populateTable(List<Booking> list) {
        tableModel.setRowCount(0);
        for (Booking b : list) {
            tableModel.addRow(new Object[]{
                b.getBookingId(),
                b.getTouristName(),
                b.getPackageName(),
                b.getBookingDate(),
                b.getTravelDate(),
                b.getNumberOfPeople(),
                b.getBookingStatus(),
                String.format("₹%,.2f", b.getEstimatedAmount())
            });
        }
    }

    private void loadSelectedBookingToForm(int bookingId) {
        new Thread(() -> {
            try {
                Booking b = bookingService.getBookingById(bookingId);
                if (b != null) {
                    SwingUtilities.invokeLater(() -> {
                        txtId.setText(String.valueOf(b.getBookingId()));
                        for (int i = 0; i < cmbTourist.getItemCount(); i++) {
                            if (cmbTourist.getItemAt(i).getUserId() == b.getTouristId()) {
                                cmbTourist.setSelectedIndex(i);
                                break;
                            }
                        }
                        for (int i = 0; i < cmbPackage.getItemCount(); i++) {
                            if (cmbPackage.getItemAt(i).getPackageId() == b.getPackageId()) {
                                cmbPackage.setSelectedIndex(i);
                                break;
                            }
                        }
                        txtBookingDate.setText(b.getBookingDate());
                        txtTravelDate.setText(b.getTravelDate());
                        txtPeople.setText(String.valueOf(b.getNumberOfPeople()));
                        cmbStatus.setSelectedItem(b.getBookingStatus());
                        updateEstimatedCostDisplay();
                    });
                }
            } catch (Exception ignored) {}
        }).start();
    }

    private void searchBookings() {
        String q = txtSearch.getText().trim();
        new Thread(() -> {
            try {
                List<Booking> list = bookingService.searchBookings(q);
                SwingUtilities.invokeLater(() -> populateTable(list));
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> UITheme.showError(this, "Search error:\n" + ex.getMessage()));
            }
        }).start();
    }

    private void addBooking() {
        try {
            Booking b = getBookingFromForm();
            bookingService.addBooking(b);
            UITheme.showSuccess(this, "Booking #" + b.getBookingId() + " created successfully!");
            clearFields();
            loadBookings();
        } catch (ValidationException ve) {
            UITheme.showError(this, ve.getMessage());
        } catch (Exception ex) {
            UITheme.showError(this, "Failed to create booking:\n" + ex.getMessage());
        }
    }

    private void updateBookingStatus() {
        String idText = txtId.getText().trim();
        if (idText.isEmpty()) {
            UITheme.showError(this, "Please select a booking to update.");
            return;
        }

        try {
            int bId = Integer.parseInt(idText);
            String status = (String) cmbStatus.getSelectedItem();
            bookingService.updateStatus(bId, status);
            UITheme.showSuccess(this, "Booking #" + bId + " status updated to '" + status + "'!");
            loadBookings();
        } catch (ValidationException ve) {
            UITheme.showError(this, ve.getMessage());
        } catch (Exception ex) {
            UITheme.showError(this, "Failed to update booking status:\n" + ex.getMessage());
        }
    }

    private void deleteBooking() {
        String idText = txtId.getText().trim();
        if (idText.isEmpty()) {
            UITheme.showError(this, "Please select a booking to delete.");
            return;
        }

        int bId;
        try {
            bId = Integer.parseInt(idText);
        } catch (NumberFormatException ex) {
            UITheme.showError(this, "Booking ID must be a numeric integer.");
            return;
        }

        if (!UITheme.showConfirm(this, "Are you sure you want to delete Booking #" + bId + "?")) {
            return;
        }

        try {
            bookingService.deleteBooking(bId);
            UITheme.showSuccess(this, "Booking #" + bId + " deleted successfully!");
            clearFields();
            loadBookings();
        } catch (ValidationException ve) {
            UITheme.showError(this, ve.getMessage());
        } catch (Exception ex) {
            UITheme.showError(this, "Failed to delete booking:\n" + ex.getMessage());
        }
    }

    private Booking getBookingFromForm() throws ValidationException {
        String idStr = txtId.getText().trim();
        if (idStr.isEmpty()) throw new ValidationException("Booking ID is required.");
        int id;
        try {
            id = Integer.parseInt(idStr);
        } catch (NumberFormatException ex) {
            throw new ValidationException("Booking ID must be a numeric integer.");
        }

        User tourist = (User) cmbTourist.getSelectedItem();
        if (tourist == null) throw new ValidationException("Please select a Tourist.");

        TourPackage pkg = (TourPackage) cmbPackage.getSelectedItem();
        if (pkg == null) throw new ValidationException("Please select a Tour Package.");

        String bDate = txtBookingDate.getText().trim();
        String tDate = txtTravelDate.getText().trim();

        String pplStr = txtPeople.getText().trim();
        int people;
        try {
            people = Integer.parseInt(pplStr);
        } catch (NumberFormatException ex) {
            throw new ValidationException("Number of people must be an integer.");
        }

        String status = (String) cmbStatus.getSelectedItem();

        return new Booking(id, tourist.getUserId(), pkg.getPackageId(), bDate, tDate, people, status);
    }

    private void clearFields() {
        txtId.setText("");
        txtBookingDate.setText(LocalDate.now().toString());
        txtTravelDate.setText(LocalDate.now().plusDays(14).toString());
        txtPeople.setText("1");
        tblBookings.clearSelection();
        suggestNextId();
        updateEstimatedCostDisplay();
    }

    private void suggestNextId() {
        new Thread(() -> {
            try {
                int next = bookingService.getNextBookingId();
                SwingUtilities.invokeLater(() -> {
                    if (txtId.getText().trim().isEmpty()) {
                        txtId.setText(String.valueOf(next));
                    }
                });
            } catch (Exception ignored) {}
        }).start();
    }
}
