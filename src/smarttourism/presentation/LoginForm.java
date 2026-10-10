package smarttourism.presentation;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.sql.Connection;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
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
 * Provides Email/User ID & Password authentication with BCrypt hashing,
 * Tourist Self-Registration, and role-based dashboard redirection.
 */
public class LoginForm extends JFrame {

    private final AuthService authService = new AuthService();

    private JTextField txtEmail;
    private JPasswordField txtPassword;
    private JCheckBox chkShowPassword;
    private JLabel lblStatus;

    public LoginForm() {
        super("Smart Tourism Management System — Login");
        initUI();
        checkDatabaseStatus();
    }

    private void initUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(480, 520);
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
        centerWrapper.setBorder(new EmptyBorder(16, 24, 16, 24));

        JPanel card = UITheme.createCardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(420, 360));
        card.setBorder(new EmptyBorder(20, 24, 20, 24));

        // Status indicator
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        statusPanel.setBackground(UITheme.COLOR_SURFACE);
        lblStatus = new JLabel("System Status: Connecting...");
        lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 12));
        statusPanel.add(lblStatus);
        card.add(statusPanel);
        card.add(Box.createVerticalStrut(16));

        // Email / User ID Field
        JLabel lblEmail = new JLabel("Email or User ID:");
        lblEmail.setFont(UITheme.FONT_LABEL);
        lblEmail.setForeground(UITheme.COLOR_TEXT_MAIN);
        txtEmail = UITheme.createTextField();
        txtEmail.setText(""); // Clean start without pre-filled credentials
        txtEmail.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        // Password Field
        JLabel lblPass = new JLabel("Password:");
        lblPass.setFont(UITheme.FONT_LABEL);
        lblPass.setForeground(UITheme.COLOR_TEXT_MAIN);
        txtPassword = new JPasswordField();
        txtPassword.setFont(UITheme.FONT_REGULAR);
        txtPassword.setText(""); // Clean start without pre-filled credentials
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
        JButton btnLogin = UITheme.createPrimaryButton("Login");
        btnLogin.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btnLogin.addActionListener(e -> performLogin());

        // Register as Tourist Button
        JButton btnRegister = UITheme.createSecondaryButton("Register as Tourist");
        btnRegister.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btnRegister.addActionListener(e -> openRegistration());

        // Assemble card
        card.add(lblEmail);
        card.add(Box.createVerticalStrut(4));
        card.add(txtEmail);
        card.add(Box.createVerticalStrut(10));
        card.add(lblPass);
        card.add(Box.createVerticalStrut(4));
        card.add(txtPassword);
        card.add(Box.createVerticalStrut(4));
        card.add(chkShowPassword);
        card.add(Box.createVerticalStrut(16));
        card.add(btnLogin);
        card.add(Box.createVerticalStrut(10));
        card.add(btnRegister);

        centerWrapper.add(card, new GridBagConstraints());
        add(centerWrapper, BorderLayout.CENTER);
    }

    private void checkDatabaseStatus() {
        new Thread(() -> {
            try (Connection c = DBConnection.getConnection()) {
                SwingUtilities.invokeLater(() -> {
                    lblStatus.setText("System Status: Online");
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

    private void performLogin() {
        String identifier = txtEmail.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (identifier.isEmpty()) {
            UITheme.showError(this, "Please enter your Email or User ID.");
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
}
