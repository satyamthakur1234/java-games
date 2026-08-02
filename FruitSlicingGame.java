package arcade;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import javax.swing.*;

public class FruitSlicingGame extends JFrame {
    private int score = 0;
    private int missed = 0;
    private String username;
    
    // Physics & Game Engine
    private Timer gameLoop;
    private ArrayList<Fruit> fruits;
    private Random rand;
    private Dimension screen;
    private final double GRAVITY = 0.4;
    
    // Inner class to represent each fruit
    private class Fruit {
        double x, y;
        double velocityX, velocityY;
        int radius;
        Color color;
        
        public Fruit(double x, double y, double velocityX, double velocityY, int radius, Color color) {
            this.x = x;
            this.y = y;
            this.velocityX = velocityX;
            this.velocityY = velocityY;
            this.radius = radius;
            this.color = color;
        }
    }

    public FruitSlicingGame(String username) {
        this.username = username;
        this.fruits = new ArrayList<>();
        this.rand = new Random();
        this.screen = Toolkit.getDefaultToolkit().getScreenSize();

        setTitle("Ninja Fruit Slicing - " + username);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setUndecorated(true);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                
                // Draw all active fruits
                for (Fruit f : fruits) {
                    g.setColor(f.color);
                    g.fillOval((int)f.x - f.radius, (int)f.y - f.radius, f.radius * 2, f.radius * 2);
                }
                
                // Draw UI overlay
                g.setColor(Color.WHITE);
                g.setFont(new Font("Arial", Font.BOLD, 24));
                g.drawString("Score: " + score, 30, 50);
                g.setColor(Color.RED);
                g.drawString("Missed: " + missed + " / 5", 30, 90);
                
                g.setColor(Color.LIGHT_GRAY);
                g.setFont(new Font("Arial", Font.PLAIN, 18));
                g.drawString("Hover mouse over fruits to slice! Press ESC to exit.", 30, 130);
            }
        };
        
        panel.setBackground(Color.DARK_GRAY);
        panel.setFocusable(true);
        
        // Mouse listener for slicing (both moving and dragging work)
        MouseAdapter slicer = new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) { slice(e.getX(), e.getY()); }
            @Override
            public void mouseDragged(MouseEvent e) { slice(e.getX(), e.getY()); }
        };
        panel.addMouseMotionListener(slicer);
        
        // Key listener for exiting
        panel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    gameLoop.stop();
                    dispose();
                    new MainMenu(username);
                }
            }
        });
        
        add(panel);
        setVisible(true);
        panel.requestFocusInWindow();

        // 60 FPS Game Loop Timer
        gameLoop = new Timer(16, e -> {
            updatePhysics();
            spawnFruits();
            panel.repaint();
        });
        gameLoop.start();
    }

    // --- LOGIC: Slice collision detection ---
    private void slice(int mouseX, int mouseY) {
        Iterator<Fruit> it = fruits.iterator();
        while (it.hasNext()) {
            Fruit f = it.next();
            // Calculate distance between mouse and fruit center
            double distance = Math.hypot(mouseX - f.x, mouseY - f.y);
            if (distance <= f.radius) {
                score++;
                it.remove(); // Remove sliced fruit immediately
            }
        }
    }

    // --- LOGIC: Apply gravity and movement ---
    private void updatePhysics() {
        Iterator<Fruit> it = fruits.iterator();
        while (it.hasNext()) {
            Fruit f = it.next();
            f.x += f.velocityX;
            f.y += f.velocityY;
            f.velocityY += GRAVITY; // Gravity pulls it down over time
            
            // If the fruit falls below the screen
            if (f.y - f.radius > screen.height) {
                missed++;
                it.remove();
                
                // Game Over condition
                if (missed >= 5) {
                    gameLoop.stop();
                    JOptionPane.showMessageDialog(this, 
                        "Game Over!\nYou missed 5 fruits.\nFinal Score: " + score, 
                        "Game Over", 
                        JOptionPane.ERROR_MESSAGE);
                    dispose();
                    new MainMenu(username);
                    return;
                }
            }
        }
    }

    // --- LOGIC: Randomly toss new fruits from the bottom ---
    private void spawnFruits() {
        // 3% chance to spawn a fruit every frame
        if (rand.nextDouble() < 0.03) {
            int radius = rand.nextInt(20) + 30; // Radius between 30 and 50
            double startX = rand.nextInt(screen.width - 200) + 100;
            double startY = screen.height + radius;
            
            // Toss upwards with a random velocity
            double velocityY = -(rand.nextDouble() * 5 + 15); // Between -15 and -20
            double velocityX = (rand.nextDouble() * 6) - 3;   // Drift left or right between -3 and 3
            
            // Pick a random fruit color (Apple, Orange, Watermelon, Blueberry, Lemon)
            Color[] colors = {Color.RED, Color.ORANGE, Color.GREEN, new Color(100, 100, 255), Color.YELLOW};
            Color c = colors[rand.nextInt(colors.length)];
            
            fruits.add(new Fruit(startX, startY, velocityX, velocityY, radius, c));
        }
    }
}