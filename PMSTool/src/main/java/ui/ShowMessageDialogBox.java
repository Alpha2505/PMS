package ui;

import java.awt.Component;
import javax.swing.JOptionPane;

public class ShowMessageDialogBox {

  public void ShowMessageDialog(Component parent, String message, String messageType) {
    int messageTypeConstant;

    switch (messageType.toLowerCase()) {
      case "success":
        messageTypeConstant = JOptionPane.INFORMATION_MESSAGE;
        break;
      case "error":
        messageTypeConstant = JOptionPane.ERROR_MESSAGE;
        break;
      case "warning":
        messageTypeConstant = JOptionPane.WARNING_MESSAGE;
        break;
      default:
        messageTypeConstant = JOptionPane.PLAIN_MESSAGE;
        break;
    }

    JOptionPane.showMessageDialog(
        parent,
        message,
        messageType,
        messageTypeConstant
    );
  }

}
