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
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import smarttourism.model.Destination;
import smarttourism.model.TourPackage;
import smarttourism.service.DestinationService;
import smarttourism.service.PackageService;
import smarttourism.service.ValidationException;

/**
 * Tier 1 - Presentation Layer: Tour Package Management Form.
 * Includes support for DESTINATION_PACKAGE many-to-many mapping.
 */
public class PackageForm extends JFrame {

    private final PackageService packageService = new PackageService();
    private final DestinationService destinationService = new DestinationService();

    private JTextField txtId;
    private JTextField txtName;
    private JTextField txtDuration;
    private JTextField txtCost;
    private JTextField txtSearch;

    private JTable tblPackages;
    private DefaultTableModel tableModel;

    // Many-to-many relationship components (DESTINATION_PACKAGE)
    private JComboBox<Destination> cmbAvailableDests;
    private DefaultListModel<Destination> listModelLinkedDests;
    private JList<Destination> listLinkedDests;
    private JLabel lblLinkedPackage;
    private int selectedPackageId = -1;

    public PackageForm() {
        super("Tour Package Management — Smart Tourism");
        initUI();
        loadPackages();
        loadAvailableDestinations();
        suggestNextId();
    }

    private void initUI() {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1080, 680);
        setLocationRelativeTo(null);
        getContentPane().setBackground(UITheme.COLOR_BG);
        setLayout(new BorderLayout(0, 10));

