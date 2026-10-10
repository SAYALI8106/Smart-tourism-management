package smarttourism.presentation;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.sql.Connection;
import java.util.List;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import smarttourism.dao.DBConnection;
import smarttourism.model.User;
import smarttourism.service.AuthService;
import smarttourism.service.ValidationException;

/**
 * Tier 1 - Presentation Layer: Login and Portal Entry Screen.
 * Provides Email & Password authentication with BCrypt hashing,
 * Tourist Self-Registration, First-Time Setup, and role-based dashboard redirection.
 */
public class LoginForm extends JFrame {

    private final AuthService authService = new AuthService();

    private JTextField txtEmail;
    private JPasswordField txtPassword;
    private JCheckBox chkShowPassword;
    private JLabel lblStatus;
    private JComboBox<User> cmbQuickUsers;

    public LoginForm() {
        super("Smart Tourism Management System — Login");
        initUI();
        loadDemoUsers();
        checkDatabaseStatus();
    }

    private void initUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(580, 640);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(UITheme.COLOR_BG);
        setLayout(new BorderLayout());

        // Header Panel
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(UITheme.COLOR_PRIMARY_DARK);
        headerPanel.setBorder(new EmptyBorder(22, 24, 22, 24));

        JLabel titleLbl = new JLabel("Smart Tourism Management System", SwingConstants.CENTER);
        titleLbl.setFont(UITheme.FONT_TITLE);
        titleLbl.setForeground(Color.WHITE);
        titleLbl.setAlignmentX(CENTER_ALIGNMENT);

        JLabel subtitleLbl = new JLabel("Three-Tier Database Application Portal", SwingConstants.CENTER);
        subtitleLbl.setFont(UITheme.FONT_SUBTITLE);
        subtitleLbl.setForeground(new Color(204, 251, 241));
        subtitleLbl.setAlignmentX(CENTER_ALIGNMENT);

        headerPanel.add(titleLbl);
        headerPanel.add(Box.createVerticalStrut(4));
        headerPanel.add(subtitleLbl);
        add(headerPanel, BorderLayout.NORTH);

        // Center Card Panel
        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setBackground(UITheme.COLOR_BG);
        centerWrapper.setBorder(new EmptyBorder(12, 24, 12, 24));

