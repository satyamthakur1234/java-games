package arcade;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class PingPongGame extends JFrame implements ActionListener, KeyListener {
    private int ballX, ballY, ballXDir = -3, ballYDir = 3;
    private int paddle1Y, paddle2Y;
    private Timer timer;
    private String username;
    private Dimension screen;
    private GamePanel gamePanel; // Added a custom JPanel

    public PingPongGame(String username) {
        this.username = username;
        setTitle("Ping Pong - " + username);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setUndecorated(true);
        setLocationRelativeTo(null);
        
        screen = Toolkit.getDefaultToolkit().getScreenSize();
        ballX = screen.width / 2;
        ballY = screen.height / 2;
        paddle1Y = screen.height / 2 - 50;
        paddle2Y = screen.height / 2 - 50;

        // Initialize and add the double-buffered panel
        gamePanel = new GamePanel();
        setContentPane(gamePanel);

        addKeyListener(this);
        setFocusable(true);

        timer = new Timer(10, this);
        timer.start();
        setVisible(true);
        requestFocusInWindow();
    }

    // Inner class for flicker-free rendering
    private class GamePanel extends JPanel {
        public GamePanel() {
            setBackground(Color.BLACK); // Let the panel handle the background
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g); // This clears the screen smoothly using double buffering

            g.setColor(Color.white);
            g.fillRect(40, paddle1Y, 20, 100);
            g.fillRect(screen.width - 60, paddle2Y, 20, 100);
            g.fillOval(ballX, ballY, 25, 25);
            g.setFont(new Font("Arial", Font.BOLD, 20));
            g.drawString("Controls: W/S to move. Press ESC to quit.", 50, 50);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        ballX += ballXDir;
        ballY += ballYDir;

        // Ball collision with top/bottom walls
        if (ballY < 0 || ballY > screen.height - 40) ballYDir *= -1;
        
        // Simple AI for the right paddle
        if (paddle2Y + 50 < ballY) paddle2Y += 3; else paddle2Y -= 3;

        // Ball collision with paddles
        if (ballX <= 60 && ballY >= paddle1Y && ballY <= paddle1Y + 100) ballXDir *= -1;
        if (ballX >= screen.width - 80 && ballY >= paddle2Y && ballY <= paddle2Y + 100) ballXDir *= -1;

        // Win/Loss condition
        if (ballX < 0 || ballX > screen.width) {
            timer.stop();
            JOptionPane.showMessageDialog(this, "Game Over!");
            dispose();
            new MainMenu(username);
        }
        
        // Repaint the panel instead of the frame
        gamePanel.repaint(); 
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_W && paddle1Y > 0) paddle1Y -= 30;
        if (e.getKeyCode() == KeyEvent.VK_S && paddle1Y < screen.height - 100) paddle1Y += 30;
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) { timer.stop(); dispose(); new MainMenu(username); }
    }
    
    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}
}