        // Header Banner
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(124, 58, 237)); // Vibrant violet
        headerPanel.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel titleLbl = new JLabel("Tour Package Management");
        titleLbl.setFont(UITheme.FONT_TITLE);
        titleLbl.setForeground(Color.WHITE);

        JLabel subLbl = new JLabel("Create and manage holiday packages and tourist itineraries");
        subLbl.setFont(UITheme.FONT_SUBTITLE);
        subLbl.setForeground(new Color(237, 233, 254));

        headerPanel.add(titleLbl, BorderLayout.WEST);
        headerPanel.add(subLbl, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Center Content
        JPanel centerPanel = new JPanel(new BorderLayout(16, 0));
        centerPanel.setBackground(UITheme.COLOR_BG);
        centerPanel.setBorder(new EmptyBorder(0, 16, 16, 16));

        // LEFT: Package Input & Destination Mapping
        JPanel leftContainer = new JPanel();
        leftContainer.setLayout(new BoxLayout(leftContainer, BoxLayout.Y_AXIS));
        leftContainer.setBackground(UITheme.COLOR_BG);
        leftContainer.setPreferredSize(new Dimension(400, 0));

        // 1. Package Form Card
        JPanel formCard = UITheme.createCardPanel();
        formCard.setLayout(new BorderLayout());

        JPanel formFields = new JPanel(new GridBagLayout());
        formFields.setBackground(UITheme.COLOR_SURFACE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.gridx = 0;
        gbc.weightx = 1.0;

        int row = 0;
        gbc.gridy = row++;
        JLabel lblId = new JLabel("Package ID:");
        lblId.setFont(UITheme.FONT_LABEL);
        formFields.add(lblId, gbc);

        gbc.gridy = row++;
        txtId = UITheme.createTextField();
        formFields.add(txtId, gbc);

        gbc.gridy = row++;
        JLabel lblName = new JLabel("Package Name:");
        lblName.setFont(UITheme.FONT_LABEL);
        formFields.add(lblName, gbc);

        gbc.gridy = row++;
        txtName = UITheme.createTextField();
        formFields.add(txtName, gbc);

        gbc.gridy = row++;
        JLabel lblDur = new JLabel("Duration (Days):");
        lblDur.setFont(UITheme.FONT_LABEL);
        formFields.add(lblDur, gbc);

        gbc.gridy = row++;
        txtDuration = UITheme.createTextField();
        formFields.add(txtDuration, gbc);

        gbc.gridy = row++;
        JLabel lblCost = new JLabel("Package Cost (₹):");
        lblCost.setFont(UITheme.FONT_LABEL);
        formFields.add(lblCost, gbc);

        gbc.gridy = row++;
        txtCost = UITheme.createTextField();
        formFields.add(txtCost, gbc);

        formCard.add(formFields, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 8));
        btnPanel.setBackground(UITheme.COLOR_SURFACE);

        JButton btnAdd = UITheme.createPrimaryButton("Save / Add");
        btnAdd.addActionListener(e -> addPackage());

        JButton btnUpdate = UITheme.createPrimaryButton("Update");
        btnUpdate.addActionListener(e -> updatePackage());

        JButton btnDelete = UITheme.createDangerButton("Delete");
        btnDelete.addActionListener(e -> deletePackage());

        JButton btnClear = UITheme.createSecondaryButton("Clear");
        btnClear.addActionListener(e -> clearFields());

        btnPanel.add(btnAdd);
        btnPanel.add(btnUpdate);
        btnPanel.add(btnDelete);
        btnPanel.add(btnClear);
        formCard.add(btnPanel, BorderLayout.SOUTH);

        leftContainer.add(formCard);
        leftContainer.add(Box.createVerticalStrut(12));

        // 2. Included Destinations Mapping Card (DESTINATION_PACKAGE)
        JPanel mapCard = UITheme.createCardPanel();
        mapCard.setLayout(new BorderLayout(0, 6));

        lblLinkedPackage = new JLabel("Included Destinations:");
        lblLinkedPackage.setFont(UITheme.FONT_LABEL);
        lblLinkedPackage.setForeground(UITheme.COLOR_TEXT_MAIN);
        mapCard.add(lblLinkedPackage, BorderLayout.NORTH);

        listModelLinkedDests = new DefaultListModel<>();
        listLinkedDests = new JList<>(listModelLinkedDests);
        listLinkedDests.setFont(UITheme.FONT_REGULAR);
        JScrollPane listScroll = new JScrollPane(listLinkedDests);
        listScroll.setPreferredSize(new Dimension(0, 110));
        mapCard.add(listScroll, BorderLayout.CENTER);

        JPanel linkControls = new JPanel(new BorderLayout(4, 0));
        linkControls.setBackground(UITheme.COLOR_SURFACE);
        cmbAvailableDests = new JComboBox<>();
        cmbAvailableDests.setFont(UITheme.FONT_REGULAR);
        linkControls.add(cmbAvailableDests, BorderLayout.CENTER);

        JPanel linkBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        linkBtns.setBackground(UITheme.COLOR_SURFACE);
        JButton btnLink = UITheme.createPrimaryButton("+ Link");
        btnLink.addActionListener(e -> linkDestination());
        JButton btnUnlink = UITheme.createDangerButton("Remove Link");
        btnUnlink.addActionListener(e -> unlinkDestination());
        linkBtns.add(btnLink);
        linkBtns.add(btnUnlink);
        linkControls.add(linkBtns, BorderLayout.EAST);
        mapCard.add(linkControls, BorderLayout.SOUTH);

        leftContainer.add(mapCard);
        centerPanel.add(leftContainer, BorderLayout.WEST);

        // RIGHT: Search and Table
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
        btnSearch.addActionListener(e -> searchPackages());

        JButton btnRefresh = UITheme.createSecondaryButton("Refresh");
        btnRefresh.addActionListener(e -> {
            txtSearch.setText("");
            loadPackages();
        });

        searchBtns.add(btnSearch);
        searchBtns.add(btnRefresh);
        searchRow.add(searchBtns, BorderLayout.EAST);
        tableCard.add(searchRow, BorderLayout.NORTH);

        String[] cols = {"Package ID", "Package Name", "Duration (Days)", "Cost (₹)"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tblPackages = new JTable(tableModel);
        UITheme.formatTable(tblPackages);

        tblPackages.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tblPackages.getSelectedRow() != -1) {
                int r = tblPackages.getSelectedRow();
                int pkgId = (int) tableModel.getValueAt(r, 0);
                selectedPackageId = pkgId;
                txtId.setText(String.valueOf(pkgId));
                txtName.setText(String.valueOf(tableModel.getValueAt(r, 1)));
                txtDuration.setText(String.valueOf(tableModel.getValueAt(r, 2)));
                txtCost.setText(String.valueOf(tableModel.getValueAt(r, 3)));
                loadLinkedDestinations(pkgId);
            }
        });

        JScrollPane sp = new JScrollPane(tblPackages);
        sp.setBorder(BorderFactory.createLineBorder(UITheme.COLOR_BORDER, 1));
        sp.getViewport().setBackground(Color.WHITE);
        tableCard.add(sp, BorderLayout.CENTER);

        centerPanel.add(tableCard, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);
    }

    private void loadPackages() {
        new Thread(() -> {
            try {
                List<TourPackage> list = packageService.getAllPackages();
                SwingUtilities.invokeLater(() -> populateTable(list));
            } catch (SQLException ex) {
                SwingUtilities.invokeLater(() -> UITheme.showError(this, "Failed to load packages:\n" + ex.getMessage()));
            }
        }).start();
    }

    private void populateTable(List<TourPackage> list) {
        tableModel.setRowCount(0);
        for (TourPackage p : list) {
            tableModel.addRow(new Object[]{
                p.getPackageId(),
                p.getPackageName(),
                p.getDuration(),
                p.getCost()
            });
        }
    }

    private void loadAvailableDestinations() {
        new Thread(() -> {
            try {
                List<Destination> list = destinationService.getAllDestinations();
                SwingUtilities.invokeLater(() -> {
                    cmbAvailableDests.removeAllItems();
                    for (Destination d : list) cmbAvailableDests.addItem(d);
                });
            } catch (Exception ignored) {}
        }).start();
    }

    private void loadLinkedDestinations(int pkgId) {
        lblLinkedPackage.setText("Destinations in Package #" + pkgId + ":");
        new Thread(() -> {
            try {
                List<Destination> dests = packageService.getDestinationsForPackage(pkgId);
                SwingUtilities.invokeLater(() -> {
                    listModelLinkedDests.clear();
                    for (Destination d : dests) listModelLinkedDests.addElement(d);
                });
            } catch (SQLException ex) {
                SwingUtilities.invokeLater(() -> UITheme.showError(this, "Failed to load linked destinations:\n" + ex.getMessage()));
            }
        }).start();
    }

    private void linkDestination() {
        if (selectedPackageId <= 0) {
            UITheme.showError(this, "Please select a package from the table first.");
            return;
        }
        Destination dest = (Destination) cmbAvailableDests.getSelectedItem();
        if (dest == null) {
            UITheme.showError(this, "Please select a destination to link.");
            return;
        }
        try {
            packageService.linkDestination(dest.getDestinationId(), selectedPackageId);
            UITheme.showSuccess(this, "Destination '" + dest.getName() + "' linked to Package #" + selectedPackageId);
            loadLinkedDestinations(selectedPackageId);
        } catch (ValidationException ve) {
            UITheme.showError(this, ve.getMessage());
        } catch (Exception ex) {
            UITheme.showError(this, "Failed to link destination:\n" + ex.getMessage());
        }
    }

    private void unlinkDestination() {
        if (selectedPackageId <= 0) {
            UITheme.showError(this, "Please select a package from the table first.");
            return;
        }
        Destination dest = listLinkedDests.getSelectedValue();
        if (dest == null) {
            UITheme.showError(this, "Please select a destination from the list above to remove.");
            return;
        }
        try {
            packageService.unlinkDestination(dest.getDestinationId(), selectedPackageId);
            UITheme.showSuccess(this, "Destination unlinked successfully.");
            loadLinkedDestinations(selectedPackageId);
        } catch (Exception ex) {
            UITheme.showError(this, "Failed to unlink destination:\n" + ex.getMessage());
        }
    }

    private void searchPackages() {
        String q = txtSearch.getText().trim();
        new Thread(() -> {
            try {
                List<TourPackage> list = packageService.searchPackages(q);
                SwingUtilities.invokeLater(() -> populateTable(list));
            } catch (SQLException ex) {
                SwingUtilities.invokeLater(() -> UITheme.showError(this, "Search error:\n" + ex.getMessage()));
            }
        }).start();
    }

    private void addPackage() {
        try {
            TourPackage p = getPackageFromForm();
            packageService.addPackage(p);
            UITheme.showSuccess(this, "Tour Package '" + p.getPackageName() + "' added successfully!");
            clearFields();
            loadPackages();
            suggestNextId();
        } catch (ValidationException ve) {
            UITheme.showError(this, ve.getMessage());
        } catch (Exception ex) {
            UITheme.showError(this, "Failed to add package:\n" + ex.getMessage());
        }
    }

    private void updatePackage() {
        try {
            TourPackage p = getPackageFromForm();
            packageService.updatePackage(p);
            UITheme.showSuccess(this, "Package #" + p.getPackageId() + " updated successfully!");
            loadPackages();
        } catch (ValidationException ve) {
            UITheme.showError(this, ve.getMessage());
        } catch (Exception ex) {
            UITheme.showError(this, "Failed to update package:\n" + ex.getMessage());
        }
    }

    private void deletePackage() {
        String idText = txtId.getText().trim();
        if (idText.isEmpty()) {
            UITheme.showError(this, "Please select or enter a Package ID to delete.");
            return;
        }

        int id;
        try {
            id = Integer.parseInt(idText);
        } catch (NumberFormatException ex) {
            UITheme.showError(this, "Package ID must be an integer.");
            return;
        }

        if (!UITheme.showConfirm(this, "Are you sure you want to delete Tour Package #" + id + "?")) {
            return;
        }

        try {
            packageService.deletePackage(id);
            UITheme.showSuccess(this, "Package #" + id + " deleted successfully!");
            clearFields();
            loadPackages();
            suggestNextId();
        } catch (ValidationException ve) {
            UITheme.showError(this, ve.getMessage());
        } catch (Exception ex) {
            UITheme.showError(this, "Failed to delete package:\n" + ex.getMessage());
        }
    }

    private TourPackage getPackageFromForm() throws ValidationException {
        String idStr = txtId.getText().trim();
        if (idStr.isEmpty()) throw new ValidationException("Package ID is required.");
        int id;
        try {
            id = Integer.parseInt(idStr);
        } catch (NumberFormatException ex) {
            throw new ValidationException("Package ID must be a numeric integer.");
        }

        String name = txtName.getText().trim();
        String durStr = txtDuration.getText().trim();
        int dur;
        try {
            dur = Integer.parseInt(durStr);
        } catch (NumberFormatException ex) {
            throw new ValidationException("Duration must be a numeric number of days.");
        }

        String costStr = txtCost.getText().trim();
        double cost;
        try {
            cost = Double.parseDouble(costStr);
        } catch (NumberFormatException ex) {
            throw new ValidationException("Cost must be a valid numeric value.");
        }

        return new TourPackage(id, name, dur, cost);
    }

    private void clearFields() {
        txtId.setText("");
        txtName.setText("");
        txtDuration.setText("");
        txtCost.setText("");
        tblPackages.clearSelection();
        selectedPackageId = -1;
        lblLinkedPackage.setText("Included Destinations:");
        listModelLinkedDests.clear();
        suggestNextId();
    }

    private void suggestNextId() {
        new Thread(() -> {
            try {
                int next = packageService.getNextPackageId();
                SwingUtilities.invokeLater(() -> {
                    if (txtId.getText().trim().isEmpty()) {
                        txtId.setText(String.valueOf(next));
                    }
                });
            } catch (Exception ignored) {}
        }).start();
    }
}
