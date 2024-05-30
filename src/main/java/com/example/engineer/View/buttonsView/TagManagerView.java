package com.example.engineer.View.buttonsView;

import com.example.engineer.Model.Tag;
import com.example.engineer.Service.FrameService;
import com.example.engineer.Threads.TagManagerThread;
import com.example.engineer.View.FrameHopperView;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

@Component
@RequiredArgsConstructor
public class TagManagerView extends JFrame implements ApplicationContextAware {
    @Autowired
    private final FrameService frameService;
    private JTable tagTable;

    private List<Tag> originalTags;
    private List<Tag> currentTags;
    private Integer frameNo;
    private String videoName;
    private FrameHopperView frameHopperView;

    private static ApplicationContext ctx;
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        ctx = applicationContext;
    }

    public void setUpView(FrameHopperView frameHopperView){
        this.frameHopperView = frameHopperView;

        setSize(250, 400);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        JLabel frameLabel = new JLabel("FRAME: ", SwingConstants.CENTER);
        frameLabel.setFont(new Font("Arial", Font.BOLD, 16));
        add(frameLabel, BorderLayout.NORTH);


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
        ((JLabel) getContentPane().getComponent(0)).setText("FRAME: " + (frameNo+1));

        originalTags = new ArrayList<>(frameHopperView.getTagsOfFrame(frameNo));
        currentTags = new ArrayList<>(originalTags);

        Object[] columnNames = {" ","TAG","VALUE","ID"};

        int tableLen = 0;
        for(Tag t : FrameHopperView.TAG_LIST)
            if(!t.isDeleted() || isTagHeld(t))
                tableLen++;

        Object[][]  data = new Object[tableLen][];

        int i = 0;
        for (Tag t : FrameHopperView.TAG_LIST){
            if(!t.isDeleted() || isTagHeld(t)){
                data[i++] = new Object[]{
                        originalTags.size() != 0 && isTagHeld(t),
                        t.getName(),
                        t.getValue(),
                        t.getId()
                };
            }
        }

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

        //set size of ID to 0 - 4th row
        tagTable.getColumnModel().getColumn(3).setMinWidth(0);
        tagTable.getColumnModel().getColumn(3).setMaxWidth(0);
        tagTable.getColumnModel().getColumn(3).setWidth(0);

        tagTable.revalidate();

        setVisible(true);
    }

    private boolean isTagHeld(Tag tag){
        return currentTags.stream()
                .anyMatch(t -> t.getId().equals(tag.getId()));
    }

    private void addTag(Integer tagId){
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


}
