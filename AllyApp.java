import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

// Main Application Class
public class AllyApp extends JFrame {
    private static final String DATA_FILE = "safety_reports.dat";
    private static final String RESOURCES_FILE = "safety_resources.dat";
    
    private JTabbedPane tabbedPane;
    private List<SafetyReport> reports;
    private Map<String, Integer> trendData;
    private String userRole = "student"; // Can be "student", "admin", or "counselor"
    
    public AllyApp() {
        reports = new ArrayList<>();
        trendData = new HashMap<>();
        loadData();
        initializeGUI();
        loadSampleResources();
    }
    
    private void initializeGUI() {
        setTitle("Ally - School Safety Reporting Tool");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(null);
        
        // Create menu bar for role switching (for demo purposes)
        JMenuBar menuBar = new JMenuBar();
        JMenu roleMenu = new JMenu("User Role");
        
        JMenuItem studentItem = new JMenuItem("Student View");
        JMenuItem adminItem = new JMenuItem("Administrator View");
        JMenuItem counselorItem = new JMenuItem("Counselor View");
        
        studentItem.addActionListener(e -> switchRole("student"));
        adminItem.addActionListener(e -> switchRole("admin"));
        counselorItem.addActionListener(e -> switchRole("counselor"));
        
        roleMenu.add(studentItem);
        roleMenu.add(adminItem);
        roleMenu.add(counselorItem);
        menuBar.add(roleMenu);
        setJMenuBar(menuBar);
        
        // Create tabbed pane
        tabbedPane = new JTabbedPane();
        
        // Add tabs based on user role
        updateTabsForRole();
        
        add(tabbedPane);
    }
    
