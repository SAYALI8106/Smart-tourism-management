package smarttourism.presentation;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.sql.SQLException;
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
import smarttourism.model.User;
import smarttourism.service.UserService;
import smarttourism.service.ValidationException;

/**
 * Tier 1 - Presentation Layer: User & Tourist Management Form.
 * Manages USER table along with specialized TOURIST and ADMINISTRATOR role records.
 */
public class UserForm extends JFrame {

    private final UserService userService = new UserService();

    private JTextField txtId;
    private JTextField txtFirstName;
    private JTextField txtLastName;
    private JTextField txtEmail;
    private JTextField txtPhone;
    private JTextField txtStreet;
    private JTextField txtCity;
    private JTextField txtState;
    private JTextField txtCountry;
    private JComboBox<String> cmbRole;
    private JTextField txtSearch;

    private JTable tblUsers;
    private DefaultTableModel tableModel;

    public UserForm() {
        super("User Management — Smart Tourism");
        initUI();
        loadUsers();
        suggestNextId();
    }

    private void initUI() {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1100, 700);
        setLocationRelativeTo(null);
        getContentPane().setBackground(UITheme.COLOR_BG);
        setLayout(new BorderLayout(0, 10));

        // Header Banner
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(71, 85, 105)); // Slate 600
        headerPanel.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel titleLbl = new JLabel("User & Role Management");
        titleLbl.setFont(UITheme.FONT_TITLE);
        titleLbl.setForeground(Color.WHITE);

        JLabel subLbl = new JLabel("Manage user accounts, contact information, and roles");
        subLbl.setFont(UITheme.FONT_SUBTITLE);
        subLbl.setForeground(new Color(241, 245, 249));

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
        gbc.insets = new Insets(3, 4, 3, 4);
        gbc.gridx = 0;
        gbc.weightx = 1.0;

        int row = 0;
        gbc.gridy = row++;
        JLabel lblId = new JLabel("User ID:");
        lblId.setFont(UITheme.FONT_LABEL);
        formFields.add(lblId, gbc);

        gbc.gridy = row++;
        txtId = UITheme.createTextField();
        formFields.add(txtId, gbc);

        gbc.gridy = row++;
        JLabel lblRole = new JLabel("Account Role:");
        lblRole.setFont(UITheme.FONT_LABEL);
        formFields.add(lblRole, gbc);

        gbc.gridy = row++;
        cmbRole = new JComboBox<>(new String[]{"Tourist", "Administrator"});
        cmbRole.setFont(UITheme.FONT_REGULAR);
        formFields.add(cmbRole, gbc);

        gbc.gridy = row++;
        JLabel lblFN = new JLabel("First Name:");
        lblFN.setFont(UITheme.FONT_LABEL);
        formFields.add(lblFN, gbc);

        gbc.gridy = row++;
        txtFirstName = UITheme.createTextField();
        formFields.add(txtFirstName, gbc);

        gbc.gridy = row++;
        JLabel lblLN = new JLabel("Last Name:");
        lblLN.setFont(UITheme.FONT_LABEL);
        formFields.add(lblLN, gbc);

        gbc.gridy = row++;
        txtLastName = UITheme.createTextField();
        formFields.add(txtLastName, gbc);

        gbc.gridy = row++;
        JLabel lblEmail = new JLabel("Email Address:");
        lblEmail.setFont(UITheme.FONT_LABEL);
        formFields.add(lblEmail, gbc);

        gbc.gridy = row++;
        txtEmail = UITheme.createTextField();
        formFields.add(txtEmail, gbc);

        gbc.gridy = row++;
        JLabel lblPhone = new JLabel("Phone Number:");
        lblPhone.setFont(UITheme.FONT_LABEL);
        formFields.add(lblPhone, gbc);

        gbc.gridy = row++;
        txtPhone = UITheme.createTextField();
        formFields.add(txtPhone, gbc);

        gbc.gridy = row++;
        JLabel lblStreet = new JLabel("Street Address:");
        lblStreet.setFont(UITheme.FONT_LABEL);
        formFields.add(lblStreet, gbc);

        gbc.gridy = row++;
        txtStreet = UITheme.createTextField();
        formFields.add(txtStreet, gbc);

        gbc.gridy = row++;
        JLabel lblCity = new JLabel("City & State:");
        lblCity.setFont(UITheme.FONT_LABEL);
        formFields.add(lblCity, gbc);

        gbc.gridy = row++;
        JPanel cityStatePanel = new JPanel(new GridLayout(1, 2, 4, 0));
        cityStatePanel.setBackground(UITheme.COLOR_SURFACE);
        txtCity = UITheme.createTextField();
        txtState = UITheme.createTextField();
        cityStatePanel.add(txtCity);
        cityStatePanel.add(txtState);
        formFields.add(cityStatePanel, gbc);

        gbc.gridy = row++;
        JLabel lblCountry = new JLabel("Country:");
        lblCountry.setFont(UITheme.FONT_LABEL);
        formFields.add(lblCountry, gbc);

        gbc.gridy = row++;
        txtCountry = UITheme.createTextField();
        txtCountry.setText("India");
        formFields.add(txtCountry, gbc);

        formCard.add(formFields, BorderLayout.CENTER);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 8));
        btnPanel.setBackground(UITheme.COLOR_SURFACE);

        JButton btnAdd = UITheme.createPrimaryButton("Register User");
        btnAdd.addActionListener(e -> addUser());

        JButton btnUpdate = UITheme.createPrimaryButton("Update");
        btnUpdate.addActionListener(e -> updateUser());

        JButton btnDelete = UITheme.createDangerButton("Delete");
        btnDelete.addActionListener(e -> deleteUser());

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
        btnSearch.addActionListener(e -> searchUsers());

        JButton btnRefresh = UITheme.createSecondaryButton("Refresh");
        btnRefresh.addActionListener(e -> {
            txtSearch.setText("");
            loadUsers();
        });

        searchBtns.add(btnSearch);
        searchBtns.add(btnRefresh);
        searchRow.add(searchBtns, BorderLayout.EAST);
        tableCard.add(searchRow, BorderLayout.NORTH);

        String[] cols = {"User ID", "Full Name", "Role", "Email", "Phone", "City", "State"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tblUsers = new JTable(tableModel);
        UITheme.formatTable(tblUsers);

        tblUsers.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tblUsers.getSelectedRow() != -1) {
                int r = tblUsers.getSelectedRow();
                int uId = (int) tableModel.getValueAt(r, 0);
                loadSelectedUserToForm(uId);
            }
        });

        JScrollPane sp = new JScrollPane(tblUsers);
        sp.setBorder(BorderFactory.createLineBorder(UITheme.COLOR_BORDER, 1));
        sp.getViewport().setBackground(Color.WHITE);
        tableCard.add(sp, BorderLayout.CENTER);

        centerPanel.add(tableCard, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);
    }

    private void loadUsers() {
        new Thread(() -> {
            try {
                List<User> list = userService.getAllUsers();
                SwingUtilities.invokeLater(() -> populateTable(list));
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> UITheme.showError(this, "Failed to load users:\n" + ex.getMessage()));
            }
        }).start();
    }

    private void populateTable(List<User> list) {
        tableModel.setRowCount(0);
        for (User u : list) {
            tableModel.addRow(new Object[]{
                u.getUserId(),
                u.getFullName(),
                u.getRole(),
                u.getEmail(),
                u.getPhone(),
                u.getCity(),
                u.getState()
            });
        }
    }

    private void loadSelectedUserToForm(int userId) {
        new Thread(() -> {
            try {
                User u = userService.getUserById(userId);
                if (u != null) {
                    SwingUtilities.invokeLater(() -> {
                        txtId.setText(String.valueOf(u.getUserId()));
                        txtFirstName.setText(u.getFirstName());
                        txtLastName.setText(u.getLastName());
                        txtEmail.setText(u.getEmail());
                        txtPhone.setText(u.getPhone());
                        txtStreet.setText(u.getStreet() != null ? u.getStreet() : "");
                        txtCity.setText(u.getCity() != null ? u.getCity() : "");
                        txtState.setText(u.getState() != null ? u.getState() : "");
                        txtCountry.setText(u.getCountry() != null ? u.getCountry() : "India");
                        if ("Administrator".equalsIgnoreCase(u.getRole())) {
                            cmbRole.setSelectedItem("Administrator");
                        } else {
                            cmbRole.setSelectedItem("Tourist");
                        }
                    });
                }
            } catch (Exception ignored) {}
        }).start();
    }

    private void searchUsers() {
        String q = txtSearch.getText().trim();
        new Thread(() -> {
            try {
                List<User> list = userService.searchUsers(q);
                SwingUtilities.invokeLater(() -> populateTable(list));
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> UITheme.showError(this, "Search error:\n" + ex.getMessage()));
            }
        }).start();
    }

    private void addUser() {
        try {
            User u = getUserFromForm();
            String role = (String) cmbRole.getSelectedItem();
            userService.addUser(u, role);
            UITheme.showSuccess(this, "User " + u.getFullName() + " registered successfully as " + role + "!");
            clearFields();
            loadUsers();
            suggestNextId();
        } catch (ValidationException ve) {
            UITheme.showError(this, ve.getMessage());
        } catch (Exception ex) {
            UITheme.showError(this, "Failed to add user:\n" + ex.getMessage());
        }
    }

    private void updateUser() {
        try {
            User u = getUserFromForm();
            userService.updateUser(u);
            UITheme.showSuccess(this, "User #" + u.getUserId() + " details updated successfully!");
            loadUsers();
        } catch (ValidationException ve) {
            UITheme.showError(this, ve.getMessage());
        } catch (Exception ex) {
            UITheme.showError(this, "Failed to update user:\n" + ex.getMessage());
        }
    }

    private void deleteUser() {
        String idText = txtId.getText().trim();
        if (idText.isEmpty()) {
            UITheme.showError(this, "Please select a user to delete.");
            return;
        }

        int uId;
        try {
            uId = Integer.parseInt(idText);
        } catch (NumberFormatException ex) {
            UITheme.showError(this, "User ID must be an integer.");
            return;
        }

        if (!UITheme.showConfirm(this, "Are you sure you want to delete User #" + uId + " and associated roles?")) {
            return;
        }

        try {
            userService.deleteUser(uId);
            UITheme.showSuccess(this, "User #" + uId + " deleted successfully!");
            clearFields();
            loadUsers();
            suggestNextId();
        } catch (ValidationException ve) {
            UITheme.showError(this, ve.getMessage());
        } catch (Exception ex) {
            UITheme.showError(this, "Failed to delete user:\n" + ex.getMessage());
        }
    }

    private User getUserFromForm() throws ValidationException {
        String idStr = txtId.getText().trim();
        if (idStr.isEmpty()) throw new ValidationException("User ID is required.");
        int id;
        try {
            id = Integer.parseInt(idStr);
        } catch (NumberFormatException ex) {
            throw new ValidationException("User ID must be a numeric integer.");
        }

        String fn = txtFirstName.getText().trim();
        String ln = txtLastName.getText().trim();
        String email = txtEmail.getText().trim();
        String phone = txtPhone.getText().trim();
        String street = txtStreet.getText().trim();
        String city = txtCity.getText().trim();
        String state = txtState.getText().trim();
        String country = txtCountry.getText().trim();
        String role = (String) cmbRole.getSelectedItem();

        return new User(id, fn, ln, email, phone, street, city, state, country, role);
    }

    private void clearFields() {
        txtId.setText("");
        txtFirstName.setText("");
        txtLastName.setText("");
        txtEmail.setText("");
        txtPhone.setText("");
        txtStreet.setText("");
        txtCity.setText("");
        txtState.setText("");
        txtCountry.setText("India");
        tblUsers.clearSelection();
        suggestNextId();
    }

    private void suggestNextId() {
        new Thread(() -> {
            try {
                int next = userService.getNextUserId();
                SwingUtilities.invokeLater(() -> {
                    if (txtId.getText().trim().isEmpty()) {
                        txtId.setText(String.valueOf(next));
                    }
                });
            } catch (Exception ignored) {}
        }).start();
    }
}
