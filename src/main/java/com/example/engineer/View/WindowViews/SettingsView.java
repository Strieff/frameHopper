package com.example.engineer.View.WindowViews;


import com.example.engineer.Model.Tag;
import com.example.engineer.Service.TagService;
import com.example.engineer.View.Elements.MultilineTableCellRenderer;
import com.example.engineer.View.Elements.UserSettingsManager;
import com.example.engineer.View.Elements.actions.PasteRecentAction;
import com.example.engineer.View.Elements.TagListManager;
import com.example.engineer.View.FrameHopperView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.io.Serial;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class SettingsView extends JFrame implements ApplicationContextAware {
    //needed services and components
    @Autowired
    private TagService tagService;
    @Autowired
    private TagListManager tagList;
    @Autowired
    PasteRecentAction pasteRecentAction;
    @Autowired
    UserSettingsManager userSettings;

    //JComponents
    private JTable tagTable;

    //context
    private static ApplicationContext ctx;
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        ctx = applicationContext;
    }

    public void setUpView(){
        //KEYBINDING
        //close keybinding
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_S,KeyEvent.SHIFT_DOWN_MASK,false),"OpenSettings");
        getRootPane().getActionMap().put("OpenSettings", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                close();
            }
        });

        //set needed information
        setSize(700, 400);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        //COMPONENTS

        //main panel
        JPanel mainPanel = new JPanel(new BorderLayout());

        //tag list panel
        JPanel tablePanel = new JPanel(new BorderLayout());

        //BUTTONS

        //add new tag button
        JButton createNewTagButton = new JButton("Add Code");
        createNewTagButton.addActionListener(e -> {
            ctx.getBean(TagDetailsView.class).openWindow();
        });

        //batch add new tags
        JButton addBatchTagsButton = new JButton("Add Codes");
        addBatchTagsButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            FileNameExtensionFilter filter = new FileNameExtensionFilter("txt/csv files", "txt", "csv");
            fileChooser.setFileFilter(filter);

            int returnValue = fileChooser.showOpenDialog(null);
            if (returnValue == JFileChooser.APPROVE_OPTION)
                loadTagsFromFile(fileChooser.getSelectedFile().getAbsolutePath());
        });

        //batch hide tags button
        JButton massHideButton = new JButton("Hide codes");
        massHideButton.addActionListener(e -> {
            int[] selectedRows = tagTable.getSelectedRows();
            if (selectedRows.length == 0) {
                JOptionPane.showMessageDialog(this, "No codes selected!", "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            if(getActionConfirmation(Arrays.stream(selectedRows).mapToObj(row -> (String)tagTable.getValueAt(row,0)).collect(Collectors.toList()),"hide"))
                return;

            List<Integer> ids = new ArrayList<>();
            for (int selectedRow : selectedRows) {
                int tagId = (int) tagTable.getValueAt(selectedRow, 5);

                ids.add(tagId);

                tagList.changeHideStatus(tagId,true);
            }

            ctx.getBean(FrameHopperView.class).displayTagList();
            notifyTableChange();

            tagService.hideTags(ids,true);
        });

        //batch un hide tags button
        JButton massUnhideButton = new JButton("Unhide codes");
        massUnhideButton.addActionListener(e -> {
            int[] selectedRows = tagTable.getSelectedRows();
            if (selectedRows.length == 0) {
                JOptionPane.showMessageDialog(this, "No codes selected!", "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            if(getActionConfirmation(Arrays.stream(selectedRows).mapToObj(row -> (String)tagTable.getValueAt(row,0)).collect(Collectors.toList()),"unhide"))
                return;

            List<Integer> ids = new ArrayList<>();
            for (int selectedRow : selectedRows) {
                int tagId = (int) tagTable.getValueAt(selectedRow, 5);

                ids.add(tagId);

                tagList.changeHideStatus(tagId,false);
            }

            ctx.getBean(FrameHopperView.class).displayTagList();
            notifyTableChange();

            tagService.hideTags(ids,false);
        });

        //batch delete tags
        JButton massDeleteButton = new JButton("Delete codes");
        massDeleteButton.addActionListener(e -> {
            int[] selectedRows = tagTable.getSelectedRows();
            if (selectedRows.length == 0) {
                JOptionPane.showMessageDialog(this, "No codes selected!", "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            if(getActionConfirmation(Arrays.stream(selectedRows).mapToObj(row -> (String)tagTable.getValueAt(row,0)).toList(),"delete"))
                return;

            List<Tag> tagsToDelete = new ArrayList<>();
            for (int selectedRow : selectedRows) {
                Tag temp = Tag.builder()
                        .id((int) tagTable.getValueAt(selectedRow, 5))
                        .build();

                if(ctx.getBean(FrameHopperView.class).loaded)
                    ctx.getBean(FrameHopperView.class).removeTagFromAllFrames(temp);

                tagsToDelete.add(temp);
            }

            tagList.removeTags(tagsToDelete);

            if(ctx.getBean(FrameHopperView.class).loaded)
                ctx.getBean(FrameHopperView.class).displayTagList();

            notifyTableChange();

            tagService.deleteTag(tagsToDelete);
        });

        //toolbar
        JToolBar toolBar = new JToolBar();
        toolBar.add(createNewTagButton);
        toolBar.add(addBatchTagsButton);
        toolBar.add(massHideButton);
        toolBar.add(massUnhideButton);
        toolBar.add(massDeleteButton);
        tablePanel.add(toolBar,BorderLayout.NORTH);

        //settings label
        JLabel frameLabel = new JLabel("Code manager | Settings", SwingConstants.CENTER);
        frameLabel.setFont(new Font("Arial", Font.BOLD, 16));
        mainPanel.add(frameLabel, BorderLayout.NORTH);

        //tag table
        tagTable = new JTable() {
            @Serial
            private static final long serialVersionUID = 1L;

            @Override
            public Class<?> getColumnClass(int column) {
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

        //create data rows
        int tableLen = userSettings.ShowHidden() ?
                tagList.getSize() :
                tagList.getNumberOfVisibleTags();
        Object[][] data = new Object[tableLen][];

        int i = 0;
        for (Tag t : tagList.getTagList()) {
            if(!t.isDeleted()){
                data[i++] = new Object[]{
                        t.getName(),
                        t.getValue(),
                        t.getDescription(),
                        new ImageIcon(getIconFromPath("/icons/bin.png")),
                        new ImageIcon(getIconFromPath("/icons/edit.png")),
                        t.getId()
                };
            }
        }

        //model
        DefaultTableModel model = new DefaultTableModel(data, new String[]{"CODE", "VALUE", "DESCRIPTION", " ", " ","ID"}) {
            public Class<?> getColumnClass(int column) {
                if (column == 3 || column == 4) {
                    return Icon.class;
                }
                return super.getColumnClass(column);
            }
        };
        tagTable.setModel(model);

        //set needed parameters of the table
        tagTable.getTableHeader().setReorderingAllowed(false);
        DefaultTableCellRenderer centerRender = new DefaultTableCellRenderer();
        centerRender.setHorizontalAlignment(SwingConstants.CENTER);
        tagTable.getTableHeader().setResizingAllowed(false);

        //set size to trash icon - 5th column
        tagTable.getColumnModel().getColumn(4).setPreferredWidth(24);
        tagTable.getColumnModel().getColumn(4).setMaxWidth(24);

        //set size to edit icon - 4th column
        tagTable.getColumnModel().getColumn(3).setPreferredWidth(24);
        tagTable.getColumnModel().getColumn(3).setMaxWidth(24);

        //set size to value and center - 2nd column
        tagTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        tagTable.getColumnModel().getColumn(1).setMaxWidth(100);
        tagTable.getColumnModel().getColumn(1).setCellRenderer(centerRender);

        //set multi row to description and name
        tagTable.getColumnModel().getColumn(2).setCellRenderer(new MultilineTableCellRenderer());

        //set size of ID to 0 - 6th column
        tagTable.getColumnModel().getColumn(5).setMinWidth(0);
        tagTable.getColumnModel().getColumn(5).setMaxWidth(0);
        tagTable.getColumnModel().getColumn(5).setWidth(0);

        //listeners for edit/delete icons in table
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
                    boolean hidden = tagList.getTag(tagID).isDeleted();
                    ctx.getBean(TagDetailsView.class).getDetailsData(tagName, tagValue, tagDescription,tagID,hidden);
                }

                if(row>0 && column == 4){ //check if the click is on the 5th column
                    Integer tagID = (Integer) tagTable.getValueAt(row,5);
                    showOptionsDialog(tagID);
                }
            }
        });

        //scroll pane for the table
        JScrollPane scrollPane = new JScrollPane(tagTable);
        scrollPane.setPreferredSize(new Dimension(550, getHeight())); // Adjust the width as needed

        tablePanel.add(scrollPane, BorderLayout.CENTER);

        mainPanel.add(tablePanel,BorderLayout.WEST);

        //SETTINGS PANEL

        //show hidden tags checkbox
        JCheckBox hiddenTags = new JCheckBox("Show hidden tags");
        hiddenTags.addItemListener(e -> {
            if(e.getStateChange() == ItemEvent.SELECTED)
                userSettings.setShowHidden(true);
            else if(e.getStateChange() == ItemEvent.DESELECTED)
                userSettings.setShowHidden(false);

            userSettings.save();

            notifyTableChange();
        });
        hiddenTags.setSelected(userSettings.ShowHidden());

        //open recent checkbox
        JCheckBox openRecent = new JCheckBox("Open recent");
        openRecent.addItemListener(e -> {
            if(e.getStateChange() == ItemEvent.SELECTED)
                userSettings.setOpenRecent(true);
            else if(e.getStateChange() == ItemEvent.DESELECTED)
                userSettings.setOpenRecent(false);

            userSettings.save();
        });
        openRecent.setSelected(userSettings.openRecent());

        //panel to hold settings
        JPanel settingsPanel = new JPanel(new GridLayout(0,1));
        settingsPanel.add(hiddenTags);
        settingsPanel.add(openRecent);

        mainPanel.add(settingsPanel, BorderLayout.CENTER);

        add(mainPanel);

        //operation on close
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                close();
            }
        });
    }

    public void open(){
        setVisible(true);
    }

    public void close(){
        setVisible(false);
    }

    public synchronized void notifyTableChange(){
        DefaultTableModel model = (DefaultTableModel) tagTable.getModel();
        model.setRowCount(0); // Clear the existing rows


        for (Tag tag : tagList.getTagList()) {
            // Assuming tag has properties: id, name, value, description
            if(!tag.isDeleted() || userSettings.ShowHidden()){
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

    private void showOptionsDialog(Integer tagId){
        String[] options = {"Cancel","Delete"};

        int actionChoice = JOptionPane.showOptionDialog(this, "Delete tag?", "",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
                null, options, options[0]);

        switch(actionChoice){
            case 0: //cancel
                break;
            case 1: //delete
                deleteTag(tagId);
                break;
        }
    }

    //delete tag from database
    private void deleteTag(Integer id){
        Tag tag = tagList.getTag(id);

        //remove tag from list
        tagList.removeTag(id);

        //notify settings
        notifyTableChange();

        if(ctx.getBean(FrameHopperView.class).loaded){
            ctx.getBean(FrameHopperView.class).removeTagFromAllFrames(tag);
            ctx.getBean(FrameHopperView.class).displayTagList();
        }
    }

    //load tags from file
    private void loadTagsFromFile(String path){
        try {
            List<String> read = Files.readAllLines(Paths.get(path));

            for(String line : read) {
                String[] tagInfo = line.split(";");

                tagList.addTag(
                        tagInfo[0],
                        Double.parseDouble(tagInfo[1].replace(",",".")),
                        tagInfo.length == 3 ? tagInfo[2] : "");
            }

            notifyTableChange();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //get action confirmation
    private boolean getActionConfirmation(List<String> names, String action){
        StringBuilder toDelete = new StringBuilder();
        for(String name : names)
            toDelete.append("\n").append(name);

        return JOptionPane.showConfirmDialog(
                null,
                 String.format("Do you want to %s these codes?%s",action,toDelete),
                "Confirm action",
                JOptionPane.YES_NO_OPTION
        ) == JOptionPane.NO_OPTION;
    }
}
