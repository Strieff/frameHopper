package com.example.engineer.View.WindowViews;

import com.example.engineer.Model.Tag;
import com.example.engineer.Service.FrameService;
import com.example.engineer.Service.TagService;
import com.example.engineer.View.Elements.Dictionary;
import com.example.engineer.View.Elements.LanguageChangeListener;
import com.example.engineer.View.Elements.LanguageManager;
import com.example.engineer.View.Elements.TagListManager;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

@Component
@DependsOn()
public class TagDetailsView extends JFrame implements ApplicationContextAware, LanguageChangeListener {
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
    @Autowired
    LanguageManager languageManager;

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

        JLabel nameLabel = new JLabel(Dictionary.getText("details.name"));
        nameLabel.putClientProperty("text","details.name");
        nameTextField = new JTextField();
        labels.add(nameLabel);

        namePanel.add(nameLabel,BorderLayout.NORTH);
        namePanel.add(nameTextField, BorderLayout.CENTER);

        //tag value section
        valuePanel = new JPanel();
        valuePanel.setLayout(new BorderLayout());

        JLabel valueLabel = new JLabel(Dictionary.getText("details.value"));
        valueLabel.putClientProperty("text","details.value");
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
        JLabel descriptionLabel = new JLabel(Dictionary.getText("details.desc"));
        descriptionLabel.putClientProperty("text","details.desc");
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
        addButton = new JButton(Dictionary.getText("details.save"));
        addButton.putClientProperty("text","details.save");
        addButton.addActionListener(e -> close(true));

        //cancel button
        cancelButton = new JButton(Dictionary.getText("details.cancel"));
        cancelButton.putClientProperty("text","details.cancel");
        cancelButton.addActionListener(e -> close(false));

        //hide button
        hiddenStatusButton = new JButton();
        hiddenStatusButton.putClientProperty("hide","details.hide");
        hiddenStatusButton.putClientProperty("unhide","details.unhide");
        hiddenStatusButton.addActionListener(e -> {
            if(ID!=-1){
                changeHiddenStatus();
                close(false);
            }
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

    @PostConstruct
    public void init(){
        languageManager.addListener(this);
    }

    public void getDetailsData(String name, Double value, String description, Integer id,boolean hidden){
        nameTextField.setText(name.contains(Dictionary.getText("details.hidden")) ? name.replaceAll(" \\(.*?\\)$","") : name);
        valueTextField.setText(value.toString());
        descriptionTextArea.setText(description);
        ID = id;

        openWindow(hidden);
    }

    public void openWindow(boolean hidden){
        hiddenStatusButton.setText(Dictionary.getText(hidden? "details.unhide" : "details.hide"));
        hiddenStatusButton.putClientProperty("isHidden",hidden);
        hiddenStatusButton.revalidate();

        setComponentColor(hidden ? Color.DARK_GRAY : null);

        setVisible(true);
    }

    public void openWindow(){
        setVisible(true);
        hiddenStatusButton.putClientProperty("isHidden",true);
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

        hiddenStatusButton.putClientProperty("isHidden",null);

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

    @Override
    public void changeLanguage() {
        for(var label : labels)
            label.setText(Dictionary.getText((String)label.getClientProperty("text")));

        addButton.setText(Dictionary.getText((String)addButton.getClientProperty("text")));
        cancelButton.setText(Dictionary.getText((String)cancelButton.getClientProperty("text")));
        if(hiddenStatusButton.getClientProperty("isHidden")!=null){
            var isHidden = (Boolean)hiddenStatusButton.getClientProperty("isHidden") ? "hide" : "unhide";
            hiddenStatusButton.setText(Dictionary.getText((String)(hiddenStatusButton.getClientProperty(isHidden))));
        }
    }
}
