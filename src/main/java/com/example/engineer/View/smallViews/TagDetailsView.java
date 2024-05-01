package com.example.engineer.View.smallViews;

import com.example.engineer.Model.Tag;
import com.example.engineer.Service.FrameService;
import com.example.engineer.Service.TagService;
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

//TODO checkbox to unhide if hidden
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
    private Integer ID = -1;
    private Boolean saveData = false;

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
        JButton addButton = new JButton("Save");
        addButton.addActionListener(e -> {
            saveData = true;
            onClose();
        });

        //cancel button
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> onClose());

        // Panel for buttons
        JPanel buttonPanel = new JPanel(new GridLayout(1,2));
        buttonPanel.add(cancelButton);
        buttonPanel.add(addButton);

        // Add button panel to the frame
        add(buttonPanel,BorderLayout.SOUTH);

        setResizable(false);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                onClose();
            }
        });
    }

    public void getDetailsData(String name, Double value, String description, Integer id){
        nameTextField.setText(name);
        valueTextField.setText(value.toString());
        descriptionTextArea.setText(description);
        ID = id;

        openWindow();
    }

    public void openWindow(){
        setVisible(true);
    }

    public void onClose(){
        setVisible(false);

        //if empty name/value or cancel
        if(nameTextField.getText().isEmpty() || valueTextField.getText().isEmpty() || !saveData) {
            clearData();
            return;
        }

        //if ID == -1 => new tag
        if(ID ==-1){
            //add tag to database and the list
             FrameHopperView.TAG_LIST.add(tagService.createTag(
                     nameTextField.getText(),
                     Double.parseDouble(valueTextField.getText()),
                     descriptionTextArea.getText()

             ));
        }else {
            //save changes to the database
            Tag t = tagService.editTag(
                    ID,
                    nameTextField.getText(),
                    Double.parseDouble(valueTextField.getText()),
                    descriptionTextArea.getText()
            );

            //edit tag in the list
            int index = FrameHopperView.findTagIndexById(ID);
            FrameHopperView.TAG_LIST.get(index).setName(nameTextField.getText());
            FrameHopperView.TAG_LIST.get(index).setValue(Double.parseDouble(valueTextField.getText()));
            FrameHopperView.TAG_LIST.get(index).setDescription(descriptionTextArea.getText());
        }

        //update table models
        notifyTagsChanged();

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
        saveData = false;
    }
}
