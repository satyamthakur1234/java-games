package arcade;

import java.awt.*;
import java.awt.event.*;
import java.util.Random;
import javax.swing.*;

public class TargetShootingGame extends JFrame {
    private String username;
    private Timer gameLoop;
    private Dimension screen;
    
    // Game States
    private enum State { AIMING, SHOOTING }
    private State gameState = State.AIMING;
    
    // Stats & Progression
    private int score = 0;
    private int arrowsLeft = 20;
    private int level = 1;
    
    // Bow & Aiming
    private double bowX = 150;
    private double bowY; // Set to center screen later
    private double bowAngle = 0;
    private int mouseX, mouseY;
    
    // Arrow Physics
    private double arrowX, arrowY;
    private double arrowVX, arrowVY;
    private double arrowAngle = 0;
    private final double SHOOT_POWER = 25.0; // Arrow speed
    private double windForce = 0.0; // Level 2+ mechanic
    
    // Target Mechanics
    private double targetX, targetY;
    private double targetSpeed = 0.0; // Level 3+ mechanic
    private int targetDir = 1;
    private Random rand = new Random();

    public TargetShootingGame(String username) {
        this.username = username;
        this.screen = Toolkit.getDefaultToolkit().getScreenSize();
        
        setTitle("Archery Master - " + username);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setUndecorated(true);
        setLocationRelativeTo(null);

        bowY = screen.height / 2.0;
        targetX = screen.width - 200;
        targetY = screen.height / 2.0;
        resetArrow();

        JPanel canvas = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Background
                g2d.setColor(new Color(30, 40, 50));
                g2d.fillRect(0, 0, getWidth(), getHeight());

                drawTarget(g2d);
                drawBowAndArrow(g2d);
                drawUI(g2d);
            }
        };
        canvas.setFocusable(true);

        // --- MOUSE AIMING ---
        canvas.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                mouseX = e.getX();
                mouseY = e.getY();
                if (gameState == State.AIMING) {
                    // Calculate angle between bow and mouse cursor
                    bowAngle = Math.atan2(mouseY - bowY, mouseX - bowX);
                    // Prevent aiming backward
                    if (bowAngle > Math.PI / 2) bowAngle = Math.PI / 2;
                    if (bowAngle < -Math.PI / 2) bowAngle = -Math.PI / 2;
                    canvas.repaint();
                }
            }
        });

        // --- MOUSE SHOOTING ---
        canvas.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e) && gameState == State.AIMING && arrowsLeft > 0) {
                    gameState = State.SHOOTING;
                    arrowsLeft--;
                    // Fire arrow with trigonometry
                    arrowVX = SHOOT_POWER * Math.cos(bowAngle);
                    arrowVY = SHOOT_POWER * Math.sin(bowAngle);
                }
            }
        });

        // --- EXIT CONTROL ---
        canvas.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) exitGame();
            }
        });

        add(canvas);
        setVisible(true);
        canvas.requestFocusInWindow();

        // --- GAME LOOP (60 FPS) ---
        gameLoop = new Timer(16, e -> {
            updatePhysics();
            canvas.repaint();
        });
        gameLoop.start();
    }

    // --- PHYSICS & COLLISION ---
    private void updatePhysics() {
        // 1. Move Target (Level 3+)
        if (level >= 3) {
            targetY += targetSpeed * targetDir;
            if (targetY < 150 || targetY > screen.height - 150) {
                targetDir *= -1; // Bounce off invisible ceiling/floor
            }
        }

        // 2. Update Arrow
        if (gameState == State.SHOOTING) {
            // Apply wind (gravity-like effect pushing up or down)
            arrowVY += windForce;
            
            // Move arrow
            arrowX += arrowVX;
            arrowY += arrowVY;
            
            // Rotate arrow to face its movement trajectory
            arrowAngle = Math.atan2(arrowVY, arrowVX);

            // 3. Collision Detection
            double distance = Math.hypot(arrowX - targetX, arrowY - targetY);
            
            if (distance <= 60) { // Outer ring radius
                if (distance <= 20) score += 50;      // Bullseye
                else if (distance <= 40) score += 20; // Middle ring
                else score += 10;                     // Outer ring
                
                checkLevelUp();
                resetArrow();
            } 
            // 4. Out of bounds (Miss)
            else if (arrowX > screen.width || arrowY > screen.height || arrowY < 0) {
                resetArrow();
            }
        }
    }

    // --- GAME LOGIC ---
    private void resetArrow() {
        arrowX = bowX;
        arrowY = bowY;
        arrowAngle = bowAngle;
        gameState = State.AIMING;
        
        // Randomize target position slightly between shots
        if (level < 3) {
            targetY = screen.height / 2.0 + (rand.nextInt(300) - 150);
        }

        // Randomize wind for Level 2+
        if (level >= 2) {
            windForce = (rand.nextDouble() - 0.5) * 0.8; // Range: -0.4 to 0.4
        }
        
        if (arrowsLeft <= 0) {
            gameLoop.stop();
            JOptionPane.showMessageDialog(this, 
                "Out of arrows!\nFinal Score: " + score + "\nLevel Reached: " + level, 
                "Game Over", JOptionPane.INFORMATION_MESSAGE);
            exitGame();
        }
    }

    private void checkLevelUp() {
        if (score >= 150 && level == 1) {
            level = 2;
            JOptionPane.showMessageDialog(this, "LEVEL 2: High Winds Detected! Compensate your aim.", "Level Up", JOptionPane.WARNING_MESSAGE);
        } else if (score >= 350 && level == 2) {
            level = 3;
            targetSpeed = 4.0;
            JOptionPane.showMessageDialog(this, "LEVEL 3: Moving Target!", "Level Up", JOptionPane.WARNING_MESSAGE);
        }
    }

    // --- GRAPHICS RENDERING ---
    private void drawBowAndArrow(Graphics2D g2d) {
        g2d.setStroke(new BasicStroke(4));
        
        // Draw Bow
        g2d.setColor(new Color(139, 69, 19)); // Brown
        g2d.drawArc((int)bowX - 40, (int)bowY - 60, 80, 120, (int)Math.toDegrees(-bowAngle) - 90, 180);
        
        // Draw String
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.setStroke(new BasicStroke(1));
        if (gameState == State.AIMING) {
            // String pulled back
            g2d.drawLine((int)bowX + (int)(40 * Math.sin(bowAngle)), (int)bowY - (int)(60 * Math.cos(bowAngle)), (int)bowX - 20, (int)bowY);
            g2d.drawLine((int)bowX - (int)(40 * Math.sin(bowAngle)), (int)bowY + (int)(60 * Math.cos(bowAngle)), (int)bowX - 20, (int)bowY);
        } else {
            // String straight
            g2d.drawLine((int)bowX + (int)(40 * Math.sin(bowAngle)), (int)bowY - (int)(60 * Math.cos(bowAngle)), 
                         (int)bowX - (int)(40 * Math.sin(bowAngle)), (int)bowY + (int)(60 * Math.cos(bowAngle)));
        }

        // Draw Arrow
        double currentArrowAngle = (gameState == State.AIMING) ? bowAngle : arrowAngle;
        
        g2d.translate(arrowX, arrowY);
        g2d.rotate(currentArrowAngle);
        
        g2d.setColor(Color.WHITE);
        g2d.drawLine(-50, 0, 0, 0); // Shaft
        
        // Arrowhead
        g2d.setColor(Color.LIGHT_GRAY);
        Polygon tip = new Polygon(new int[]{0, -10, -10}, new int[]{0, -5, 5}, 3);
        g2d.fillPolygon(tip);
        
        // Fletching (Feathers)
        g2d.setColor(Color.RED);
        g2d.drawLine(-50, 0, -45, -5);
        g2d.drawLine(-50, 0, -45, 5);
        
        g2d.rotate(-currentArrowAngle);
        g2d.translate(-arrowX, -arrowY);
    }

    private void drawTarget(Graphics2D g2d) {
        // Outer Ring (10 pts)
        g2d.setColor(Color.WHITE);
        g2d.fillOval((int)targetX - 60, (int)targetY - 60, 120, 120);
        
        // Middle Ring (20 pts)
        g2d.setColor(Color.RED);
        g2d.fillOval((int)targetX - 40, (int)targetY - 40, 80, 80);
        
        // Bullseye (50 pts)
        g2d.setColor(Color.YELLOW);
        g2d.fillOval((int)targetX - 20, (int)targetY - 20, 40, 40);
        
        // Stand
        g2d.setColor(new Color(101, 67, 33)); // Dark Wood
        g2d.fillRect((int)targetX - 5, (int)targetY + 60, 10, screen.height - ((int)targetY + 60));
    }

    private void drawUI(Graphics2D g2d) {
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 24));
        
        g2d.drawString("Score: " + score, 30, 40);
        g2d.drawString("Arrows: " + arrowsLeft + " / 20", 30, 80);
        g2d.drawString("Level: " + level, 30, 120);
        
        g2d.setFont(new Font("Arial", Font.PLAIN, 18));
        g2d.drawString("ESC to Exit | Move mouse to aim | Left Click to Shoot", 30, screen.height - 30);

        // Wind Indicator
        if (level >= 2) {
            g2d.setFont(new Font("Arial", Font.BOLD, 20));
            String windDir = (windForce > 0) ? "DOWN" : (windForce < 0) ? "UP" : "NONE";
            int windStrength = (int)(Math.abs(windForce) * 100);
            
            g2d.setColor(windForce == 0 ? Color.GREEN : Color.ORANGE);
            g2d.drawString("Wind: " + windStrength + " MPH " + windDir, screen.width / 2 - 100, 50);
        }
    }

    private void exitGame() {
        if (gameLoop != null) gameLoop.stop();
        dispose();
        new MainMenu(username);
    }
}