    private void switchRole(String role) {
        this.userRole = role;
        updateTabsForRole();
        JOptionPane.showMessageDialog(this, "Switched to " + role + " view", 
            "Role Changed", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void updateTabsForRole() {
        tabbedPane.removeAll();
        
        // All roles can view trending issues and resources
        tabbedPane.addTab("Trending Issues", createTrendingPanel());
        tabbedPane.addTab("Safety Resources", createResourcesPanel());
        
        // Students can report issues
        if ("student".equals(userRole)) {
            tabbedPane.addTab("Report Issue", createReportPanel());
        }
        
        // Admins and counselors can view detailed reports
        if ("admin".equals(userRole) || "counselor".equals(userRole)) {
            tabbedPane.addTab("Review Reports", createAdminPanel());
            tabbedPane.addTab("Analytics", createAnalyticsPanel());
        }
        
        tabbedPane.revalidate();
        tabbedPane.repaint();
    }
    
    private JPanel createReportPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Title
        JLabel titleLabel = new JLabel("Anonymous Safety Report");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(titleLabel, BorderLayout.NORTH);
        
        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Category selection
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Issue Category:"), gbc);
        
        String[] categories = {"Bullying", "Unsafe Area", "Harassment", "Facility Issue", 
                              "Social Exclusion", "Cyber Bullying", "Other"};
        JComboBox<String> categoryCombo = new JComboBox<>(categories);
        gbc.gridx = 1;
        formPanel.add(categoryCombo, gbc);
        
        // Location
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Location:"), gbc);
        
        String[] locations = {"Classroom", "Hallway", "Cafeteria", "Bathroom", "Library", "Gym", "Playground", "Bus", "Online", "Other"};
        JComboBox<String> locationCombo = new JComboBox<>(locations);
        gbc.gridx = 1;
        formPanel.add(locationCombo, gbc);
        
        // Severity
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Severity:"), gbc);
        
        String[] severities = {"Low", "Medium", "High", "Urgent"};
        JComboBox<String> severityCombo = new JComboBox<>(severities);
        gbc.gridx = 1;
        formPanel.add(severityCombo, gbc);
        
        // Description
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        formPanel.add(new JLabel("Description:"), gbc);
        
        JTextArea descriptionArea = new JTextArea(5, 30);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(descriptionArea);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        formPanel.add(scrollPane, gbc);
        
        // Anonymous checkbox
        gbc.gridx = 0; gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        JCheckBox anonymousCheck = new JCheckBox("Keep this report anonymous (recommended)", true);
        anonymousCheck.setEnabled(false); // Always anonymous for safety
        formPanel.add(anonymousCheck, gbc);
        
        panel.add(formPanel, BorderLayout.CENTER);
        
        // Submit button
        JPanel buttonPanel = new JPanel();
        JButton submitButton = new JButton("Submit Report");
        submitButton.setFont(new Font("Arial", Font.BOLD, 16));
        submitButton.setBackground(new Color(70, 130, 180));
        submitButton.setForeground(Color.WHITE);
        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String category = (String) categoryCombo.getSelectedItem();
                String location = (String) locationCombo.getSelectedItem();
                String severity = (String) severityCombo.getSelectedItem();
                String description = descriptionArea.getText().trim();
                
                if (description.isEmpty()) {
                    JOptionPane.showMessageDialog(panel, "Please provide a description of the issue.", 
                        "Missing Information", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                SafetyReport report = new SafetyReport(category, location, severity, description);
                reports.add(report);
                updateTrendData(category);
                saveData();
                
                // Clear form
                categoryCombo.setSelectedIndex(0);
                locationCombo.setSelectedIndex(0);
                severityCombo.setSelectedIndex(0);
                descriptionArea.setText("");
                
                JOptionPane.showMessageDialog(panel, 
                    "Your report has been submitted anonymously. Thank you for helping make our school safer!", 
                    "Report Submitted", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        
        buttonPanel.add(submitButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createTrendingPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Title
        JLabel titleLabel = new JLabel("Trending Safety Issues");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(titleLabel, BorderLayout.NORTH);
        
        // Create trending issues display
        JPanel trendsPanel = new JPanel(new GridLayout(0, 1, 10, 10));
        
        if (trendData.isEmpty()) {
            JLabel noDataLabel = new JLabel("No trending issues at this time - great news!");
            noDataLabel.setHorizontalAlignment(SwingConstants.CENTER);
            noDataLabel.setFont(new Font("Arial", Font.ITALIC, 16));
            trendsPanel.add(noDataLabel);
        } else {
            // Sort trends by frequency
            List<Map.Entry<String, Integer>> sortedTrends = new ArrayList<>(trendData.entrySet());
            sortedTrends.sort(Map.Entry.<String, Integer>comparingByValue().reversed());
            
            for (Map.Entry<String, Integer> trend : sortedTrends) {
                JPanel trendItem = createTrendItem(trend.getKey(), trend.getValue());
                trendsPanel.add(trendItem);
            }
        }
        
        JScrollPane scrollPane = new JScrollPane(trendsPanel);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Refresh button
        JButton refreshButton = new JButton("Refresh Data");
        refreshButton.addActionListener(e -> updateTabsForRole());
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(refreshButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createTrendItem(String category, int count) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.GRAY),
            new EmptyBorder(10, 10, 10, 10)
        ));
        
        JLabel categoryLabel = new JLabel(category);
        categoryLabel.setFont(new Font("Arial", Font.BOLD, 16));
        
        JLabel countLabel = new JLabel(count + " reports");
        countLabel.setForeground(Color.BLUE);
        
        // Color code by severity
        Color bgColor = Color.WHITE;
        if (count >= 5) bgColor = new Color(255, 200, 200); // Light red
        else if (count >= 3) bgColor = new Color(255, 255, 200); // Light yellow
        else bgColor = new Color(200, 255, 200); // Light green
        
        panel.setBackground(bgColor);
        
        panel.add(categoryLabel, BorderLayout.WEST);
        panel.add(countLabel, BorderLayout.EAST);
        
        return panel;
    }
    
    private JPanel createResourcesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Title
        JLabel titleLabel = new JLabel("Safety Resources & Tips");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(titleLabel, BorderLayout.NORTH);
        
        // Resources content
        JPanel resourcesPanel = new JPanel(new GridLayout(0, 1, 10, 10));
        
        // Add different resource categories
        resourcesPanel.add(createResourceCategory("Bullying Prevention", 
            new String[]{
                "• Speak up: Tell a trusted adult about bullying situations",
                "• Be an ally: Support classmates who are being bullied",
                "• Document incidents: Keep records of bullying behavior",
                "• Stay in groups: Bullies are less likely to target groups",
                "• Practice confident body language and responses"
            }));
        
        resourcesPanel.add(createResourceCategory("Staying Safe Online",
            new String[]{
                "• Never share personal information with strangers",
                "• Use strong, unique passwords for all accounts",
                "• Think before you post - digital footprints are permanent",
                "• Report cyberbullying to platforms and trusted adults",
                "• Be kind and respectful in all online interactions"
            }));
        
        resourcesPanel.add(createResourceCategory("Emergency Procedures",
            new String[]{
                "• Know your school's emergency contact information",
                "• Familiarize yourself with evacuation routes",
                "• Report suspicious behavior to school security",
                "• In emergencies, call 911 or use emergency alert systems",
                "• Stay calm and follow adult instructions during drills"
            }));
        
        resourcesPanel.add(createResourceCategory("Mental Health Support",
            new String[]{
                "• Talk to school counselors about any concerns",
                "• Practice stress-reduction techniques like deep breathing",
                "• Maintain healthy friendships and support networks",
                "• Get enough sleep, exercise, and nutritious food",
                "• Know that asking for help is a sign of strength"
            }));
        
        JScrollPane scrollPane = new JScrollPane(resourcesPanel);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createResourceCategory(String title, String[] tips) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(title),
            new EmptyBorder(10, 10, 10, 10)
        ));
        
        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setOpaque(false);
        textArea.setFont(new Font("Arial", Font.PLAIN, 14));
        
        StringBuilder content = new StringBuilder();
        for (String tip : tips) {
            content.append(tip).append("\n");
        }
        textArea.setText(content.toString());
        
        panel.add(textArea, BorderLayout.CENTER);
        return panel;
    }
    
