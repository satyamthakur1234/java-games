package arcade;

import java.awt.*;
import java.util.Random;
import javax.swing.*;

public class TypingTest extends JFrame {
    private String username;
    private JPanel mainPanel;
    private CardLayout cardLayout;

    // Components for Setup Screen
    private JComboBox<String> timeSelector;
    private JComboBox<String> diffSelector;

    // Components for Typing Screen
    private JTextArea targetTextArea;
    private JTextArea inputTextArea;
    private JLabel timerLabel;
    private Timer countdownTimer;
    private int timeLeft;
    private String targetText = "";

    public TypingTest(String username) {
        this.username = username;
        setTitle("Typing Speed Test - " + username);
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Full Screen
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // Build the two screens
        mainPanel.add(createSetupPanel(), "SETUP");
        mainPanel.add(createTypingPanel(), "TYPING");

        add(mainPanel);
        setVisible(true);
    }

    // --- SCREEN 1: Setup Menu ---
    private JPanel createSetupPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.DARK_GRAY);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.gridx = 0; gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("Typing Speed Test", JLabel.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 36));
        title.setForeground(Color.WHITE);
        panel.add(title, gbc);

        String[] times = {"30 Seconds (100-120 words)", "1 Minute (220-240 words)", "2 Minutes (600-620 words)"};
        timeSelector = new JComboBox<>(times);
        timeSelector.setFont(new Font("Arial", Font.PLAIN, 20));
        panel.add(timeSelector, gbc);

        String[] difficulties = {"Easy (Lowercase, no punctuation)", "Hard (Mixed case, numbers, punctuation)"};
        diffSelector = new JComboBox<>(difficulties);
        diffSelector.setFont(new Font("Arial", Font.PLAIN, 20));
        panel.add(diffSelector, gbc);

        JButton startBtn = new JButton("Start Test");
        startBtn.setFont(new Font("Arial", Font.BOLD, 24));
        startBtn.setBackground(Color.GREEN);
        startBtn.addActionListener(e -> startGame());
        panel.add(startBtn, gbc);

        JButton backBtn = new JButton("Back to Arcade Menu");
        backBtn.setFont(new Font("Arial", Font.BOLD, 20));
        backBtn.setBackground(Color.RED);
        backBtn.setForeground(Color.WHITE);
        backBtn.addActionListener(e -> {
            dispose();
            new MainMenu(username);
        });
        panel.add(backBtn, gbc);

        return panel;
    }

    // --- SCREEN 2: Typing Area ---
    private JPanel createTypingPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));

        timerLabel = new JLabel("Time Left: 00:00", JLabel.CENTER);
        timerLabel.setFont(new Font("Arial", Font.BOLD, 28));
        timerLabel.setForeground(Color.RED);
        panel.add(timerLabel, BorderLayout.NORTH);

        JPanel textPanel = new JPanel(new GridLayout(2, 1, 10, 10));

        targetTextArea = new JTextArea();
        targetTextArea.setFont(new Font("Monospaced", Font.PLAIN, 22));
        targetTextArea.setLineWrap(true);
        targetTextArea.setWrapStyleWord(true);
        targetTextArea.setEditable(false);
        targetTextArea.setBackground(new Color(240, 240, 240));
        JScrollPane targetScroll = new JScrollPane(targetTextArea);
        targetScroll.setBorder(BorderFactory.createTitledBorder("Text to Type"));

        inputTextArea = new JTextArea();
        inputTextArea.setFont(new Font("Monospaced", Font.PLAIN, 22));
        inputTextArea.setLineWrap(true);
        inputTextArea.setWrapStyleWord(true);
        JScrollPane inputScroll = new JScrollPane(inputTextArea);
        inputScroll.setBorder(BorderFactory.createTitledBorder("Type Here"));

        textPanel.add(targetScroll);
        textPanel.add(inputScroll);
        panel.add(textPanel, BorderLayout.CENTER);

        JButton endBtn = new JButton("Submit / End Early");
        endBtn.setFont(new Font("Arial", Font.BOLD, 20));
        endBtn.addActionListener(e -> endGame());
        panel.add(endBtn, BorderLayout.SOUTH);

        return panel;
    }

    // --- GAME LOGIC ---
    private void startGame() {
        int timeIdx = timeSelector.getSelectedIndex();
        timeLeft = (timeIdx == 0) ? 30 : (timeIdx == 1) ? 60 : 120;
        
        // Define exact word counts based on your rules
        int minWords = (timeIdx == 0) ? 100 : (timeIdx == 1) ? 220 : 600;
        int maxWords = (timeIdx == 0) ? 120 : (timeIdx == 1) ? 240 : 620;
        boolean isEasy = diffSelector.getSelectedIndex() == 0;

        targetText = generateText(minWords, maxWords, isEasy);
        targetTextArea.setText(targetText);
        inputTextArea.setText("");
        inputTextArea.setEditable(true);
        timerLabel.setText("Time Left: " + timeLeft + "s");

        cardLayout.show(mainPanel, "TYPING");
        inputTextArea.requestFocusInWindow();

        if (countdownTimer != null) countdownTimer.stop();
        countdownTimer = new Timer(1000, e -> {
            timeLeft--;
            timerLabel.setText("Time Left: " + timeLeft + "s");
            if (timeLeft <= 0) endGame();
        });
        countdownTimer.start();
    }

    private void endGame() {
        if (countdownTimer != null) countdownTimer.stop();
        inputTextArea.setEditable(false);

        String typed = inputTextArea.getText().trim();
        int charsTyped = typed.length();
        
        int selectedTime = (timeSelector.getSelectedIndex() == 0 ? 30 : timeSelector.getSelectedIndex() == 1 ? 60 : 120);
        int timeSpent = selectedTime - Math.max(0, timeLeft);
        if (timeSpent == 0) timeSpent = 1;
        
        double minutes = timeSpent / 60.0;
        int wpm = (int) ((charsTyped / 5.0) / minutes);
        
        int correctChars = 0;
        int checkLength = Math.min(charsTyped, targetText.length());
        for (int i = 0; i < checkLength; i++) {
            if (typed.charAt(i) == targetText.charAt(i)) {
                correctChars++;
            }
        }
        
        int accuracy = charsTyped == 0 ? 0 : (int) (((double) correctChars / charsTyped) * 100);

        String msg = "Test Completed!\n\n"
                   + "Words Per Minute (WPM): " + wpm + "\n"
                   + "Accuracy: " + accuracy + "%\n"
                   + "Characters Typed: " + charsTyped + "\n\n"
                   + "Returning to Arcade Menu...";

        JOptionPane.showMessageDialog(this, msg, "Results", JOptionPane.INFORMATION_MESSAGE);
        
        dispose();
        new MainMenu(username);
    }

    // --- COHERENT PARAGRAPH GENERATOR ---
    private String generateText(int minWords, int maxWords, boolean easy) {
        // Easy arrays contain coherent thoughts, entirely lowercase, no punctuation
        String[] easySentences = {
            "learning to write java programs is a very enjoyable process that takes some practice",
            "the quick brown fox jumps gracefully over the very lazy dog resting in the grass",
            "building small arcade games gives you a great feeling of success and understanding",
            "drinking enough water every single day is highly important for a healthy lifestyle",
            "birds sing early in the morning when the bright sun begins to rise in the clear sky",
            "coding can feel a bit difficult at first but it definitely gets much easier over time",
            "i love eating warm pizza covered in melted cheese and tasty tomato sauce",
            "reading good books helps to increase your knowledge and expand your imagination",
            "walking outside in the fresh air makes you feel calm and completely relaxed",
            "we are writing a fun typing game right now using custom code on our computers"
        };

        // Hard arrays contain capital letters, numbers, and special symbols
        String[] hardSentences = {
            "Java 17, released in September 2021, introduced advanced features like sealed classes!",
            "Did you know the speed of light is precisely 299,792,458 meters per second?",
            "Wow! In 2022, Elon Musk purchased Twitter (now X) for a staggering $44.0 billion.",
            "Error 404: The requested URL (/java/arcade-game) was NOT found on this server.",
            "Please email your resume and cover letter to applicant_2026@company-xyz.org.",
            "At 5:30 AM, the stock market plunged by 12.5%, causing massive global panic!",
            "To compile this code, open CMD and type: 'javac -d bin src/arcade/*.java'.",
            "Is the IP address of the local machine always set to 127.0.0.1 (localhost)?",
            "My password must contain 1 uppercase, 1 number, and symbols (e.g., #, @, or *).",
            "The Apollo 11 spacecraft traveled ~240,000 miles to land on the Moon in 1969."
        };

        Random rand = new Random();
        StringBuilder paragraph = new StringBuilder();
        String[] pool = easy ? easySentences : hardSentences;
        int currentWordCount = 0;
        int targetWords = rand.nextInt((maxWords - minWords) + 1) + minWords; // Target between min and max

        while (currentWordCount < targetWords) {
            String sentence = pool[rand.nextInt(pool.length)];
            int sentenceWords = sentence.split(" ").length;
            
            // If adding this sentence exceeds our exact max words, we cut the sentence short
            if (currentWordCount + sentenceWords > targetWords) {
                String[] words = sentence.split(" ");
                for (int i = 0; i < (targetWords - currentWordCount); i++) {
                    paragraph.append(words[i]).append(" ");
                }
                break; // Target reached
            } else {
                paragraph.append(sentence).append(" ");
                currentWordCount += sentenceWords;
            }
        }
        
        return paragraph.toString().trim();
    }
}