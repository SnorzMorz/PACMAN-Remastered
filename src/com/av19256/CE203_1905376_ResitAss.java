package com.av19256;

import javax.swing.*;

public class CE203_1905376_ResitAss extends JFrame {

    public CE203_1905376_ResitAss() {
        add(new PACMAN());
    }


    public static void main(String[] args) { // Main RUN THIS
        CE203_1905376_ResitAss pac = new CE203_1905376_ResitAss();
        pac.setSize(410, 430);
        pac.setTitle("Pacman - 1905376");
        pac.setVisible(true);
        pac.setDefaultCloseOperation(EXIT_ON_CLOSE);
        pac.setLocationRelativeTo(null);

    }



}
