package com.example.engineer.View.smallViews;

import com.example.engineer.View.FrameHopperView;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;

@Component
@RequiredArgsConstructor
public class StatusView extends JFrame implements ApplicationContextAware {

    private static ApplicationContext ctx;
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        ctx = applicationContext;
    }

    JLabel label;

    public void setUpView(){
        setSize(150,75);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        setUndecorated(true);

        label = new JLabel("",SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 16));

        add(label,BorderLayout.CENTER);

        setResizable(false);

        setLocationRelativeTo(ctx.getBean(FrameHopperView.class));

        setFocusableWindowState(false);

        setAlwaysOnTop(true);

        setOpacity(0.75f);

        getContentPane().setBackground(new Color(255,255,255,125));

        getRootPane().setBorder(BorderFactory.createMatteBorder(4,4,4,4,Color.black));

    }

    public void open(String text){
        label.setText(text);
        setVisible(true);
    }

    public void close(){
        setVisible(false);
        label.setText("");
    }
}
