// Java Swing class for managing Teams with view, create, edit, delete, and add members functionality
package ui.common;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.*;
import java.util.List;

public class TeamsDialog extends JFrame {
  private final Connection con;
  private final String userRole;
  private final Integer managerId;
  private final JFrame parent;

  public TeamsDialog(JFrame parent, String userRole, Connection connection, Integer managerId) {
    super("Team Management");
    this.parent = parent;
    this.con = connection;
    this.userRole = userRole;
    this.managerId = managerId;

    JTabbedPane tabbedPane = new JTabbedPane();
    JPanel viewPanel = new JPanel(new BorderLayout());
    JPanel listPanel = new JPanel();
    listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));

    Map<Integer, String> projectMap = new HashMap<>();
    Map<Integer, String> departmentMap = new HashMap<>();

    try {
      PreparedStatement ps = con.prepareStatement("SELECT project_id, project_name FROM projects");
      ResultSet rs = ps.executeQuery();
      while (rs.next()) {
        projectMap.put(rs.getInt("project_id"), rs.getString("project_name"));
      }
    } catch (SQLException e) {
      JOptionPane.showMessageDialog(null, "Error loading projects: " + e.getMessage());
    }

    try {
      PreparedStatement ps = con.prepareStatement("SELECT department_id, department_name FROM department");
      ResultSet rs = ps.executeQuery();
      while (rs.next()) {
        departmentMap.put(rs.getInt("department_id"), rs.getString("department_name"));
      }
    } catch (SQLException e) {
      JOptionPane.showMessageDialog(null, "Error loading departments: " + e.getMessage());
    }

    try {
      PreparedStatement stmt = con.prepareStatement("SELECT * FROM team");
      ResultSet rs = stmt.executeQuery();

      JPanel header = new JPanel(new GridLayout(1, 4));
      header.add(new JLabel("Team Name"));
      header.add(new JLabel("Department"));
      header.add(new JLabel("Actions"));
      listPanel.add(header);

      while (rs.next()) {
        JPanel row = new JPanel(new GridLayout(1, 4));
        int teamId = rs.getInt("team_id");
        String teamName = rs.getString("team_name");
        int departmentId = rs.getInt("department_id");
        int manager = rs.getInt("manager_id");

        row.add(new JLabel(teamName));
        row.add(new JLabel(departmentMap.getOrDefault(departmentId, String.valueOf(departmentId))));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton viewBtn = new JButton("View");
        JButton editBtn = new JButton("Edit");
        JButton deleteBtn = new JButton("Delete");
        JButton addMembersBtn = new JButton("View Members");

        if (!"Manager".equalsIgnoreCase(userRole) || !Objects.equals(manager, managerId)) {
          editBtn.setEnabled(false);
          deleteBtn.setEnabled(false);
          addMembersBtn.setEnabled(false);
        }

        buttonPanel.add(viewBtn);
        buttonPanel.add(editBtn);
        buttonPanel.add(deleteBtn);
        buttonPanel.add(addMembersBtn);

        row.add(buttonPanel);
        listPanel.add(row);

        viewBtn.addActionListener(e -> JOptionPane.showMessageDialog(null, "Team Name: " + teamName +
            "\nDepartment: " + departmentMap.getOrDefault(departmentId, String.valueOf(departmentId))));

        editBtn.addActionListener(e -> openTeamForm("Edit Team", teamId, teamName, departmentId, manager, projectMap, departmentMap));

        deleteBtn.addActionListener(e -> {
          int confirm = JOptionPane.showConfirmDialog(null, "Delete this team?", "Confirm", JOptionPane.YES_NO_OPTION);
          if (confirm == JOptionPane.YES_OPTION) {
            try {
              PreparedStatement ps = con.prepareStatement("DELETE FROM team WHERE team_id=?");
              ps.setInt(1, teamId);
              ps.executeUpdate();
              JOptionPane.showMessageDialog(null, "Team deleted.");
              dispose();
              new TeamsDialog(parent, userRole, con, managerId);
            } catch (SQLException ex) {
              JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage());
            }
          }
        });

        addMembersBtn.addActionListener(e -> new ViewMembersDialog(this, con, teamId, teamName));
      }

    } catch (SQLException e) {
      JOptionPane.showMessageDialog(null, "Error loading teams: " + e.getMessage());
    }

    JScrollPane scrollPane = new JScrollPane(listPanel);
    viewPanel.add(scrollPane, BorderLayout.CENTER);
    tabbedPane.addTab("View", viewPanel);

    if ("Manager".equalsIgnoreCase(userRole)) {
      JButton createTeamBtn = new JButton("Create Team");
      createTeamBtn.addActionListener(e -> openTeamForm("Create Team", null, "", 0, managerId, projectMap, departmentMap));
      viewPanel.add(createTeamBtn, BorderLayout.SOUTH);
    }

    add(tabbedPane);
    setSize(700, 500);
    setLocationRelativeTo(null);
    setVisible(true);
  }

  private void openTeamForm(String title, Integer teamId, String teamName, int deptId, int managerId, Map<Integer, String> projectMap, Map<Integer, String> departmentMap) {
    JTextField teamNameField = new JTextField(teamName);

    JComboBox<String> deptDropdown = new JComboBox<>(departmentMap.values().toArray(new String[0]));
    int deptIndex = new ArrayList<>(departmentMap.keySet()).indexOf(deptId);
    if (deptIndex >= 0) deptDropdown.setSelectedIndex(deptIndex);

    JComboBox<String> projectDropdown = new JComboBox<>(projectMap.values().toArray(new String[0]));

    JPanel form = new JPanel(new GridLayout(0, 2));
    form.add(new JLabel("Team Name:"));
    form.add(teamNameField);
    form.add(new JLabel("Department:"));
    form.add(deptDropdown);
    form.add(new JLabel("Assign Project:"));
    form.add(projectDropdown);

    int result = JOptionPane.showConfirmDialog(null, form, title, JOptionPane.OK_CANCEL_OPTION);
    if (result == JOptionPane.OK_OPTION) {
      try {
        String name = teamNameField.getText().trim();
        int dept = (int) departmentMap.keySet().toArray()[deptDropdown.getSelectedIndex()];
        int selectedProjectId = (int) projectMap.keySet().toArray()[projectDropdown.getSelectedIndex()];

        if (teamId == null) {
          PreparedStatement ps = con.prepareStatement("INSERT INTO team(team_name, department_id, manager_id) VALUES (?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
          ps.setString(1, name);
          ps.setInt(2, dept);
          ps.setInt(3, managerId);
          ps.executeUpdate();
          ResultSet keys = ps.getGeneratedKeys();
          if (keys.next()) {
            int newTeamId = keys.getInt(1);
            PreparedStatement tpStmt = con.prepareStatement("INSERT INTO team_project(team_id, project_id) VALUES (?, ?)");
            tpStmt.setInt(1, newTeamId);
            tpStmt.setInt(2, selectedProjectId);
            tpStmt.executeUpdate();
          }
        } else {
          PreparedStatement ps = con.prepareStatement("UPDATE team SET team_name=?, department_id=? WHERE team_id=?");
          ps.setString(1, name);
          ps.setInt(2, dept);
          ps.setInt(3, teamId);
          ps.executeUpdate();
        }

        JOptionPane.showMessageDialog(null, "Team saved successfully.");
        dispose();
        new TeamsDialog(parent, userRole, con, managerId);

      } catch (SQLException | NumberFormatException ex) {
        JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage());
      }
    }
  }

  class ViewMembersDialog extends JDialog {
    public ViewMembersDialog(JFrame parent, Connection con, int teamId, String teamName) {
      super(parent, "Team Members - " + teamName, true);
      setLayout(new BorderLayout());

      DefaultListModel<String> memberListModel = new DefaultListModel<>();
      try {
        PreparedStatement stmt = con.prepareStatement(
            "SELECT e.first_name, e.last_name FROM team_members tm " +
                "JOIN employee e ON tm.employee_id = e.employee_id WHERE tm.team_id = ?");
        stmt.setInt(1, teamId);
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
          String fullName = rs.getString("first_name") + " " + rs.getString("last_name");
          memberListModel.addElement(fullName);
        }
      } catch (SQLException e) {
        JOptionPane.showMessageDialog(null, "Error fetching team members: " + e.getMessage());
      }

      JList<String> memberList = new JList<>(memberListModel);
      JScrollPane scrollPane = new JScrollPane(memberList);
      add(scrollPane, BorderLayout.CENTER);

      JButton addMemberBtn = new JButton("Add New Member");
      addMemberBtn.addActionListener(e -> new AddSingleMemberDialog(this, con, teamId));
      JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
      bottomPanel.add(addMemberBtn);

      add(bottomPanel, BorderLayout.SOUTH);
      setSize(400, 300);
      setLocationRelativeTo(parent);
      setVisible(true);
    }
  }

  class AddSingleMemberDialog extends JDialog {
    public AddSingleMemberDialog(JDialog parent, Connection con, int teamId) {
      super(parent, "Add Member", true);
      setLayout(new BorderLayout());

      JComboBox<String> employeeDropdown = new JComboBox<>();
      Map<String, Integer> employeeMap = new HashMap<>();

      try {
        PreparedStatement stmt = con.prepareStatement(
            "SELECT employee_id, first_name, last_name FROM employee " +
                "WHERE employee_id NOT IN (SELECT employee_id FROM team_members WHERE team_id = ?)");
        stmt.setInt(1, teamId);
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
          int id = rs.getInt("employee_id");
          String name = rs.getString("first_name") + " " + rs.getString("last_name");
          employeeDropdown.addItem(name);
          employeeMap.put(name, id);
        }
      } catch (SQLException e) {
        JOptionPane.showMessageDialog(null, "Error loading employees: " + e.getMessage());
      }

      JPanel centerPanel = new JPanel(new GridLayout(2, 1));
      centerPanel.add(new JLabel("Select Employee:"));
      centerPanel.add(employeeDropdown);

      JButton confirmBtn = new JButton("Add to Team");
      confirmBtn.addActionListener(e -> {
        String selected = (String) employeeDropdown.getSelectedItem();
        if (selected != null) {
          int empId = employeeMap.get(selected);
          try {
            PreparedStatement ps = con.prepareStatement("INSERT INTO team_members(team_id, employee_id) VALUES (?, ?)");
            ps.setInt(1, teamId);
            ps.setInt(2, empId);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(null, "Employee added to team.");
            dispose(); // Close this dialog
            parent.dispose(); // Close parent to refresh
            new ViewMembersDialog((JFrame) parent.getParent(), con, teamId, ""); // reopen refreshed view
          } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Error adding member: " + ex.getMessage());
          }
        }
      });

      add(centerPanel, BorderLayout.CENTER);
      JPanel bottomPanel = new JPanel();
      bottomPanel.add(confirmBtn);
      add(bottomPanel, BorderLayout.SOUTH);

      setSize(300, 150);
      setLocationRelativeTo(parent);
      setVisible(true);
    }
  }


}