    private JPanel createAdminPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Title
        String roleTitle = "admin".equals(userRole) ? "Administrator" : "Counselor";
        JLabel titleLabel = new JLabel(roleTitle + " - Safety Reports Review");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(titleLabel, BorderLayout.NORTH);
        
        // Reports table
        String[] columnNames = {"Date/Time", "Category", "Location", "Severity", "Status"};
        Object[][] data = new Object[reports.size()][5];
        
        for (int i = 0; i < reports.size(); i++) {
            SafetyReport report = reports.get(i);
            data[i][0] = report.getTimestamp().format(DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm"));
            data[i][1] = report.getCategory();
            data[i][2] = report.getLocation();
            data[i][3] = report.getSeverity();
            data[i][4] = report.getStatus();
        }
        
        JTable table = new JTable(data, columnNames);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);
        
        // Add selection listener to view report details
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow >= 0) {
                    SafetyReport report = reports.get(selectedRow);
                    showReportDetails(report);
                }
            }
        });
        
        JScrollPane tableScrollPane = new JScrollPane(table);
        panel.add(tableScrollPane, BorderLayout.CENTER);
        
        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> updateTabsForRole());
        buttonPanel.add(refreshButton);
        
        JButton exportButton = new JButton("Export Data");
        exportButton.addActionListener(e -> exportReportsToFile());
        buttonPanel.add(exportButton);
        
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createAnalyticsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Title
        JLabel titleLabel = new JLabel("Safety Analytics Dashboard");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(titleLabel, BorderLayout.NORTH);
        
        // Analytics content
        JPanel analyticsPanel = new JPanel(new GridLayout(2, 2, 20, 20));
        
        // Total reports
        analyticsPanel.add(createAnalyticsCard("Total Reports", String.valueOf(reports.size()), Color.BLUE));
        
        // Most common category
        String mostCommon = "None";
        if (!trendData.isEmpty()) {
            mostCommon = Collections.max(trendData.entrySet(), Map.Entry.comparingByValue()).getKey();
        }
        analyticsPanel.add(createAnalyticsCard("Most Common Issue", mostCommon, Color.ORANGE));
        
        // High severity reports
        long highSeverity = reports.stream().filter(r -> "High".equals(r.getSeverity()) || "Urgent".equals(r.getSeverity())).count();
        analyticsPanel.add(createAnalyticsCard("High/Urgent Reports", String.valueOf(highSeverity), Color.RED));
        
        // Resolved reports
        long resolved = reports.stream().filter(r -> "Resolved".equals(r.getStatus())).count();
        analyticsPanel.add(createAnalyticsCard("Resolved Reports", String.valueOf(resolved), Color.GREEN));
        
        panel.add(analyticsPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createAnalyticsCard(String title, String value, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color, 2),
            new EmptyBorder(20, 20, 20, 20)
        ));
        
        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        
        JLabel valueLabel = new JLabel(value, SwingConstants.CENTER);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 36));
        valueLabel.setForeground(color);
        
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        
        return card;
    }
    
    private void showReportDetails(SafetyReport report) {
        JDialog dialog = new JDialog(this, "Report Details", true);
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Report info
        JTextArea detailsArea = new JTextArea();
        detailsArea.setEditable(false);
        detailsArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
        StringBuilder details = new StringBuilder();
        details.append("Report ID: ").append(report.getId()).append("\n\n");
        details.append("Timestamp: ").append(report.getTimestamp().format(DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss"))).append("\n");
        details.append("Category: ").append(report.getCategory()).append("\n");
        details.append("Location: ").append(report.getLocation()).append("\n");
        details.append("Severity: ").append(report.getSeverity()).append("\n");
        details.append("Status: ").append(report.getStatus()).append("\n\n");
        details.append("Description:\n").append(report.getDescription());
        
        detailsArea.setText(details.toString());
        
        JScrollPane scrollPane = new JScrollPane(detailsArea);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Status update buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        
        String[] statuses = {"Under Review", "In Progress", "Resolved", "Needs Follow-up"};
        for (String status : statuses) {
            if (!status.equals(report.getStatus())) {
                JButton statusButton = new JButton("Mark as " + status);
                statusButton.addActionListener(e -> {
                    report.setStatus(status);
                    saveData();
                    dialog.dispose();
                    updateTabsForRole();
                    JOptionPane.showMessageDialog(this, "Report status updated to: " + status);
                });
                buttonPanel.add(statusButton);
            }
        }
        
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dialog.dispose());
        buttonPanel.add(closeButton);
        
        panel.add(buttonPanel, BorderLayout.SOUTH);
        dialog.add(panel);
        dialog.setVisible(true);
    }
    
    private void updateTrendData(String category) {
        trendData.put(category, trendData.getOrDefault(category, 0) + 1);
    }
    
    private void exportReportsToFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File("safety_reports_export.txt"));
        
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                writer.println("Ally Safety Reports Export");
                writer.println("Generated: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss")));
                writer.println("=" .repeat(50));
                writer.println();
                
                for (SafetyReport report : reports) {
                    writer.println("Report ID: " + report.getId());
                    writer.println("Timestamp: " + report.getTimestamp().format(DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss")));
                    writer.println("Category: " + report.getCategory());
                    writer.println("Location: " + report.getLocation());
                    writer.println("Severity: " + report.getSeverity());
                    writer.println("Status: " + report.getStatus());
                    writer.println("Description: " + report.getDescription());
                    writer.println("-".repeat(30));
                    writer.println();
                }
                
                JOptionPane.showMessageDialog(this, "Reports exported successfully to: " + file.getAbsolutePath());
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error exporting reports: " + e.getMessage(), 
                    "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void saveData() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            oos.writeObject(reports);
            oos.writeObject(trendData);
        } catch (IOException e) {
            System.err.println("Error saving data: " + e.getMessage());
        }
    }
    
    @SuppressWarnings("unchecked")
    private void loadData() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(DATA_FILE))) {
            reports = (List<SafetyReport>) ois.readObject();
            trendData = (Map<String, Integer>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            // File doesn't exist or is corrupted, start with empty data
            reports = new ArrayList<>();
            trendData = new HashMap<>();
        }
    }
    
    private void loadSampleResources() {
        // This method could load additional resources from a file
        // For now, resources are hardcoded in createResourcesPanel()
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeel());
            } catch (Exception e) {
                // Use default look and feel
            }
            
            new AllyApp().setVisible(true);
        });
    }
}

// Safety Report Data Class
class SafetyReport implements Serializable {
    private static final long serialVersionUID = 1L;
    private static int nextId = 1;
    
    private int id;
    private String category;
    private String location;
    private String severity;
    private String description;
    private LocalDateTime timestamp;
    private String status;
    
    public SafetyReport(String category, String location, String severity, String description) {
        this.id = nextId++;
        this.category = category;
        this.location = location;
        this.severity = severity;
        this.description = description;
        this.timestamp = LocalDateTime.now();
        this.status = "New";
    }
    
    // Getters
    public int getId() { return id; }
    public String getCategory() { return category; }
    public String getLocation() { return location; }
    public String getSeverity() { return severity; }
    public String getDescription() { return description; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getStatus() { return status; }
    
    // Setters
    public void setStatus(String status) { this.status = status; }
    
    @Override
    public String toString() {
        return "SafetyReport{" +
                "id=" + id +
                ", category='" + category + '\'' +
                ", location='" + location + '\'' +
                ", severity='" + severity + '\'' +
                ", timestamp=" + timestamp +
                ", status='" + status + '\'' +
                '}';
    }
}