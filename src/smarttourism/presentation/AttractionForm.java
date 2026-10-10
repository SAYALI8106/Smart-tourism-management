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
import javax.swing.JComboBox;
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
import smarttourism.model.Attraction;
import smarttourism.model.AttractionType;
import smarttourism.model.Destination;
import smarttourism.service.AttractionService;
import smarttourism.service.DestinationService;
import smarttourism.service.ValidationException;

/**
 * Tier 1 - Presentation Layer: Attraction Management Form.
 * Respects composite primary key (destination_id, attraction_id) and foreign key constraints.
 */
public class AttractionForm extends JFrame {

    private final AttractionService attractionService = new AttractionService();
    private final DestinationService destinationService = new DestinationService();

    private JComboBox<Destination> cmbDestination;
    private JTextField txtAttractionId;
    private JTextField txtName;
    private JComboBox<String> cmbType;
    private JTextField txtEntryFee;
    private JTextField txtClosingTime;
    private JTextField txtOperatingTime;
    private JTextArea txtDescription;
    private JTextField txtSearch;

    private JTable tblAttractions;
    private DefaultTableModel tableModel;

    public AttractionForm() {
        super("Attraction Management — Smart Tourism");
        initUI();
        loadDropdowns();
        loadAttractions();
    }

    private void initUI() {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1050, 680);
        setLocationRelativeTo(null);
        getContentPane().setBackground(UITheme.COLOR_BG);
        setLayout(new BorderLayout(0, 10));

