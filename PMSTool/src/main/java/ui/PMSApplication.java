package ui;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import ui.admin.AdminDashboard;
import ui.employeeDashboards.ManagerDashboard;
import ui.employeeDashboards.SupervisorDashboard;
import ui.employeeDashboards.TeamMemberDashboard;

public class PMSApplication {

  private static final ShowMessageDialogBox showMessage = new ShowMessageDialogBox();

  public static void main(String[] args) {
    String url = "jdbc:mysql://localhost:3306/pms";
    String username = "root";
    String password = "Vhy456$46";

    try {
      Connection conn = DriverManager.getConnection(url, username, password);
      System.out.println("Connected to MySQL!");
      //new SupervisorDashboard(conn, 3);
      //new ManagerDashboard(conn, 1);
      new TeamMemberDashboard(conn, 5);
      //new AdminDashboard(conn);
      //new LoginScreen(conn);
    } catch (SQLException e) {
      System.out.println("Connection failed: " + e.getMessage());
      showMessage.ShowMessageDialog(null, "Connection failed: " + e.getMessage(), "Error");
    }
  }
}
