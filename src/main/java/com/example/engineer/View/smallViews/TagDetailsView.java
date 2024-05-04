package com.example.engineer.View.smallViews;

import com.example.engineer.Service.FrameService;
import com.example.engineer.Service.TagService;
import com.example.engineer.Threads.SetHiddenStatusThread;
import com.example.engineer.Threads.TagSettingsThread;
import com.example.engineer.View.FrameHopperView;
import com.example.engineer.View.buttonsView.SettingsView;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

//TODO unhide button if hidden - not working properly
@Component
@RequiredArgsConstructor
public class TagDetailsView extends JFrame implements ApplicationContextAware {
    private static ApplicationContext ctx;
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        ctx = applicationContext;
    }
    @Autowired
    TagService tagService;
    @Autowired
    FrameService frameService;

    private JTextField nameTextField;
    private JTextField valueTextField;
    private JTextArea descriptionTextArea;
    private JButton unhideButton;
    private JButton addButton;
    private JButton cancelButton;
    private JPanel buttonPanel;

    private Integer ID = -1;

    public void setUpView(){
        setSize(300, 300);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        setLayout(new BorderLayout());

        // set up name section
        JPanel namePanel = new JPanel();
        namePanel.setLayout(new BorderLayout());

        JLabel nameLabel = new JLabel("Name:");
        nameTextField = new JTextField();

        namePanel.add(nameLabel,BorderLayout.NORTH);
        namePanel.add(nameTextField, BorderLayout.CENTER);

        //set up value section
        JPanel valuePanel = new JPanel();
        valuePanel.setLayout(new BorderLayout());

        JLabel valueLabel = new JLabel("Value:");
        valueTextField = new JTextField();

        valuePanel.add(valueLabel,BorderLayout.NORTH);
        valuePanel.add(valueTextField, BorderLayout.CENTER);

        //set up upper part
        JPanel upperPanel = new JPanel();
        upperPanel.setLayout(new GridLayout(1,2));

        upperPanel.add(namePanel);
        upperPanel.add(valuePanel);

        add(upperPanel,BorderLayout.NORTH);

        //set up lower part
        JPanel lowerPanel = new JPanel();
        lowerPanel.setLayout(new BorderLayout());

        JLabel descriptionLabel = new JLabel("Description:");
        descriptionTextArea = new JTextArea();
        descriptionTextArea.setLineWrap(true); // Enable word wrap for the description text area

        lowerPanel.add(descriptionLabel,BorderLayout.NORTH);
        lowerPanel.add(descriptionTextArea,BorderLayout.CENTER);

        add(lowerPanel,BorderLayout.CENTER);

        //save button
        addButton = new JButton("Save");
        addButton.addActionListener(e -> close(true));

        //cancel button
        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> close(false));

        unhideButton = new JButton("Unhide");
        unhideButton.addActionListener(e -> unHide());


        // Panel for buttons
        buttonPanel = new JPanel();

        // Add button panel to the frame
        add(buttonPanel,BorderLayout.SOUTH);

        setResizable(false);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                close(false);
            }
        });
    }

    public void getDetailsData(String name, Double value, String description, Integer id,boolean hidden){
        nameTextField.setText(name);
        valueTextField.setText(value.toString());
        descriptionTextArea.setText(description);
        ID = id;

        openWindow(hidden);
    }

    public void openWindow(boolean hidden){
        if(hidden){
            buttonPanel.setLayout(new GridLayout(1,3));
            buttonPanel.add(cancelButton);
            buttonPanel.add(unhideButton);
            buttonPanel.add(addButton);
        }else{
            buttonPanel.setLayout(new GridLayout(1,2));
            buttonPanel.add(cancelButton);
            buttonPanel.add(addButton);
        }

        buttonPanel.revalidate();
        setVisible(true);
    }

    public void unHide(){
        FrameHopperView.TAG_LIST.get(FrameHopperView.findTagIndexById(ID)).setDeleted(false);

        //tagService.unHideTag(ID);
        new SetHiddenStatusThread(tagService,ID,false).start();

        notifyTagsChanged();

        close(false);
    }

    public void close(boolean save){
        //hide
        setVisible(false);


        if(save){
            //if empty name/value
            if (nameTextField.getText().isEmpty() || valueTextField.getText().isEmpty()) {
                clearData();
                return;
            }

            //if ID == -1 => new tag
            new TagSettingsThread(tagService,
                    ID != -1 ? ID : null,
                    nameTextField.getText(),
                    Double.parseDouble(valueTextField.getText()),
                    descriptionTextArea.getText()).start();

            //update table models
            notifyTagsChanged();
        }

        //clear tag data from window
        clearData();
    }

    private void notifyTagsChanged(){
        //notify settings
        ctx.getBean(SettingsView.class).notifyTableChange();
    }

    private void clearData(){
        nameTextField.setText("");
        valueTextField.setText("");
        descriptionTextArea.setText("");
        ID = -1;

        buttonPanel.removeAll();
    }
}