        // Header Banner
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UITheme.COLOR_ACCENT);
        headerPanel.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel titleLbl = new JLabel("Attraction Management");
        titleLbl.setFont(UITheme.FONT_TITLE);
        titleLbl.setForeground(Color.WHITE);

        JLabel subLbl = new JLabel("Add, explore, and manage points of interest across destinations");
        subLbl.setFont(UITheme.FONT_SUBTITLE);
        subLbl.setForeground(new Color(224, 242, 254));

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
        JLabel lblDest = new JLabel("Destination:");
        lblDest.setFont(UITheme.FONT_LABEL);
        formFields.add(lblDest, gbc);

        gbc.gridy = row++;
        cmbDestination = new JComboBox<>();
        cmbDestination.setFont(UITheme.FONT_REGULAR);
        cmbDestination.addActionListener(e -> onDestinationChanged());
        formFields.add(cmbDestination, gbc);

        gbc.gridy = row++;
        JLabel lblAttrId = new JLabel("Attraction ID:");
        lblAttrId.setFont(UITheme.FONT_LABEL);
        formFields.add(lblAttrId, gbc);

        gbc.gridy = row++;
        txtAttractionId = UITheme.createTextField();
        formFields.add(txtAttractionId, gbc);

        gbc.gridy = row++;
        JLabel lblName = new JLabel("Attraction Name:");
        lblName.setFont(UITheme.FONT_LABEL);
        formFields.add(lblName, gbc);

        gbc.gridy = row++;
        txtName = UITheme.createTextField();
        formFields.add(txtName, gbc);

        gbc.gridy = row++;
        JLabel lblType = new JLabel("Attraction Category:");
        lblType.setFont(UITheme.FONT_LABEL);
        formFields.add(lblType, gbc);

        gbc.gridy = row++;
        cmbType = new JComboBox<>();
        cmbType.setFont(UITheme.FONT_REGULAR);
        formFields.add(cmbType, gbc);

        gbc.gridy = row++;
        JLabel lblFee = new JLabel("Entry Fee (₹):");
        lblFee.setFont(UITheme.FONT_LABEL);
        formFields.add(lblFee, gbc);

        gbc.gridy = row++;
        txtEntryFee = UITheme.createTextField();
        txtEntryFee.setText("0.00");
        formFields.add(txtEntryFee, gbc);

        gbc.gridy = row++;
        JLabel lblClosing = new JLabel("Closing Time (e.g. 18:30:00):");
        lblClosing.setFont(UITheme.FONT_LABEL);
        formFields.add(lblClosing, gbc);

        gbc.gridy = row++;
        txtClosingTime = UITheme.createTextField();
        formFields.add(txtClosingTime, gbc);

        gbc.gridy = row++;
        JLabel lblOperating = new JLabel("Operating Hours (e.g. 09:00 - 18:30):");
        lblOperating.setFont(UITheme.FONT_LABEL);
        formFields.add(lblOperating, gbc);

        gbc.gridy = row++;
        txtOperatingTime = UITheme.createTextField();
        formFields.add(txtOperatingTime, gbc);

        gbc.gridy = row++;
        JLabel lblDesc = new JLabel("Description:");
        lblDesc.setFont(UITheme.FONT_LABEL);
        formFields.add(lblDesc, gbc);

        gbc.gridy = row++;
        txtDescription = new JTextArea(3, 20);
        txtDescription.setFont(UITheme.FONT_REGULAR);
        txtDescription.setLineWrap(true);
        txtDescription.setWrapStyleWord(true);
        JScrollPane descScroll = new JScrollPane(txtDescription);
        descScroll.setBorder(BorderFactory.createLineBorder(UITheme.COLOR_BORDER, 1));
        formFields.add(descScroll, gbc);

        formCard.add(formFields, BorderLayout.CENTER);

        // Buttons Row
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 8));
        btnPanel.setBackground(UITheme.COLOR_SURFACE);

        JButton btnAdd = UITheme.createPrimaryButton("Save / Add");
        btnAdd.addActionListener(e -> addAttraction());

        JButton btnUpdate = UITheme.createPrimaryButton("Update");
        btnUpdate.addActionListener(e -> updateAttraction());

        JButton btnDelete = UITheme.createDangerButton("Delete");
        btnDelete.addActionListener(e -> deleteAttraction());

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
        btnSearch.addActionListener(e -> searchAttractions());

        JButton btnRefresh = UITheme.createSecondaryButton("Refresh");
        btnRefresh.addActionListener(e -> {
            txtSearch.setText("");
            loadAttractions();
        });

        searchBtns.add(btnSearch);
        searchBtns.add(btnRefresh);
        searchRow.add(searchBtns, BorderLayout.EAST);
        tableCard.add(searchRow, BorderLayout.NORTH);

        // Attractions Table
        String[] cols = {"Dest ID", "Destination", "Attraction ID", "Attraction Name", "Category", "Entry Fee (₹)", "Closing Time", "Operating Time"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tblAttractions = new JTable(tableModel);
        UITheme.formatTable(tblAttractions);

        tblAttractions.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tblAttractions.getSelectedRow() != -1) {
                int r = tblAttractions.getSelectedRow();
                int destId = (int) tableModel.getValueAt(r, 0);
                int attrId = (int) tableModel.getValueAt(r, 2);
                loadSelectedAttractionToForm(destId, attrId);
            }
        });

        JScrollPane sp = new JScrollPane(tblAttractions);
        sp.setBorder(BorderFactory.createLineBorder(UITheme.COLOR_BORDER, 1));
        sp.getViewport().setBackground(Color.WHITE);
        tableCard.add(sp, BorderLayout.CENTER);

        centerPanel.add(tableCard, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);
    }

    private void loadDropdowns() {
        new Thread(() -> {
            try {
                List<Destination> destinations = destinationService.getAllDestinations();
                List<AttractionType> types = attractionService.getAllAttractionTypes();
                SwingUtilities.invokeLater(() -> {
                    cmbDestination.removeAllItems();
                    for (Destination d : destinations) cmbDestination.addItem(d);

                    cmbType.removeAllItems();
                    for (AttractionType t : types) cmbType.addItem(t.getType());

                    onDestinationChanged();
                });
            } catch (SQLException ex) {
                SwingUtilities.invokeLater(() -> UITheme.showError(this, "Failed loading dropdowns:\n" + ex.getMessage()));
            }
        }).start();
    }

    private void onDestinationChanged() {
        Destination selected = (Destination) cmbDestination.getSelectedItem();
        if (selected != null) {
            new Thread(() -> {
                try {
                    int next = attractionService.getNextAttractionId(selected.getDestinationId());
                    SwingUtilities.invokeLater(() -> {
                        if (tblAttractions.getSelectedRow() == -1) {
                            txtAttractionId.setText(String.valueOf(next));
                        }
                    });
                } catch (Exception ignored) {}
            }).start();
        }
    }

    private void loadAttractions() {
        new Thread(() -> {
            try {
                List<Attraction> list = attractionService.getAllAttractions();
                SwingUtilities.invokeLater(() -> populateTable(list));
            } catch (SQLException ex) {
                SwingUtilities.invokeLater(() -> UITheme.showError(this, "Failed to load attractions:\n" + ex.getMessage()));
            }
        }).start();
    }

    private void populateTable(List<Attraction> list) {
        tableModel.setRowCount(0);
        for (Attraction a : list) {
            tableModel.addRow(new Object[]{
                a.getDestinationId(),
                a.getDestinationName(),
                a.getAttractionId(),
                a.getName(),
                a.getType(),
                String.format("%.2f", a.getEntryFee()),
                a.getClosingTime(),
                a.getOperatingTime()
            });
        }
    }

    private void loadSelectedAttractionToForm(int destId, int attrId) {
        new Thread(() -> {
            try {
                Attraction a = attractionService.getAttractionByKey(destId, attrId);
                if (a != null) {
                    SwingUtilities.invokeLater(() -> {
                        for (int i = 0; i < cmbDestination.getItemCount(); i++) {
                            if (cmbDestination.getItemAt(i).getDestinationId() == a.getDestinationId()) {
                                cmbDestination.setSelectedIndex(i);
                                break;
                            }
                        }
                        txtAttractionId.setText(String.valueOf(a.getAttractionId()));
                        txtName.setText(a.getName());
                        cmbType.setSelectedItem(a.getType());
                        txtEntryFee.setText(String.format("%.2f", a.getEntryFee()));
                        txtClosingTime.setText(a.getClosingTime() != null ? a.getClosingTime() : "");
                        txtOperatingTime.setText(a.getOperatingTime() != null ? a.getOperatingTime() : "");
                        txtDescription.setText(a.getDescription() != null ? a.getDescription() : "");
                    });
                }
            } catch (Exception ignored) {}
        }).start();
    }

    private void searchAttractions() {
        String q = txtSearch.getText().trim();
        new Thread(() -> {
            try {
                List<Attraction> list = attractionService.searchAttractions(q);
                SwingUtilities.invokeLater(() -> populateTable(list));
            } catch (SQLException ex) {
                SwingUtilities.invokeLater(() -> UITheme.showError(this, "Search error:\n" + ex.getMessage()));
            }
        }).start();
    }

    private void addAttraction() {
        try {
            Attraction a = getAttractionFromForm();
            attractionService.addAttraction(a);
            UITheme.showSuccess(this, "Attraction '" + a.getName() + "' added successfully!");
            clearFields();
            loadAttractions();
        } catch (ValidationException ve) {
            UITheme.showError(this, ve.getMessage());
        } catch (Exception ex) {
            UITheme.showError(this, "Failed to add attraction:\n" + ex.getMessage());
        }
    }

    private void updateAttraction() {
        try {
            Attraction a = getAttractionFromForm();
            attractionService.updateAttraction(a);
            UITheme.showSuccess(this, "Attraction updated successfully!");
            loadAttractions();
        } catch (ValidationException ve) {
            UITheme.showError(this, ve.getMessage());
        } catch (Exception ex) {
            UITheme.showError(this, "Failed to update attraction:\n" + ex.getMessage());
        }
    }

    private void deleteAttraction() {
        Destination dest = (Destination) cmbDestination.getSelectedItem();
        if (dest == null) {
            UITheme.showError(this, "Please select a Destination.");
            return;
        }

        String attrIdStr = txtAttractionId.getText().trim();
        if (attrIdStr.isEmpty()) {
            UITheme.showError(this, "Please enter or select an Attraction ID.");
            return;
        }

        int attrId;
        try {
            attrId = Integer.parseInt(attrIdStr);
        } catch (NumberFormatException ex) {
            UITheme.showError(this, "Attraction ID must be a numeric integer.");
            return;
        }

        if (!UITheme.showConfirm(this, "Delete Attraction #" + attrId + " under " + dest.getName() + "?")) {
            return;
        }

        try {
            attractionService.deleteAttraction(dest.getDestinationId(), attrId);
            UITheme.showSuccess(this, "Attraction deleted successfully!");
            clearFields();
            loadAttractions();
        } catch (ValidationException ve) {
            UITheme.showError(this, ve.getMessage());
        } catch (Exception ex) {
            UITheme.showError(this, "Failed to delete attraction:\n" + ex.getMessage());
        }
    }

    private Attraction getAttractionFromForm() throws ValidationException {
        Destination dest = (Destination) cmbDestination.getSelectedItem();
        if (dest == null) throw new ValidationException("Please select a valid Destination.");

        String idStr = txtAttractionId.getText().trim();
        if (idStr.isEmpty()) throw new ValidationException("Attraction ID is required.");
        int attrId;
        try {
            attrId = Integer.parseInt(idStr);
        } catch (NumberFormatException ex) {
            throw new ValidationException("Attraction ID must be a valid number.");
        }

        String name = txtName.getText().trim();
        String type = (String) cmbType.getSelectedItem();

        String feeStr = txtEntryFee.getText().trim();
        double fee = 0.0;
        if (!feeStr.isEmpty()) {
            try {
                fee = Double.parseDouble(feeStr);
            } catch (NumberFormatException ex) {
                throw new ValidationException("Entry fee must be a valid numeric amount.");
            }
        }

        String closing = txtClosingTime.getText().trim();
        String operating = txtOperatingTime.getText().trim();
        String desc = txtDescription.getText().trim();

        return new Attraction(dest.getDestinationId(), attrId, name, type, fee, desc, closing, operating);
    }

    private void clearFields() {
        txtAttractionId.setText("");
        txtName.setText("");
        txtEntryFee.setText("0.00");
        txtClosingTime.setText("");
        txtOperatingTime.setText("");
        txtDescription.setText("");
        tblAttractions.clearSelection();
        onDestinationChanged();
    }
}
