package com.example.engineer.View.buttonsView;

import com.example.engineer.Model.Frame;
import com.example.engineer.Model.Tag;
import com.example.engineer.Model.Video;
import com.example.engineer.Service.FrameService;
import com.example.engineer.Service.TagService;
import com.example.engineer.Service.VideoService;
import com.example.engineer.View.Elements.MultilineTableCellRenderer;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

//TODO instead of fetching everything create a query that will return list of Tag/integers that will tell how many are in each video
@Component
public class ExportView extends JFrame {
    @Autowired
    private VideoService videoService;
    @Autowired
    private FrameService frameService;
    @Autowired
    private TagService tagService;

    private JTable videoNameTable;

    public void setUpView(){
        setSize(300, 400);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        DefaultTableModel model = new DefaultTableModel(null, new String[]{"","",""});
        videoNameTable = new JTable(model){
            @Serial
            private static final long serialVersionUID = 1L;

            @Override
            public Class getColumnClass(int column) {
                return switch (column) {
                    case 0 -> Boolean.class;
                    case 1 -> String.class;
                    default -> Integer.class;
                };
            }
        };

        //set size of ID to 0 - 3rd row
        videoNameTable.getColumnModel().getColumn(2).setMinWidth(0);
        videoNameTable.getColumnModel().getColumn(2).setMaxWidth(0);
        videoNameTable.getColumnModel().getColumn(2).setWidth(0);

        //set checkbox cell size
        videoNameTable.getColumnModel().getColumn(0).setPreferredWidth(24);
        videoNameTable.getColumnModel().getColumn(0).setMaxWidth(24);

        videoNameTable.getColumnModel().getColumn(1).setCellRenderer(new MultilineTableCellRenderer());

        videoNameTable.setTableHeader(null);
        videoNameTable.setFocusable(false);
        videoNameTable.setRowSelectionAllowed(false);
        JScrollPane scrollPane = new JScrollPane(videoNameTable);

        add(scrollPane);

        //export button
        JButton exportButton = new JButton("EXPORT");
        exportButton.addActionListener(e -> {
            List<Integer> selected = getExportList();

            if(selected.size() == 0){
                JOptionPane.showMessageDialog(this, "No videos were selected", "No Videos Selected", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String path = getSaveLocation();
            if(path == null)
                return;

            if (selected.size() == 1) {
                exportData(selected.get(0), path);
            } else {
                exportData(selected, path);
            }
        });

        //cancel button
        JButton cancelButton = new JButton("CANCEL");
        cancelButton.addActionListener(e -> close());

        JPanel buttonPanel = new JPanel(new GridLayout(1,2));
        buttonPanel.add(cancelButton);
        buttonPanel.add(exportButton);

        add(buttonPanel,BorderLayout.SOUTH);

        setResizable(false);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                close();
            }
        });
    }

    private void close() {
        setVisible(false);
        ((DefaultTableModel) videoNameTable.getModel()).setRowCount(0);
    }

    public void open() {
        DefaultTableModel model = (DefaultTableModel) videoNameTable.getModel();
        List<Video> videos = videoService.getAll();

        if(!videos.isEmpty())
            for (Video v : videos)
                model.addRow(new Object[]{
                        false,
                        v.getName(),
                        v.getId()
                });

        videoNameTable.setModel(model);
        videoNameTable.revalidate();

        setVisible(true);
    }

    private String getSaveLocation(){
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int userSelection = fileChooser.showSaveDialog(this);

        if(userSelection == JFileChooser.APPROVE_OPTION)
            return fileChooser.getSelectedFile().getAbsolutePath();
        else
            return null;
    }

    private List<Integer> getExportList(){
        List<Integer> selected = new ArrayList<>();
        DefaultTableModel model = (DefaultTableModel) videoNameTable.getModel();

        if(model.getRowCount() == 0)
            return selected;

        for (int row = 0; row < model.getRowCount(); row++) {
            Boolean isSelected = (Boolean) model.getValueAt(row,0);
            if(isSelected != null && isSelected){
                Integer id = (Integer) model.getValueAt(row,2);
                if(id != null)
                    selected.add(id);
            }
        }

        return selected;
    }

    private void exportData(int index, String path){
        Video video = videoService.getExportData(index);
        setTagsOnFrames(video);
        try {
            exportToExcel(video,path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void exportData(List<Integer> indexList, String path){
        List<Video> videos = videoService.getExportData(indexList);
        for (Video video : videos)
            setTagsOnFrames(video);

    }

    private void setTagsOnFrames(Video video){
        if(video.getFrames().isEmpty())
            return;

        for (int i = 0; i < video.getFrames().size(); i++) {
            List<Tag> tags = tagService.getTagsOnFrame(video.getFrames().get(i).getFrameNumber(),video);
            if (tags.isEmpty())
                continue;

            video.getFrames().get(i).setTags(tags);
        }
    }

    private void exportToExcel(Video video,String path) throws IOException {

        try(
                Workbook workbook = WorkbookFactory.create(true);
                FileOutputStream outputStream = new FileOutputStream(path+ File.separator+"test.xlsx")
        ){
            Sheet sheet = workbook.createSheet("TEST");

            // Create header row
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Name");
            headerRow.createCell(1).setCellValue("Total Points");
            headerRow.createCell(2).setCellValue("Amount");

            // Write the workbook to a file
            workbook.write(outputStream);
            outputStream.flush();

        }catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
