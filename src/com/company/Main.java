package com.company;

import javax.swing.*;
import java.awt.*;

public class Main extends JFrame{

    JLabel statusbar;

    public Main() {
        statusbar = new JLabel(" 0");
        add(statusbar, BorderLayout.SOUTH);
        Board board = new Board(this);
        add(board);

        board.start();

        setSize(200 * 2, 400 * 2);
        setTitle("Tetris");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
    }

    public JLabel getStatusbar() {
        return statusbar;
    }

    public static void main(String[] args) {

        Main game = new Main();
        game.setLocationRelativeTo(null);
        game.setVisible(true);

    }
}
