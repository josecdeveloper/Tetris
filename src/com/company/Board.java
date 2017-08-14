package com.company;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Board extends JPanel implements ActionListener, BoardBase {

    //Holds current shape of the piece
    private Shape curPiece;

    private Timer timer;
    private JLabel statusbarJL;
    private Tetromino[] board;

    //Board Dimensions
//    private final int Const.BOARD_WIDTH = 10;
//    private final int Const.BOARD_HEIGHT = 22;

    private boolean isFallingFinished = false;
    private boolean isStarted = false;
    private boolean isPaused = false;
    private int numLinesRemoved = 0;
    private int curX = 0;
    private int curY = 0;


    public Board(Main parent) {
        setFocusable(true);
        curPiece = new Shape();
        timer = new Timer(400, this);
        timer.start();

        statusbarJL = parent.getStatusbar();
        board = new Tetromino[Const.BOARD_WIDTH * Const.BOARD_HEIGHT];
        addKeyListener(new TAdapter());
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        if (isFallingFinished) {
            isFallingFinished = false;
            newPiece();
        } else {
            oneLineDown();
        }

    }

    int squareWidth() {
        return (int) getSize().getWidth() / Const.BOARD_WIDTH;
    }

    int squareHeight() {
        return (int) getSize().getHeight() / Const.BOARD_HEIGHT;
    }

    Tetromino shapeAt(int x, int y) {
        return board[(y * Const.BOARD_WIDTH) + x];
    }

    @Override
    public void start() {

        if (isPaused)
            return;

        isStarted = true;
        isFallingFinished = false;
        numLinesRemoved = 0;
        clearBoard();

        newPiece();
        timer.start();

    }

    @Override
    public void pause() {
        if (!isStarted)
            return;

        isPaused = !isPaused;
        if (isPaused) {
            timer.stop();
            statusbarJL.setText("PAUSED");
        } else {
            timer.start();
            statusbarJL.setText(String.valueOf(numLinesRemoved));
        }

        repaint();
    }

    @Override
    public void paint(Graphics g) {

        super.paint(g);

        Dimension size = getSize();
        int boardTop = (int) size.getHeight() - Const.BOARD_HEIGHT * squareHeight();

        for (int i = 0; i < Const.BOARD_HEIGHT; i++) {
            for (int j = 0; j < Const.BOARD_WIDTH; j++) {

                Tetromino shape = shapeAt(j, Const.BOARD_HEIGHT - i - 1);
                if (shape != Tetromino.NoShape)
                    drawSquare(g, 0 + j * squareWidth(),
                            boardTop + i * squareHeight(), shape);

            }

        }

        if (curPiece.getShape() != Tetromino.NoShape) {
            for (int i = 0; i < 4; i++) {
                int x = curX + curPiece.x(i);
                int y = curY - curPiece.y(i);
                drawSquare(g, 0 + x * squareWidth(),
                        boardTop + (Const.BOARD_HEIGHT - y - 1) * squareHeight(),
                        curPiece.getShape());
            }
        }
    }

    @Override
    public void clearBoard() {
        for (int i = 0; i < Const.BOARD_HEIGHT * Const.BOARD_WIDTH; i++) {
            board[i] = Tetromino.NoShape;
        }
    }

    private void dropDown() {
        int newY = curY;
        while(newY > 0) {
            if(!tryMove(curPiece, curX, newY - 1))
                break;
            --newY;
        }
        pieceDropped();
    }

    private void oneLineDown() {
        if (!tryMove(curPiece, curX, curY - 1))
            pieceDropped();
    }

    private void pieceDropped() {
        for (int i = 0; i < 4; i++) {
            int x = curX + curPiece.x(i);
            int y = curY - curPiece.y(i);
            board[(y * Const.BOARD_WIDTH) + x] = curPiece.getShape();
        }

        removeFullLines();

        if (!isFallingFinished)
            newPiece();
    }

    private void newPiece() {
            curPiece.setRandomShape();
        curX = Const.BOARD_WIDTH / 2 + 1;
        curY = Const.BOARD_HEIGHT - 1 + curPiece.minY();

        if (!tryMove(curPiece, curX, curY)) {
            curPiece.setShape(Tetromino.NoShape);
            timer.stop();
            isStarted = false;
            statusbarJL.setText("GAME OVER");
        }
    }

    private boolean tryMove(Shape newPiece, int newX, int newY) {
        for (int i = 0; i < 4; i++) {
            int x = newX + newPiece.x(i);
            int y = newY - newPiece.y(i);

            if (x < 0 || x >= Const.BOARD_WIDTH || y < 0 || y >= Const.BOARD_HEIGHT)
                return false;
            if (shapeAt(x, y) != Tetromino.NoShape)
                return false;
        }

        curPiece = newPiece;
        curX = newX;
        curY = newY;
        repaint();
        return true;
    }

    private void removeFullLines() {
        int numFullLines = 0;

        for (int i = Const.BOARD_HEIGHT - 1; i >= 0; --i) {
            boolean lineIsFull = true;

            for (int j = 0; j < Const.BOARD_WIDTH; ++j) {
                if (shapeAt(j, i) == Tetromino.NoShape) {
                    lineIsFull = false;
                    break;
                }
            }

            if (lineIsFull) {
                ++numFullLines;
                for (int k = i; k < Const.BOARD_HEIGHT - 1; ++k) {
                    for (int j = 0; j < Const.BOARD_WIDTH; ++j)
                        board[(k * Const.BOARD_WIDTH) + j] = shapeAt(j, k + 1);
                }
            }
        }

        if (numFullLines > 0) {
            numLinesRemoved += numFullLines;
            statusbarJL.setText(String.valueOf(numLinesRemoved));
            isFallingFinished = true;
            curPiece.setShape(Tetromino.NoShape);
            repaint();
        }
    }

    private void drawSquare(Graphics g, int x, int y, Tetromino shape) {
        Color colors[] = { new Color(0, 0, 0), new Color(204, 102, 102),
                new Color(102, 204, 102), new Color(102, 102, 204),
                new Color(204, 204, 102), new Color(204, 102, 204),
                new Color(102, 204, 204), new Color(218, 170, 0)
        };


        Color color = colors[shape.ordinal()];

        g.setColor(color);
        g.fillRect(x + 1, y + 1, squareWidth() - 2, squareHeight() - 2);

        g.setColor(color.brighter());
        g.drawLine(x, y + squareHeight() - 1, x, y);
        g.drawLine(x, y, x + squareWidth() - 1, y);

        g.setColor(color.darker());
        g.drawLine(x + 1, y + squareHeight() - 1,
                x + squareWidth() - 1, y + squareHeight() - 1);
        g.drawLine(x + squareWidth() - 1, y + squareHeight() - 1,
                x + squareWidth() - 1, y + 1);
    }

    private class TAdapter extends KeyAdapter {
        public void keyPressed(KeyEvent e) {
            if (!isStarted || curPiece.getShape() == Tetromino.NoShape) {
                return;
            }

            int keycode = e.getKeyCode();

            if (keycode == 'p' || keycode == 'P') {
                pause();
                return;
            }

            if (isPaused)
                return;
            switch (keycode) {
                case KeyEvent.VK_LEFT:
                    tryMove(curPiece, curX - 1, curY);
                    break;
                case KeyEvent.VK_RIGHT:
                    tryMove(curPiece, curX + 1, curY);
                    break;
                case KeyEvent.VK_DOWN:
                    tryMove(curPiece.rotateRight(), curX, curY);
                    break;
                case KeyEvent.VK_UP:
                    tryMove(curPiece.rotateLeft(), curX, curY);
                    break;
                case KeyEvent.VK_SPACE:
                    dropDown();
                    break;
                case 'd':
                    oneLineDown();
                    break;
                case 'D':
                    oneLineDown();
                    break;
            }
        }
    }
}
