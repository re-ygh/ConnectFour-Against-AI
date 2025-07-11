import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ConnectFour {

    // Constants for board dimensions and cell size
    private static final int ROW_COUNT = 6;
    private static final int COLUMN_COUNT = 7;
    private static final int CELL_SIZE = 100;

    // Constants for colors used in the game
    private static final Color BLUE = Color.BLUE;
    private static final Color BLACK = Color.BLACK;
    private static final Color RED = Color.RED;
    private static final Color YELLOW = Color.YELLOW;

    // Game state variables
    private int[][] board; // Represents the game board
    private boolean gameOver; // Indicates if the game is over
    private int turn; // Current player's turn (0 for RED, 1 for YELLOW)
    private boolean aiVsAi; // True if AI vs AI mode is enabled
    private int mouseX = -1; // Tracks mouse position for human player

    // Swing components for the UI
    private JFrame frame;
    private JPanel panel;

    // Constants for players
    private static final int MAX_PLAYER = 1; // AI player
    private static final int MIN_PLAYER = 2; // Opponent (human or AI)

    // Constructor to start the game with a menu
    public ConnectFour() {
        showMenu();
    }

    // Displays the initial menu for selecting game mode
    private void showMenu() {
        JFrame menuFrame = new JFrame("Connect Four - Select Mode");
        menuFrame.setSize(400, 200);
        menuFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        menuFrame.setLayout(new GridLayout(2, 1));

        JButton humanVsAiButton = new JButton("Human vs AI");
        JButton aiVsAiButton = new JButton("AI vs AI");

        // Set up action listeners for menu buttons
        humanVsAiButton.addActionListener(e -> {
            aiVsAi = false; // Human vs AI mode
            turn = 0; // Human starts first
            menuFrame.dispose();
            initializeGame();
        });

        aiVsAiButton.addActionListener(e -> {
            aiVsAi = true; // AI vs AI mode
            turn = 0; // First AI starts
            menuFrame.dispose();
            initializeGame();
        });

        menuFrame.add(humanVsAiButton);
        menuFrame.add(aiVsAiButton);

        menuFrame.setLocationRelativeTo(null); // Center the menu window
        menuFrame.setVisible(true);
    }

    // Initializes the game board and UI components
    private void initializeGame() {
        board = new int[ROW_COUNT][COLUMN_COUNT]; // Empty board
        gameOver = false;

        frame = new JFrame("Connect Four");
        frame.setSize(COLUMN_COUNT * CELL_SIZE, (ROW_COUNT + 1) * CELL_SIZE);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Custom panel to render the board
        panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawBoard(g); // Draw the board and pieces

                // Draw a mouse indicator for the human player's turn
                if (!gameOver && !aiVsAi && mouseX != -1) {
                    g.setColor(turn == 0 ? RED : YELLOW);
                    g.fillOval(mouseX - CELL_SIZE / 2, 0, CELL_SIZE, CELL_SIZE);
                }
            }
        };

        panel.setPreferredSize(new Dimension(COLUMN_COUNT * CELL_SIZE, (ROW_COUNT + 1) * CELL_SIZE));

        // Track mouse movement for visual feedback
        panel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                if (!gameOver && !aiVsAi) {
                    mouseX = e.getX();
                    panel.repaint();
                }
            }
        });

        // Handle mouse clicks for placing pieces
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!gameOver && !aiVsAi) {
                    //if the board is full we have a draw
                    if(isBoardFull()){
                        JOptionPane.showMessageDialog(frame, "all units are full. we have a draw", "Game Over", JOptionPane.INFORMATION_MESSAGE);
                        gameOver = true;
                    }
                    int col = e.getX() / CELL_SIZE; // Determine column from mouse click
                    if (isValidLocation(col)) {
                        int row = getNextOpenRow(col);
                        dropPiece(row, col, turn + 1);

                        // Check for a win after the move
                        if (winningMove(turn + 1)) {
                            JOptionPane.showMessageDialog(frame, "Player " + (turn == 0 ? "Human" : "AI") + " wins!!", "Game Over", JOptionPane.INFORMATION_MESSAGE);
                            gameOver = true;
                        }

                        turn = (turn + 1) % 2; // Switch turn
                        panel.repaint();


                        if (!gameOver && turn == 1) {
                            aiPlayer(turn + 1); // Trigger AI move
                        }
                    }
                }
            }
        });

        frame.add(panel);
        frame.pack();
        frame.setVisible(true);

        // Start AI vs AI mode if selected
        if (aiVsAi) {
            runAiVsAi();
        }
    }

    // Draws the game board and pieces
    private void drawBoard(Graphics g) {
        for (int r = 0; r < ROW_COUNT; r++) {
            for (int c = 0; c < COLUMN_COUNT; c++) {
                g.setColor(BLUE);
                g.fillRect(c * CELL_SIZE, (ROW_COUNT - r) * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                g.setColor(BLACK);
                g.fillOval(c * CELL_SIZE + 5, (ROW_COUNT - r) * CELL_SIZE + 5, CELL_SIZE - 10, CELL_SIZE - 10);

                // Draw pieces based on the board state
                if (board[r][c] == 1) {
                    g.setColor(RED);
                    g.fillOval(c * CELL_SIZE + 5, (ROW_COUNT - r) * CELL_SIZE + 5, CELL_SIZE - 10, CELL_SIZE - 10);
                } else if (board[r][c] == 2) {
                    g.setColor(YELLOW);
                    g.fillOval(c * CELL_SIZE + 5, (ROW_COUNT - r) * CELL_SIZE + 5, CELL_SIZE - 10, CELL_SIZE - 10);
                }
            }
        }
    }

    // Drops a piece into the specified location on the board
    private void dropPiece(int row, int col, int piece) {
        board[row][col] = piece;
    }

    // Checks if a column has space for a piece
    private boolean isValidLocation(int col) {
        return board[ROW_COUNT - 1][col] == 0;
    }

    // Finds the next open row in a column
    private int getNextOpenRow(int col) {
        for (int r = 0; r < ROW_COUNT; r++) {
            if (board[r][col] == 0) {
                return r;
            }
        }
        return -1; // No open rows (shouldn't happen if isValidLocation is true)
    }

    // Checks if the current move results in a win
    private boolean winningMove(int piece) {
        // Horizontal check
        for (int r = 0; r < ROW_COUNT; r++) {
            for (int c = 0; c < COLUMN_COUNT - 3; c++) {
                if (board[r][c] == piece && board[r][c + 1] == piece && board[r][c + 2] == piece && board[r][c + 3] == piece) {
                    return true;
                }
            }
        }

        // Vertical check
        for (int c = 0; c < COLUMN_COUNT; c++) {
            for (int r = 0; r < ROW_COUNT - 3; r++) {
                if (board[r][c] == piece && board[r + 1][c] == piece && board[r + 2][c] == piece && board[r + 3][c] == piece) {
                    return true;
                }
            }
        }

        // Positive diagonal check
        for (int r = 0; r < ROW_COUNT - 3; r++) {
            for (int c = 0; c < COLUMN_COUNT - 3; c++) {
                if (board[r][c] == piece && board[r + 1][c + 1] == piece && board[r + 2][c + 2] == piece && board[r + 3][c + 3] == piece) {
                    return true;
                }
            }
        }

        // Negative diagonal check
        for (int r = 3; r < ROW_COUNT; r++) {
            for (int c = 0; c < COLUMN_COUNT - 3; c++) {
                if (board[r][c] == piece && board[r - 1][c + 1] == piece && board[r - 2][c + 2] == piece && board[r - 3][c + 3] == piece) {
                    return true;
                }
            }
        }

        return false; // No win found
    }

    // Starts an AI vs AI game loop
    private void runAiVsAi() {
        new Thread(() -> {
            while (!gameOver) {
                try {
                    Thread.sleep(1000); // Delay for AI moves
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                int col = aiMove(turn + 1); // Get AI's move
                if (isValidLocation(col)) {
                    int row = getNextOpenRow(col);
                    dropPiece(row, col, turn + 1);

                    if (winningMove(turn + 1)) {
                        JOptionPane.showMessageDialog(frame, "Player " + (turn + 1) + " wins!!", "Game Over", JOptionPane.INFORMATION_MESSAGE);
                        gameOver = true;
                    }

                    turn = (turn + 1) % 2; // Switch turn
                    panel.repaint();
                }
            }
        }).start();
    }

    // Handles AI move logic
    private void aiPlayer(int player) {
        int col = aiMove(player); // Get AI's move
        int row = getNextOpenRow(col);
        dropPiece(row, col, player);

        if (winningMove(player)) {
            JOptionPane.showMessageDialog(frame, "AI wins!!", "Game Over", JOptionPane.INFORMATION_MESSAGE);
            gameOver = true;
        } else {
            turn = (turn + 1) % 2; // Switch turn to human
            panel.repaint();
        }
    }

    private boolean isBoardFull() {
        // Check if the board is completely filled by iterating over each column.
        for (int c = 0; c < COLUMN_COUNT; c++) {
            // If the top row of any column is empty, the board is not full.
            if (board[ROW_COUNT - 1][c] == 0) {
                return false;
            }
        }
        // Return true if no empty cells are found in the top row.
        return true;
    }

    private boolean checkBlockingMove(int player, int col) {
        // Verify if placing a piece in the column blocks the opponent's win.
        if (isValidLocation(col)) {
            int row = getNextOpenRow(col); // Get the next available row in the column.
            board[row][col] = player; // Temporarily place the player's piece.

            // Check if this move results in a win for the player.
            if (winningMove(player)) {
                board[row][col] = 0; // Reset the board state.
                return true;
            }
            board[row][col] = 0; // Reset the board state if no win.
        }
        return false;
    }

    private int evaluateLine(int r, int c, int dr, int dc, int player) {
        // Evaluate a line of 4 cells starting from (r, c) in direction (dr, dc).
        int score = 0;
        int opponent = (player == MAX_PLAYER) ? MIN_PLAYER : MAX_PLAYER;

        for (int i = 0; i < 4; i++) {
            int row = r + i * dr;
            int col = c + i * dc;

            // Ensure the cell is within board boundaries.
            if (row >= 0 && row < ROW_COUNT && col >= 0 && col < COLUMN_COUNT) {
                if (board[row][col] == player) {
                    score += 1; // Increment score for player's piece.
                } else if (board[row][col] == opponent) {
                    score -= 1; // Decrement score for opponent's piece.
                }
            }
        }

        return score; // Return the total score for the line.
    }

    private int evaluateBoard() {
        // Evaluate the board for both players and calculate the overall score.
        int score = 0;

        // Add AI player's evaluation score.
        score += evaluatePlayer(MAX_PLAYER);
        // Subtract opponent's evaluation score.
        score -= evaluatePlayer(MIN_PLAYER);

        return score;
    }

    private int evaluatePlayer(int player) {
        // Calculate the score for a specific player by evaluating potential lines.
        int score = 0;

        // Evaluate horizontal lines.
        for (int r = 0; r < ROW_COUNT; r++) {
            for (int c = 0; c < COLUMN_COUNT - 3; c++) {
                score += evaluateLine(r, c, 0, 1, player);
            }
        }

        // Evaluate vertical lines.
        for (int r = 0; r < ROW_COUNT - 3; r++) {
            for (int c = 0; c < COLUMN_COUNT; c++) {
                score += evaluateLine(r, c, 1, 0, player);
            }
        }

        // Evaluate diagonal lines (bottom-left to top-right).
        for (int r = 0; r < ROW_COUNT - 3; r++) {
            for (int c = 0; c < COLUMN_COUNT - 3; c++) {
                score += evaluateLine(r, c, 1, 1, player);
            }
        }

        // Evaluate diagonal lines (top-left to bottom-right).
        for (int r = 3; r < ROW_COUNT; r++) {
            for (int c = 0; c < COLUMN_COUNT - 3; c++) {
                score += evaluateLine(r, c, -1, 1, player);
            }
        }

        return score;
    }

    private int aiMove(int player) {
        // Determine the best move for the AI using heuristics and minimax.
        int bestScore = Integer.MIN_VALUE;
        int bestCol = -1;

        // First, check for immediate win or block opportunities.
        for (int col = 0; col < COLUMN_COUNT; col++) {
            if (isValidLocation(col)) {
                int row = getNextOpenRow(col);
                int opponent = (player == MAX_PLAYER) ? MIN_PLAYER : MAX_PLAYER;

                // Check if this move wins the game.
                board[row][col] = player;
                if (winningMove(player)) {
                    board[row][col] = 0;
                    return col; // Return the column to win the game.
                }

                // Check if this move blocks the opponent from winning.
                board[row][col] = opponent;
                if (checkBlockingMove(opponent, col)) {
                    board[row][col] = 0;
                    return col; // Return the column to block the opponent.
                }

                board[row][col] = 0; // Reset the board state.
            }
        }

        // If no immediate moves, play a random move if it is the first turn.
        int flag = 0;
        first_loop:
        for (int i = 0; i < ROW_COUNT; i++) {
            for (int j = 0; j < COLUMN_COUNT; j++) {
                if (board[i][j] != 0) {
                    flag = 1;
                    break first_loop;
                }
            }
        }
        if (flag == 0) {
            int first_random_column = (int) (Math.random() * 7);
            return first_random_column;
        }

        // Use minimax algorithm with alpha-beta pruning for deeper strategy.
        for (int col = 0; col < COLUMN_COUNT; col++) {
            if (isValidLocation(col)) {
                int row = getNextOpenRow(col);
                board[row][col] = player;

                int score = minimax(5, aiVsAi, Integer.MIN_VALUE, Integer.MAX_VALUE);
                board[row][col] = 0;

                if (score > bestScore) {
                    bestScore = score;
                    bestCol = col;
                }
            }
        }

        return bestCol; // Return the best column for the AI.
    }

    private int minimax(int depth, boolean isMaximizing, int alpha, int beta) {
        // Minimax algorithm with alpha-beta pruning to determine optimal moves.
        if (winningMove(MAX_PLAYER)) return 1000 - depth;
        if (winningMove(MIN_PLAYER)) return -1000 + depth;
        if (isBoardFull() || depth == 0) return evaluateBoard();

        if (isMaximizing) {
            int maxEval = Integer.MIN_VALUE;
            for (int col = 0; col < COLUMN_COUNT; col++) {
                if (isValidLocation(col)) {
                    int row = getNextOpenRow(col);
                    board[row][col] = MAX_PLAYER;

                    int eval = minimax(depth - 1, false, alpha, beta);
                    board[row][col] = 0;

                    maxEval = Math.max(maxEval, eval);
                    alpha = Math.max(alpha, eval);

                    if (alpha >= beta) break; // Prune the search tree.
                }
            }
            return maxEval;
        } else {
            int minEval = Integer.MAX_VALUE;
            for (int col = 0; col < COLUMN_COUNT; col++) {
                if (isValidLocation(col)) {
                    int row = getNextOpenRow(col);
                    board[row][col] = MIN_PLAYER;

                    int eval = minimax(depth - 1, true, alpha, beta);
                    board[row][col] = 0;

                    minEval = Math.min(minEval, eval);
                    beta = Math.min(beta, eval);

                    if (alpha >= beta) break; // Prune the search tree.
                }
            }
            return minEval;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ConnectFour::new); // Start the game
    }
}
