package arcade;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            String username = JOptionPane.showInputDialog(
                null, 
                "Enter your name to enter the Arcade:", 
                "Java Game Arcade", 
                JOptionPane.QUESTION_MESSAGE
            );

            if (username != null && !username.trim().isEmpty()) {
                JOptionPane.showMessageDialog(
                    null, 
                    "Welcome, " + username.trim() + "!", 
                    "Greeting", 
                    JOptionPane.INFORMATION_MESSAGE
                );
                new MainMenu(username.trim());
            } else {
                JOptionPane.showMessageDialog(
                    null, 
                    "Name cannot be empty. Exiting.", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE
                );
            }
        });
    }
}