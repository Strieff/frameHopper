package com.example.engineer.View.buttonsView;

import com.example.engineer.Model.Frame;
import com.example.engineer.Model.Tag;
import com.example.engineer.Model.Video;
import com.example.engineer.Service.FrameService;
import com.example.engineer.Service.TagService;
import com.example.engineer.Service.VideoService;
import com.example.engineer.View.Elements.MultilineTableCellRenderer;
import com.example.engineer.View.FrameHopperView;
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
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ExportView extends JFrame {
    @Autowired
    private VideoService videoService;
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
        List<Object[]> data = tagService.countTagsOnFramesOfVideo(video);

        Map<String,Long> tagAmount = new HashMap<>();
        for(Object[] o : data)
            tagAmount.put((String)o[0],(Long)o[1]);

        exportToExcel(video,path,tagAmount);
    }

    private void exportData(List<Integer> indexList, String path){
        List<Object[]> data = tagService.countTagsOnFramesOfVideo(indexList);

        Map<Video,Map<String,Long>> tagAmountOnVideos = new HashMap<>();
        for(Object[] o : data){
            if(!tagAmountOnVideos.containsKey((Video)o[0]))
                tagAmountOnVideos.put((Video)o[0],new HashMap<>());

            tagAmountOnVideos.get((Video)o[0]).put((String)o[1],(Long)o[2]);
        }

        for(Video video : tagAmountOnVideos.keySet())
            exportToExcel(video,path,tagAmountOnVideos.get(video));
    }

    private void exportToExcel(Video video, String path, Map<String,Long> tagData){
        long amount = tagService.getAmountOfUniqueTagsOnVideo(video);

        String fileName = video.getName().substring(0,video.getName().lastIndexOf('.'))+"_data.xlsx";

        try(
                Workbook workbook = WorkbookFactory.create(true);
                FileOutputStream outputStream = new FileOutputStream(path + File.separator + fileName)
        ){
            //create general data
            Sheet sheet = workbook.createSheet("Overview");

            // Create header row
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("FRAME COUNT");
            headerRow.createCell(1).setCellValue("TAGS");
            headerRow.createCell(2).setCellValue("RUNTIME (SECONDS)");
            headerRow.createCell(3).setCellValue("FRAMERATE");
            headerRow.createCell(4).setCellValue("TOTAL POINTS");
            headerRow.createCell(5).setCellValue("COMPLEXITY");

            //create data row
            Row dataRow = sheet.createRow(1);
            dataRow.createCell(0).setCellValue(video.getTotalFrames());
            dataRow.createCell(1).setCellValue(amount);
            dataRow.createCell(2).setCellValue(video.getDuration());
            dataRow.createCell(3).setCellValue(video.getFrameRate());

            //get total points
            int totalPoints = 0;
            for (String s : tagData.keySet())
                totalPoints += FrameHopperView.findTagByName(s).getValue()*tagData.get(s);

            dataRow.createCell(4 ).setCellValue(totalPoints);

            DecimalFormat df = new DecimalFormat("#.###");
            String complexity = df.format((double)totalPoints/video.getDuration()).replace(',','.');
            dataRow.createCell(5 ).setCellValue(Double.parseDouble(complexity));

            //resize columns
            for (int j = 0; j < 6; j++)
                sheet.autoSizeColumn(j);

            //create tag data info
            Sheet tagSheet = workbook.createSheet("Tag data");

            Row tagHeaderRow = tagSheet.createRow(0);
            tagHeaderRow.createCell(0).setCellValue("TAG");
            tagHeaderRow.createCell(1).setCellValue("VALUE");
            tagHeaderRow.createCell(2).setCellValue("AMOUNT");
            tagHeaderRow.createCell(3).setCellValue("TOTAL POINTS");

            int i = 1;
            for(String s : tagData.keySet()){
                Row infoRow = tagSheet.createRow(i++);
                infoRow.createCell(0).setCellValue(s);
                infoRow.createCell(1).setCellValue(FrameHopperView.findTagByName(s).getValue());
                infoRow.createCell(2).setCellValue(tagData.get(s));
                infoRow.createCell(3).setCellValue(FrameHopperView.findTagByName(s).getValue()*tagData.get(s));
            }

            //resize columns
            for (int j = 0; j < 4; j++)
                tagSheet.autoSizeColumn(j);

            // Write the workbook to a file
            workbook.write(outputStream);
            outputStream.flush();

        }catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
