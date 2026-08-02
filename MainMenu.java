package arcade;

import java.awt.*;
import javax.swing.*;

public class MainMenu extends JFrame {
    private String username;

    public MainMenu(String username) {
        this.username = username;
        setTitle("Java Game Arcade - Player: " + username);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Full screen
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JLabel welcomeLabel = new JLabel("Welcome, " + username + "! Select a Game:", JLabel.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 28));
        welcomeLabel.setBorder(BorderFactory.createEmptyBorder(40, 10, 40, 10));
        add(welcomeLabel, BorderLayout.NORTH);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(3, 2, 20, 20));
        panel.setBorder(BorderFactory.createEmptyBorder(50, 150, 100, 150));

        JButton btnSnake = new JButton("1. Snake Game");
        JButton btnPingPong = new JButton("2. Ping Pong");
        JButton btnChess = new JButton("3. Chess");
        JButton btnTyping = new JButton("4. Typing Speed Test");
        JButton btnShooting = new JButton("5. Shooting Targets");
        JButton btnFruit = new JButton("6. Fruit Slicing");

        Font btnFont = new Font("Arial", Font.BOLD, 18);
        JButton[] buttons = {btnSnake, btnPingPong, btnChess, btnTyping, btnShooting, btnFruit};
        for (JButton b : buttons) {
            b.setFont(btnFont);
            panel.add(b);
        }

        btnSnake.addActionListener(e -> { dispose(); new SnakeGame(username); });
        btnPingPong.addActionListener(e -> { dispose(); new PingPongGame(username); });
        btnChess.addActionListener(e -> { dispose(); new ChessGame(username); });
        btnTyping.addActionListener(e -> { dispose(); new TypingTest(username); });
        btnShooting.addActionListener(e -> { dispose(); new TargetShootingGame(username); });
        btnFruit.addActionListener(e -> { dispose(); new FruitSlicingGame(username); });

        add(panel, BorderLayout.CENTER);
        setVisible(true);
    }
}