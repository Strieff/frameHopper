package com.example.engineer.View.ViewModel.PathReeling;

import com.example.engineer.Service.VideoService;
import com.example.engineer.View.Elements.tableRenderer.MultilineTableCellRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.Serial;

@Component
public class PathReelingView extends JFrame{
    @Autowired
    VideoService videoService;

    private final JTable videoNameTable;


    public PathReelingView(){
        //set needed information
        setSize(300, 400);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        //main panel
        JPanel mainPanel = new JPanel(new BorderLayout());

        DefaultTableModel model = new DefaultTableModel(null, new String[]{"",""}){
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        videoNameTable = new JTable(model){
            @Serial
            private static final long serialVersionUID = 1L;

            @Override
            public Class<?> getColumnClass(int column) {
                return String.class;
            }
        };

        //set size of ID to 0 - 1st row
        videoNameTable.getColumnModel().getColumn(0).setMinWidth(0);
        videoNameTable.getColumnModel().getColumn(0).setMaxWidth(0);
        videoNameTable.getColumnModel().getColumn(0).setWidth(0);

        //set renderer
        videoNameTable.getColumnModel().getColumn(1).setCellRenderer(new MultilineTableCellRenderer());

        videoNameTable.setTableHeader(null);

        mainPanel.add(new JScrollPane(videoNameTable));

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
        DefaultTableModel model = (DefaultTableModel) videoNameTable.getModel();

        for(var v : videoService.getAll())
            model.addRow(new Object[]{
                    v.getId(),
                    v.getPath()
            });

        setVisible(true);
    }

    public void close(){
        setVisible(false);
        ((DefaultTableModel) videoNameTable.getModel()).setRowCount(0);
    }
}
