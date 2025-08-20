package ui.admin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.*;
import java.awt.*;
import ui.ShowMessageDialogBox;

public class ClientDialog extends JFrame {

  private final Connection con;
  private final ShowMessageDialogBox showMessage;

  public ClientDialog(JFrame parent, Connection connection) {
    super();
    setTitle("Client Management");
    con = connection;
    showMessage = new ShowMessageDialogBox();

    JTabbedPane tabbedPane = new JTabbedPane();

    JPanel viewPanel = new JPanel(new BorderLayout());
    JPanel listPanel = new JPanel();
    listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));

    try {
      PreparedStatement stmt = con.prepareStatement(
          "SELECT * FROM client"
      );

      ResultSet rs = stmt.executeQuery();

      JPanel header = new JPanel(new GridLayout(1, 5));
      header.add(new JLabel("Client Name"));
      header.add(new JLabel(""));
      listPanel.add(header);

      listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS)); // Vertical list of rows

      while (rs.next()) {
        JPanel row = new JPanel(new GridLayout(1, 5)); // 1 row, 5 columns (adjust as needed)

        String name = rs.getString("client_name");
        String email = rs.getString("email");
        String phone = rs.getString("phone");
        String address = rs.getString("address");
        String contact = rs.getString("contact_person");
        String company = rs.getString("company_name");
        int id = rs.getInt("client_id");

        row.add(new JLabel(name));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton viewBtn = new JButton("View");
        JButton editBtn = new JButton("Edit");
        JButton deleteBtn = new JButton("Delete");

        buttonPanel.add(viewBtn);
        buttonPanel.add(editBtn);
        buttonPanel.add(deleteBtn);

        row.add(buttonPanel);
        listPanel.add(row);

        //Edit client data
        editBtn.addActionListener(e -> {
          JDialog editDialog = new JDialog((JFrame) null, "Edit Client Details", true);
          editDialog.setLayout(new GridLayout(0, 2, 10, 10));

          JTextField nameField = new JTextField(name);
          JTextField emailField = new JTextField(email);
          JTextField phoneField = new JTextField(phone);
          JTextField companyField = new JTextField(company);
          JTextField contactField = new JTextField(contact);
          JTextField addressField = new JTextField(address);

          editDialog.add(new JLabel("Client Name:"));
          editDialog.add(nameField);
          editDialog.add(new JLabel("Email:"));
          editDialog.add(emailField);
          editDialog.add(new JLabel("Phone:"));
          editDialog.add(phoneField);
          editDialog.add(new JLabel("Company:"));
          editDialog.add(companyField);
          editDialog.add(new JLabel("Contact Person:"));
          editDialog.add(contactField);
          editDialog.add(new JLabel("Address:"));
          editDialog.add(addressField);

          JButton saveBtn = new JButton("Save");
          JButton cancelBtn = new JButton("Cancel");

          editDialog.add(saveBtn);
          editDialog.add(cancelBtn);

          saveBtn.addActionListener(ev -> {
            try {
              String updateQuery = "UPDATE client SET client_name = ?, email = ?, phone = ?, company_name = ?, contact_person = ?, address = ? WHERE client_id = ?";
              PreparedStatement updateStmt = con.prepareStatement(updateQuery);
              updateStmt.setString(1, nameField.getText());
              updateStmt.setString(2, emailField.getText());
              updateStmt.setString(3, phoneField.getText());
              updateStmt.setString(4, companyField.getText());
              updateStmt.setString(5, contactField.getText());
              updateStmt.setString(6, addressField.getText());
              updateStmt.setInt(7, id);

              int rowsUpdated = updateStmt.executeUpdate();
              if (rowsUpdated > 0) {
                JOptionPane.showMessageDialog(editDialog, "Client updated successfully!");
                editDialog.dispose();
                dispose();
                new ClientDialog(parent, connection);
              } else {
                JOptionPane.showMessageDialog(editDialog, "Update failed.");
              }
            } catch (SQLException ex) {
              JOptionPane.showMessageDialog(editDialog, "Database error: " + ex.getMessage());
            }
          });

          cancelBtn.addActionListener(ev -> editDialog.dispose());

          editDialog.pack();
          editDialog.setLocationRelativeTo(null);
          editDialog.setVisible(true);
        });

        //Delete client data
        deleteBtn.addActionListener(e -> {
          int confirm = JOptionPane.showConfirmDialog(
              null,
              "Are you sure you want to delete this client?",
              "Confirm Deletion",
              JOptionPane.YES_NO_OPTION
          );

          if (confirm == JOptionPane.YES_OPTION) {
            try {
              String deleteQuery = "DELETE FROM client WHERE client_id = ?";
              PreparedStatement deleteStmt = con.prepareStatement(deleteQuery);
              deleteStmt.setInt(1, id);

              int rowsDeleted = deleteStmt.executeUpdate();
              if (rowsDeleted > 0) {
                JOptionPane.showMessageDialog(null, "Client deleted successfully.");
                listPanel.remove(row);
                listPanel.revalidate();
                listPanel.repaint();
                dispose();
                new ClientDialog(parent, connection);
              } else {
                JOptionPane.showMessageDialog(null, "Client deletion failed.");
              }
            } catch (SQLException ex) {
              JOptionPane.showMessageDialog(null, "Database error: " + ex.getMessage());
            }
          }
        });

        //View a particular client details
        viewBtn.addActionListener(e -> {
          JDialog dialog = new JDialog((JFrame) null, "Client Details", true);
          dialog.setLayout(new GridLayout(0, 1, 10, 5)); // auto rows, 1 column

          dialog.add(new JLabel("Name: " + name));
          dialog.add(new JLabel("Email: " + email));
          dialog.add(new JLabel("Phone: " + phone));
          dialog.add(new JLabel("Company: " + company));
          dialog.add(new JLabel("Contact Person: " + contact));
          dialog.add(new JLabel("Address: " + address));

          JButton closeBtn = new JButton("Close");
          closeBtn.addActionListener(ev -> dialog.dispose());
          dialog.add(closeBtn);

          dialog.pack();
          dialog.setLocationRelativeTo(null);
          dialog.setVisible(true);
        });
      }

      JScrollPane scrollPane = new JScrollPane(listPanel);
      viewPanel.add(scrollPane, BorderLayout.CENTER);
      tabbedPane.addTab("View", viewPanel);

      // Create a new client
      JPanel createPanel = new JPanel(new GridLayout(0, 2, 5, 5));

      JTextField nameField = new JTextField();
      JTextField emailField = new JTextField();
      JTextField contactField = new JTextField();
      JTextField companyField = new JTextField();
      JTextField phoneField = new JTextField();
      JTextField addressField = new JTextField();

      createPanel.add(new JLabel("Client Name:"));
      createPanel.add(nameField);

      createPanel.add(new JLabel("Email:"));
      createPanel.add(emailField);

      createPanel.add(new JLabel("Contact Person:"));
      createPanel.add(contactField);

      createPanel.add(new JLabel("Company Name:"));
      createPanel.add(companyField);

      createPanel.add(new JLabel("Phone:"));
      createPanel.add(phoneField);

      createPanel.add(new JLabel("Address:"));
      createPanel.add(addressField);

      JButton createBtn = new JButton("Create Client");
      createPanel.add(new JLabel());
      createPanel.add(createBtn);

      tabbedPane.addTab("Create", createPanel);

      createBtn.addActionListener(e -> {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String contact = contactField.getText().trim();
        String company = companyField.getText().trim();
        String phone = phoneField.getText().trim();
        String address = addressField.getText().trim();

        if (name.isEmpty() || email.isEmpty()) {
          JOptionPane.showMessageDialog(null, "Client name and email are required.");
          return;
        }

        try {
          String insertQuery = "INSERT INTO client (client_name, contact_person, company_name, email, phone, address) VALUES (?, ?, ?, ?, ?, ?)";
          PreparedStatement stmt1 = con.prepareStatement(insertQuery);
          stmt1.setString(1, name);
          stmt1.setString(2, contact);
          stmt1.setString(3, company);
          stmt1.setString(4, email);
          stmt1.setString(5, phone);
          stmt1.setString(6, address);

          int rowsInserted = stmt1.executeUpdate();
          if (rowsInserted > 0) {
            JOptionPane.showMessageDialog(null, "Client created successfully!");
            nameField.setText("");
            emailField.setText("");
            contactField.setText("");
            companyField.setText("");
            phoneField.setText("");
            addressField.setText("");
            dispose();
            new ClientDialog(parent, connection);
          } else {
            JOptionPane.showMessageDialog(null, "Failed to create client.");
          }

        } catch (SQLException ex) {
          JOptionPane.showMessageDialog(null, "Database error: " + ex.getMessage());
        }
      });

      setLayout(new BorderLayout());
      add(tabbedPane, BorderLayout.CENTER);

      setSize(500, 400);
      setLocationRelativeTo(parent);
      setVisible(true);

    } catch (Exception ex) {
      ex.printStackTrace();
      showMessage.ShowMessageDialog(this, "Error connecting to database", "Error");
    }
  }
}