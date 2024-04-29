package com.example.engineer.View.smallViews;

import javax.swing.*;
import java.awt.*;

public class LoadingView extends JFrame{
    public LoadingView(String message) {
        setSize(300, 100);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // Create and configure the label
        JLabel label = new JLabel(message, SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 16)); // Customize font if needed

        // Add the label to the frame's content pane
        getContentPane().add(label, BorderLayout.CENTER);

        // Center the window on the screen
        setLocationRelativeTo(null);

        //set always on top
        setAlwaysOnTop(true);

        // Make the window visible
        setVisible(true);
    }
}
