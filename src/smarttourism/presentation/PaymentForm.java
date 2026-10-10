package smarttourism.presentation;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
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
import smarttourism.model.Payment;
import smarttourism.model.User;
import smarttourism.service.PaymentService;
import smarttourism.service.ValidationException;

/**
 * Tier 1 - Presentation Layer: Payment Management Form.
 * Enforces business rules and constraints (Amount > 0, valid payment methods & statuses).
 */
public class PaymentForm extends JFrame {

    private final PaymentService paymentService = new PaymentService();

    private JTextField txtId;
    private JComboBox<Booking> cmbBooking;
    private JComboBox<User> cmbAdmin;
    private JTextField txtAmount;
    private JTextField txtPaymentDate;
    private JComboBox<String> cmbMethod;
    private JTextField txtRefundInfo;
    private JComboBox<String> cmbStatus;
    private JTextField txtSearch;

    private JTable tblPayments;
    private DefaultTableModel tableModel;

    public PaymentForm() {
        super("Payment Management — Smart Tourism");
        initUI();
        loadDropdowns();
        loadPayments();
    }

    private void initUI() {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1100, 700);
        setLocationRelativeTo(null);
        getContentPane().setBackground(UITheme.COLOR_BG);
        setLayout(new BorderLayout(0, 10));

