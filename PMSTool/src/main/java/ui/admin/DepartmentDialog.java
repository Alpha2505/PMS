package ui.admin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.*;
import java.awt.*;

public class DepartmentDialog extends JFrame {

  private final Connection con;

  public DepartmentDialog(JFrame parent, Connection connection) {
    super();
    setTitle("Department Management");
    this.con = connection;

    JTabbedPane tabbedPane = new JTabbedPane();

    // ==== View Panel ====
    JPanel viewPanel = new JPanel(new BorderLayout());
    JPanel listPanel = new JPanel();
    listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));

    try {
      PreparedStatement stmt = con.prepareStatement("SELECT * FROM department");
      ResultSet rs = stmt.executeQuery();

      JPanel header = new JPanel(new GridLayout(1, 2));
      header.add(new JLabel("Department Name"));
      header.add(new JLabel(""));
      listPanel.add(header);

      while (rs.next()) {
        int id = rs.getInt("department_id");
        String name = rs.getString("department_name");

        JPanel row = new JPanel(new GridLayout(1, 2));
        row.add(new JLabel(name));

        JPanel btnPanel = new JPanel();
        JButton editBtn = new JButton("Edit");
        JButton deleteBtn = new JButton("Delete");

        btnPanel.add(editBtn);
        btnPanel.add(deleteBtn);
        row.add(btnPanel);
        listPanel.add(row);

        // ===== Edit Functionality =====
        editBtn.addActionListener(e -> {
          String newName = JOptionPane.showInputDialog(this, "Edit Department Name:", name);
          if (newName != null && !newName.trim().isEmpty()) {
            try {
              PreparedStatement updateStmt = con.prepareStatement(
                  "UPDATE department SET department_name = ? WHERE department_id = ?");
              updateStmt.setString(1, newName.trim());
              updateStmt.setInt(2, id);
              int rows = updateStmt.executeUpdate();
              if (rows > 0) {
                JOptionPane.showMessageDialog(this, "Updated successfully");
                dispose();
                new DepartmentDialog(parent, connection);
              }
            } catch (SQLException ex) {
              JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
          }
        });

        // ===== Delete Functionality =====
        deleteBtn.addActionListener(e -> {
          int confirm = JOptionPane.showConfirmDialog(this, "Delete this department?", "Confirm",
              JOptionPane.YES_NO_OPTION);
          if (confirm == JOptionPane.YES_OPTION) {
            try {
              PreparedStatement deleteStmt = con.prepareStatement(
                  "DELETE FROM department WHERE department_id = ?");
              deleteStmt.setInt(1, id);
              int rows = deleteStmt.executeUpdate();
              if (rows > 0) {
                JOptionPane.showMessageDialog(this, "Deleted successfully");
                dispose();
                new DepartmentDialog(parent, connection);
              }
            } catch (SQLException ex) {
              JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
          }
        });
      }

      viewPanel.add(new JScrollPane(listPanel), BorderLayout.CENTER);
      tabbedPane.addTab("View", viewPanel);

      // ==== Create Panel ====
      JPanel createPanel = new JPanel(new GridLayout(2, 2, 10, 10));
      JTextField deptField = new JTextField();
      JButton createBtn = new JButton("Create");

      createPanel.add(new JLabel("Department Name:"));
      createPanel.add(deptField);
      createPanel.add(new JLabel(""));
      createPanel.add(createBtn);

      tabbedPane.addTab("Create", createPanel);

      createBtn.addActionListener(e -> {
        String deptName = deptField.getText().trim();
        if (deptName.isEmpty()) {
          JOptionPane.showMessageDialog(this, "Department name is required.");
          return;
        }

        try {
          PreparedStatement insertStmt = con.prepareStatement(
              "INSERT INTO department (department_name) VALUES (?)");
          insertStmt.setString(1, deptName);
          int rows = insertStmt.executeUpdate();
          if (rows > 0) {
            JOptionPane.showMessageDialog(this, "Department created!");
            deptField.setText("");
            dispose();
            new DepartmentDialog(parent, connection);
          }
        } catch (SQLException ex) {
          JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
      });

    } catch (SQLException ex) {
      JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage());
    }

    setLayout(new BorderLayout());
    add(tabbedPane, BorderLayout.CENTER);
    setSize(400, 300);
    setLocationRelativeTo(parent);
    setVisible(true);
  }
}