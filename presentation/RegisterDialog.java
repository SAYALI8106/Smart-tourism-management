package smarttourism.presentation;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import smarttourism.model.User;
import smarttourism.service.AuthService;
import smarttourism.service.ValidationException;

/**
 * Tier 1 - Presentation Layer: Tourist Self-Registration Dialog.
 * Allows new tourists to register with credentials, address, and contact info.
 */
public class RegisterDialog extends JDialog {

    private final AuthService authService = new AuthService();
    private boolean registrationSuccessful = false;
    private String registeredEmail = "";

    private JTextField txtFirstName;
    private JTextField txtLastName;
    private JTextField txtEmail;
    private JTextField txtPhone;
    private JTextField txtStreet;
    private JTextField txtCity;
    private JTextField txtState;
    private JTextField txtCountry;
    private JPasswordField txtPassword;
    private JPasswordField txtConfirmPassword;
    private JCheckBox chkShowPassword;

    public RegisterDialog(JFrame parent) {
        super(parent, "Register as a Tourist", true);
        initUI();
    }

    private void initUI() {
        setSize(560, 680);
        setLocationRelativeTo(getParent());
        setResizable(false);
        setLayout(new BorderLayout());
        getContentPane().setBackground(UITheme.COLOR_BG);

        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(UITheme.COLOR_PRIMARY_DARK);
        headerPanel.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel titleLbl = new JLabel("Create Tourist Account", SwingConstants.CENTER);
        titleLbl.setFont(UITheme.FONT_TITLE);
        titleLbl.setForeground(Color.WHITE);
        titleLbl.setAlignmentX(CENTER_ALIGNMENT);

        JLabel subLbl = new JLabel("Join Smart Tourism Management System to book and manage tours", SwingConstants.CENTER);
        subLbl.setFont(UITheme.FONT_SUBTITLE);
        subLbl.setForeground(new Color(204, 251, 241));
        subLbl.setAlignmentX(CENTER_ALIGNMENT);

        headerPanel.add(titleLbl);
        headerPanel.add(Box.createVerticalStrut(4));
        headerPanel.add(subLbl);
        add(headerPanel, BorderLayout.NORTH);

        // Form fields in Card
        JPanel card = UITheme.createCardPanel();
        card.setLayout(new GridBagLayout());
        card.setBorder(new EmptyBorder(16, 20, 16, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.weightx = 1.0;

        txtFirstName = UITheme.createTextField();
        txtLastName = UITheme.createTextField();
        txtEmail = UITheme.createTextField();
        txtPhone = UITheme.createTextField();
        txtStreet = UITheme.createTextField();
        txtCity = UITheme.createTextField();
        txtState = UITheme.createTextField();
        txtCountry = UITheme.createTextField();
        txtCountry.setText("India");

        txtPassword = new JPasswordField();
        txtPassword.setFont(UITheme.FONT_REGULAR);
        txtPassword.setBorder(txtFirstName.getBorder());

        txtConfirmPassword = new JPasswordField();
        txtConfirmPassword.setFont(UITheme.FONT_REGULAR);
        txtConfirmPassword.setBorder(txtFirstName.getBorder());

        chkShowPassword = new JCheckBox("Show Passwords");
        chkShowPassword.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkShowPassword.setBackground(UITheme.COLOR_SURFACE);
        chkShowPassword.setForeground(UITheme.COLOR_TEXT_MUTED);
        chkShowPassword.addActionListener(e -> {
            char echo = chkShowPassword.isSelected() ? (char) 0 : '•';
            txtPassword.setEchoChar(echo);
            txtConfirmPassword.setEchoChar(echo);
        });

        int row = 0;
        // First & Last Name
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1;
        card.add(createFieldPanel("First Name *", txtFirstName), gbc);
        gbc.gridx = 1; gbc.gridy = row++;
        card.add(createFieldPanel("Last Name *", txtLastName), gbc);

        // Email & Phone
        gbc.gridx = 0; gbc.gridy = row;
        card.add(createFieldPanel("Email Address *", txtEmail), gbc);
        gbc.gridx = 1; gbc.gridy = row++;
        card.add(createFieldPanel("Phone Number *", txtPhone), gbc);

        // Street Address (full width)
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        card.add(createFieldPanel("Street Address", txtStreet), gbc);

        // City & State
        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = row;
        card.add(createFieldPanel("City", txtCity), gbc);
        gbc.gridx = 1; gbc.gridy = row++;
        card.add(createFieldPanel("State", txtState), gbc);

        // Country
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        card.add(createFieldPanel("Country", txtCountry), gbc);

        // Passwords
        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = row;
        card.add(createFieldPanel("Password (min 6 chars) *", txtPassword), gbc);
        gbc.gridx = 1; gbc.gridy = row++;
        card.add(createFieldPanel("Confirm Password *", txtConfirmPassword), gbc);

        // Show password checkbox
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        card.add(chkShowPassword, gbc);

        add(card, BorderLayout.CENTER);

        // Bottom Actions
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        bottomPanel.setBackground(UITheme.COLOR_BG);

        JButton btnCancel = UITheme.createSecondaryButton("Cancel");
        btnCancel.addActionListener(e -> dispose());

        JButton btnSubmit = UITheme.createPrimaryButton("Create Account");
        btnSubmit.addActionListener(e -> performRegistration());

        bottomPanel.add(btnCancel);
        bottomPanel.add(btnSubmit);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JPanel createFieldPanel(String label, JTextField field) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(UITheme.COLOR_SURFACE);

        JLabel lbl = new JLabel(label);
        lbl.setFont(UITheme.FONT_LABEL);
        lbl.setForeground(UITheme.COLOR_TEXT_MAIN);

        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        field.setPreferredSize(new Dimension(200, 34));

        p.add(lbl);
        p.add(Box.createVerticalStrut(3));
        p.add(field);
        return p;
    }

    private void performRegistration() {
        String fName = txtFirstName.getText();
        String lName = txtLastName.getText();
        String email = txtEmail.getText();
        String phone = txtPhone.getText();
        String street = txtStreet.getText();
        String city = txtCity.getText();
        String state = txtState.getText();
        String country = txtCountry.getText();
        String password = new String(txtPassword.getPassword());
        String confirm = new String(txtConfirmPassword.getPassword());

        try {
            User registered = authService.registerTourist(
                fName, lName, email, phone, street, city, state, country, password, confirm
            );
            registrationSuccessful = true;
            registeredEmail = registered.getEmail();
            UITheme.showSuccess(this, "Registration Successful!\nWelcome, " + registered.getFullName() + "!\nYou can now log in with your email.");
            dispose();
        } catch (ValidationException ve) {
            UITheme.showError(this, ve.getMessage());
        } catch (Exception ex) {
            UITheme.showError(this, "Registration Error: " + ex.getMessage());
        }
    }

    public boolean isRegistrationSuccessful() {
        return registrationSuccessful;
    }

    public String getRegisteredEmail() {
        return registeredEmail;
    }
}
