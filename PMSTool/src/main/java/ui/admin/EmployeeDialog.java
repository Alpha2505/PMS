package ui.admin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.swing.*;
import java.awt.*;
import ui.ShowMessageDialogBox;

public class EmployeeDialog extends JFrame {

  private final Connection con;
  private final ShowMessageDialogBox showMessage;

  public EmployeeDialog(JFrame parent, Connection connection) {
    super();
    setTitle("Employee Management");
    this.con = connection;
    this.showMessage = new ShowMessageDialogBox();

    JTabbedPane tabbedPane = new JTabbedPane();

    // View Panel
    JPanel viewPanel = new JPanel(new BorderLayout());
    JPanel listPanel = new JPanel();
    listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));

    try {
      PreparedStatement stmt = con.prepareStatement(
          "SELECT e.employee_id, e.first_name, e.last_name, e.email, e.role, " +
              "e.department_id, d.department_name FROM employee e " +
              "LEFT JOIN department d ON e.department_id = d.department_id"
      );
      ResultSet rs = stmt.executeQuery();

      JPanel header = new JPanel(new GridLayout(1, 5));
      header.add(new JLabel("Employee Name"));
      header.add(new JLabel("Email"));
      header.add(new JLabel("Role"));
      header.add(new JLabel("Department"));
      header.add(new JLabel("Actions"));
      listPanel.add(header);

      while (rs.next()) {
        int id = rs.getInt("employee_id");
        String firstName = rs.getString("first_name");
        String lastName = rs.getString("last_name");
        String email = rs.getString("email");
        String role = rs.getString("role");
        String department = rs.getString("department_name");
        int departmentId = rs.getInt("department_id");

        JPanel row = new JPanel(new GridLayout(1, 5));
        row.add(new JLabel(firstName + " " + lastName));
        row.add(new JLabel(email));
        row.add(new JLabel(role));
        row.add(new JLabel(department != null ? department : "N/A"));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton viewBtn = new JButton("View");
        JButton editBtn = new JButton("Edit");
        JButton deleteBtn = new JButton("Delete");
        buttonPanel.add(viewBtn);
        buttonPanel.add(editBtn);
        buttonPanel.add(deleteBtn);
        row.add(buttonPanel);

        listPanel.add(row);

        // View
        viewBtn.addActionListener(e -> {
          JDialog dialog = new JDialog(this, "Employee Details", true);
          dialog.setLayout(new GridLayout(0, 1));
          dialog.add(new JLabel("Name: " + firstName + " " + lastName));
          dialog.add(new JLabel("Email: " + email));
          dialog.add(new JLabel("Role: " + role));
          dialog.add(new JLabel("Department: " + (department != null ? department : "N/A")));
          JButton closeBtn = new JButton("Close");
          closeBtn.addActionListener(ev -> dialog.dispose());
          dialog.add(closeBtn);
          dialog.pack();
          dialog.setLocationRelativeTo(this);
          dialog.setVisible(true);
        });

        // Edit
        editBtn.addActionListener(e -> {
          JDialog editDialog = new JDialog(this, "Edit Employee", true);
          editDialog.setLayout(new GridLayout(0, 2, 10, 10));

          JTextField firstField = new JTextField(firstName);
          JTextField lastField = new JTextField(lastName);
          JTextField emailField = new JTextField(email);
          JComboBox<String> roleBox = new JComboBox<>(new String[]{"employee", "supervisor", "manager", "admin"});
          roleBox.setSelectedItem(role);
          JComboBox<DepartmentItem> deptBox = getDepartmentDropdown();

          // Set current department
          for (int i = 0; i < deptBox.getItemCount(); i++) {
            if (deptBox.getItemAt(i).id == departmentId) {
              deptBox.setSelectedIndex(i);
              break;
            }
          }

          editDialog.add(new JLabel("First Name:"));
          editDialog.add(firstField);
          editDialog.add(new JLabel("Last Name:"));
          editDialog.add(lastField);
          editDialog.add(new JLabel("Email:"));
          editDialog.add(emailField);
          editDialog.add(new JLabel("Role:"));
          editDialog.add(roleBox);
          editDialog.add(new JLabel("Department:"));
          editDialog.add(deptBox);

          JButton saveBtn = new JButton("Save");
          JButton cancelBtn = new JButton("Cancel");
          editDialog.add(saveBtn);
          editDialog.add(cancelBtn);

          saveBtn.addActionListener(ev -> {
            try {
              String updateQuery = "UPDATE employee SET first_name = ?, last_name = ?, email = ?, login_password = ?, role = ?, department_id = ? WHERE employee_id = ?";
              PreparedStatement updateStmt = con.prepareStatement(updateQuery);
              updateStmt.setString(1, firstField.getText().trim());
              updateStmt.setString(2, lastField.getText().trim());
              updateStmt.setString(3, emailField.getText().trim());
              updateStmt.setString(5, roleBox.getSelectedItem().toString());
              updateStmt.setObject(6, deptBox.getSelectedItem() != null ? ((DepartmentItem) deptBox.getSelectedItem()).id : null);
              updateStmt.setInt(7, id);

              int rowsUpdated = updateStmt.executeUpdate();
              if (rowsUpdated > 0) {
                JOptionPane.showMessageDialog(editDialog, "Employee updated successfully!");
                editDialog.dispose();
                dispose();
                new EmployeeDialog(parent, connection);
              }
            } catch (SQLException ex) {
              JOptionPane.showMessageDialog(editDialog, "Error: " + ex.getMessage());
            }
          });

          cancelBtn.addActionListener(ev -> editDialog.dispose());
          editDialog.pack();
          editDialog.setLocationRelativeTo(this);
          editDialog.setVisible(true);
        });


        // Delete
        deleteBtn.addActionListener(e -> {
          int confirm = JOptionPane.showConfirmDialog(this, "Delete this employee?", "Confirm",
              JOptionPane.YES_NO_OPTION);
          if (confirm == JOptionPane.YES_OPTION) {
            try {
              PreparedStatement deleteStmt = con.prepareStatement(
                  "DELETE FROM employee WHERE employee_id = ?");
              deleteStmt.setInt(1, id);
              if (deleteStmt.executeUpdate() > 0) {
                JOptionPane.showMessageDialog(this, "Employee deleted.");
                dispose();
                new EmployeeDialog(parent, connection);
              }
            } catch (SQLException ex) {
              JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
          }
        });
      }

      JScrollPane scrollPane = new JScrollPane(listPanel);
      viewPanel.add(scrollPane, BorderLayout.CENTER);
      tabbedPane.addTab("View", viewPanel);

      // Create Panel
      JPanel createPanel = new JPanel(new GridLayout(0, 2, 10, 10));
      JTextField firstField = new JTextField();
      JTextField lastField = new JTextField();
      JTextField emailField = new JTextField();
      JTextField passwordField = new JTextField(); // optional hashing
      JComboBox<String> roleBox = new JComboBox<>(
          new String[]{"employee", "supervisor", "manager", "admin"});
      JComboBox<DepartmentItem> deptBox = getDepartmentDropdown();

      createPanel.add(new JLabel("First Name:"));
      createPanel.add(firstField);
      createPanel.add(new JLabel("Last Name:"));
      createPanel.add(lastField);
      createPanel.add(new JLabel("Email:"));
      createPanel.add(emailField);
      createPanel.add(new JLabel("Password:"));
      createPanel.add(passwordField);
      createPanel.add(new JLabel("Role:"));
      createPanel.add(roleBox);
      createPanel.add(new JLabel("Department:"));
      createPanel.add(deptBox);

      JButton createBtn = new JButton("Create Employee");
      createPanel.add(new JLabel());
      createPanel.add(createBtn);
      tabbedPane.addTab("Create", createPanel);

      createBtn.addActionListener(e -> {
        try {
          String query = "INSERT INTO employee (first_name, last_name, email, login_password, role, department_id) VALUES (?, ?, ?, ?, ?, ?)";
          PreparedStatement insertStmt = con.prepareStatement(query);
          insertStmt.setString(1, firstField.getText().trim());
          insertStmt.setString(2, lastField.getText().trim());
          insertStmt.setString(3, emailField.getText().trim());
          insertStmt.setString(4, passwordField.getText().trim()); // hash if needed
          insertStmt.setString(5, roleBox.getSelectedItem().toString());
          insertStmt.setObject(6,
              deptBox.getSelectedItem() != null ? ((DepartmentItem) deptBox.getSelectedItem()).id
                  : null);

          if (insertStmt.executeUpdate() > 0) {
            JOptionPane.showMessageDialog(this, "Employee created!");
            dispose();
            new EmployeeDialog(parent, connection);
          }
        } catch (SQLException ex) {
          JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
      });

      // Finalize
      setLayout(new BorderLayout());
      add(tabbedPane, BorderLayout.CENTER);
      setSize(800, 500);
      setLocationRelativeTo(parent);
      setVisible(true);

    } catch (Exception ex) {
      ex.printStackTrace();
      showMessage.ShowMessageDialog(this, "Error loading employee data", "Error");
    }
  }

  private JComboBox<DepartmentItem> getDepartmentDropdown() {
    JComboBox<DepartmentItem> comboBox = new JComboBox<>();
    try {
      Statement stmt = con.createStatement();
      ResultSet rs = stmt.executeQuery("SELECT department_id, department_name FROM department");
      while (rs.next()) {
        comboBox.addItem(new DepartmentItem(rs.getInt("department_id"), rs.getString("department_name")));
      }
    } catch (SQLException ex) {
      JOptionPane.showMessageDialog(this, "Error loading departments: " + ex.getMessage());
    }
    return comboBox;
  }

  private static class DepartmentItem {

    int id;
    String name;

    DepartmentItem(int id, String name) {
      this.id = id;
      this.name = name;
    }

    // Override toString to return only the department name
    @Override
    public String toString() {
      return name; // Only display the department name
    }
  }

}