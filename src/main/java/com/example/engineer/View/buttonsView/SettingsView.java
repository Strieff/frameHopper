package com.example.engineer.View.buttonsView;


import com.example.engineer.Model.Tag;
import com.example.engineer.Service.SettingsService;
import com.example.engineer.Service.TagService;
import com.example.engineer.Threads.DeleteTagThread;
import com.example.engineer.Threads.SaveSettingsThread;
import com.example.engineer.Threads.SetHiddenStatusThread;
import com.example.engineer.View.Elements.MultilineTableCellRenderer;
import com.example.engineer.View.FrameHopperView;
import com.example.engineer.View.smallViews.TagDetailsView;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.Serial;
import java.net.URL;

@Component
@RequiredArgsConstructor
public class SettingsView extends JFrame implements ApplicationContextAware {
    @Autowired
    private final TagService tagService;
    @Autowired
    private final SettingsService settingsService;
    private JTable tagTable;
    private JPanel mainPanel;

    private static ApplicationContext ctx;
    public static ApplicationContext getApplicationContext() {
        return ctx;
    }
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        ctx = applicationContext;
    }

    public void setUpView() {
        setSize(700, 400);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        mainPanel = new JPanel(new BorderLayout());

        JPanel tablePanel = new JPanel(new BorderLayout());

        //new tag button
        JButton createNewTagButton = new JButton("Add tag");
        createNewTagButton.addActionListener(e -> {
            getApplicationContext().getBean(TagDetailsView.class).openWindow(false);
        });

        //toolbar
        JToolBar toolBar = new JToolBar();
        toolBar.add(createNewTagButton);
        tablePanel.add(toolBar,BorderLayout.NORTH);

        //settings label
        JLabel frameLabel = new JLabel("Tag manager | Settings", SwingConstants.CENTER);
        frameLabel.setFont(new Font("Arial", Font.BOLD, 16));
        mainPanel.add(frameLabel, BorderLayout.NORTH);

        //tag table
        tagTable = new JTable() {
            @Serial
            private static final long serialVersionUID = 1L;

            @Override
            public Class getColumnClass(int column) {
                return switch (column) {
                    case 0, 2 -> String.class;
                    case 1 -> Double.class;
                    case 5 -> Integer.class;
                    default -> Icon.class;
                };
            }

            @Override
            public boolean isCellEditable(int row,int column){
                return false;
            }
        };

        Object[] columnNames = {"TAG", "VALUE", "DESCRIPTION", " ", " ","ID"};

        int tableLen = FrameHopperView.USER_SETTINGS.getShowDeleted()?
                FrameHopperView.TAG_LIST.size():
                FrameHopperView.getNumberOfVisibleTags();
        Object[][] data = new Object[tableLen][];

        //create data rows
        int i = 0;
        for (Tag t : FrameHopperView.TAG_LIST) {
            Image scaledIconBin = getIconFromPath("/icons/bin.png");

            Image scaledIconEdit = getIconFromPath("/icons/edit.png");

            if(!t.isDeleted()){
                data[i++] = new Object[]{
                        t.getName(),
                        t.getValue(),
                        t.getDescription(),
                        new ImageIcon(scaledIconEdit),
                        new ImageIcon(scaledIconBin),
                        t.getId()
                };
            }
        }

        //model
        DefaultTableModel model = new DefaultTableModel(data, columnNames) {
            public Class<?> getColumnClass(int column) {
                if (column == 3 || column == 4) {
                    return Icon.class;
                }
                return super.getColumnClass(column);
            }
        };

        tagTable.setModel(model);
        tagTable.getTableHeader().setReorderingAllowed(false);

        DefaultTableCellRenderer centerRender = new DefaultTableCellRenderer();
        centerRender.setHorizontalAlignment(SwingConstants.CENTER);

        //set size to trash icon - 5th row
        tagTable.getColumnModel().getColumn(4).setPreferredWidth(24);
        tagTable.getColumnModel().getColumn(4).setMaxWidth(24);

        //set size to edit icon - 4th row
        tagTable.getColumnModel().getColumn(3).setPreferredWidth(24);
        tagTable.getColumnModel().getColumn(3).setMaxWidth(24);

        //set size to value and center - 2nd row
        tagTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        tagTable.getColumnModel().getColumn(1).setMaxWidth(100);
        tagTable.getColumnModel().getColumn(1).setCellRenderer(centerRender);

        //set multi row to description and name
        tagTable.getColumnModel().getColumn(2).setCellRenderer(new MultilineTableCellRenderer());

        //set size of ID to 0 - 6th row
        tagTable.getColumnModel().getColumn(5).setMinWidth(0);
        tagTable.getColumnModel().getColumn(5).setMaxWidth(0);
        tagTable.getColumnModel().getColumn(5).setWidth(0);

        //disable row select
        tagTable.setRowSelectionAllowed(false);

        //set resizable to false
        tagTable.getTableHeader().setResizingAllowed(false);

        //listeners for edit/delete icons
        tagTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = tagTable.rowAtPoint(e.getPoint());
                int column = tagTable.columnAtPoint(e.getPoint());
                if (row >= 0 && column == 3) { // Check if the click is on the 4th column
                    String tagName = (String) tagTable.getValueAt(row, 0);
                    Double tagValue = (Double) tagTable.getValueAt(row, 1);
                    String tagDescription = (String) tagTable.getValueAt(row, 2);
                    Integer tagID = (Integer) tagTable.getValueAt(row,5);
                    boolean hidden = FrameHopperView.TAG_LIST.get(FrameHopperView.findTagIndexById(tagID)).isDeleted();
                    getApplicationContext().getBean(TagDetailsView.class).getDetailsData(tagName, tagValue, tagDescription,tagID,hidden);
                }

                if(row>0 && column == 4){ //check if the click is on the 5th column
                    Integer tagID = (Integer) tagTable.getValueAt(row,5);
                    String tagName = (String) tagTable.getValueAt(row, 0);
                    showOptionsDialog(tagID,tagName);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(tagTable);

        // Set preferred sizes to make the table take more space
        scrollPane.setPreferredSize(new Dimension(550, getHeight())); // Adjust the width as needed

        tablePanel.add(scrollPane, BorderLayout.CENTER);

        mainPanel.add(tablePanel,BorderLayout.WEST);

        //set up settings boxes
        JPanel settingsPanel = setUpSettings();

        mainPanel.add(settingsPanel, BorderLayout.CENTER);

        add(mainPanel);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                onClose();
            }
        });
    }

    public void setUpData(){
        setVisible(true);
    }

    private void onClose(){
        setVisible(false);
    }

    public synchronized void notifyTableChange(){
        DefaultTableModel model = (DefaultTableModel) tagTable.getModel();
        model.setRowCount(0); // Clear the existing rows


        for (Tag tag : FrameHopperView.TAG_LIST) {
            // Assuming tag has properties: id, name, value, description
            if(!tag.isDeleted() || FrameHopperView.USER_SETTINGS.getShowDeleted()){
                model.addRow(new Object[]{
                        tag.getName() + (tag.isDeleted() ? " (hidden)" : ""),
                        tag.getValue(),
                        tag.getDescription(),
                        new ImageIcon(getIconFromPath("/icons/edit.png")),
                        new ImageIcon(getIconFromPath("/icons/bin.png")),
                        tag.getId(),
                });
            }
        }

        tagTable.setModel(model);
        tagTable.revalidate();
    }

    private Image getIconFromPath(String path){
        URL iconURL = getClass().getResource(path);
        Image scaledIcon = null;
        if (iconURL != null) {
            ImageIcon imageIcon = new ImageIcon(iconURL);
            scaledIcon = imageIcon.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
        }

        return scaledIcon;
    }

    private void showOptionsDialog(Integer tagId,String name){
        String[] options = {"Cancel","Hide","Delete"};

        int actionChoice = JOptionPane.showOptionDialog(this, "Message", "Title",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
                null, options, options[0]);

        switch(actionChoice){
            case 0: //cancel
                break;
            case 1: //hide
                hideTag(tagId);
                break;
            case 2: //delete
                deleteTag(tagId);
                break;
        }
    }

    private void hideTag(Integer id){
        //set deleted - hidden to true
        FrameHopperView.TAG_LIST.get(FrameHopperView.findTagIndexById(id)).setDeleted(true);

        //set deleted to true in database
        //tagService.hideTag(id);
        new SetHiddenStatusThread(tagService,id,true).start();

        //notify table changed
        notifyTableChange();
    }

    private void deleteTag(Integer id){
        //delete tag from database
        //tagService.deleteTag(FrameHopperView.TAG_LIST.get(FrameHopperView.findTagIndexById(id)));
        Tag tag = FrameHopperView.TAG_LIST.get(FrameHopperView.findTagIndexById(id));

        new DeleteTagThread(tagService,tag).start();

        //remove tag from list
        FrameHopperView.TAG_LIST.remove(FrameHopperView.findTagIndexById(id));

        //notify settings
        notifyTableChange();
    }

    private JPanel setUpSettings(){
        JPanel settingsPanel = new JPanel(new GridLayout(0, 1));

        JCheckBox hiddenTags = new JCheckBox("Show hidden tags");
        hiddenTags.addItemListener(e -> {
            if(e.getStateChange() == ItemEvent.SELECTED)
                FrameHopperView.USER_SETTINGS.setShowDeleted(true);
            else if(e.getStateChange() == ItemEvent.DESELECTED)
                FrameHopperView.USER_SETTINGS.setShowDeleted(false);

            new SaveSettingsThread(settingsService).start();
            notifyTableChange();
        });

        if(FrameHopperView.USER_SETTINGS.getShowDeleted())
            hiddenTags.doClick();

        JCheckBox checkBox2 = new JCheckBox("TEST 2");

        settingsPanel.add(hiddenTags);
        settingsPanel.add(checkBox2);

        return settingsPanel;
    }


}
