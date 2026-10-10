package smarttourism.presentation;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
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
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import smarttourism.model.Destination;
import smarttourism.service.DestinationService;
import smarttourism.service.ValidationException;

/**
 * Tier 1 - Presentation Layer: Destination Management Form.
 */
public class DestinationForm extends JFrame {

    private final DestinationService destinationService = new DestinationService();

    private JTextField txtId;
    private JTextField txtName;
    private JTextField txtLocation;
    private JTextArea txtDescription;
    private JTextField txtSearch;

    private JTable tblDestinations;
    private DefaultTableModel tableModel;

    public DestinationForm() {
        super("Destination Management — Smart Tourism");
        initUI();
        loadDestinations();
        suggestNextId();
    }

    private void initUI() {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(950, 620);
        setLocationRelativeTo(null);
        getContentPane().setBackground(UITheme.COLOR_BG);
        setLayout(new BorderLayout(0, 10));

        // Header Banner
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UITheme.COLOR_PRIMARY);
        headerPanel.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel titleLbl = new JLabel("Destination Management");
        titleLbl.setFont(UITheme.FONT_TITLE);
        titleLbl.setForeground(Color.WHITE);

        JLabel subLbl = new JLabel("Add, edit, search, and manage tourist destinations");
        subLbl.setFont(UITheme.FONT_SUBTITLE);
        subLbl.setForeground(new Color(204, 251, 241));

