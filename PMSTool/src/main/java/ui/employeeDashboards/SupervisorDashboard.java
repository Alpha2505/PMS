package ui.employeeDashboards;

import java.sql.Connection;
import javax.swing.*;
import ui.LoginScreen;
import ui.common.MilestoneDialog;
import ui.common.ProjectDialog;
import ui.common.TaskDialog;

public class SupervisorDashboard extends JFrame {
    public SupervisorDashboard(Connection connection, Integer employee_id) {
        String userRole = "Supervisor";
        setTitle("Supervisor Dashboard");
        setSize(400, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JButton projectButton = new JButton("Manage Projects");
        projectButton.addActionListener(e -> new ProjectDialog(this, userRole, connection, employee_id));

        JButton milestoneButton = new JButton("Manage Milestones");
        milestoneButton.addActionListener(e -> new MilestoneDialog(this, userRole, connection, employee_id));

        JButton taskButton = new JButton("Manage Tasks");
        taskButton.addActionListener(e -> new TaskDialog(this, userRole, connection, employee_id));

        JButton signOutButton = new JButton("Sign Out");
        signOutButton.addActionListener(e -> {
            dispose();
            new LoginScreen(connection);
        });

        JPanel panel = new JPanel();
        panel.add(projectButton);
        panel.add(milestoneButton);
        panel.add(taskButton);
        panel.add(signOutButton);

        add(panel);
        setVisible(true);
    }
}