package ui.common;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;

public class TimeLogsDialog extends JFrame {

  private final Connection con;

  public TimeLogsDialog(JFrame parent, String userRole, Connection connection, Integer employee_id) {
    super("Time Log Management");
    this.con = connection;

    JTabbedPane tabbedPane = new JTabbedPane();
    JPanel viewPanel = new JPanel(new BorderLayout());
    JPanel listPanel = new JPanel();
    listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));

    try {
      PreparedStatement stmt;
      if ("Manager".equalsIgnoreCase(userRole)) {
        stmt = con.prepareStatement("SELECT tl.* FROM time_logs tl JOIN team_members tm ON tl.added_by = tm.employee_id JOIN team t ON tm.team_id = t.team_id WHERE t.manager_id = ?");
        stmt.setInt(1, employee_id);
      } else {
        stmt = con.prepareStatement("SELECT * FROM time_logs WHERE added_by = ?");
        stmt.setInt(1, employee_id);
      }

      ResultSet rs = stmt.executeQuery();

      JPanel header = new JPanel(new GridLayout(1, 5));
      header.add(new JLabel("Date"));
      header.add(new JLabel("Hours"));
      header.add(new JLabel("Task Name"));
      if (!"employee".equalsIgnoreCase(userRole)) {
        header.add(new JLabel("Employee Name"));
      } else {
        header.add(new JLabel(""));
      }
      listPanel.add(header);

      while (rs.next()) {
        JPanel row = new JPanel(new GridLayout(1, 5));
        int logId = rs.getInt("log_id");
        String date = rs.getDate("date").toString();
        String hours = rs.getString("hours_spent");
        int taskId = rs.getInt("task_id");
        int trackedBy = rs.getInt("tracked_by");
        int addedBy = rs.getInt("added_by");

        // Fetch task name using taskId
        String taskName = "Unknown";
        try {
          PreparedStatement taskStmt = con.prepareStatement("SELECT task_name FROM tasks WHERE task_id = ?");
          taskStmt.setInt(1, taskId);
          ResultSet taskRs = taskStmt.executeQuery();
          if (taskRs.next()) {
            taskName = taskRs.getString("task_name");
          }
          taskRs.close();
          taskStmt.close();
        } catch (SQLException ex) {
          taskName = "Error fetching name";
        }

        row.add(new JLabel(date));
        row.add(new JLabel(hours));
        row.add(new JLabel(taskName));

        if (!"employee".equalsIgnoreCase(userRole)) {
          String employeeName = "";
          try {
            PreparedStatement empStmt = con.prepareStatement("SELECT first_name, last_name FROM employee WHERE employee_id = ?");
            empStmt.setInt(1, addedBy);
            ResultSet empRs = empStmt.executeQuery();
            if (empRs.next()) {
              employeeName = empRs.getString("first_name") + " " + empRs.getString("last_name");
            }
            empRs.close();
            empStmt.close();
          } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Error occured: " + ex.getMessage());
          }
          row.add(new JLabel(employeeName));
        }

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton editBtn = new JButton("Edit");
        JButton deleteBtn = new JButton("Delete");

        if ("employee".equalsIgnoreCase(userRole)) {
          buttonPanel.add(editBtn);
          buttonPanel.add(deleteBtn);
        }
        row.add(buttonPanel);
        listPanel.add(row);

        editBtn.addActionListener(e -> {
          JTextField hoursField = new JTextField(hours);
          JTextField dateField = new JTextField(date);
          JPanel panel = new JPanel(new GridLayout(0, 2));
          panel.add(new JLabel("Hours:"));
          panel.add(hoursField);
          panel.add(new JLabel("Date (YYYY-MM-DD):"));
          panel.add(dateField);

          int result = JOptionPane.showConfirmDialog(null, panel, "Edit Time Log", JOptionPane.OK_CANCEL_OPTION);
          if (result == JOptionPane.OK_OPTION) {
            try {
              PreparedStatement updateStmt = con.prepareStatement("UPDATE time_logs SET hours_spent=?, date=? WHERE log_id=?");
              updateStmt.setBigDecimal(1, new java.math.BigDecimal(hoursField.getText().trim()));
              updateStmt.setDate(2, java.sql.Date.valueOf(LocalDate.parse(dateField.getText().trim())));
              updateStmt.setInt(3, logId);
              updateStmt.executeUpdate();

              JOptionPane.showMessageDialog(null, "Time Log updated successfully.");
              dispose();
              new TimeLogsDialog(parent, userRole, connection, employee_id);
            } catch (Exception ex) {
              JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage());
            }
          }
        });

        deleteBtn.addActionListener(e -> {
          int confirm = JOptionPane.showConfirmDialog(null, "Delete this time log?", "Confirm", JOptionPane.YES_NO_OPTION);
          if (confirm == JOptionPane.YES_OPTION) {
            try {
              PreparedStatement deleteStmt = con.prepareStatement("DELETE FROM time_logs WHERE log_id=?");
              deleteStmt.setInt(1, logId);
              deleteStmt.executeUpdate();
              JOptionPane.showMessageDialog(null, "Time Log deleted successfully.");
              dispose();
              new TimeLogsDialog(parent, userRole, connection, employee_id);
            } catch (SQLException ex) {
              JOptionPane.showMessageDialog(null, "Error deleting time log: " + ex.getMessage());
            }
          }
        });
      }

      JScrollPane scrollPane = new JScrollPane(listPanel);
      viewPanel.add(scrollPane, BorderLayout.CENTER);
      tabbedPane.addTab("View", viewPanel);

    } catch (SQLException e) {
      JOptionPane.showMessageDialog(null, "Failed to load time logs.");
    }

    if ("Employee".equalsIgnoreCase(userRole)) {
      JPanel createPanel = new JPanel(new GridLayout(0, 2, 5, 5));
      JTextField hoursField = new JTextField();
      JTextField dateField = new JTextField();

      // Dropdown for tasks
      Map<String, Integer> taskMap = new HashMap<>();
      JComboBox<String> taskDropdown = new JComboBox<>();

      try {
        String taskQuery = "SELECT task_id, task_name FROM tasks WHERE assigned_to = ?";
        PreparedStatement taskStmt = con.prepareStatement(taskQuery);
        taskStmt.setInt(1, employee_id);
        ResultSet taskRs = taskStmt.executeQuery();

        while (taskRs.next()) {
          int id = taskRs.getInt("task_id");
          String name = taskRs.getString("task_name");
          taskMap.put(name, id);
          taskDropdown.addItem(name);
        }

        if (taskMap.isEmpty()) {
          taskDropdown.addItem("No tasks assigned");
          taskDropdown.setEnabled(false);
        }

      } catch (SQLException ex) {
        JOptionPane.showMessageDialog(null, "Failed to load tasks: " + ex.getMessage());
      }

      createPanel.add(new JLabel("Hours Spent:"));
      createPanel.add(hoursField);
      createPanel.add(new JLabel("Date (YYYY-MM-DD):"));
      createPanel.add(dateField);
      createPanel.add(new JLabel("Task:"));
      createPanel.add(taskDropdown);

      JButton createBtn = new JButton("Add Time Log");
      createPanel.add(new JLabel());
      createPanel.add(createBtn);

      createBtn.addActionListener(e -> {
        try {
          if (!taskDropdown.isEnabled()) {
            JOptionPane.showMessageDialog(null, "No valid task selected.");
            return;
          }

          double hours = Double.parseDouble(hoursField.getText().trim());
          String date = dateField.getText().trim();
          String selectedTaskName = (String) taskDropdown.getSelectedItem();
          int taskId = taskMap.get(selectedTaskName);

          // Get manager_id
          String query = "SELECT t.manager_id FROM team t " +
              "JOIN team_members tm ON t.team_id = tm.team_id " +
              "WHERE tm.employee_id = ?";
          PreparedStatement managerStmt = con.prepareStatement(query);
          managerStmt.setInt(1, employee_id);
          ResultSet rs = managerStmt.executeQuery();

          if (rs.next()) {
            int managerId = rs.getInt("manager_id");
            PreparedStatement insertStmt = con.prepareStatement("INSERT INTO time_logs (hours_spent, date, tracked_by, added_by, task_id) VALUES (?, ?, ?, ?, ?)");
            insertStmt.setBigDecimal(1, new java.math.BigDecimal(hours));
            insertStmt.setDate(2, java.sql.Date.valueOf(LocalDate.parse(date)));
            insertStmt.setInt(3, managerId);
            insertStmt.setInt(4, employee_id);
            insertStmt.setInt(5, taskId);
            insertStmt.executeUpdate();

            JOptionPane.showMessageDialog(null, "Time Log added successfully.");
            dispose();
            new TimeLogsDialog(parent, userRole, connection, employee_id);
          } else {
            JOptionPane.showMessageDialog(null, "Unable to determine manager for this task.");
          }
        } catch (DateTimeParseException dtpe) {
          JOptionPane.showMessageDialog(null, "Invalid date format. Use YYYY-MM-DD.");
        } catch (Exception ex) {
          JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage());
        }
      });

      tabbedPane.addTab("Create", createPanel);
    }

    add(tabbedPane);
    setSize(800, 500);
    setLocationRelativeTo(parent);
    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    setVisible(true);
  }
}