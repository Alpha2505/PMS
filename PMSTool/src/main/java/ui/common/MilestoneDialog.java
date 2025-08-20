package ui.common;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.Date;
import javax.swing.*;
import java.awt.*;
import ui.ShowMessageDialogBox;

public class MilestoneDialog extends JFrame {

    private final Connection con;
    private final ShowMessageDialogBox showMessage;

    public MilestoneDialog(JFrame parent, String userRole, Connection connection, Integer employee_id) {
        super("Milestone Management");
        con = connection;
        showMessage = new ShowMessageDialogBox();

        JTabbedPane tabbedPane = new JTabbedPane();
        JPanel viewPanel = new JPanel(new BorderLayout());
        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));

        Map<Integer, String> projectMap = new HashMap<>();
        try {
            PreparedStatement projectStmt = con.prepareStatement("SELECT project_id, project_name FROM projects");
            ResultSet projectRS = projectStmt.executeQuery();
            while (projectRS.next()) {
                projectMap.put(projectRS.getInt("project_id"), projectRS.getString("project_name"));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Failed to load project list.");
        }

        try {
            PreparedStatement stmt = con.prepareStatement("SELECT * FROM milestone WHERE supervisor_id = ?");
            stmt.setInt(1, employee_id);
            ResultSet rs = stmt.executeQuery();

            JPanel header = new JPanel(new GridLayout(1, 4));
            header.add(new JLabel("Project Name"));
            header.add(new JLabel("Milestone Name"));
            header.add(new JLabel("Status"));
            header.add(new JLabel("Actions"));
            listPanel.add(header);

            while (rs.next()) {
                JPanel row = new JPanel(new GridLayout(1, 4));

                int id = rs.getInt("milestone_id");
                int projId = rs.getInt("project_id");
                String projName = projectMap.getOrDefault(projId, "Unknown Project");
                String name = rs.getString("milestone_name");
                String status = rs.getString("status");
                Date actualDue = rs.getDate("actual_due_date");
                Date expectedDue = rs.getDate("expected_due_date");

                row.add(new JLabel(projName));
                row.add(new JLabel(name));
                row.add(new JLabel(status));

                JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                JButton viewBtn = new JButton("View");
                JButton editBtn = new JButton("Edit");
                JButton deleteBtn = new JButton("Delete");

                buttonPanel.add(viewBtn);
                buttonPanel.add(editBtn);
                buttonPanel.add(deleteBtn);
                row.add(buttonPanel);
                listPanel.add(row);

                viewBtn.addActionListener(e -> {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    JOptionPane.showMessageDialog(null,
                        "Project: " + projName +
                            "\nName: " + name +
                            "\nStatus: " + status +
                            "\nExpected Due: " + sdf.format(expectedDue) +
                            (actualDue != null ? ("\nActual Due: " + sdf.format(actualDue)) : "")
                    );
                });

                editBtn.addActionListener(e -> {
                    JTextField nameField = new JTextField(name);
                    JTextField expectedDueField = new JTextField(expectedDue.toString());
                    JTextField actualDueField = new JTextField(actualDue != null ? actualDue.toString() : "");
                    JComboBox<String> statusCombo = new JComboBox<>(new String[]{"Not Started", "In Progress", "Completed", "Delayed"});
                    statusCombo.setSelectedItem(status);

                    JPanel panel = new JPanel(new GridLayout(0, 2));
                    panel.add(new JLabel("Milestone Name:"));
                    panel.add(nameField);
                    panel.add(new JLabel("Expected Due Date (YYYY-MM-DD):"));
                    panel.add(expectedDueField);
                    panel.add(new JLabel("Actual Due Date (optional):"));
                    panel.add(actualDueField);
                    panel.add(new JLabel("Status:"));
                    panel.add(statusCombo);

                    int result = JOptionPane.showConfirmDialog(null, panel, "Edit Milestone", JOptionPane.OK_CANCEL_OPTION);
                    if (result == JOptionPane.OK_OPTION) {
                        try {
                            PreparedStatement updateStmt = con.prepareStatement("UPDATE milestone SET milestone_name = ?, expected_due_date = ?, actual_due_date = ?, status = ? WHERE milestone_id = ?");
                            updateStmt.setString(1, nameField.getText().trim());
                            try {
                                LocalDate expectedDate = LocalDate.parse(expectedDueField.getText().trim());
                                updateStmt.setDate(2, java.sql.Date.valueOf(expectedDate));
                            } catch (DateTimeParseException ex) {
                                JOptionPane.showMessageDialog(null, "Invalid expected date format. Use YYYY-MM-DD.");
                                return;
                            }
                            String actual = actualDueField.getText().trim();
                            if (!actual.isEmpty()) {
                                try {
                                    LocalDate actualDate = LocalDate.parse(actual);
                                    updateStmt.setDate(3, java.sql.Date.valueOf(actualDate));
                                } catch (DateTimeParseException ex) {
                                    JOptionPane.showMessageDialog(null, "Invalid expected date format. Use YYYY-MM-DD.");
                                    return;
                                }
                            }
                            else {
                                updateStmt.setNull(3, Types.DATE);
                            }
                            updateStmt.setString(4, Objects.requireNonNull(statusCombo.getSelectedItem()).toString());
                            updateStmt.setInt(5, id);

                            updateStmt.executeUpdate();
                            JOptionPane.showMessageDialog(null, "Milestone updated successfully.");
                            dispose();
                            new MilestoneDialog(parent, userRole, connection, employee_id);
                        } catch (SQLException ex) {
                            JOptionPane.showMessageDialog(null, "Database error: " + ex.getMessage());
                        }
                    }
                });

                deleteBtn.addActionListener(e -> {
                    int confirm = JOptionPane.showConfirmDialog(null, "Delete this milestone?", "Confirm", JOptionPane.YES_NO_OPTION);
                    if (confirm == JOptionPane.YES_OPTION) {
                        try {
                            PreparedStatement deleteStmt = con.prepareStatement("DELETE FROM milestone WHERE milestone_id = ?");
                            deleteStmt.setInt(1, id);
                            deleteStmt.executeUpdate();
                            JOptionPane.showMessageDialog(null, "Milestone deleted successfully.");
                            dispose();
                            new MilestoneDialog(parent, userRole, connection, employee_id);
                        } catch (SQLException ex) {
                            JOptionPane.showMessageDialog(null, "Database error: " + ex.getMessage());
                        }
                    }
                });
            }

            JScrollPane scrollPane = new JScrollPane(listPanel);
            viewPanel.add(scrollPane, BorderLayout.CENTER);
            tabbedPane.addTab("View", viewPanel);

            // Create tab
            JPanel createPanel = new JPanel(new GridLayout(0, 2, 5, 5));
            JTextField nameField = new JTextField();
            JTextField expectedDueField = new JTextField();
            JTextField actualDueField = new JTextField();
            JComboBox<String> statusCombo = new JComboBox<>(new String[]{"Not Started", "In Progress", "Completed", "Delayed"});

            JComboBox<String> projectDropdown = new JComboBox<>();
            Map<String, Integer> reverseProjectMap = new HashMap<>();
            for (Map.Entry<Integer, String> entry : projectMap.entrySet()) {
                projectDropdown.addItem(entry.getValue());
                reverseProjectMap.put(entry.getValue(), entry.getKey());
            }

            createPanel.add(new JLabel("Project:"));
            createPanel.add(projectDropdown);
            createPanel.add(new JLabel("Milestone Name:"));
            createPanel.add(nameField);
            createPanel.add(new JLabel("Expected Due Date (YYYY-MM-DD):"));
            createPanel.add(expectedDueField);
            createPanel.add(new JLabel("Actual Due Date (optional):"));
            createPanel.add(actualDueField);
            createPanel.add(new JLabel("Status:"));
            createPanel.add(statusCombo);

            JButton createBtn = new JButton("Create Milestone");
            createPanel.add(new JLabel());
            createPanel.add(createBtn);

            tabbedPane.addTab("Create", createPanel);

            createBtn.addActionListener(e -> {
                try {
                    String name = nameField.getText().trim();
                    String expected = expectedDueField.getText().trim();
                    String actual = actualDueField.getText().trim();
                    String status = Objects.requireNonNull(statusCombo.getSelectedItem()).toString();
                    String selectedProjectName = (String) projectDropdown.getSelectedItem();

                    if (name.isEmpty() || expected.isEmpty() || selectedProjectName == null) {
                        JOptionPane.showMessageDialog(null, "All fields except actual due date are required.");
                        return;
                    }

                    int selectedProjectId = reverseProjectMap.get(selectedProjectName);

                    PreparedStatement insertStmt = con.prepareStatement("INSERT INTO milestone (project_id, supervisor_id, milestone_name, expected_due_date, actual_due_date, status) VALUES (?, ?, ?, ?, ?, ?)");
                    insertStmt.setInt(1, selectedProjectId);
                    insertStmt.setInt(2, employee_id != null ? employee_id : 1); // Use actual or fallback supervisor_id
                    insertStmt.setString(3, name);
                    insertStmt.setDate(4, java.sql.Date.valueOf(LocalDate.parse(expected)));
                    if (!actual.isEmpty()) {
                        insertStmt.setDate(5, java.sql.Date.valueOf(LocalDate.parse(actual)));
                    } else {
                        insertStmt.setNull(5, Types.DATE);
                    }
                    insertStmt.setString(6, status);

                    insertStmt.executeUpdate();
                    JOptionPane.showMessageDialog(null, "Milestone created successfully.");
                    dispose();
                    new MilestoneDialog(parent, userRole, connection, employee_id);
                } catch (SQLException | DateTimeParseException ex) {
                    JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage());
                }
            });

            setLayout(new BorderLayout());
            add(tabbedPane, BorderLayout.CENTER);
            setSize(700, 500);
            setLocationRelativeTo(parent);
            setVisible(true);

        } catch (Exception ex) {
            ex.printStackTrace();
            showMessage.ShowMessageDialog(this, "Error loading milestone data.", "Error");
        }
    }
}
