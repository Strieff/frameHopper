package com.example.engineer.View.smallViews;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;

public class LoadingView extends JFrame{
    JLabel label;

    public LoadingView(String message) {
        setSize(300, 100);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // Create and configure the label
        label = new JLabel(message, SwingConstants.CENTER);
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