        // Header Banner
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(16, 185, 129)); // Emerald green
        headerPanel.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel titleLbl = new JLabel("Payment Management");
        titleLbl.setFont(UITheme.FONT_TITLE);
        titleLbl.setForeground(Color.WHITE);

        JLabel subLbl = new JLabel("Record transactions, issue refunds, and track payment status");
        subLbl.setFont(UITheme.FONT_SUBTITLE);
        subLbl.setForeground(new Color(209, 250, 229));

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
        formCard.setPreferredSize(new Dimension(380, 0));

        JPanel formFields = new JPanel(new GridBagLayout());
        formFields.setBackground(UITheme.COLOR_SURFACE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.gridx = 0;
        gbc.weightx = 1.0;

        int row = 0;
        gbc.gridy = row++;
        JLabel lblId = new JLabel("Payment ID:");
        lblId.setFont(UITheme.FONT_LABEL);
        formFields.add(lblId, gbc);

        gbc.gridy = row++;
        txtId = UITheme.createTextField();
        formFields.add(txtId, gbc);

        gbc.gridy = row++;
        JLabel lblBooking = new JLabel("Booking:");
        lblBooking.setFont(UITheme.FONT_LABEL);
        formFields.add(lblBooking, gbc);

        gbc.gridy = row++;
        cmbBooking = new JComboBox<>();
        cmbBooking.setFont(UITheme.FONT_REGULAR);
        cmbBooking.addActionListener(e -> onBookingSelected());
        formFields.add(cmbBooking, gbc);

        gbc.gridy = row++;
        JLabel lblAdmin = new JLabel("Processed By (Administrator):");
        lblAdmin.setFont(UITheme.FONT_LABEL);
        formFields.add(lblAdmin, gbc);

        gbc.gridy = row++;
        cmbAdmin = new JComboBox<>();
        cmbAdmin.setFont(UITheme.FONT_REGULAR);
        formFields.add(cmbAdmin, gbc);

        gbc.gridy = row++;
        JLabel lblAmount = new JLabel("Payment Amount (₹):");
        lblAmount.setFont(UITheme.FONT_LABEL);
        formFields.add(lblAmount, gbc);

        gbc.gridy = row++;
        txtAmount = UITheme.createTextField();
        formFields.add(txtAmount, gbc);

        gbc.gridy = row++;
        JLabel lblDate = new JLabel("Payment Date (YYYY-MM-DD):");
        lblDate.setFont(UITheme.FONT_LABEL);
        formFields.add(lblDate, gbc);

        gbc.gridy = row++;
        JPanel datePanel = new JPanel(new BorderLayout(4, 0));
        datePanel.setBackground(UITheme.COLOR_SURFACE);
        txtPaymentDate = UITheme.createTextField();
        txtPaymentDate.setText(LocalDate.now().toString());
        JButton btnToday = UITheme.createSecondaryButton("Today");
        btnToday.addActionListener(e -> txtPaymentDate.setText(LocalDate.now().toString()));
        datePanel.add(txtPaymentDate, BorderLayout.CENTER);
        datePanel.add(btnToday, BorderLayout.EAST);
        formFields.add(datePanel, gbc);

        gbc.gridy = row++;
        JLabel lblMethod = new JLabel("Payment Method:");
        lblMethod.setFont(UITheme.FONT_LABEL);
        formFields.add(lblMethod, gbc);

        gbc.gridy = row++;
        cmbMethod = new JComboBox<>(PaymentService.ALLOWED_METHODS.toArray(new String[0]));
        cmbMethod.setFont(UITheme.FONT_REGULAR);
        formFields.add(cmbMethod, gbc);

        gbc.gridy = row++;
        JLabel lblRefund = new JLabel("Refund Info (Optional):");
        lblRefund.setFont(UITheme.FONT_LABEL);
        formFields.add(lblRefund, gbc);

        gbc.gridy = row++;
        txtRefundInfo = UITheme.createTextField();
        formFields.add(txtRefundInfo, gbc);

        gbc.gridy = row++;
        JLabel lblSt = new JLabel("Payment Status:");
        lblSt.setFont(UITheme.FONT_LABEL);
        formFields.add(lblSt, gbc);

        gbc.gridy = row++;
        cmbStatus = new JComboBox<>(PaymentService.ALLOWED_STATUSES.toArray(new String[0]));
        cmbStatus.setFont(UITheme.FONT_REGULAR);
        formFields.add(cmbStatus, gbc);

        formCard.add(formFields, BorderLayout.CENTER);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 8));
        btnPanel.setBackground(UITheme.COLOR_SURFACE);

        JButton btnAdd = UITheme.createPrimaryButton("Record Payment");
        btnAdd.addActionListener(e -> addPayment());

        JButton btnUpdate = UITheme.createPrimaryButton("Update Status");
        btnUpdate.addActionListener(e -> updatePaymentStatus());

        JButton btnDelete = UITheme.createDangerButton("Delete");
        btnDelete.addActionListener(e -> deletePayment());

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
        btnSearch.addActionListener(e -> searchPayments());

        JButton btnRefresh = UITheme.createSecondaryButton("Refresh");
        btnRefresh.addActionListener(e -> {
            txtSearch.setText("");
            loadPayments();
        });

        searchBtns.add(btnSearch);
        searchBtns.add(btnRefresh);
        searchRow.add(searchBtns, BorderLayout.EAST);
        tableCard.add(searchRow, BorderLayout.NORTH);

        String[] cols = {"Payment ID", "Booking ID", "Admin Name", "Amount (₹)", "Date", "Method", "Status", "Refund Info"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tblPayments = new JTable(tableModel);
        UITheme.formatTable(tblPayments);

        tblPayments.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tblPayments.getSelectedRow() != -1) {
                int r = tblPayments.getSelectedRow();
                int pId = (int) tableModel.getValueAt(r, 0);
                loadSelectedPaymentToForm(pId);
            }
        });

        JScrollPane sp = new JScrollPane(tblPayments);
        sp.setBorder(BorderFactory.createLineBorder(UITheme.COLOR_BORDER, 1));
        sp.getViewport().setBackground(Color.WHITE);
        tableCard.add(sp, BorderLayout.CENTER);

        centerPanel.add(tableCard, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);
    }

    private void loadDropdowns() {
        new Thread(() -> {
            try {
                List<Booking> bookings = paymentService.getBookings();
                List<User> admins = paymentService.getAdministrators();
                SwingUtilities.invokeLater(() -> {
                    cmbBooking.removeAllItems();
                    for (Booking b : bookings) cmbBooking.addItem(b);

                    cmbAdmin.removeAllItems();
                    for (User a : admins) cmbAdmin.addItem(a);

                    onBookingSelected();
                });
            } catch (SQLException ex) {
                SwingUtilities.invokeLater(() -> UITheme.showError(this, "Failed loading dropdowns:\n" + ex.getMessage()));
            }
        }).start();
    }

    private void onBookingSelected() {
        Booking b = (Booking) cmbBooking.getSelectedItem();
        if (b != null && (txtAmount.getText().trim().isEmpty() || tblPayments.getSelectedRow() == -1)) {
            txtAmount.setText(String.format("%.2f", b.getEstimatedAmount()));
        }
    }

    private void loadPayments() {
        new Thread(() -> {
            try {
                List<Payment> list = paymentService.getAllPayments();
                SwingUtilities.invokeLater(() -> populateTable(list));
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> UITheme.showError(this, "Failed to load payments:\n" + ex.getMessage()));
            }
        }).start();
    }

    private void populateTable(List<Payment> list) {
        tableModel.setRowCount(0);
        for (Payment p : list) {
            tableModel.addRow(new Object[]{
                p.getPaymentId(),
                p.getBookingId(),
                p.getAdminName(),
                String.format("%.2f", p.getAmount()),
                p.getPaymentDate(),
                p.getPaymentMethod(),
                p.getPaymentStatus(),
                p.getRefundInfo() != null ? p.getRefundInfo() : "None"
            });
        }
    }

    private void loadSelectedPaymentToForm(int paymentId) {
        new Thread(() -> {
            try {
                Payment p = paymentService.getPaymentById(paymentId);
                if (p != null) {
                    SwingUtilities.invokeLater(() -> {
                        txtId.setText(String.valueOf(p.getPaymentId()));
                        for (int i = 0; i < cmbBooking.getItemCount(); i++) {
                            if (cmbBooking.getItemAt(i).getBookingId() == p.getBookingId()) {
                                cmbBooking.setSelectedIndex(i);
                                break;
                            }
                        }
                        for (int i = 0; i < cmbAdmin.getItemCount(); i++) {
                            if (cmbAdmin.getItemAt(i).getUserId() == p.getAdminId()) {
                                cmbAdmin.setSelectedIndex(i);
                                break;
                            }
                        }
                        txtAmount.setText(String.format("%.2f", p.getAmount()));
                        txtPaymentDate.setText(p.getPaymentDate());
                        cmbMethod.setSelectedItem(p.getPaymentMethod());
                        txtRefundInfo.setText(p.getRefundInfo() != null ? p.getRefundInfo() : "");
                        cmbStatus.setSelectedItem(p.getPaymentStatus());
                    });
                }
            } catch (Exception ignored) {}
        }).start();
    }

    private void searchPayments() {
        String q = txtSearch.getText().trim();
        new Thread(() -> {
            try {
                List<Payment> list = paymentService.searchPayments(q);
                SwingUtilities.invokeLater(() -> populateTable(list));
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> UITheme.showError(this, "Search error:\n" + ex.getMessage()));
            }
        }).start();
    }

    private void addPayment() {
        try {
            Payment p = getPaymentFromForm();
            paymentService.addPayment(p);
            UITheme.showSuccess(this, "Payment #" + p.getPaymentId() + " recorded successfully!");
            clearFields();
            loadPayments();
        } catch (ValidationException ve) {
            UITheme.showError(this, ve.getMessage());
        } catch (Exception ex) {
            UITheme.showError(this, "Failed to record payment:\n" + ex.getMessage());
        }
    }

    private void updatePaymentStatus() {
        String idText = txtId.getText().trim();
        if (idText.isEmpty()) {
            UITheme.showError(this, "Please select a payment to update.");
            return;
        }

        try {
            int pId = Integer.parseInt(idText);
            String status = (String) cmbStatus.getSelectedItem();
            paymentService.updateStatus(pId, status);
            UITheme.showSuccess(this, "Payment #" + pId + " status updated to '" + status + "'!");
            loadPayments();
        } catch (ValidationException ve) {
            UITheme.showError(this, ve.getMessage());
        } catch (Exception ex) {
            UITheme.showError(this, "Failed to update payment status:\n" + ex.getMessage());
        }
    }

    private void deletePayment() {
        String idText = txtId.getText().trim();
        if (idText.isEmpty()) {
            UITheme.showError(this, "Please select a payment to delete.");
            return;
        }

        int pId;
        try {
            pId = Integer.parseInt(idText);
        } catch (NumberFormatException ex) {
            UITheme.showError(this, "Payment ID must be an integer.");
            return;
        }

        if (!UITheme.showConfirm(this, "Are you sure you want to delete Payment #" + pId + "?")) {
            return;
        }

        try {
            paymentService.deletePayment(pId);
            UITheme.showSuccess(this, "Payment #" + pId + " deleted successfully!");
            clearFields();
            loadPayments();
        } catch (ValidationException ve) {
            UITheme.showError(this, ve.getMessage());
        } catch (Exception ex) {
            UITheme.showError(this, "Failed to delete payment:\n" + ex.getMessage());
        }
    }

    private Payment getPaymentFromForm() throws ValidationException {
        String idStr = txtId.getText().trim();
        if (idStr.isEmpty()) throw new ValidationException("Payment ID is required.");
        int id;
        try {
            id = Integer.parseInt(idStr);
        } catch (NumberFormatException ex) {
            throw new ValidationException("Payment ID must be a numeric integer.");
        }

        Booking b = (Booking) cmbBooking.getSelectedItem();
        if (b == null) throw new ValidationException("Please select a valid Booking.");

        User a = (User) cmbAdmin.getSelectedItem();
        if (a == null) throw new ValidationException("Please select a valid Administrator.");

        String amtStr = txtAmount.getText().trim();
        double amount;
        try {
            amount = Double.parseDouble(amtStr);
        } catch (NumberFormatException ex) {
            throw new ValidationException("Amount must be a numeric value.");
        }

        String pDate = txtPaymentDate.getText().trim();
        String method = (String) cmbMethod.getSelectedItem();
        String refund = txtRefundInfo.getText().trim();
        if (refund.isEmpty()) refund = null;
        String status = (String) cmbStatus.getSelectedItem();

        return new Payment(id, b.getBookingId(), a.getUserId(), amount, pDate, method, refund, status);
    }

    private void clearFields() {
        txtId.setText("");
        txtPaymentDate.setText(LocalDate.now().toString());
        txtAmount.setText("");
        txtRefundInfo.setText("");
        tblPayments.clearSelection();
        suggestNextId();
        onBookingSelected();
    }

    private void suggestNextId() {
        new Thread(() -> {
            try {
                int next = paymentService.getNextPaymentId();
                SwingUtilities.invokeLater(() -> {
                    if (txtId.getText().trim().isEmpty()) {
                        txtId.setText(String.valueOf(next));
                    }
                });
            } catch (Exception ignored) {}
        }).start();
    }
}