        JPanel card = UITheme.createCardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(500, 480));
        card.setBorder(new EmptyBorder(16, 20, 16, 20));

        // Status indicator
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        statusPanel.setBackground(UITheme.COLOR_SURFACE);
        lblStatus = new JLabel("System Status: Connecting...");
        lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 12));
        statusPanel.add(lblStatus);
        card.add(statusPanel);
        card.add(Box.createVerticalStrut(12));

        // Email / User ID Field
        JLabel lblEmail = new JLabel("Email Address / User ID:");
        lblEmail.setFont(UITheme.FONT_LABEL);
        lblEmail.setForeground(UITheme.COLOR_TEXT_MAIN);
        txtEmail = UITheme.createTextField();
        txtEmail.setText("priya@example.com");
        txtEmail.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        // Password Field
        JLabel lblPass = new JLabel("Password:");
        lblPass.setFont(UITheme.FONT_LABEL);
        lblPass.setForeground(UITheme.COLOR_TEXT_MAIN);
        txtPassword = new JPasswordField();
        txtPassword.setFont(UITheme.FONT_REGULAR);
        txtPassword.setText("admin123");
        txtPassword.setBorder(txtEmail.getBorder());
        txtPassword.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        // Show Password Checkbox
        chkShowPassword = new JCheckBox("Show Password");
        chkShowPassword.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkShowPassword.setBackground(UITheme.COLOR_SURFACE);
        chkShowPassword.setForeground(UITheme.COLOR_TEXT_MUTED);
        chkShowPassword.addActionListener(e -> {
            txtPassword.setEchoChar(chkShowPassword.isSelected() ? (char) 0 : '•');
        });

        // Login Button
        JButton btnLogin = UITheme.createPrimaryButton("Login to Portal");
        btnLogin.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btnLogin.addActionListener(e -> performLogin());

        // Register as Tourist Button & Setup Button
        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        actionRow.setBackground(UITheme.COLOR_SURFACE);
        JButton btnRegister = UITheme.createSecondaryButton("Register as Tourist");
        btnRegister.addActionListener(e -> openRegistration());

        JButton btnSetup = UITheme.createSecondaryButton("First-Time Setup");
        btnSetup.addActionListener(e -> openFirstTimeSetup());
        actionRow.add(btnRegister);
        actionRow.add(btnSetup);

        // Quick Demo Selector
        JLabel lblQuick = new JLabel("Demo Accounts Quick Select:");
        lblQuick.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblQuick.setForeground(UITheme.COLOR_TEXT_MUTED);
        lblQuick.setAlignmentX(CENTER_ALIGNMENT);

        cmbQuickUsers = new JComboBox<>();
        cmbQuickUsers.setFont(UITheme.FONT_REGULAR);
        cmbQuickUsers.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        cmbQuickUsers.addActionListener(e -> {
            User selected = (User) cmbQuickUsers.getSelectedItem();
            if (selected != null) {
                txtEmail.setText(selected.getEmail());
                if ("Administrator".equalsIgnoreCase(selected.getRole())) {
                    txtPassword.setText("admin123");
                } else {
                    txtPassword.setText("tourist123");
                }
            }
        });

        JPanel quickBtnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        quickBtnRow.setBackground(UITheme.COLOR_SURFACE);
        JButton btnAdminQuick = UITheme.createSecondaryButton("Admin (Priya)");
        btnAdminQuick.addActionListener(e -> {
            txtEmail.setText("priya@example.com");
            txtPassword.setText("admin123");
            performLogin();
        });
        JButton btnTouristQuick = UITheme.createSecondaryButton("Tourist (Aarav)");
        btnTouristQuick.addActionListener(e -> {
            txtEmail.setText("aarav@example.com");
            txtPassword.setText("tourist123");
            performLogin();
        });
        quickBtnRow.add(btnAdminQuick);
        quickBtnRow.add(btnTouristQuick);

        // Assemble card
        card.add(lblEmail);
        card.add(Box.createVerticalStrut(4));
        card.add(txtEmail);
        card.add(Box.createVerticalStrut(8));
        card.add(lblPass);
        card.add(Box.createVerticalStrut(4));
        card.add(txtPassword);
        card.add(Box.createVerticalStrut(4));
        card.add(chkShowPassword);
        card.add(Box.createVerticalStrut(12));
        card.add(btnLogin);
        card.add(Box.createVerticalStrut(10));
        card.add(actionRow);
        card.add(Box.createVerticalStrut(12));
        card.add(lblQuick);
        card.add(Box.createVerticalStrut(4));
        card.add(cmbQuickUsers);
        card.add(Box.createVerticalStrut(6));
        card.add(quickBtnRow);

        centerWrapper.add(card, new GridBagConstraints());
        add(centerWrapper, BorderLayout.CENTER);
    }

    private void checkDatabaseStatus() {
        new Thread(() -> {
            try (Connection c = DBConnection.getConnection()) {
                SwingUtilities.invokeLater(() -> {
                    lblStatus.setText("System Status: Online (MySQL smart_tourism_db)");
                    lblStatus.setForeground(UITheme.COLOR_SUCCESS);
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    lblStatus.setText("System Status: Offline");
                    lblStatus.setForeground(UITheme.COLOR_DANGER);
                });
            }
        }).start();
    }

    private void loadDemoUsers() {
        new Thread(() -> {
            try {
                List<User> admins = authService.getAvailableAdmins();
                List<User> tourists = authService.getAvailableTourists();
                SwingUtilities.invokeLater(() -> {
                    cmbQuickUsers.removeAllItems();
                    for (User u : admins) cmbQuickUsers.addItem(u);
                    for (User u : tourists) cmbQuickUsers.addItem(u);
                });
            } catch (Exception ignored) {
            }
        }).start();
    }

    private void performLogin() {
        String identifier = txtEmail.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (identifier.isEmpty()) {
            UITheme.showError(this, "Please enter your Email Address or User ID.");
            return;
        }
        if (password.isEmpty()) {
            UITheme.showError(this, "Please enter your password.");
            return;
        }

        try {
            User loggedIn = authService.login(identifier, password);
            dispose();

            SwingUtilities.invokeLater(() -> {
                if ("Administrator".equalsIgnoreCase(loggedIn.getRole())) {
                    new Dashboard(loggedIn).setVisible(true);
                } else {
                    new TouristDashboard(loggedIn).setVisible(true);
                }
            });
        } catch (ValidationException ve) {
            UITheme.showError(this, ve.getMessage());
        } catch (Exception ex) {
            UITheme.showError(this, "Login Failed: " + ex.getMessage());
        }
    }

    private void openRegistration() {
        RegisterDialog dlg = new RegisterDialog(this);
        dlg.setVisible(true);
        if (dlg.isRegistrationSuccessful()) {
            txtEmail.setText(dlg.getRegisteredEmail());
            txtPassword.setText("");
            txtPassword.requestFocus();
        }
    }

    private void openFirstTimeSetup() {
        SetupPasswordDialog dlg = new SetupPasswordDialog(this);
        dlg.setVisible(true);
    }
}
