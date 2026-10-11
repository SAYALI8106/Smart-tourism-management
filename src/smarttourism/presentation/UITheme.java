package smarttourism.presentation;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

/**
 * Modern styling utilities and color palette for the Smart Tourism Management System.
 */
public final class UITheme {

    // Color Palette
    public static final Color COLOR_BG = new Color(248, 250, 252);          // Slate 50
    public static final Color COLOR_SURFACE = new Color(255, 255, 255);     // White
    public static final Color COLOR_PRIMARY = new Color(13, 148, 136);      // Teal 600
    public static final Color COLOR_PRIMARY_DARK = new Color(15, 118, 110); // Teal 700
    public static final Color COLOR_ACCENT = new Color(2, 132, 199);        // Sky 600
    public static final Color COLOR_TEXT_MAIN = new Color(15, 23, 42);      // Slate 900
    public static final Color COLOR_TEXT_MUTED = new Color(100, 116, 139);  // Slate 500
    public static final Color COLOR_BORDER = new Color(226, 232, 240);      // Slate 200
    public static final Color COLOR_BORDER_ACTIVE = new Color(13, 148, 136);// Teal
    public static final Color COLOR_SUCCESS = new Color(22, 163, 74);       // Green 600
    public static final Color COLOR_WARNING = new Color(217, 119, 6);       // Amber 600
    public static final Color COLOR_DANGER = new Color(220, 38, 38);        // Red 600
    public static final Color COLOR_DANGER_DARK = new Color(185, 28, 28);
    public static final Color COLOR_SECONDARY = new Color(100, 116, 139);
    public static final Color COLOR_CARD_BG = new Color(255, 255, 255);
    public static final Color COLOR_ZEBRA = new Color(248, 250, 252);

    // Fonts
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font FONT_LABEL = new Font("Segoe UI", Font.BOLD, 12);
    public static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BUTTON = new Font("Segoe UI", Font.BOLD, 12);
    public static final Font FONT_STAT_NUM = new Font("Segoe UI", Font.BOLD, 24);
    public static final Font FONT_STAT_LBL = new Font("Segoe UI", Font.PLAIN, 12);

    private UITheme() {}

    /**
     * Creates a styled modern primary button.
     */
    public static JButton createPrimaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BUTTON);
        btn.setBackground(COLOR_PRIMARY);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setMargin(new Insets(8, 16, 8, 16));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(COLOR_PRIMARY_DARK);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(COLOR_PRIMARY);
            }
        });
        return btn;
    }

    /**
     * Creates a styled modern secondary / cancel / refresh button.
     */
    public static JButton createSecondaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BUTTON);
        btn.setBackground(new Color(241, 245, 249)); // Slate 100
        btn.setForeground(COLOR_TEXT_MAIN);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createLineBorder(COLOR_BORDER, 1));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setMargin(new Insets(8, 14, 8, 14));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(226, 232, 240));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(241, 245, 249));
            }
        });
        return btn;
    }

    /**
     * Creates a styled modern danger / delete button.
     */
    public static JButton createDangerButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BUTTON);
        btn.setBackground(COLOR_DANGER);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setMargin(new Insets(8, 16, 8, 16));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(COLOR_DANGER_DARK);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(COLOR_DANGER);
            }
        });
        return btn;
    }

    /**
     * Creates a styled card panel with subtle border.
     */
    public static JPanel createCardPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(COLOR_SURFACE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_BORDER, 1),
            new EmptyBorder(16, 16, 16, 16)
        ));
        return panel;
    }

    /**
     * Formats and styles a JTable with modern look.
     * Enforces a custom opaque header renderer to guarantee column headers are always
     * clearly visible across all Look and Feels (avoiding native white-on-white bug).
     */
    public static void formatTable(JTable table) {
        table.setFont(FONT_REGULAR);
        table.setRowHeight(28);
        table.setGridColor(COLOR_BORDER);
        table.setShowGrid(true);
        table.setSelectionBackground(new Color(204, 251, 241)); // Teal 100
        table.setSelectionForeground(COLOR_TEXT_MAIN);

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(COLOR_PRIMARY_DARK);
        header.setForeground(Color.WHITE);
        header.setOpaque(true);
        header.setPreferredSize(new Dimension(header.getWidth(), 34));
        header.setReorderingAllowed(false);

        // Custom Header Renderer ensuring dark teal background and crisp white text
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, col);
                lbl.setOpaque(true);
                lbl.setBackground(COLOR_PRIMARY_DARK); // Deep Teal #0F766E
                lbl.setForeground(Color.WHITE);        // Crisp White Text #FFFFFF
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
                lbl.setHorizontalAlignment(SwingConstants.LEFT);
                lbl.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 2, 1, new Color(13, 148, 136)),
                    new EmptyBorder(6, 10, 6, 10)
                ));
                return lbl;
            }
        });

        // Default alternating row renderer
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int col) {
                Component c = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, col);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : COLOR_ZEBRA);
                    c.setForeground(COLOR_TEXT_MAIN);
                }
                setBorder(new EmptyBorder(0, 8, 0, 8));
                return c;
            }
        });
    }

    /**
     * Creates a labeled text input field.
     */
    public static JTextField createTextField() {
        JTextField tf = new JTextField();
        tf.setFont(FONT_REGULAR);
        tf.setForeground(COLOR_TEXT_MAIN);
        Border line = BorderFactory.createLineBorder(COLOR_BORDER, 1);
        Border pad = new EmptyBorder(6, 10, 6, 10);
        tf.setBorder(new CompoundBorder(line, pad));
        return tf;
    }

    public static void showError(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Validation / System Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void showSuccess(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    public static boolean showConfirm(Component parent, String message) {
        int opt = JOptionPane.showConfirmDialog(parent, message, "Confirm Action", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        return opt == JOptionPane.YES_OPTION;
    }
}
