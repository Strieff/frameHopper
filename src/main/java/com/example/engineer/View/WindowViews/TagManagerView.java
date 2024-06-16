package com.example.engineer.View.WindowViews;

import com.example.engineer.Model.Tag;
import com.example.engineer.Service.FrameService;
import com.example.engineer.Threads.TagManagerThread;
import com.example.engineer.View.Elements.MultilineTableCellRenderer;
import com.example.engineer.View.FrameHopperView;
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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

@Component
@RequiredArgsConstructor
public class TagManagerView extends JFrame implements ApplicationContextAware {
    @Autowired
    private final FrameService frameService;
    private JTable tagTable;
    private JLabel frameLabel;
    private JButton nameSortButton;
    private JButton valueSortButton;

    private List<Tag> originalTags;
    private List<Tag> currentTags;
    private Integer frameNo;
    private String videoName;
    private FrameHopperView frameHopperView;

    private String search = "";
    private Integer lastTagId = -1;

    private static ApplicationContext ctx;
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        ctx = applicationContext;
    }

    public void setUpView(FrameHopperView frameHopperView){
        this.frameHopperView = frameHopperView;

        setSize(250, 400);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        frameLabel = new JLabel("FRAME: ", SwingConstants.CENTER);
        frameLabel.setFont(new Font("Arial", Font.BOLD, 16));

        tagTable = new JTable(){
            @Serial
            private static final long serialVersionUID = 1L;

            @Override
            public Class<?> getColumnClass(int column) {
                return switch (column) {
                    case 0 -> Boolean.class;
                    case 1 -> String.class;
                    default -> Double.class;
                };
            }
        };

        tagTable.setPreferredScrollableViewportSize(tagTable.getPreferredSize());

        JScrollPane scrollPane = new JScrollPane(tagTable);

        nameSortButton = new JButton("TAG");
        valueSortButton = new JButton("VALUE");

        nameSortButton.addActionListener(e -> {
            if(nameSortButton.getText().contains("(a-z)"))
                nameSortButton.setText("TAG (z-a)");
            else if (nameSortButton.getText().contains("(z-a)"))
                nameSortButton.setText("TAG");
            else
                nameSortButton.setText("TAG (a-z)");

            arrangeTags(nameSortButton,valueSortButton);
        });

        valueSortButton.addActionListener(e -> {
            if(valueSortButton.getText().contains("▲"))
                valueSortButton.setText("VALUE ▼");
            else if(valueSortButton.getText().contains("▼"))
                valueSortButton.setText("VALUE");
            else
                valueSortButton.setText("VALUE ▲");

            arrangeTags(nameSortButton,valueSortButton);
        });

        JPanel upperButtonPanel = new JPanel(new GridLayout(1,2));
        upperButtonPanel.add(nameSortButton);
        upperButtonPanel.add(valueSortButton);

        JPanel searchPanel = new JPanel(new GridBagLayout());

        JTextField searchField = new JTextField();
        searchField.setPreferredSize(new Dimension(200,25));

        JButton searchButton = new JButton();
        searchButton.addActionListener(e -> {
            if(!isCloseIcon(searchButton)) {
                search = searchField.getText();
                if(!searchField.getText().isEmpty()) {
                    changeIcon("/icons/close.png", searchButton);
                    arrangeTags(nameSortButton,valueSortButton);
                }
            }
            else {
                search = "";
                searchField.setText("");
                changeIcon("/icons/search.png", searchButton);
                arrangeTags(nameSortButton,valueSortButton);
            }
        });
        changeIcon("/icons/search.png", searchButton);
        searchButton.setPreferredSize(new Dimension(20, 20));

        searchPanel.add(searchField);
        searchPanel.add(searchButton);

        JPanel upperPanel = new JPanel(new GridLayout(3, 1));
        upperPanel.add(frameLabel);
        upperPanel.add(searchPanel);
        upperPanel.add(upperButtonPanel);

        add(upperPanel, BorderLayout.NORTH);

        //add scroll pane
        add(scrollPane);

        //save buttons
        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e ->{
            save();
            close();
        });

        //cancel button
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> {
            close();
        });

        //button Panel
        JPanel buttonPanel = new JPanel(new GridLayout(1,2));
        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);

        add(buttonPanel,BorderLayout.SOUTH);


        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                close();
            }
        });

        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_M,KeyEvent.SHIFT_DOWN_MASK,false),"OpenManager");
        getRootPane().getActionMap().put("OpenManager", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                close();
            }
        });
    }

    //set up the table
    public void setUpData(String videoName, int frameNo){
        this.videoName = videoName;
        this.frameNo = frameNo;

        // Update the frame number label
        frameLabel.setText("FRAME: " + (frameNo+1));

        originalTags = new ArrayList<>(frameHopperView.getTagsOfFrame(frameNo));
        currentTags = new ArrayList<>(originalTags);

        Object[] columnNames = {" ","TAG","VALUE","ID"};
        Object[][]  data = new Object[getTableLen()][];

        DefaultTableModel model = new DefaultTableModel(data,columnNames);

        //checkbox for tags
        model.addTableModelListener(e -> {
            int row = e.getFirstRow();
            int column = e.getColumn();

            if(column == 0){
                Boolean value = (Boolean) model.getValueAt(row,column);
                Integer id = (Integer) model.getValueAt(row,3);

                if(value)
                    addTag(id);
                else
                    removeTag(id);
            }
        });

        tagTable.setModel(model);
        tagTable.getTableHeader().setReorderingAllowed(false);

        //set size of ID to 0 - 4th column
        tagTable.getColumnModel().getColumn(3).setMinWidth(0);
        tagTable.getColumnModel().getColumn(3).setMaxWidth(0);
        tagTable.getColumnModel().getColumn(3).setWidth(0);

        //set size of checkbox to - 1st column
        tagTable.getColumnModel().getColumn(0).setPreferredWidth(24);
        tagTable.getColumnModel().getColumn(0).setMaxWidth(24);

        //make name column wrap - 2nd column
        tagTable.getColumnModel().getColumn(1).setCellRenderer(new MultilineTableCellRenderer());

        //center value column - 3rd column
        DefaultTableCellRenderer centerRender = new DefaultTableCellRenderer();
        centerRender.setHorizontalAlignment(SwingConstants.CENTER);
        tagTable.getColumnModel().getColumn(2).setCellRenderer(centerRender);

        //disable row select
        tagTable.setRowSelectionAllowed(false);

        tagTable.revalidate();

        getRootPane().requestFocus();
        arrangeTags(nameSortButton,valueSortButton);
        setVisible(true);
    }

    private void populateTable(){
        DefaultTableModel model = (DefaultTableModel) tagTable.getModel();
        model.setRowCount(0);

       for(Tag t : search.isEmpty() ? FrameHopperView.TAG_LIST : getSearchResult(FrameHopperView.TAG_LIST)){
           if(FrameHopperView.USER_SETTINGS.getShowDeleted() || !t.isDeleted()){
               model.addRow(new Object[]{
                       !originalTags.isEmpty() && isTagHeld(t),
                       t.getName() + ((FrameHopperView.USER_SETTINGS.getShowDeleted() && t.isDeleted())? " (hidden)" : ""),
                       t.getValue(),
                       t.getId()
               });
           }
       }

       tagTable.setModel(model);
    }

    private void populateTable(List<Tag> sortedData){
        DefaultTableModel model = (DefaultTableModel) tagTable.getModel();
        model.setRowCount(0);

        for(Tag t : search.isEmpty() ? sortedData : getSearchResult(sortedData)){
            if(FrameHopperView.USER_SETTINGS.getShowDeleted() || !t.isDeleted())
                model.addRow(new Object[]{
                    !originalTags.isEmpty() && isTagHeld(t),
                    t.getName() + ((FrameHopperView.USER_SETTINGS.getShowDeleted() && t.isDeleted())? " (hidden)" : ""),
                    t.getValue(),
                    t.getId()
                });
        }

        tagTable.setModel(model);
    }

    private int getTableLen() {
        return (int) FrameHopperView.TAG_LIST.stream()
                .filter(t -> !t.isDeleted() || isTagHeld(t))
                .count();
    }

    private List<Tag> getSearchResult(List<Tag> data){
        return data.stream()
                .filter(t -> t.getName().contains(search))
                .toList();
    }

    private boolean isTagHeld(Tag tag){
        return currentTags.stream()
                .anyMatch(t -> t.getId().equals(tag.getId()));
    }

    private void addTag(Integer tagId){
        lastTagId = tagId;
        currentTags.add(getTagById(tagId));
    }

    private void removeTag(Integer tagId){
        int forRemoval = IntStream.range(0, currentTags.size())
                .filter(i -> currentTags.get(i).getId().intValue() == tagId.intValue())
                .findFirst()
                .orElse(-1);

        if(forRemoval!=-1)
            currentTags.remove(forRemoval);
    }

    private Tag getTagById(Integer id){
        return FrameHopperView.TAG_LIST.stream()
                .filter(t -> t.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public void close() {
        setVisible(false);

        //clear data
        frameNo = null;
        videoName = null;
    }

    private void save(){
        frameHopperView.setCurrentTags(currentTags,frameNo);

        ctx.getBean(FrameHopperView.class).displayTagList();

        //save data async
        new TagManagerThread().setUp(currentTags,originalTags,frameNo,videoName,frameService).start();
    }

    public void addLastTag(){
        if(lastTagId!=-1)
            ctx.getBean(FrameHopperView.class).addLastTag(getTagById(lastTagId));
    }

    private void changeIcon(String path, JButton button){
        URL iconURL = getClass().getResource(path);
        if(iconURL != null) {
            ImageIcon imageIcon = new ImageIcon(iconURL);
            Image scaledIcon = imageIcon.getImage().getScaledInstance(16,16,Image.SCALE_SMOOTH);
            button.setIcon(new ImageIcon(scaledIcon));
            button.putClientProperty("icon",path);
        }
    }

    private boolean isCloseIcon(JButton button){
        return ((String) button.getClientProperty("icon")).contains("close");
    }

    private List<Tag> sortTags(boolean alphabetical, boolean ascending){
        List<Tag> sortedList = new ArrayList<>(FrameHopperView.TAG_LIST);

        Comparator<Tag> comparator = Comparator.comparing(Tag::getName,String.CASE_INSENSITIVE_ORDER);
        if(!alphabetical)
            comparator = comparator.reversed();

        comparator = comparator.thenComparing(Tag::getValue);
        if(!ascending)
            comparator = comparator.thenComparing(Comparator.comparing(Tag::getValue).reversed());

        sortedList.sort(comparator);
        return sortedList;
    }

    private List<Tag> sortAlphabetical(boolean alphabetical){
        List<Tag> sortedList = new ArrayList<>(FrameHopperView.TAG_LIST);

        Comparator<Tag> comparator = Comparator.comparing(Tag::getName,String.CASE_INSENSITIVE_ORDER);
        if(!alphabetical)
            comparator = comparator.reversed();

        sortedList.sort(comparator);

        return sortedList;
    }

    private List<Tag> sortByValue(boolean asc){
        List<Tag> sortedList = new ArrayList<>(FrameHopperView.TAG_LIST);

        Comparator<Tag> comparator = Comparator.comparing(Tag::getValue);
        if (!asc) {
            comparator = comparator.reversed();
        }

        sortedList.sort(comparator);
        return sortedList;
    }

    private void arrangeTags(JButton nameButton, JButton valueButton){
        if(isBlank(nameButton)){
            if(isBlank(valueButton))
                populateTable();
            else if(isAsc(valueButton)) {
                populateTable(sortByValue(true));
            }else
                populateTable(sortByValue(false));

            return;
        }

        if(isAlphabetical(nameButton)){
            if(isBlank(valueButton))
                populateTable(sortAlphabetical(true));
            else if(isAsc(valueButton))
                populateTable(sortTags(true,true));
            else
                populateTable(sortTags(true,false));

            return;
        }

        if(!isAlphabetical(nameButton)){
            if(isBlank(valueButton))
                populateTable(sortAlphabetical(false));
            else if(isAsc(valueButton))
                populateTable(sortTags(false,true));
            else
                populateTable(sortTags(false,false));
        }
    }

    private boolean isAlphabetical(JButton nameButton){
        return nameButton.getText().contains("(a-z)");
    }

    private boolean isAsc(JButton valueButton){
        return valueButton.getText().contains("▲");
    }

    private boolean isBlank(JButton button){
        return (button.getText().equals("TAG") || button.getText().equals("VALUE"));
    }
}
