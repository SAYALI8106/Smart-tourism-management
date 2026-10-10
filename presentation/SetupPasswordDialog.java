package smarttourism.presentation;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
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
import smarttourism.service.AuthService;
import smarttourism.service.ValidationException;

/**
 * Tier 1 - Presentation Layer: First-time Password Setup Dialog.
 * Allows accounts in existing USER database without credentials to activate their password safely.
 */
public class SetupPasswordDialog extends JDialog {

    private final AuthService authService = new AuthService();

    private JTextField txtIdentifier;
    private JPasswordField txtNewPassword;
    private JPasswordField txtConfirmPassword;
    private JCheckBox chkShow;

    public SetupPasswordDialog(JFrame parent) {
        super(parent, "First-Time Password Setup", true);
        initUI();
    }

    private void initUI() {
        setSize(440, 420);
        setLocationRelativeTo(getParent());
        setResizable(false);
        setLayout(new BorderLayout());
        getContentPane().setBackground(UITheme.COLOR_BG);

        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(UITheme.COLOR_PRIMARY_DARK);
        headerPanel.setBorder(new EmptyBorder(14, 20, 14, 20));

        JLabel titleLbl = new JLabel("First-Time Password Setup", SwingConstants.CENTER);
        titleLbl.setFont(UITheme.FONT_TITLE);
        titleLbl.setForeground(Color.WHITE);
        titleLbl.setAlignmentX(CENTER_ALIGNMENT);

        JLabel subLbl = new JLabel("Activate your pre-existing database account", SwingConstants.CENTER);
        subLbl.setFont(UITheme.FONT_SUBTITLE);
        subLbl.setForeground(new Color(204, 251, 241));
        subLbl.setAlignmentX(CENTER_ALIGNMENT);

        headerPanel.add(titleLbl);
        headerPanel.add(Box.createVerticalStrut(4));
        headerPanel.add(subLbl);
        add(headerPanel, BorderLayout.NORTH);

        // Card Panel
        JPanel card = UITheme.createCardPanel();
        card.setLayout(new GridBagLayout());
        card.setBorder(new EmptyBorder(16, 20, 16, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.weightx = 1.0;
        gbc.gridx = 0;

        txtIdentifier = UITheme.createTextField();
        txtNewPassword = new JPasswordField();
        txtNewPassword.setFont(UITheme.FONT_REGULAR);
        txtNewPassword.setBorder(txtIdentifier.getBorder());

        txtConfirmPassword = new JPasswordField();
        txtConfirmPassword.setFont(UITheme.FONT_REGULAR);
        txtConfirmPassword.setBorder(txtIdentifier.getBorder());

        chkShow = new JCheckBox("Show Passwords");
        chkShow.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkShow.setBackground(UITheme.COLOR_SURFACE);
        chkShow.setForeground(UITheme.COLOR_TEXT_MUTED);
        chkShow.addActionListener(e -> {
            char echo = chkShow.isSelected() ? (char) 0 : '•';
            txtNewPassword.setEchoChar(echo);
            txtConfirmPassword.setEchoChar(echo);
        });

        int row = 0;
        gbc.gridy = row++;
        card.add(createFieldPanel("Registered Email or User ID *", txtIdentifier), gbc);

        gbc.gridy = row++;
        card.add(createFieldPanel("New Password (min 6 characters) *", txtNewPassword), gbc);

        gbc.gridy = row++;
        card.add(createFieldPanel("Confirm New Password *", txtConfirmPassword), gbc);

        gbc.gridy = row++;
        card.add(chkShow, gbc);

        add(card, BorderLayout.CENTER);

        // Actions
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        bottom.setBackground(UITheme.COLOR_BG);

        JButton btnCancel = UITheme.createSecondaryButton("Cancel");
        btnCancel.addActionListener(e -> dispose());

        JButton btnSave = UITheme.createPrimaryButton("Activate Account");
        btnSave.addActionListener(e -> performSetup());

        bottom.add(btnCancel);
        bottom.add(btnSave);
        add(bottom, BorderLayout.SOUTH);
    }

    private JPanel createFieldPanel(String label, JTextField field) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(UITheme.COLOR_SURFACE);

        JLabel lbl = new JLabel(label);
        lbl.setFont(UITheme.FONT_LABEL);
        lbl.setForeground(UITheme.COLOR_TEXT_MAIN);

        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        field.setPreferredSize(new Dimension(280, 34));

        p.add(lbl);
        p.add(Box.createVerticalStrut(3));
        p.add(field);
        return p;
    }

    private void performSetup() {
        String idOrEmail = txtIdentifier.getText();
        String newPass = new String(txtNewPassword.getPassword());
        String confirm = new String(txtConfirmPassword.getPassword());

        try {
            authService.setupFirstTimePassword(idOrEmail, newPass, confirm);
            UITheme.showSuccess(this, "Account successfully activated!\nYou can now log in using your new credentials.");
            dispose();
        } catch (ValidationException ve) {
            UITheme.showError(this, ve.getMessage());
        } catch (Exception ex) {
            UITheme.showError(this, "Activation Error: " + ex.getMessage());
        }
    }
}
