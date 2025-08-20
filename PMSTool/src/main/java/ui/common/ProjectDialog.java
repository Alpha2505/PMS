package ui.common;

import java.util.Date;
import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
import ui.ShowMessageDialogBox;

public class ProjectDialog extends JFrame {

    private final Connection con;
    private final ShowMessageDialogBox showMessage;

    public ProjectDialog(JFrame parent, String userRole, Connection connection, Integer employee_id) {
        super("Project Management");
        this.con = connection;
        this.showMessage = new ShowMessageDialogBox();

        JTabbedPane tabbedPane = new JTabbedPane();
        JPanel viewPanel = new JPanel(new BorderLayout());
        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));

        Map<Integer, String> clientMap = new HashMap<>();
        try {
            PreparedStatement clientStmt = con.prepareStatement("SELECT client_id, client_name FROM client");
            ResultSet clientRS = clientStmt.executeQuery();
            while (clientRS.next()) {
                clientMap.put(clientRS.getInt("client_id"), clientRS.getString("client_name"));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Failed to load client list.");
        }

        try {
            PreparedStatement stmt;
            if ("Supervisor".equalsIgnoreCase(userRole)) {
                stmt = con.prepareStatement("SELECT * FROM projects WHERE supervisor_id = ?");
                stmt.setInt(1, employee_id);
            } else if ("manager".equalsIgnoreCase(userRole)) {
                stmt = con.prepareStatement("SELECT * FROM projects");
            } else {
                stmt = con.prepareStatement(
                    "SELECT DISTINCT p.project_id, p.project_name, p.description, p.start_date, p.end_date, p.status, p.client_id " +
                        "FROM projects p " +
                        "JOIN tasks t ON p.project_id = t.project_id " +
                        "WHERE t.assigned_to = ?"
                );
                stmt.setInt(1, employee_id);
            }

            ResultSet rs = stmt.executeQuery();

            JPanel header = new JPanel(new GridLayout(1, 5));
            header.add(new JLabel("Project Name"));
            header.add(new JLabel("Start Date"));
            header.add(new JLabel("End Date"));
            header.add(new JLabel("Status"));
            header.add(new JLabel("Actions"));
            listPanel.add(header);

            while (rs.next()) {
                JPanel row = new JPanel(new GridLayout(1, 5));
                int projectId = rs.getInt("project_id");
                String name = rs.getString("project_name");
                String description = rs.getString("description");
                Date startDate = rs.getDate("start_date");
                Date endDate = rs.getDate("end_date");
                String status = rs.getString("status");
                int clientId = rs.getInt("client_id");
                String clientName = clientMap.getOrDefault(clientId, "Unknown");

                row.add(new JLabel(name));
                row.add(new JLabel(startDate.toString()));
                row.add(new JLabel(endDate != null ? endDate.toString() : "N/A"));
                row.add(new JLabel(status));

                JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                JButton viewBtn = new JButton("View");
                JButton editBtn = new JButton("Edit");
                JButton deleteBtn = new JButton("Delete");

                buttonPanel.add(viewBtn);

                if ("Supervisor".equalsIgnoreCase(userRole)) {
                    buttonPanel.add(editBtn);
                    buttonPanel.add(deleteBtn);
                }

                row.add(buttonPanel);
                listPanel.add(row);

                viewBtn.addActionListener(e -> {
                    JOptionPane.showMessageDialog(null,
                        "Project Name: " + name +
                            "\nDescription: " + description +
                            "\nStart Date: " + startDate +
                            "\nEnd Date: " + (endDate != null ? endDate : "N/A") +
                            "\nStatus: " + status +
                            "\nClient: " + clientName);
                });

                editBtn.addActionListener(e -> {
                    JTextField nameField = new JTextField(name);
                    JTextArea descriptionArea = new JTextArea(description, 3, 20);
                    JTextField startField = new JTextField(startDate.toString());
                    JTextField endField = new JTextField(endDate != null ? endDate.toString() : "");
                    JComboBox<String> statusCombo = new JComboBox<>(new String[]{"Not Started", "In Progress", "Completed", "On Hold"});
                    statusCombo.setSelectedItem(status);

                    JPanel panel = new JPanel(new GridLayout(0, 2));
                    panel.add(new JLabel("Project Name:"));
                    panel.add(nameField);
                    panel.add(new JLabel("Description:"));
                    panel.add(new JScrollPane(descriptionArea));
                    panel.add(new JLabel("Start Date (YYYY-MM-DD):"));
                    panel.add(startField);
                    panel.add(new JLabel("End Date (optional):"));
                    panel.add(endField);
                    panel.add(new JLabel("Status:"));
                    panel.add(statusCombo);

                    int result = JOptionPane.showConfirmDialog(null, panel, "Edit Project", JOptionPane.OK_CANCEL_OPTION);
                    if (result == JOptionPane.OK_OPTION) {
                        try {
                            PreparedStatement updateStmt = con.prepareStatement(
                                "UPDATE projects SET project_name=?, description=?, start_date=?, end_date=?, status=? WHERE project_id=?");

                            updateStmt.setString(1, nameField.getText().trim());
                            updateStmt.setString(2, descriptionArea.getText().trim());
                            updateStmt.setDate(3, java.sql.Date.valueOf(LocalDate.parse(startField.getText().trim())));
                            String endText = endField.getText().trim();
                            if (!endText.isEmpty()) {
                                updateStmt.setDate(4, java.sql.Date.valueOf(LocalDate.parse(endText)));
                            } else {
                                updateStmt.setNull(4, Types.DATE);
                            }
                            updateStmt.setString(5, Objects.requireNonNull(statusCombo.getSelectedItem()).toString());
                            updateStmt.setInt(6, projectId);
                            updateStmt.executeUpdate();

                            JOptionPane.showMessageDialog(null, "Project updated successfully.");
                            dispose();
                            new ProjectDialog(parent, userRole, connection, employee_id);
                        } catch (SQLException | DateTimeParseException ex) {
                            JOptionPane.showMessageDialog(null, "Error updating project: " + ex.getMessage());
                        }
                    }
                });

                deleteBtn.addActionListener(e -> {
                    int confirm = JOptionPane.showConfirmDialog(null, "Delete this project?", "Confirm", JOptionPane.YES_NO_OPTION);
                    if (confirm == JOptionPane.YES_OPTION) {
                        try {
                            PreparedStatement deleteStmt = con.prepareStatement("DELETE FROM projects WHERE project_id=?");
                            deleteStmt.setInt(1, projectId);
                            deleteStmt.executeUpdate();
                            JOptionPane.showMessageDialog(null, "Project deleted successfully.");
                            dispose();
                            new ProjectDialog(parent, userRole, connection, employee_id);
                        } catch (SQLException ex) {
                            JOptionPane.showMessageDialog(null, "Error deleting project: " + ex.getMessage());
                        }
                    }
                });
            }

            JScrollPane scrollPane = new JScrollPane(listPanel);
            viewPanel.add(scrollPane, BorderLayout.CENTER);
            tabbedPane.addTab("View", viewPanel);

            if ("Supervisor".equalsIgnoreCase(userRole)) {
                JPanel createPanel = new JPanel(new GridLayout(0, 2, 5, 5));
                JTextField nameField = new JTextField();
                JTextArea descriptionArea = new JTextArea(3, 20);
                JTextField startField = new JTextField();
                JTextField endField = new JTextField();
                JComboBox<String> statusCombo = new JComboBox<>(new String[]{"Not Started", "In Progress", "Completed", "On Hold"});
                JComboBox<String> clientCombo = new JComboBox<>();
                Map<String, Integer> reverseClientMap = new HashMap<>();

                for (Map.Entry<Integer, String> entry : clientMap.entrySet()) {
                    clientCombo.addItem(entry.getValue());
                    reverseClientMap.put(entry.getValue(), entry.getKey());
                }

                createPanel.add(new JLabel("Project Name:"));
                createPanel.add(nameField);
                createPanel.add(new JLabel("Description:"));
                createPanel.add(new JScrollPane(descriptionArea));
                createPanel.add(new JLabel("Start Date (YYYY-MM-DD):"));
                createPanel.add(startField);
                createPanel.add(new JLabel("End Date (optional):"));
                createPanel.add(endField);
                createPanel.add(new JLabel("Status:"));
                createPanel.add(statusCombo);
                createPanel.add(new JLabel("Client:"));
                createPanel.add(clientCombo);

                JButton createBtn = new JButton("Create Project");
                createPanel.add(new JLabel());
                createPanel.add(createBtn);

                createBtn.addActionListener(e -> {
                    try {
                        String name = nameField.getText().trim();
                        String description = descriptionArea.getText().trim();
                        String start = startField.getText().trim();
                        String end = endField.getText().trim();
                        String status = Objects.requireNonNull(statusCombo.getSelectedItem()).toString();
                        int clientId = reverseClientMap.get(Objects.requireNonNull(clientCombo.getSelectedItem()));

                        if (name.isEmpty() || start.isEmpty()) {
                            JOptionPane.showMessageDialog(null, "Project name and start date are required.");
                            return;
                        }

                        PreparedStatement insertStmt = con.prepareStatement("INSERT INTO projects (project_name, description, start_date, end_date, status, supervisor_id, client_id) VALUES (?, ?, ?, ?, ?, ?, ?)");
                        insertStmt.setString(1, name);
                        insertStmt.setString(2, description);
                        insertStmt.setDate(3, java.sql.Date.valueOf(LocalDate.parse(start)));
                        if (!end.isEmpty()) {
                            insertStmt.setDate(4, java.sql.Date.valueOf(LocalDate.parse(end)));
                        } else {
                            insertStmt.setNull(4, Types.DATE);
                        }
                        insertStmt.setString(5, status);
                        insertStmt.setInt(6, employee_id);
                        insertStmt.setInt(7, clientId);

                        insertStmt.executeUpdate();
                        JOptionPane.showMessageDialog(null, "Project created successfully.");
                        dispose();
                        new ProjectDialog(parent, userRole, connection, employee_id);
                    } catch (SQLException | DateTimeParseException ex) {
                        JOptionPane.showMessageDialog(null, "Error creating project: " + ex.getMessage());
                    }
                });

                tabbedPane.addTab("Create", createPanel);
            }

            setLayout(new BorderLayout());
            add(tabbedPane, BorderLayout.CENTER);
            setSize(850, 500);
            setLocationRelativeTo(parent);
            setVisible(true);

        } catch (Exception ex) {
            ex.printStackTrace();
            showMessage.ShowMessageDialog(this, "Error loading project data.", "Error");
        }
    }
}
