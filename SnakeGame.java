package arcade;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;

public class SnakeGame extends JFrame implements ActionListener {
    private int tileSize = 30;
    private ArrayList<Tile> snakeBody;
    private Tile snakeHead, food;
    private Random random;
    private Timer gameLoop;
    private int velocityX = 1, velocityY = 0;
    private boolean gameOver = false;
    private String username;

    private class Tile {
        int x, y;
        Tile(int x, int y) { this.x = x; this.y = y; }
    }

    public SnakeGame(String username) {
        this.username = username;
        setTitle("Snake Game - " + username);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setUndecorated(true);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                draw(g);
            }
        };
        panel.setBackground(Color.black);
        panel.setFocusable(true);
        panel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_UP && velocityY != 1) { velocityX = 0; velocityY = -1; }
                else if (e.getKeyCode() == KeyEvent.VK_DOWN && velocityY != -1) { velocityX = 0; velocityY = 1; }
                else if (e.getKeyCode() == KeyEvent.VK_LEFT && velocityX != 1) { velocityX = -1; velocityY = 0; }
                else if (e.getKeyCode() == KeyEvent.VK_RIGHT && velocityX != -1) { velocityX = 1; velocityY = 0; }
                else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) { dispose(); new MainMenu(username); }
            }
        });
        add(panel);

        snakeHead = new Tile(5, 5);
        snakeBody = new ArrayList<>();
        food = new Tile(10, 10);
        random = new Random();
        placeFood();

        gameLoop = new Timer(100, this);
        gameLoop.start();
        setVisible(true);
        panel.requestFocusInWindow();
    }

    void placeFood() {
        food.x = random.nextInt(Toolkit.getDefaultToolkit().getScreenSize().width / tileSize);
        food.y = random.nextInt(Toolkit.getDefaultToolkit().getScreenSize().height / tileSize);
    }

    public void move() {
        if (snakeHead.x == food.x && snakeHead.y == food.y) {
            snakeBody.add(new Tile(food.x, food.y));
            placeFood();
        }
        for (int i = snakeBody.size() - 1; i >= 0; i--) {
            Tile part = snakeBody.get(i);
            if (i == 0) { part.x = snakeHead.x; part.y = snakeHead.y; }
            else { Tile prev = snakeBody.get(i - 1); part.x = prev.x; part.y = prev.y; }
        }
        snakeHead.x += velocityX;
        snakeHead.y += velocityY;

        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int maxCols = screen.width / tileSize;
        int maxRows = screen.height / tileSize;

        for (Tile part : snakeBody) {
            if (snakeHead.x == part.x && snakeHead.y == part.y) gameOver = true;
        }
        if (snakeHead.x < 0 || snakeHead.x >= maxCols || snakeHead.y < 0 || snakeHead.y >= maxRows) gameOver = true;
    }

    public void draw(Graphics g) {
        g.setColor(Color.red);
        g.fillRect(food.x * tileSize, food.y * tileSize, tileSize, tileSize);

        g.setColor(Color.green);
        g.fillRect(snakeHead.x * tileSize, snakeHead.y * tileSize, tileSize, tileSize);
        for (Tile part : snakeBody) {
            g.fillRect(part.x * tileSize, part.y * tileSize, tileSize, tileSize);
        }

        g.setColor(Color.white);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("Score: " + snakeBody.size() + " | Press ESC to exit to menu", 30, 40);

        if (gameOver) {
            gameLoop.stop();
            g.setColor(Color.red);
            g.setFont(new Font("Arial", Font.BOLD, 50));
            g.drawString("GAME OVER! Returning to menu...", screenCenter().x - 300, screenCenter().y);
            Timer t = new Timer(2000, e -> { dispose(); new MainMenu(username); });
            t.setRepeats(false);
            t.start();
        }
    }

    private Point screenCenter() {
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        return new Point(d.width / 2, d.height / 2);
    }

    @Override
    public void actionPerformed(ActionEvent e) { move(); repaint(); }
}