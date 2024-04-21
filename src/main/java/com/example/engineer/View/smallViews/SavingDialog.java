package com.example.engineer.View.smallViews;

import javax.swing.*;
import java.awt.*;

public class SavingDialog extends JDialog {
    private final JLabel label;

    public SavingDialog(Frame parent){
        super(parent,"Saving...",false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(200, 100);

        label = new JLabel("SAVING...");
        getContentPane().add(label,BorderLayout.CENTER);
        setVisible(true);
    }

    @Override
    public void dispose(){
        label.setText("SAVED!");
        revalidate();

        try {
            Thread.sleep(2000);
        }catch (Exception e){
            e.printStackTrace();
        }

        super.dispose();
    }
}
