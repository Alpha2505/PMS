package ui.employeeDashboards;

import java.sql.Connection;
import javax.swing.*;
import ui.LoginScreen;
import ui.common.ProjectDialog;
import ui.common.TaskDialog;
import ui.common.TeamsDialog;
import ui.common.TimeLogsDialog;

public class ManagerDashboard extends JFrame {
    public ManagerDashboard(Connection connection, Integer employee_id) {
        String userRole = "manager";
        setTitle("Manager Dashboard");
        setSize(400, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JButton teamButton = new JButton("Manage Teams");
        teamButton.addActionListener(e -> new TeamsDialog(this, userRole, connection, employee_id));

        JButton projectButton = new JButton("View Projects");
        projectButton.addActionListener(e -> new ProjectDialog(this, userRole, connection, employee_id));

//        JButton taskButton = new JButton("View Tasks");
//        taskButton.addActionListener(e -> new TaskDialog(this, userRole, connection, employee_id));

        JButton trackTimeLogButton = new JButton("Track Employee Timelogs");
        trackTimeLogButton.addActionListener(e -> new TimeLogsDialog(this, userRole, connection, employee_id));

        JButton signOutButton = new JButton("Sign Out");
        signOutButton.addActionListener(e -> {
            dispose();
            new LoginScreen(connection);
        });

        JPanel panel = new JPanel();
        panel.add(teamButton);
        panel.add(projectButton);
        //panel.add(taskButton);
        panel.add(trackTimeLogButton);
        panel.add(signOutButton);

        add(panel);
        setVisible(true);
    }
}
