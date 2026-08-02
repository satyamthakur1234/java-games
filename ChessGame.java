package arcade;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class ChessGame extends JFrame {
    private String username;
    private JButton[][] squares = new JButton[8][8];
    private boolean isWhiteTurn = true;
    private JLabel statusLabel;

    private int selectedRow = -1;
    private int selectedCol = -1;

    private final String[] WHITE_PIECES = {"♖", "♘", "♗", "♕", "♔", "♗", "♘", "♖"};
    private final String WHITE_PAWN = "♙";
    
    private final String[] BLACK_PIECES = {"♜", "♞", "♝", "♛", "♚", "♝", "♞", "♜"};
    private final String BLACK_PAWN = "♟";

    private final Color LIGHT_SQUARE = new Color(240, 217, 181);
    private final Color DARK_SQUARE = new Color(181, 136, 99);
    private final Color HIGHLIGHT_COLOR = new Color(173, 216, 230); // Blue (Selected)
    private final Color POSSIBLE_MOVE_COLOR = new Color(144, 238, 144); // Green (Valid moves)

    public ChessGame(String username) {
        this.username = username;
        setTitle("Chess Arcade Mini - " + username);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setUndecorated(true);
        setLayout(new BorderLayout());

        statusLabel = new JLabel("Turn: WHITE", JLabel.CENTER);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 28));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        statusLabel.setBackground(Color.DARK_GRAY);
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setOpaque(true);
        add(statusLabel, BorderLayout.NORTH);

        JPanel boardPanel = new JPanel(new GridLayout(8, 8));
        boardPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 4));
        initializeBoard(boardPanel);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(Color.DARK_GRAY);
        JButton backBtn = new JButton("Exit to Menu (Press ESC)");
        backBtn.setFont(new Font("Arial", Font.BOLD, 18));
        backBtn.addActionListener(e -> exitToMenu());
        bottomPanel.add(backBtn);
        add(bottomPanel, BorderLayout.SOUTH);

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) exitToMenu();
            }
        });
        setFocusable(true);
        setVisible(true);
    }

    private void initializeBoard(JPanel boardPanel) {
        Font pieceFont = new Font("SansSerif", Font.PLAIN, 60);

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                JButton square = new JButton("");
                square.setFont(pieceFont);
                square.setFocusPainted(false);
                square.setForeground(Color.BLACK);
                
                setSquareColor(square, r, c);

                if (r == 0) square.setText(BLACK_PIECES[c]);
                else if (r == 1) square.setText(BLACK_PAWN);
                else if (r == 6) square.setText(WHITE_PAWN);
                else if (r == 7) square.setText(WHITE_PIECES[c]);

                final int row = r;
                final int col = c;
                square.addActionListener(e -> handleSquareClick(row, col));
                
                squares[r][c] = square;
                boardPanel.add(square);
            }
        }
        add(boardPanel, BorderLayout.CENTER);
    }

    private void handleSquareClick(int r, int c) {
        String piece = squares[r][c].getText();

        // 1. SELECT PIECE
        if (selectedRow == -1) {
            // Only allow selection of your own pieces
            if (!piece.isEmpty() && isWhitePiece(piece) == isWhiteTurn) {
                selectedRow = r;
                selectedCol = c;
                squares[r][c].setBackground(HIGHLIGHT_COLOR);
                highlightPossibleMoves(r, c, piece);
            }
        } 
        // 2. EXECUTE MOVE (or deselect)
        else {
            clearHighlights(); // Clear all green and blue squares

            // Deselect if clicking the same square
            if (r == selectedRow && c == selectedCol) {
                selectedRow = -1; selectedCol = -1;
                return;
            }

            String selectedPiece = squares[selectedRow][selectedCol].getText();
            
            // Check if the move is entirely legal (including King safety)
            if (isMoveLegal(selectedRow, selectedCol, r, c, selectedPiece)) {
                
                // Perform move
                squares[r][c].setText(selectedPiece);
                squares[selectedRow][selectedCol].setText("");
                
                // Pawn Promotion (Auto-promotes to Queen)
                if (selectedPiece.equals("♙") && r == 0) squares[r][c].setText("♕");
                if (selectedPiece.equals("♟") && r == 7) squares[r][c].setText("♛");

                // End Turn
                selectedRow = -1; 
                selectedCol = -1;
                isWhiteTurn = !isWhiteTurn;
                
                // Check Game Over Conditions
                checkGameState();
            } else {
                // Invalid move, simply deselect
                selectedRow = -1; 
                selectedCol = -1;
            }
            requestFocusInWindow();
        }
    }

    // --- MOVE HIGHLIGHTING ---
    private void highlightPossibleMoves(int sr, int sc, String piece) {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                if (isMoveLegal(sr, sc, r, c, piece)) {
                    squares[r][c].setBackground(POSSIBLE_MOVE_COLOR);
                }
            }
        }
    }

    private void clearHighlights() {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                setSquareColor(squares[r][c], r, c);
            }
        }
    }

    // --- ADVANCED RULE ENGINE (KING SAFETY) ---

    // Checks rules AND simulates the move to ensure King is not left in check
    private boolean isMoveLegal(int startR, int startC, int endR, int endC, String piece) {
        if (!isValidMove(startR, startC, endR, endC, piece)) return false;

        // SIMULATE THE MOVE
        String originalTarget = squares[endR][endC].getText();
        squares[endR][endC].setText(piece);
        squares[startR][startC].setText("");

        // Check if this move puts/leaves own king in check
        boolean kingInDanger = isKingInCheck(isWhitePiece(piece));

        // UNDO THE MOVE
        squares[startR][startC].setText(piece);
        squares[endR][endC].setText(originalTarget);

        return !kingInDanger;
    }

    // Checks if the specified King is currently under attack
    private boolean isKingInCheck(boolean checkWhiteKing) {
        int kingR = -1, kingC = -1;
        String kingSymbol = checkWhiteKing ? "♔" : "♚";

        // Find the King
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                if (squares[r][c].getText().equals(kingSymbol)) {
                    kingR = r; kingC = c;
                }
            }
        }

        // See if any enemy piece can move to the King's square
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                String p = squares[r][c].getText();
                if (!p.isEmpty() && isWhitePiece(p) != checkWhiteKing) {
                    if (isValidMove(r, c, kingR, kingC, p)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    // Checks if the current player has ANY legal moves left
    private boolean hasLegalMoves(boolean checkWhiteTurn) {
        for (int sr = 0; sr < 8; sr++) {
            for (int sc = 0; sc < 8; sc++) {
                String p = squares[sr][sc].getText();
                if (!p.isEmpty() && isWhitePiece(p) == checkWhiteTurn) {
                    for (int er = 0; er < 8; er++) {
                        for (int ec = 0; ec < 8; ec++) {
                            if (isMoveLegal(sr, sc, er, ec, p)) {
                                return true; // Found at least one valid move!
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    private void checkGameState() {
        if (!hasLegalMoves(isWhiteTurn)) {
            if (isKingInCheck(isWhiteTurn)) {
                String winner = isWhiteTurn ? "BLACK" : "WHITE";
                JOptionPane.showMessageDialog(this, "CHECKMATE! " + winner + " WINS!", "Game Over", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "STALEMATE! It's a draw.", "Game Over", JOptionPane.INFORMATION_MESSAGE);
            }
            exitToMenu();
        } else {
            updateStatusLabel();
            
            // Warn player if they are in check
            if (isKingInCheck(isWhiteTurn)) {
                statusLabel.setText(statusLabel.getText() + " (CHECK!)");
            }
        }
    }

    // --- BASIC PIECE MOVEMENT RULES ---
    private boolean isValidMove(int startR, int startC, int endR, int endC, String piece) {
        String targetPiece = squares[endR][endC].getText();
        
        // Cannot capture your own piece
        if (!targetPiece.isEmpty() && isWhitePiece(piece) == isWhitePiece(targetPiece)) return false;

        int dr = endR - startR;
        int dc = endC - startC;

        switch (piece) {
            case "♙": // White Pawn
                if (dc == 0 && targetPiece.isEmpty()) { 
                    if (dr == -1) return true;
                    if (dr == -2 && startR == 6 && squares[5][startC].getText().isEmpty()) return true; 
                } else if (Math.abs(dc) == 1 && dr == -1 && !targetPiece.isEmpty()) return true;
                return false;

            case "♟": // Black Pawn
                if (dc == 0 && targetPiece.isEmpty()) {
                    if (dr == 1) return true;
                    if (dr == 2 && startR == 1 && squares[2][startC].getText().isEmpty()) return true;
                } else if (Math.abs(dc) == 1 && dr == 1 && !targetPiece.isEmpty()) return true;
                return false;

            case "♖": case "♜": // Rook
                if (dr != 0 && dc != 0) return false;
                return isPathClear(startR, startC, endR, endC);

            case "♘": case "♞": // Knight
                return (Math.abs(dr) == 2 && Math.abs(dc) == 1) || (Math.abs(dr) == 1 && Math.abs(dc) == 2);

            case "♗": case "♝": // Bishop
                if (Math.abs(dr) != Math.abs(dc)) return false;
                return isPathClear(startR, startC, endR, endC);

            case "♕": case "♛": // Queen
                if ((dr != 0 && dc != 0) && (Math.abs(dr) != Math.abs(dc))) return false;
                return isPathClear(startR, startC, endR, endC);

            case "♔": case "♚": // King
                return Math.abs(dr) <= 1 && Math.abs(dc) <= 1;

            default:
                return false;
        }
    }

    private boolean isPathClear(int startR, int startC, int endR, int endC) {
        int rowStep = Integer.compare(endR, startR); 
        int colStep = Integer.compare(endC, startC);

        int currentR = startR + rowStep;
        int currentC = startC + colStep;

        while (currentR != endR || currentC != endC) {
            if (!squares[currentR][currentC].getText().isEmpty()) return false;
            currentR += rowStep;
            currentC += colStep;
        }
        return true;
    }

    // --- HELPERS ---
    private boolean isWhitePiece(String piece) {
        return "♖♘♗♕♔♙".contains(piece);
    }

    private void setSquareColor(JButton square, int r, int c) {
        if ((r + c) % 2 == 0) square.setBackground(LIGHT_SQUARE);
        else square.setBackground(DARK_SQUARE);
    }

    private void updateStatusLabel() {
        if (isWhiteTurn) {
            statusLabel.setText("Turn: WHITE");
            statusLabel.setForeground(Color.WHITE);
        } else {
            statusLabel.setText("Turn: BLACK");
            statusLabel.setForeground(Color.RED);
        }
    }

    private void exitToMenu() {
        dispose();
        new MainMenu(username);
    }
}