        headerPanel.add(titleLbl, BorderLayout.WEST);
        headerPanel.add(subLbl, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Center Content: Left Form Panel, Right Table Panel
        JPanel centerPanel = new JPanel(new BorderLayout(16, 0));
        centerPanel.setBackground(UITheme.COLOR_BG);
        centerPanel.setBorder(new EmptyBorder(0, 16, 16, 16));

        // LEFT: Form Inputs Card
        JPanel formCard = UITheme.createCardPanel();
        formCard.setLayout(new BorderLayout());
        formCard.setPreferredSize(new Dimension(340, 0));

        JPanel formFields = new JPanel(new GridBagLayout());
        formFields.setBackground(UITheme.COLOR_SURFACE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 4, 6, 4);
        gbc.gridx = 0;
        gbc.weightx = 1.0;

        int row = 0;
        gbc.gridy = row++;
        JLabel lblId = new JLabel("Destination ID:");
        lblId.setFont(UITheme.FONT_LABEL);
        formFields.add(lblId, gbc);

        gbc.gridy = row++;
        txtId = UITheme.createTextField();
        formFields.add(txtId, gbc);

        gbc.gridy = row++;
        JLabel lblName = new JLabel("Destination Name:");
        lblName.setFont(UITheme.FONT_LABEL);
        formFields.add(lblName, gbc);

        gbc.gridy = row++;
        txtName = UITheme.createTextField();
        formFields.add(txtName, gbc);

        gbc.gridy = row++;
        JLabel lblLoc = new JLabel("Location (e.g. Goa, India):");
        lblLoc.setFont(UITheme.FONT_LABEL);
        formFields.add(lblLoc, gbc);

        gbc.gridy = row++;
        txtLocation = UITheme.createTextField();
        formFields.add(txtLocation, gbc);

        gbc.gridy = row++;
        JLabel lblDesc = new JLabel("Description:");
        lblDesc.setFont(UITheme.FONT_LABEL);
        formFields.add(lblDesc, gbc);

        gbc.gridy = row++;
        txtDescription = new JTextArea(4, 20);
        txtDescription.setFont(UITheme.FONT_REGULAR);
        txtDescription.setLineWrap(true);
        txtDescription.setWrapStyleWord(true);
        JScrollPane descScroll = new JScrollPane(txtDescription);
        descScroll.setBorder(BorderFactory.createLineBorder(UITheme.COLOR_BORDER, 1));
        formFields.add(descScroll, gbc);

        formCard.add(formFields, BorderLayout.CENTER);

        // Buttons row
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 8));
        btnPanel.setBackground(UITheme.COLOR_SURFACE);

        JButton btnAdd = UITheme.createPrimaryButton("Save / Add");
        btnAdd.addActionListener(e -> addDestination());

        JButton btnUpdate = UITheme.createPrimaryButton("Update");
        btnUpdate.addActionListener(e -> updateDestination());

        JButton btnDelete = UITheme.createDangerButton("Delete");
        btnDelete.addActionListener(e -> deleteDestination());

        JButton btnClear = UITheme.createSecondaryButton("Clear");
        btnClear.addActionListener(e -> clearFields());

        btnPanel.add(btnAdd);
        btnPanel.add(btnUpdate);
        btnPanel.add(btnDelete);
        btnPanel.add(btnClear);
        formCard.add(btnPanel, BorderLayout.SOUTH);

        centerPanel.add(formCard, BorderLayout.WEST);

        // RIGHT: Search Bar and Destinations JTable
        JPanel tableCard = UITheme.createCardPanel();
        tableCard.setLayout(new BorderLayout(0, 10));

        // Search Bar Row
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
        btnSearch.addActionListener(e -> searchDestinations());

        JButton btnRefresh = UITheme.createSecondaryButton("Refresh");
        btnRefresh.addActionListener(e -> {
            txtSearch.setText("");
            loadDestinations();
        });

        searchBtns.add(btnSearch);
        searchBtns.add(btnRefresh);
        searchRow.add(searchBtns, BorderLayout.EAST);
        tableCard.add(searchRow, BorderLayout.NORTH);

        // Table
        String[] cols = {"ID", "Destination Name", "Location", "Description"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tblDestinations = new JTable(tableModel);
        UITheme.formatTable(tblDestinations);

        tblDestinations.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tblDestinations.getSelectedRow() != -1) {
                int rowIdx = tblDestinations.getSelectedRow();
                txtId.setText(String.valueOf(tableModel.getValueAt(rowIdx, 0)));
                txtName.setText(String.valueOf(tableModel.getValueAt(rowIdx, 1)));
                txtLocation.setText(String.valueOf(tableModel.getValueAt(rowIdx, 2)));
                Object d = tableModel.getValueAt(rowIdx, 3);
                txtDescription.setText(d != null ? d.toString() : "");
            }
        });

        JScrollPane sp = new JScrollPane(tblDestinations);
        sp.setBorder(BorderFactory.createLineBorder(UITheme.COLOR_BORDER, 1));
        sp.getViewport().setBackground(Color.WHITE);
        tableCard.add(sp, BorderLayout.CENTER);

        centerPanel.add(tableCard, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);
    }

    private void loadDestinations() {
        new Thread(() -> {
            try {
                List<Destination> list = destinationService.getAllDestinations();
                SwingUtilities.invokeLater(() -> populateTable(list));
            } catch (SQLException ex) {
                SwingUtilities.invokeLater(() -> UITheme.showError(this, "Failed to load destinations:\n" + ex.getMessage()));
            }
        }).start();
    }

    private void populateTable(List<Destination> list) {
        tableModel.setRowCount(0);
        for (Destination d : list) {
            tableModel.addRow(new Object[]{
                d.getDestinationId(),
                d.getName(),
                d.getLocation(),
                d.getDescription()
            });
        }
    }

    private void searchDestinations() {
        String q = txtSearch.getText().trim();
        new Thread(() -> {
            try {
                List<Destination> list = destinationService.searchDestinations(q);
                SwingUtilities.invokeLater(() -> populateTable(list));
            } catch (SQLException ex) {
                SwingUtilities.invokeLater(() -> UITheme.showError(this, "Search error:\n" + ex.getMessage()));
            }
        }).start();
    }

    private void addDestination() {
        try {
            Destination d = getDestinationFromForm();
            destinationService.addDestination(d);
            UITheme.showSuccess(this, "Destination '" + d.getName() + "' added successfully!");
            clearFields();
            loadDestinations();
            suggestNextId();
        } catch (ValidationException ve) {
            UITheme.showError(this, ve.getMessage());
        } catch (Exception ex) {
            UITheme.showError(this, "Failed to add destination:\n" + ex.getMessage());
        }
    }

    private void updateDestination() {
        try {
            Destination d = getDestinationFromForm();
            destinationService.updateDestination(d);
            UITheme.showSuccess(this, "Destination #" + d.getDestinationId() + " updated successfully!");
            loadDestinations();
        } catch (ValidationException ve) {
            UITheme.showError(this, ve.getMessage());
        } catch (Exception ex) {
            UITheme.showError(this, "Failed to update destination:\n" + ex.getMessage());
        }
    }

    private void deleteDestination() {
        String idText = txtId.getText().trim();
        if (idText.isEmpty()) {
            UITheme.showError(this, "Please select or specify a Destination ID to delete.");
            return;
        }

        int id;
        try {
            id = Integer.parseInt(idText);
        } catch (NumberFormatException ex) {
            UITheme.showError(this, "Destination ID must be a numeric integer.");
            return;
        }

        if (!UITheme.showConfirm(this, "Are you sure you want to delete Destination #" + id + "?")) {
            return;
        }

        try {
            destinationService.deleteDestination(id);
            UITheme.showSuccess(this, "Destination #" + id + " deleted successfully!");
            clearFields();
            loadDestinations();
            suggestNextId();
        } catch (ValidationException ve) {
            UITheme.showError(this, ve.getMessage());
        } catch (Exception ex) {
            UITheme.showError(this, "Failed to delete destination:\n" + ex.getMessage());
        }
    }

    private Destination getDestinationFromForm() throws ValidationException {
        String idStr = txtId.getText().trim();
        if (idStr.isEmpty()) throw new ValidationException("Destination ID is required.");
        int id;
        try {
            id = Integer.parseInt(idStr);
        } catch (NumberFormatException ex) {
            throw new ValidationException("Destination ID must be a valid number.");
        }
        return new Destination(id, txtName.getText().trim(), txtLocation.getText().trim(), txtDescription.getText().trim());
    }

    private void clearFields() {
        txtId.setText("");
        txtName.setText("");
        txtLocation.setText("");
        txtDescription.setText("");
        tblDestinations.clearSelection();
        suggestNextId();
    }

    private void suggestNextId() {
        new Thread(() -> {
            try {
                int next = destinationService.getNextDestinationId();
                SwingUtilities.invokeLater(() -> {
                    if (txtId.getText().trim().isEmpty()) {
                        txtId.setText(String.valueOf(next));
                    }
                });
            } catch (Exception ignored) {}
        }).start();
    }
}
