import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimpleHTTPClientUI extends JFrame {

    // --- UI Component Fields ---
    private final JComboBox<String> methodComboBox;
    private final JTextField urlField;
    private final JButton sendButton;
    private final DefaultTableModel headersModel;
    private final JTextArea requestBodyArea;
    private final JLabel statusLabel, timeLabel, sizeLabel;
    private final JTextArea responseBodyArea, responseHeadersArea;
    private final JList<String> historyList;
    private final DefaultListModel<String> historyListModel;
    private final JButton aiButton;
    private final JPanel summaryPanel;

    // --- Data Field ---
    private List<Request> requestHistory;
    private List<Request> filteredHistory;  // For search functionality
    private final PostmanBackendService backendService;
    private final RequestsDAO requestsDAO;
    private final ResponsesDAO responsesDAO;

    public SimpleHTTPClientUI() {
        setTitle("Creo - API Client");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Initialize components
        this.backendService = new PostmanBackendService();
        this.requestsDAO = new RequestsDAO();
        this.responsesDAO = new ResponsesDAO();
        historyListModel = new DefaultListModel<>();
        historyList = new JList<>(historyListModel);
        methodComboBox = new JComboBox<>(new String[]{"GET", "POST", "PUT", "DELETE"});
        urlField = new JTextField("https://jsonplaceholder.typicode.com/posts/1");
        sendButton = new JButton("Send");
        headersModel = new DefaultTableModel(new String[]{"Key", "Value"}, 0);
        requestBodyArea = new JTextArea();
        statusLabel = new JLabel("Status:");
        timeLabel = new JLabel("Time:");
        sizeLabel = new JLabel("Size:");
        responseBodyArea = new JTextArea();
        responseHeadersArea = new JTextArea();
        aiButton = new JButton("AI Summary");
        summaryPanel = createSummaryPanel();

        setupUI();
        addListeners();
        loadHistory();
    }

    private void setupUI() {
        // Main layout - BorderLayout for the frame
        setLayout(new BorderLayout());

        // Create main split pane (history | workspace)
        JPanel historyPanel = createHistoryPanel();
        JPanel workspacePanel = createWorkspacePanel();
        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, historyPanel, workspacePanel);
        mainSplitPane.setDividerLocation(250);
        mainSplitPane.setResizeWeight(0.2);

        // Add to frame
        add(mainSplitPane, BorderLayout.CENTER);

        // Add AI button to south for now (we can move it later if needed)
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(aiButton);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JPanel createHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Request History"));

        // Search/Filter at top
        JTextField searchField = new JTextField();
        searchField.putClientProperty("JTextField.placeholderText", "Search history...");
        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.add(new JLabel("Filter: "), BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // History list
        historyList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        historyList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

                // Add some padding and formatting
                setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 5));

                // Color code by HTTP method
                String text = value.toString();
                if (text.startsWith("GET")) {
                    setForeground(isSelected ? Color.WHITE : new Color(96, 181, 157));
                } else if (text.startsWith("POST")) {
                    setForeground(isSelected ? Color.WHITE : new Color(230, 200, 100));
                } else if (text.startsWith("PUT")) {
                    setForeground(isSelected ? Color.WHITE : new Color(120, 194, 255));
                } else if (text.startsWith("DELETE")) {
                    setForeground(isSelected ? Color.WHITE : new Color(204, 120, 94));
                }

                return this;
            }
        });

        JScrollPane scrollPane = new JScrollPane(historyList);
        scrollPane.setPreferredSize(new Dimension(240, 600));

        // Button panel at bottom
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton deleteButton = new JButton("Delete");
        JButton clearAllButton = new JButton("Clear All");
        JButton refreshButton = new JButton("Refresh");

        deleteButton.setToolTipText("Delete selected request");
        clearAllButton.setToolTipText("Clear all history");
        refreshButton.setToolTipText("Refresh history list");

        // Make buttons smaller
        Dimension btnSize = new Dimension(70, 25);
        deleteButton.setPreferredSize(btnSize);
        clearAllButton.setPreferredSize(btnSize);
        refreshButton.setPreferredSize(btnSize);

        buttonPanel.add(deleteButton);
        buttonPanel.add(clearAllButton);
        buttonPanel.add(refreshButton);

        // Add listeners
        searchField.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                filterHistory(searchField.getText());
            }
        });

        deleteButton.addActionListener(e -> deleteSelectedHistoryItem());
        clearAllButton.addActionListener(e -> clearAllHistory());
        refreshButton.addActionListener(e -> loadHistory());

        // Right-click context menu for history list
        JPopupMenu contextMenu = new JPopupMenu();
        JMenuItem deleteItem = new JMenuItem("Delete");
        JMenuItem copyUrlItem = new JMenuItem("Copy URL");
        JMenuItem duplicateItem = new JMenuItem("Duplicate Request");

        deleteItem.addActionListener(e -> deleteSelectedHistoryItem());
        copyUrlItem.addActionListener(e -> copySelectedUrl());
        duplicateItem.addActionListener(e -> duplicateSelectedRequest());

        contextMenu.add(copyUrlItem);
        contextMenu.add(duplicateItem);
        contextMenu.addSeparator();
        contextMenu.add(deleteItem);

        historyList.setComponentPopupMenu(contextMenu);

        panel.add(searchPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createWorkspacePanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Request control panel at top
        panel.add(createRequestControlPanel(), BorderLayout.NORTH);

        // Main workspace split (request tabs | response)
        JSplitPane workspaceSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                createRequestTabs(), createResponsePanel());
        workspaceSplit.setDividerLocation(300);
        workspaceSplit.setResizeWeight(0.5);

        panel.add(workspaceSplit, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createRequestControlPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Style the send button
        sendButton.setBackground(new Color(0, 122, 255));
        sendButton.setForeground(Color.WHITE);
        sendButton.setPreferredSize(new Dimension(80, 30));

        // Method dropdown
        methodComboBox.setPreferredSize(new Dimension(80, 30));

        panel.add(methodComboBox, BorderLayout.WEST);
        panel.add(urlField, BorderLayout.CENTER);
        panel.add(sendButton, BorderLayout.EAST);

        return panel;
    }

    private JTabbedPane createRequestTabs() {
        JTabbedPane tabs = new JTabbedPane();

        // Headers tab
        headersModel.addRow(new String[]{"Content-Type", "application/json"});
        JTable headersTable = new JTable(headersModel);
        headersTable.setFillsViewportHeight(true);

        JPanel headersPanel = new JPanel(new BorderLayout());
        headersPanel.add(new JScrollPane(headersTable), BorderLayout.CENTER);

        // Add/Remove buttons for headers
        JPanel headerButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addHeaderBtn = new JButton("Add Header");
        JButton removeHeaderBtn = new JButton("Remove Header");

        addHeaderBtn.addActionListener(e -> headersModel.addRow(new String[]{"", ""}));
        removeHeaderBtn.addActionListener(e -> {
            int selectedRow = headersTable.getSelectedRow();
            if (selectedRow >= 0) {
                headersModel.removeRow(selectedRow);
            }
        });

        headerButtonPanel.add(addHeaderBtn);
        headerButtonPanel.add(removeHeaderBtn);
        headersPanel.add(headerButtonPanel, BorderLayout.SOUTH);

        tabs.addTab("Headers", headersPanel);

        // Body tab
        requestBodyArea.setWrapStyleWord(true);
        requestBodyArea.setLineWrap(true);
        tabs.addTab("Body", new JScrollPane(requestBodyArea));

        return tabs;
    }

    private JPanel createResponsePanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Status panel
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.setBorder(BorderFactory.createEtchedBorder());
        statusPanel.add(statusLabel);
        statusPanel.add(Box.createHorizontalStrut(20));
        statusPanel.add(timeLabel);
        statusPanel.add(Box.createHorizontalStrut(20));
        statusPanel.add(sizeLabel);

        // Response tabs
        JTabbedPane responseTabs = new JTabbedPane();

        responseBodyArea.setEditable(false);
        responseBodyArea.setWrapStyleWord(true);
        responseBodyArea.setLineWrap(true);
        responseBodyArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        responseHeadersArea.setEditable(false);
        responseHeadersArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        responseTabs.addTab("Body", new JScrollPane(responseBodyArea));
        responseTabs.addTab("Headers", new JScrollPane(responseHeadersArea));

        panel.add(statusPanel, BorderLayout.NORTH);
        panel.add(responseTabs, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createSummaryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("AI Summary"));
        panel.setVisible(false);

        JTextArea summaryArea = new JTextArea("AI summary will appear here...");
        summaryArea.setEditable(false);
        summaryArea.setWrapStyleWord(true);
        summaryArea.setLineWrap(true);
        summaryArea.setRows(8);

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> panel.setVisible(false));

        panel.add(new JScrollPane(summaryArea), BorderLayout.CENTER);
        panel.add(closeButton, BorderLayout.SOUTH);

        return panel;
    }

    private void addListeners() {
        sendButton.addActionListener(e -> onSendRequest());
        historyList.addListSelectionListener(e -> onHistorySelection(e));
        aiButton.addActionListener(e -> {
            if (!summaryPanel.isVisible()) {
                // Add summary panel to a dialog or popup
                showSummaryDialog();
            }
        });
    }

    private void showSummaryDialog() {
        JDialog dialog = new JDialog(this, "AI Summary", false);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);
        dialog.add(summaryPanel);
        summaryPanel.setVisible(true);
        dialog.setVisible(true);
    }

    private void onSendRequest() {
        sendButton.setEnabled(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        SwingWorker<HttpResponse, Void> worker = new SwingWorker<>() {
            @Override
            protected HttpResponse doInBackground() {
                return backendService.handleRequest(urlField.getText(),
                        (String) methodComboBox.getSelectedItem(),
                        getHeadersFromTable(),
                        requestBodyArea.getText());
            }

            @Override
            protected void done() {
                try {
                    updateResponseFields(get());
                    loadHistory();
                } catch (Exception ex) {
                    responseBodyArea.setText("Error: \n" + ex.getMessage());
                } finally {
                    sendButton.setEnabled(true);
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        };
        worker.execute();
    }

    private void onHistorySelection(javax.swing.event.ListSelectionEvent e) {
        if (!e.getValueIsAdjusting() && historyList.getSelectedIndex() != -1) {
            Request selected = filteredHistory.get(historyList.getSelectedIndex());
            populateRequestFields(selected);
            loadResponseForRequest(selected.getID());
        }
    }

    private void loadResponseForRequest(int requestId) {
        SwingWorker<Response, Void> worker = new SwingWorker<>() {
            @Override
            protected Response doInBackground() {
                return new ResponsesDAO().FindByRequestID(requestId);
            }

            @Override
            protected void done() {
                try {
                    Response dbResponse = get();
                    if (dbResponse != null) {
                        statusLabel.setText("Status: " + dbResponse.getStatusCode());
                        timeLabel.setText("Time: (N/A)");
                        sizeLabel.setText("Size: (N/A)");
                        responseBodyArea.setText(dbResponse.getBody());
                        responseHeadersArea.setText(dbResponse.getHeaders());
                    } else {
                        clearResponseFields();
                        responseBodyArea.setText("No saved response found.");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }

    private void loadHistory() {
        SwingWorker<List<Request>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Request> doInBackground() {
                return new RequestsDAO().GetAll();
            }

            @Override
            protected void done() {
                try {
                    requestHistory = get();
                    filteredHistory = new java.util.ArrayList<>(requestHistory);
                    updateHistoryDisplay();
                } catch (Exception e) {
                    historyListModel.clear();
                    historyListModel.addElement("Error loading history: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void updateHistoryDisplay() {
        historyListModel.clear();
        for (Request req : filteredHistory) {
            String displayText = String.format("%s %s", req.getMethod(), req.getUrl());
            // Truncate long URLs for display
            if (displayText.length() > 50) {
                displayText = displayText.substring(0, 47) + "...";
            }
            historyListModel.addElement(displayText);
        }
    }

    private void filterHistory(String searchText) {
        if (requestHistory == null) return;

        filteredHistory = new java.util.ArrayList<>();
        String search = searchText.toLowerCase().trim();

        for (Request req : requestHistory) {
            if (search.isEmpty() ||
                    req.getMethod().toLowerCase().contains(search) ||
                    req.getUrl().toLowerCase().contains(search)) {
                filteredHistory.add(req);
            }
        }
        updateHistoryDisplay();
    }

    private void deleteSelectedHistoryItem() {
        int selectedIndex = historyList.getSelectedIndex();
        if (selectedIndex == -1) {
            JOptionPane.showMessageDialog(this, "Please select a request to delete.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this request from history?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            Request selectedRequest = filteredHistory.get(selectedIndex);

            SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
                @Override
                protected Boolean doInBackground() {
                    try {
                        // Delete from database
                        requestsDAO.Delete(selectedRequest.getID());
                        // Also delete associated response
                        responsesDAO.DeleteByRequestID(selectedRequest.getID());
                        return true;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }

                @Override
                protected void done() {
                    try {
                        if (get()) {
                            loadHistory(); // Refresh the list
                            clearResponseFields(); // Clear current response if it was showing this request
                        } else {
                            JOptionPane.showMessageDialog(SimpleHTTPClientUI.this,
                                    "Error deleting request from history.",
                                    "Delete Error", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            worker.execute();
        }
    }

    private void clearAllHistory() {
        if (requestHistory == null || requestHistory.isEmpty()) {
            JOptionPane.showMessageDialog(this, "History is already empty.",
                    "No History", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to clear ALL history? This action cannot be undone.",
                "Clear All History", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
                @Override
                protected Boolean doInBackground() {
                    try {
                        requestsDAO.DeleteAll();
                        responsesDAO.DeleteAll();
                        return true;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }

                @Override
                protected void done() {
                    try {
                        if (get()) {
                            loadHistory(); // Refresh the list
                            clearResponseFields();
                        } else {
                            JOptionPane.showMessageDialog(SimpleHTTPClientUI.this,
                                    "Error clearing history.",
                                    "Clear Error", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            worker.execute();
        }
    }

    private void copySelectedUrl() {
        int selectedIndex = historyList.getSelectedIndex();
        if (selectedIndex == -1) return;

        Request selectedRequest = filteredHistory.get(selectedIndex);
        java.awt.datatransfer.StringSelection stringSelection =
                new java.awt.datatransfer.StringSelection(selectedRequest.getUrl());
        java.awt.datatransfer.Clipboard clipboard =
                java.awt.Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, null);

        JOptionPane.showMessageDialog(this, "URL copied to clipboard!",
                "Copied", JOptionPane.INFORMATION_MESSAGE);
    }

    private void duplicateSelectedRequest() {
        int selectedIndex = historyList.getSelectedIndex();
        if (selectedIndex == -1) return;

        Request selectedRequest = filteredHistory.get(selectedIndex);
        populateRequestFields(selectedRequest);

        // Clear response fields since this is a new request
        clearResponseFields();
    }

    private void updateResponseFields(HttpResponse response) {
        statusLabel.setText("Status: " + response.getStatusCode() + " " + response.getStatusText());
        timeLabel.setText("Time: " + response.getDuration() + " ms");
        sizeLabel.setText("Size: " + response.getBodySize() + " bytes");
        responseBodyArea.setText(response.getBody());
        responseHeadersArea.setText(formatResponseHeaders(response.getHeaders()));
    }

    private void populateRequestFields(Request request) {
        urlField.setText(request.getUrl());
        methodComboBox.setSelectedItem(request.getMethod());
        requestBodyArea.setText(request.getBody());
        populateHeadersTable(request.getHeaders());
    }

    private void populateHeadersTable(String headersString) {
        headersModel.setRowCount(0);
        if (headersString == null || headersString.length() <= 2) return;
        String[] pairs = headersString.substring(1, headersString.length() - 1).split(",\\s*");
        for (String pair : pairs) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2) headersModel.addRow(kv);
        }
    }

    private Map<String, String> getHeadersFromTable() {
        Map<String, String> headers = new HashMap<>();
        for (int i = 0; i < headersModel.getRowCount(); i++) {
            String key = (String) headersModel.getValueAt(i, 0);
            String value = (String) headersModel.getValueAt(i, 1);
            if (key != null && !key.trim().isEmpty()) {
                headers.put(key, value != null ? value : "");
            }
        }
        return headers;
    }

    private void clearResponseFields() {
        statusLabel.setText("Status:");
        timeLabel.setText("Time:");
        sizeLabel.setText("Size:");
        responseBodyArea.setText("");
        responseHeadersArea.setText("");
    }

    private String formatResponseHeaders(Map<String, List<String>> headers) {
        StringBuilder sb = new StringBuilder();
        if (headers != null) {
            headers.forEach((key, value) ->
                    sb.append(key).append(": ").append(String.join(", ", value)).append("\n"));
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatDarculaLaf());
        } catch (Exception e) {
            System.err.println("Failed to initialize LaF");
        }
        SwingUtilities.invokeLater(() -> new SimpleHTTPClientUI().setVisible(true));
    }
}