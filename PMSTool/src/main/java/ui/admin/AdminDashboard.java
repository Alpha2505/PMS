package ui.admin;

import java.sql.Connection;
import javax.swing.*;
import ui.LoginScreen;

public class AdminDashboard extends JFrame {

  private final Connection con;

  public AdminDashboard(Connection connection) {
    con = connection;
    String userRole = "admin";
    setTitle("Admin Dashboard");
    setSize(400, 200);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setLocationRelativeTo(null);

    JButton employeeButton = new JButton("Manage Employees");
    employeeButton.addActionListener(e -> new EmployeeDialog(this, con));

    JButton departmentButton = new JButton("Manage Departments");
    departmentButton.addActionListener(e -> new DepartmentDialog(this, con));

    JButton clientButton = new JButton("Manage Clients");
    clientButton.addActionListener(e -> new ClientDialog(this, con));

    JButton signOutButton = new JButton("Sign Out");
    signOutButton.addActionListener(e -> {
      dispose();
      new LoginScreen(connection);
    });

    JPanel panel = new JPanel();
    panel.add(employeeButton);
    panel.add(departmentButton);
    panel.add(clientButton);
    panel.add(signOutButton);

    add(panel);
    setVisible(true);
  }
}
