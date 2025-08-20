package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.*;
import ui.admin.AdminDashboard;
import ui.employeeDashboards.ManagerDashboard;
import ui.employeeDashboards.SupervisorDashboard;
import ui.employeeDashboards.TeamMemberDashboard;

public class LoginScreen extends JFrame {

    private JTextField emailField;
    private JPasswordField passwordField;
    private final ShowMessageDialogBox showMessage;
    private Connection con;

    public LoginScreen(Connection connection) {
        con = connection;
        showMessage = new ShowMessageDialogBox();
        setTitle("Project Management System - Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(400, 350);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 5, 10);

        JLabel titleLabel = new JLabel("Welcome to PMS");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(titleLabel, gbc);

        JLabel taglineLabel = new JLabel("Collaborate. Track. Succeed.");
        taglineLabel.setFont(new Font("SansSerif", Font.ITALIC, 14));
        taglineLabel.setForeground(Color.DARK_GRAY);
        gbc.gridy = 1;
        panel.add(taglineLabel, gbc);

        gbc.gridwidth = 1;

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.LINE_END;
        panel.add(new JLabel("Email:"), gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        emailField = new JTextField();
        emailField.setPreferredSize(new Dimension(200, 30));
        panel.add(emailField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.LINE_END;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Password:"), gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        passwordField = new JPasswordField();
        passwordField.setPreferredSize(new Dimension(200, 30));
        panel.add(passwordField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        JButton loginButton = new JButton("Login");
        loginButton.setPreferredSize(new Dimension(100, 30));
        loginButton.addActionListener(this::handleLogin);
        panel.add(loginButton, gbc);

        gbc.gridy = 5;
        JButton forgotPasswordButton = new JButton("Forgot Password?");
        forgotPasswordButton.addActionListener(this::handleForgotPassword);
        panel.add(forgotPasswordButton, gbc);

        add(panel);
        setVisible(true);
    }

    private void handleLogin(ActionEvent e) {
        String email = emailField.getText();
        String password = new String(passwordField.getPassword());

        try {
            PreparedStatement stmt = con.prepareStatement(
                "SELECT role FROM employee WHERE email=? AND login_password=?"
            );
            stmt.setString(1, email);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String role = rs.getString("role").toLowerCase();
                Integer employee_id = rs.getInt("employee_id");
                dispose();
                switch (role) {
                    case "supervisor":
                        new SupervisorDashboard(con, employee_id);
                        break;
                    case "manager":
                        new ManagerDashboard(con, employee_id);
                        break;
                    case "admin":
                        new AdminDashboard(con);
                        break;
                    default:
                        new TeamMemberDashboard(con, employee_id);
                }
            } else {
                showMessage.ShowMessageDialog(this, "Invalid credentials", "Error");
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            showMessage.ShowMessageDialog(this, "Error connecting to database", "Error");
        }
    }

    private void handleForgotPassword(ActionEvent e) {
        String email = JOptionPane.showInputDialog(this, "Enter your email to reset password:");

        if (email != null && !email.trim().isEmpty()) {
            try {
                PreparedStatement stmt = con.prepareStatement(
                    "SELECT email FROM employee WHERE email=?"
                );
                stmt.setString(1, email);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    String newPassword = JOptionPane.showInputDialog(this, "Enter new password:");

                    if (newPassword != null && !newPassword.trim().isEmpty()) {
                        String hashedPassword = newPassword;
                        PreparedStatement updateStmt = con.prepareStatement(
                            "UPDATE employee SET login_password=? WHERE email=?"
                        );
                        updateStmt.setString(1, hashedPassword);
                        updateStmt.setString(2, email);
                        int rowsUpdated = updateStmt.executeUpdate();

                        if (rowsUpdated > 0) {
                            showMessage.ShowMessageDialog(this, "Password updated successfully", "Success");
                        } else {
                            showMessage.ShowMessageDialog(this, "Failed to update password", "Error");
                        }
                    }
                } else {
                    showMessage.ShowMessageDialog(this, "Email not found", "Error");
                }

            } catch (SQLException ex) {
                ex.printStackTrace();
                showMessage.ShowMessageDialog(this, "Error connecting to database", "Error");
            }
        }
    }
}