package com.example.engineer.View.WindowViews;

import com.example.engineer.Model.Tag;
import com.example.engineer.Service.FrameService;
import com.example.engineer.Service.TagService;
import com.example.engineer.View.Elements.TagListManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

@Component
public class TagDetailsView extends JFrame implements ApplicationContextAware {
    //context
    private static ApplicationContext ctx;
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        ctx = applicationContext;
    }

    //needed services and components
    @Autowired
    TagService tagService;
    @Autowired
    FrameService frameService;
    @Autowired
    TagListManager tagList;

    //JComponents
    private final JTextField nameTextField;
    private final JTextField valueTextField;
    private final JTextArea descriptionTextArea;
    private final JButton hiddenStatusButton;
    private final JButton addButton;
    private final JButton cancelButton;
    private final JPanel buttonPanel;
    private final JPanel namePanel;
    private final JPanel valuePanel;
    private final JPanel lowerPanel;

    //needed data
    private Integer ID = -1;
    List<JLabel> labels = new ArrayList<>();

    public TagDetailsView(){
        //set needed information
        setSize(300, 300);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setResizable(false);

        //COMPONENTS

        //tag name section
        namePanel = new JPanel();
        namePanel.setLayout(new BorderLayout());

        JLabel nameLabel = new JLabel("Name:");
        nameTextField = new JTextField();
        labels.add(nameLabel);

        namePanel.add(nameLabel,BorderLayout.NORTH);
        namePanel.add(nameTextField, BorderLayout.CENTER);

        //tag value section
        valuePanel = new JPanel();
        valuePanel.setLayout(new BorderLayout());

        JLabel valueLabel = new JLabel("Value:");
        valueTextField = new JTextField();
        labels.add(valueLabel);

        valuePanel.add(valueLabel,BorderLayout.NORTH);
        valuePanel.add(valueTextField, BorderLayout.CENTER);

        //panel to hold name and value section
        JPanel upperPanel = new JPanel();
        upperPanel.setLayout(new GridLayout(1,2));

        upperPanel.add(namePanel);
        upperPanel.add(valuePanel);

        add(upperPanel,BorderLayout.NORTH);

        //tag description panel
        JLabel descriptionLabel = new JLabel("Description:");
        descriptionTextArea = new JTextArea();
        descriptionTextArea.setLineWrap(true);
        labels.add(descriptionLabel);

        //lower panel
        lowerPanel = new JPanel();
        lowerPanel.setLayout(new BorderLayout());

        lowerPanel.add(descriptionLabel,BorderLayout.NORTH);
        lowerPanel.add(descriptionTextArea,BorderLayout.CENTER);

        add(lowerPanel,BorderLayout.CENTER);

        //save button
        addButton = new JButton("Save");
        addButton.addActionListener(e -> close(true));

        //cancel button
        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> close(false));

        //hide button
        hiddenStatusButton = new JButton();
        hiddenStatusButton.addActionListener(e -> {
            changeHiddenStatus();
            close(false);
        });

        //button panel
        buttonPanel = new JPanel(new GridLayout(1,3));
        buttonPanel.add(cancelButton);
        buttonPanel.add(hiddenStatusButton);
        buttonPanel.add(addButton);

        add(buttonPanel,BorderLayout.SOUTH);

        //operation on close
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                close(false);
            }
        });
    }

    public void getDetailsData(String name, Double value, String description, Integer id,boolean hidden){
        nameTextField.setText(name.contains(" (hidden)") ? name.substring(0,name.length()-9) : name);
        valueTextField.setText(value.toString());
        descriptionTextArea.setText(description);
        ID = id;

        openWindow(hidden);
    }

    public void openWindow(boolean hidden){
        hiddenStatusButton.setText(hidden ? "Unhide" : "Hide");
        hiddenStatusButton.revalidate();

        setComponentColor(hidden ? Color.DARK_GRAY : null);

        setVisible(true);
    }

    public void openWindow(){
        setVisible(true);
    }

    public void changeHiddenStatus(){
        Tag t = tagList.getTag(ID);

        tagList.changeHideStatus(ID,!t.isDeleted());

        notifyTagsChanged();

        hiddenStatusButton.setText(t.isDeleted() ? "Unhide" : "Hide");

        setComponentColor(t.isDeleted() ? Color.DARK_GRAY : null);
    }

    public void close(boolean save){
        //hide
        setVisible(false);

        if(save){
            //if empty name/value
            if (nameTextField.getText().isEmpty()) {
                clearData();
                return;
            }

            //if ID == -1 => new tag - program waits
            if(ID == -1)
                tagList.addTag(
                        nameTextField.getText(),
                        Double.parseDouble(valueTextField.getText().isEmpty()? "0" : valueTextField.getText().replace(",",".")),
                        descriptionTextArea.getText()
                );
            else {
                tagList.editTag(
                        ID,
                        nameTextField.getText(),
                        Double.parseDouble(valueTextField.getText().replace(",",".")),
                        descriptionTextArea.getText(),
                        hiddenStatusButton.getText().contains("hid")
                );

                Tag t = tagList.getTag(ID);
                t.setName(nameTextField.getText());
                t.setValue(Double.parseDouble(valueTextField.getText().replace(",",".")));
                t.setDescription(descriptionTextArea.getText());
            }

            //update table models
            notifyTagsChanged();
        }


        //clear tag data from window
        clearData();
    }

    public void notifyTagsChanged(){
        //notify settings
        ctx.getBean(SettingsView.class).notifyTableChange();
    }

    private void clearData(){
        nameTextField.setText("");
        valueTextField.setText("");
        descriptionTextArea.setText("");
        ID = -1;

        setComponentColor(null);
    }

    private void setComponentColor(Color color){
        //set background color
        buttonPanel.setBackground(color);
        namePanel.setBackground(color);
        valuePanel.setBackground(color);
        lowerPanel.setBackground(color);

        //labels
        for(JLabel l : labels)
            l.setForeground(color == null ? Color.BLACK : Color.WHITE);

        //text fields
        nameTextField.setBackground(color == null ? Color.WHITE : Color.GRAY);
        nameTextField.setForeground(color == null ? Color.BLACK : Color.WHITE);

        valueTextField.setBackground(color == null ? Color.WHITE : Color.GRAY);
        valueTextField.setForeground(color == null ? Color.BLACK : Color.WHITE);

        descriptionTextArea.setBackground(color == null ? Color.WHITE : Color.GRAY);
        descriptionTextArea.setForeground(color == null ? Color.BLACK : Color.WHITE);

        //buttons
        cancelButton.setBackground(color);
        cancelButton.setForeground(color == null ? Color.BLACK : Color.WHITE);

        hiddenStatusButton.setBackground(color);
        hiddenStatusButton.setForeground(color == null ? Color.BLACK : Color.WHITE);

        addButton.setBackground(color);
        addButton.setForeground(color == null ? Color.BLACK : Color.WHITE);
    }
}
