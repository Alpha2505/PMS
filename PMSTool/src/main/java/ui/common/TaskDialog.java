package ui.common;

import java.time.format.DateTimeParseException;
import java.util.Date;
import ui.ShowMessageDialogBox;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;

public class TaskDialog extends JFrame {
    private final Connection con;
    private final ShowMessageDialogBox showMessage;
    private final Integer employeeId;

    public TaskDialog(JFrame parent, String userRole, Connection connection, Integer employee_id) {
        super("Task Management");
        this.con = connection;
        this.showMessage = new ShowMessageDialogBox();
        this.employeeId = employee_id;

        JTabbedPane tabbedPane = new JTabbedPane();
        JPanel viewPanel = new JPanel(new BorderLayout());
        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));

        Map<Integer, String> employeeMap = new HashMap<>();
        Map<Integer, String> priorityMap = new HashMap<>();
        Map<Integer, String> projectMap = new HashMap<>();

        try {
            ResultSet empRS = con.createStatement().executeQuery("SELECT employee_id, first_name, last_name FROM employee");
            while (empRS.next()) {
                employeeMap.put(empRS.getInt("employee_id"), empRS.getString("first_name") + " " + empRS.getString("last_name"));
            }

            ResultSet prioRS = con.createStatement().executeQuery("SELECT priority_id, level FROM priority");
            while (prioRS.next()) {
                priorityMap.put(prioRS.getInt("priority_id"), prioRS.getString("level"));
            }

            PreparedStatement projRS = con.prepareStatement("SELECT project_id, project_name FROM projects WHERE supervisor_id = ?");
            projRS.setInt(1, employee_id);
            ResultSet rsProj = projRS.executeQuery();
            while (rsProj.next()) {
                projectMap.put(rsProj.getInt("project_id"), rsProj.getString("project_name"));
            }

            PreparedStatement taskStmt;
            if ("Supervisor".equalsIgnoreCase(userRole)) {
                taskStmt = con.prepareStatement("SELECT * FROM tasks WHERE project_id IN (SELECT project_id FROM projects WHERE supervisor_id = ?)");
                taskStmt.setInt(1, employee_id);
            } else {
                taskStmt = con.prepareStatement("SELECT * FROM tasks WHERE assigned_to = ?");
                taskStmt.setInt(1, employee_id);
            }
            ResultSet rs = taskStmt.executeQuery();

            JPanel header = new JPanel(new GridLayout(1, 7));
            header.add(new JLabel("Task Name"));
            header.add(new JLabel("Due Date"));
            header.add(new JLabel("Status"));
            header.add(new JLabel("Assigned To"));
            header.add(new JLabel("Priority"));
            header.add(new JLabel("Actions"));
            listPanel.add(header);

            while (rs.next()) {
                JPanel row = new JPanel(new GridLayout(1, 7));
                int taskId = rs.getInt("task_id");
                String taskName = rs.getString("task_name");
                Date dueDate = rs.getDate("due_date");
                String status = rs.getString("status");
                int assignedTo = rs.getInt("assigned_to");
                int priorityId = rs.getInt("priority_id");
                int projectId = rs.getInt("project_id");

                row.add(new JLabel(taskName));
                row.add(new JLabel(dueDate != null ? dueDate.toString() : "N/A"));
                row.add(new JLabel(status));
                row.add(new JLabel(employeeMap.getOrDefault(assignedTo, "Unassigned")));
                row.add(new JLabel(priorityMap.getOrDefault(priorityId, "None")));

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

                viewBtn.addActionListener(e -> viewTask(taskId));
                editBtn.addActionListener(e -> editTask(taskId));
                deleteBtn.addActionListener(e -> deleteTask(taskId));
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Error loading tasks: " + ex.getMessage());
        }

        JScrollPane scrollPane = new JScrollPane(listPanel);
        viewPanel.add(scrollPane, BorderLayout.CENTER);
        tabbedPane.addTab("View", viewPanel);

        if ("Supervisor".equalsIgnoreCase(userRole)) {
            JPanel createPanel = new JPanel(new GridLayout(0, 2, 5, 5));
            JTextField taskNameField = new JTextField();
            JTextArea descArea = new JTextArea(3, 20);
            JTextField dueDateField = new JTextField();
            JComboBox<String> statusCombo = new JComboBox<>(new String[]{"Not Started", "In Progress", "Completed", "Blocked"});
            JComboBox<String> assigneeCombo = new JComboBox<>();
            JComboBox<String> priorityCombo = new JComboBox<>();
            JComboBox<String> projectCombo = new JComboBox<>();

            employeeMap.forEach((id, name) -> assigneeCombo.addItem(name));
            priorityMap.forEach((id, level) -> priorityCombo.addItem(level));
            projectMap.forEach((id, name) -> projectCombo.addItem(name));

            createPanel.add(new JLabel("Task Name:"));
            createPanel.add(taskNameField);
            createPanel.add(new JLabel("Description:"));
            createPanel.add(new JScrollPane(descArea));
            createPanel.add(new JLabel("Due Date (YYYY-MM-DD):"));
            createPanel.add(dueDateField);
            createPanel.add(new JLabel("Status:"));
            createPanel.add(statusCombo);
            createPanel.add(new JLabel("Assigned To:"));
            createPanel.add(assigneeCombo);
            createPanel.add(new JLabel("Priority:"));
            createPanel.add(priorityCombo);
            createPanel.add(new JLabel("Project:"));
            createPanel.add(projectCombo);

            JButton createBtn = new JButton("Create Task");
            createPanel.add(new JLabel());
            createPanel.add(createBtn);

            createBtn.addActionListener(e -> {
                try {
                    String name = taskNameField.getText().trim();
                    String desc = descArea.getText().trim();
                    LocalDate dueDate = null;
                    if (!dueDateField.getText().trim().isEmpty()) {
                        try {
                            dueDate = LocalDate.parse(dueDateField.getText().trim());
                        } catch (DateTimeParseException ex) {
                            JOptionPane.showMessageDialog(null, "Invalid expected date format. Use YYYY-MM-DD.");
                            return;
                        }
                    } else {
                        JOptionPane.showMessageDialog(null, "Due date cannot be empty.");
                        return;
                    }
                    String status = (String) statusCombo.getSelectedItem();
                    int assignedId = employeeMap.entrySet().stream().filter(entry -> entry.getValue().equals(assigneeCombo.getSelectedItem())).findFirst().get().getKey();
                    int priorityId = priorityMap.entrySet().stream().filter(entry -> entry.getValue().equals(priorityCombo.getSelectedItem())).findFirst().get().getKey();
                    int projectId = projectMap.entrySet().stream().filter(entry -> entry.getValue().equals(projectCombo.getSelectedItem())).findFirst().get().getKey();

                    PreparedStatement insertStmt = con.prepareStatement("INSERT INTO tasks (project_id, task_name, description, due_date, status, assigned_to, priority_id) VALUES (?, ?, ?, ?, ?, ?, ?)");
                    insertStmt.setInt(1, projectId);
                    insertStmt.setString(2, name);
                    insertStmt.setString(3, desc);
                    insertStmt.setDate(4, java.sql.Date.valueOf(dueDate));
                    insertStmt.setString(5, status);
                    insertStmt.setInt(6, assignedId);
                    insertStmt.setInt(7, priorityId);
                    insertStmt.executeUpdate();
                    JOptionPane.showMessageDialog(null, "Task created successfully.");
                    dispose();
                    new TaskDialog(parent, userRole, con, employee_id);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "Error creating task: " + ex.getMessage());
                }
            });

            tabbedPane.addTab("Create", createPanel);
        }

        add(tabbedPane);
        setSize(900, 500);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void editTask(int taskId) {
        try {
            PreparedStatement stmt = con.prepareStatement("SELECT * FROM tasks WHERE task_id = ?");
            stmt.setInt(1, taskId);
            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) {
                JOptionPane.showMessageDialog(this, "Task not found.");
                return;
            }

            String currentName = rs.getString("task_name");
            String currentDesc = rs.getString("description");
            String currentStatus = rs.getString("status");
            LocalDate currentDueDate = rs.getDate("due_date").toLocalDate();
            int currentAssignee = rs.getInt("assigned_to");
            int currentPriority = rs.getInt("priority_id");

            JDialog editDialog = new JDialog(this, "Edit Task", true);
            editDialog.setLayout(new GridLayout(0, 2, 5, 5));

            JTextField nameField = new JTextField(currentName);
            JTextArea descArea = new JTextArea(currentDesc);
            JTextField dueDateField = new JTextField(currentDueDate.toString());
            JComboBox<String> statusCombo = new JComboBox<>(new String[]{"Not Started", "In Progress", "Completed", "Blocked"});
            statusCombo.setSelectedItem(currentStatus);

            JComboBox<String> assigneeCombo = new JComboBox<>();
            JComboBox<String> priorityCombo = new JComboBox<>();

            Map<Integer, String> employeeMap = new HashMap<>();
            ResultSet empRS = con.createStatement().executeQuery("SELECT employee_id, first_name, last_name FROM employee WHERE role != 'manager'");
            while (empRS.next()) {
                int id = empRS.getInt("employee_id");
                String name = empRS.getString("first_name") + " " + empRS.getString("last_name");
                employeeMap.put(id, name);
                assigneeCombo.addItem(name);
            }
            assigneeCombo.setSelectedItem(employeeMap.get(currentAssignee));

            Map<Integer, String> priorityMap = new HashMap<>();
            ResultSet prioRS = con.createStatement().executeQuery("SELECT priority_id, level FROM priority");
            while (prioRS.next()) {
                int id = prioRS.getInt("priority_id");
                String level = prioRS.getString("level");
                priorityMap.put(id, level);
                priorityCombo.addItem(level);
            }
            priorityCombo.setSelectedItem(priorityMap.get(currentPriority));

            editDialog.add(new JLabel("Task Name:"));
            editDialog.add(nameField);
            editDialog.add(new JLabel("Description:"));
            editDialog.add(new JScrollPane(descArea));
            editDialog.add(new JLabel("Due Date (YYYY-MM-DD):"));
            editDialog.add(dueDateField);
            editDialog.add(new JLabel("Status:"));
            editDialog.add(statusCombo);
            editDialog.add(new JLabel("Assigned To:"));
            editDialog.add(assigneeCombo);
            editDialog.add(new JLabel("Priority:"));
            editDialog.add(priorityCombo);

            JButton saveButton = new JButton("Save Changes");
            editDialog.add(saveButton);

            saveButton.addActionListener(e -> {
                try {
                    String newName = nameField.getText();
                    String newDesc = descArea.getText();
                    LocalDate newDueDate = LocalDate.parse(dueDateField.getText());
                    String newStatus = (String) statusCombo.getSelectedItem();
                    int newAssigneeId = employeeMap.entrySet().stream().filter(entry -> entry.getValue().equals(assigneeCombo.getSelectedItem())).findFirst().get().getKey();
                    int newPriorityId = priorityMap.entrySet().stream().filter(entry -> entry.getValue().equals(priorityCombo.getSelectedItem())).findFirst().get().getKey();

                    PreparedStatement updateStmt = con.prepareStatement("UPDATE tasks SET task_name = ?, description = ?, due_date = ?, status = ?, assigned_to = ?, priority_id = ? WHERE task_id = ?");
                    updateStmt.setString(1, newName);
                    updateStmt.setString(2, newDesc);
                    updateStmt.setDate(3, java.sql.Date.valueOf(newDueDate));
                    updateStmt.setString(4, newStatus);
                    updateStmt.setInt(5, newAssigneeId);
                    updateStmt.setInt(6, newPriorityId);
                    updateStmt.setInt(7, taskId);
                    updateStmt.executeUpdate();
                    JOptionPane.showMessageDialog(this, "Task updated successfully.");
                    editDialog.dispose();
                    dispose();
                    new TaskDialog(this, "Supervisor", con, employeeId);
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this, "Error updating task: " + ex.getMessage());
                }
            });

            editDialog.setSize(400, 300);
            editDialog.setLocationRelativeTo(this);
            editDialog.setVisible(true);

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading task data for edit: " + ex.getMessage());
        }
    }

    private void deleteTask(int taskId) {
        int response = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this task?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (response == JOptionPane.YES_OPTION) {
            try {
                PreparedStatement deleteStmt = con.prepareStatement("DELETE FROM tasks WHERE task_id = ?");
                deleteStmt.setInt(1, taskId);
                deleteStmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Task deleted successfully.");
                dispose();
                new TaskDialog(this, "Supervisor", con, employeeId);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error deleting task: " + ex.getMessage());
            }
        }
    }

    private void viewTask(int taskId) {
        try {
            PreparedStatement stmt = con.prepareStatement("SELECT * FROM tasks WHERE task_id = ?");
            stmt.setInt(1, taskId);
            ResultSet rs = stmt.executeQuery();

            if (!rs.next()) {
                JOptionPane.showMessageDialog(this, "Task not found.");
                return;
            }

            String taskName = rs.getString("task_name");
            String taskDesc = rs.getString("description");
            String status = rs.getString("status");
            LocalDate dueDate = rs.getDate("due_date").toLocalDate();
            int assigneeId = rs.getInt("assigned_to");
            int priorityId = rs.getInt("priority_id");
            int projectId = rs.getInt("project_id");

            // Create a dialog to display task details
            JDialog viewDialog = new JDialog(this, "View Task Details", true);
            viewDialog.setLayout(new GridLayout(0, 2, 5, 5));

            viewDialog.add(new JLabel("Task Name:"));
            viewDialog.add(new JLabel(taskName));
            viewDialog.add(new JLabel("Description:"));
            viewDialog.add(new JLabel(taskDesc));
            viewDialog.add(new JLabel("Due Date:"));
            viewDialog.add(new JLabel(dueDate.toString()));
            viewDialog.add(new JLabel("Status:"));
            viewDialog.add(new JLabel(status));

            // Load assignee names
            Map<Integer, String> employeeMap = new HashMap<>();
            ResultSet empRS = con.createStatement().executeQuery("SELECT employee_id, first_name, last_name FROM employee");
            while (empRS.next()) {
                int id = empRS.getInt("employee_id");
                String name = empRS.getString("first_name") + " " + empRS.getString("last_name");
                employeeMap.put(id, name);
            }

            String assigneeName = employeeMap.getOrDefault(assigneeId, "Unassigned");
            viewDialog.add(new JLabel("Assigned To:"));
            viewDialog.add(new JLabel(assigneeName));

            // Load priority levels
            Map<Integer, String> priorityMap = new HashMap<>();
            ResultSet prioRS = con.createStatement().executeQuery("SELECT priority_id, level FROM priority");
            while (prioRS.next()) {
                int id = prioRS.getInt("priority_id");
                String level = prioRS.getString("level");
                priorityMap.put(id, level);
            }

            String priorityLevel = priorityMap.getOrDefault(priorityId, "None");
            viewDialog.add(new JLabel("Priority:"));
            viewDialog.add(new JLabel(priorityLevel));

            String projectName = "Unknown";
            PreparedStatement projStmt = con.prepareStatement("SELECT project_name FROM projects WHERE project_id = ?");
            projStmt.setInt(1, projectId);
            ResultSet projRS = projStmt.executeQuery();
            if (projRS.next()) {
                projectName = projRS.getString("project_name");
            }

            viewDialog.add(new JLabel("Project:"));
            viewDialog.add(new JLabel(projectName));

            // Close button
            JButton closeButton = new JButton("Close");
            closeButton.addActionListener(e -> viewDialog.dispose());
            viewDialog.add(new JLabel());
            viewDialog.add(closeButton);

            viewDialog.setSize(400, 350); // slightly larger to fit the extra field
            viewDialog.setLocationRelativeTo(this);
            viewDialog.setVisible(true);

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading task details: " + ex.getMessage());
        }
    }

}
