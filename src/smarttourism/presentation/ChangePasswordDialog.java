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
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import smarttourism.service.AuthService;
import smarttourism.service.SessionManager;
import smarttourism.service.ValidationException;

/**
 * Tier 1 - Presentation Layer: Password Change Dialog.
 * Enforces requirement of entering the current password before setting a new one.
 */
public class ChangePasswordDialog extends JDialog {

    private final AuthService authService = new AuthService();

    private JPasswordField txtCurrentPassword;
    private JPasswordField txtNewPassword;
    private JPasswordField txtConfirmPassword;
    private JCheckBox chkShow;

    public ChangePasswordDialog(JFrame parent) {
        super(parent, "Change Password", true);
        initUI();
    }

    private void initUI() {
        setSize(420, 400);
        setLocationRelativeTo(getParent());
        setResizable(false);
        setLayout(new BorderLayout());
        getContentPane().setBackground(UITheme.COLOR_BG);

        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(UITheme.COLOR_PRIMARY_DARK);
        headerPanel.setBorder(new EmptyBorder(14, 20, 14, 20));

        JLabel titleLbl = new JLabel("Change Password", SwingConstants.CENTER);
        titleLbl.setFont(UITheme.FONT_TITLE);
        titleLbl.setForeground(Color.WHITE);
        titleLbl.setAlignmentX(CENTER_ALIGNMENT);

        String userName = SessionManager.isLoggedIn() ? SessionManager.getCurrentUser().getFullName() : "User";
        JLabel subLbl = new JLabel("Account: " + userName, SwingConstants.CENTER);
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

        txtCurrentPassword = new JPasswordField();
        txtCurrentPassword.setFont(UITheme.FONT_REGULAR);
        txtCurrentPassword.setBorder(UITheme.createTextField().getBorder());

        txtNewPassword = new JPasswordField();
        txtNewPassword.setFont(UITheme.FONT_REGULAR);
        txtNewPassword.setBorder(UITheme.createTextField().getBorder());

        txtConfirmPassword = new JPasswordField();
        txtConfirmPassword.setFont(UITheme.FONT_REGULAR);
        txtConfirmPassword.setBorder(UITheme.createTextField().getBorder());

        chkShow = new JCheckBox("Show Passwords");
        chkShow.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkShow.setBackground(UITheme.COLOR_SURFACE);
        chkShow.setForeground(UITheme.COLOR_TEXT_MUTED);
        chkShow.addActionListener(e -> {
            char echo = chkShow.isSelected() ? (char) 0 : '•';
            txtCurrentPassword.setEchoChar(echo);
            txtNewPassword.setEchoChar(echo);
            txtConfirmPassword.setEchoChar(echo);
        });

        int row = 0;
        gbc.gridy = row++;
        card.add(createFieldPanel("Current Password *", txtCurrentPassword), gbc);

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

        JButton btnSave = UITheme.createPrimaryButton("Update Password");
        btnSave.addActionListener(e -> performChangePassword());

        bottom.add(btnCancel);
        bottom.add(btnSave);
        add(bottom, BorderLayout.SOUTH);
    }

    private JPanel createFieldPanel(String label, JPasswordField field) {
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

    private void performChangePassword() {
        String current = new String(txtCurrentPassword.getPassword());
        String newPass = new String(txtNewPassword.getPassword());
        String confirm = new String(txtConfirmPassword.getPassword());

        try {
            authService.changePassword(current, newPass, confirm);
            UITheme.showSuccess(this, "Password updated successfully!");
            dispose();
        } catch (ValidationException ve) {
            UITheme.showError(this, ve.getMessage());
        } catch (Exception ex) {
            UITheme.showError(this, "Error updating password: " + ex.getMessage());
        }
    }
}
