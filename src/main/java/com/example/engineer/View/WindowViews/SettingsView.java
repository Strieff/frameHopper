package com.example.engineer.View.WindowViews;


import com.example.engineer.Model.Tag;
import com.example.engineer.Service.SettingsService;
import com.example.engineer.Service.TagService;
import com.example.engineer.DBActions.DeleteTagAction;
import com.example.engineer.DBActions.SaveSettingsAction;
import com.example.engineer.View.Elements.MultilineTableCellRenderer;
import com.example.engineer.View.Elements.actions.PasteRecentAction;
import com.example.engineer.View.Elements.TagListManager;
import com.example.engineer.View.FrameHopperView;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class SettingsView extends JFrame implements ApplicationContextAware {
    @Autowired
    private final TagService tagService;
    @Autowired
    private final SettingsService settingsService;
    @Autowired
    private TagListManager tagList;
    @Autowired
    PasteRecentAction pasteRecentAction;

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
        JButton createNewTagButton = new JButton("Add Code");
        createNewTagButton.addActionListener(e -> {
            getApplicationContext().getBean(TagDetailsView.class).openWindow();
        });

        JButton addBatchTagsButton = new JButton("Add Codes");
        addBatchTagsButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            FileNameExtensionFilter filter = new FileNameExtensionFilter("txt/csv files", "txt", "csv");
            fileChooser.setFileFilter(filter);

            int returnValue = fileChooser.showOpenDialog(null);
            if (returnValue == JFileChooser.APPROVE_OPTION)
                loadTagsFromFile(fileChooser.getSelectedFile().getAbsolutePath());
        });

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

        JButton massDeleteButton = new JButton("Delete codes");
        massDeleteButton.addActionListener(e -> {
            int[] selectedRows = tagTable.getSelectedRows();
            if (selectedRows.length == 0) {
                JOptionPane.showMessageDialog(this, "No codes selected!", "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            if(getActionConfirmation(Arrays.stream(selectedRows).mapToObj(row -> (String)tagTable.getValueAt(row,0)).collect(Collectors.toList()),"delete"))
                return;

            List<Tag> tagsToDelete = new ArrayList<>();
            for (int selectedRow : selectedRows) {
                Tag temp = Tag.builder()
                        .name((String) tagTable.getValueAt(selectedRow, 0))
                        .value((Double) tagTable.getValueAt(selectedRow, 1))
                        .description((String) tagTable.getValueAt(selectedRow, 2))
                        .id((int) tagTable.getValueAt(selectedRow, 5))
                        .build();

                ctx.getBean(FrameHopperView.class).removeTagFromAllFrames(temp);
                tagList.removeTag(temp.getId());

                tagsToDelete.add(temp);
            }

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

        Object[] columnNames = {"CODE", "VALUE", "DESCRIPTION", " ", " ","ID"};

        int tableLen = FrameHopperView.USER_SETTINGS.getShowDeleted()?
                tagList.getSize():
                tagList.getNumberOfVisibleTags();
        Object[][] data = new Object[tableLen][];

        //create data rows
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
                    boolean hidden = tagList.getTag(tagID).isDeleted();
                    getApplicationContext().getBean(TagDetailsView.class).getDetailsData(tagName, tagValue, tagDescription,tagID,hidden);
                }

                if(row>0 && column == 4){ //check if the click is on the 5th column
                    Integer tagID = (Integer) tagTable.getValueAt(row,5);
                    showOptionsDialog(tagID);
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
                close();
            }
        });

        //shortcut key binds
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_S,KeyEvent.SHIFT_DOWN_MASK,false),"OpenSettings");
        getRootPane().getActionMap().put("OpenSettings", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
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

        new DeleteTagAction(tagService,tag).run();

        //remove tag from list
        tagList.removeTag(id);

        //notify settings
        notifyTableChange();

        if(ctx.getBean(FrameHopperView.class).loaded){
            ctx.getBean(FrameHopperView.class).removeTagFromAllFrames(tag);
            ctx.getBean(FrameHopperView.class).displayTagList();
        }
    }

    private JPanel setUpSettings(){
        JPanel settingsPanel = new JPanel(new GridLayout(0, 1));

        JCheckBox hiddenTags = new JCheckBox("Show hidden tags");
        hiddenTags.addItemListener(e -> {
            if(e.getStateChange() == ItemEvent.SELECTED)
                FrameHopperView.USER_SETTINGS.setShowDeleted(true);
            else if(e.getStateChange() == ItemEvent.DESELECTED)
                FrameHopperView.USER_SETTINGS.setShowDeleted(false);

            new SaveSettingsAction(settingsService).run();
            notifyTableChange();
        });

        hiddenTags.setSelected(FrameHopperView.USER_SETTINGS.getShowDeleted());

        JCheckBox openRecent = new JCheckBox("Open recent");
        openRecent.addItemListener(e -> {
            if(e.getStateChange() == ItemEvent.SELECTED)
                FrameHopperView.USER_SETTINGS.setOpenRecent(true);
            else if(e.getStateChange() == ItemEvent.DESELECTED)
                FrameHopperView.USER_SETTINGS.setOpenRecent(false);

            new SaveSettingsAction(settingsService).run();
        });

        openRecent.setSelected(FrameHopperView.USER_SETTINGS.getOpenRecent());

        settingsPanel.add(hiddenTags);
        settingsPanel.add(openRecent);

        return settingsPanel;
    }

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

    private boolean getActionConfirmation(List<String> names, String action){
        StringBuilder toDelete = new StringBuilder();
        for(String name : names)
            toDelete.append("\n").append(name);

        return JOptionPane.showConfirmDialog(
                null,
                "Do you want to " + action + " these codes?" + toDelete,
                "Confirm action",
                JOptionPane.YES_NO_OPTION
        ) == JOptionPane.NO_OPTION;
    }
}
