package com.example.engineer.View.ViewModel.PathMerging;

import com.example.engineer.FrameProcessor.FrameProcessorRequestManager;
import com.example.engineer.Service.VideoService;
import com.example.engineer.View.Elements.DialogProvider;
import com.example.engineer.View.Elements.IconLoader;
import com.example.engineer.View.Elements.Language.LanguageChangeListener;
import com.example.engineer.View.Elements.Language.LanguageManager;
import com.example.engineer.View.Elements.ProgramResetResolver;
import com.example.engineer.View.Elements.tableRenderer.MultilineTableCellRenderer;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.*;
import java.io.Serial;

@Component(value = "MergingList")
@DependsOn("FrameHopperView")
public class PathMergingListView extends JFrame implements ApplicationContextAware,LanguageChangeListener {
    //context
    private static ApplicationContext ctx;
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        ctx = applicationContext;
    }

    @Autowired
    VideoService videoService;
    @Autowired
    LanguageManager languageManager;
    @Autowired
    FrameProcessorRequestManager requestManager;

    JTable pathTable;

    public PathMergingListView(){
        //set needed information
        setSize(300, 400);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        //set up table
        pathTable = new JTable(new DefaultTableModel(null,new String[]{"","","",""})){
            @Serial
            private static final long serialVersionUID = 1L;

            @Override
            public Class<?> getColumnClass(int column) {
                return switch (column) {
                    case 0 -> Integer.class;
                    case 1 -> String.class;
                    default -> Icon.class;
                };
            }

            @Override
            public boolean isCellEditable(int row,int column){
                return false;
            }
        };

        //action listener
        pathTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = pathTable.rowAtPoint(e.getPoint());
                int column = pathTable.columnAtPoint(e.getPoint());

                if (row >= 0 && column == 2) { // Check if the click is on the 4th column
                    Integer videoId = (Integer) pathTable.getValueAt(row,0);
                    ctx.getBean(PathMergingDetailsView.class).open(videoId);
                }

                if(row>=0 && column == 3){ //check if the click is on the 5th column
                    if(DialogProvider.yesNoDialog("Video will be deleted. This action cannot be reversed. Do you want to continue?")){
                        Integer videoId = (Integer) pathTable.getValueAt(row, 0);
                        videoService.deleteVideo(videoId);

                        DialogProvider.messageDialog("Please wait. The program will restart shortly.");
                        requestManager.closeServer();
                        ProgramResetResolver.reset();
                    }
                }
            }
        });

        //change id column to 0
        var idColumn = pathTable.getColumnModel().getColumn(0);
        idColumn.setMinWidth(0);
        idColumn.setMaxWidth(0);
        idColumn.setPreferredWidth(0);

        //add renderer to path column
        pathTable.getColumnModel().getColumn(1).setCellRenderer(new MultilineTableCellRenderer());

        //set size to trash icon - 4th column
        pathTable.getColumnModel().getColumn(3).setPreferredWidth(24);
        pathTable.getColumnModel().getColumn(3).setMaxWidth(24);

        //set size to edit icon - 3rd column
        pathTable.getColumnModel().getColumn(2).setPreferredWidth(24);
        pathTable.getColumnModel().getColumn(2).setMaxWidth(24);

        //scroll pane
        JScrollPane scrollPane = new JScrollPane(pathTable);
        add(scrollPane);

        //operation on close
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                close();
            }
        });

        //close key bind
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_L,KeyEvent.SHIFT_DOWN_MASK,false),"OpenVideoList");
        getRootPane().getActionMap().put("OpenVideoList", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                close();
            }
        });
    }

    @PostConstruct
    public void init(){
        languageManager.addListener(this);
    }

    public void open(){
        loadTable();

        setVisible(true);
    }

    public void loadTable(){
        DefaultTableModel model = (DefaultTableModel) pathTable.getModel();
        model.setRowCount(0);

        for (var v : videoService.getAll())
            model.addRow(new Object[]{
                    v.getId(),
                    v.getPath(),
                    IconLoader.getSmallIcon("edit.png"),
                    IconLoader.getSmallIcon("bin.png")
            });
    }

    public void close(){
        setVisible(false);
    }

    @Override
    public void changeLanguage() {

    }
}
