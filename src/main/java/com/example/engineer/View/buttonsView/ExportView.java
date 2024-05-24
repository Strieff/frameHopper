package com.example.engineer.View.buttonsView;

import com.example.engineer.Model.Video;
import com.example.engineer.Service.TagService;
import com.example.engineer.Service.VideoService;
import com.example.engineer.View.Elements.MultilineTableCellRenderer;
import com.example.engineer.View.FrameHopperView;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.QuoteMode;
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
import java.io.*;
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

            exportData(selected, path);




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

    private void close() {
        setVisible(false);
        ((DefaultTableModel) videoNameTable.getModel()).setRowCount(0);
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

    private boolean getFormatChoice(){
        return JOptionPane.showOptionDialog(
                this,
                "File format:",
                "",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                new String[]{"Excel", "CSV"},
                "Excel"
        ) == 0;
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

    private void exportData(List<Integer> indexList, String path){
        List<Object[]> data = tagService.countTagsOnFramesOfVideo(indexList);

        Map<Video,Map<String,Long>> tagAmountOnVideos = new HashMap<>();
        for(Object[] o : data){
            if(!tagAmountOnVideos.containsKey((Video)o[0]))
                tagAmountOnVideos.put((Video)o[0],new HashMap<>());

            tagAmountOnVideos.get((Video)o[0]).put((String)o[1],(Long)o[2]);
        }

        if(getFormatChoice())
            for(Video video : tagAmountOnVideos.keySet())
                exportToExcel(video,path,tagAmountOnVideos.get(video));
        else
            for(Video video : tagAmountOnVideos.keySet())
                exportToCsv(video,path,tagAmountOnVideos.get(video));

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

    private void exportToCsv(Video video, String path, Map<String,Long> tagData){
        long amount = tagService.getAmountOfUniqueTagsOnVideo(video);
        String exportDirectory = path + File.separator + video.getName().substring(0,video.getName().lastIndexOf('.'))+"_csv";
        new File(exportDirectory).mkdirs();

        String overviewFileName = File.separator + "Overview.csv";
        List<String[]> overviewData = new ArrayList<>();
        overviewData.add(new String[]{"FRAME COUNT","TAGS","RUNTIME (SECONDS)","FRAMERATE","TOTAL POINTS","COMPLEXITY"});
        overviewData.add(new String[]{
                String.valueOf(video.getTotalFrames()),
                String.valueOf(amount),
                String.valueOf(video.getDuration()),
                String.valueOf(video.getFrameRate()),
                String.valueOf(getTotalPoints(tagData)),
                String.valueOf(getComplexity(tagData,video))
        });

        writeToCSV(overviewData,exportDirectory+overviewFileName);

        String detailsFileName = File.separator + "Tag Details.csv";
        List<String[]> tagDetailsData = new ArrayList<>();
        tagDetailsData.add(new String[]{"TAG","VALUE","AMOUNT","TOTAL POINTS"});
        for(String s : tagData.keySet())
            tagDetailsData.add(new String[]{
                    s,
                    String.valueOf(FrameHopperView.findTagByName(s).getValue()),
                    String.valueOf(tagData.get(s)),
                    String.valueOf(FrameHopperView.findTagByName(s).getValue()*tagData.get(s))
            });

        writeToCSV(tagDetailsData,exportDirectory+detailsFileName);

    }

    private void writeToCSV(List<String[]> data,String path){
        try(FileWriter writer = new FileWriter(path)){
            String csvText="";
            for(String[] record : data) {
                for (String s : record)
                    csvText += s + ";";
                csvText = csvText.replaceFirst(".$","");
                csvText += "\n";
            }
            writer.write(csvText);
            writer.flush();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private int getTotalPoints(Map<String,Long> tagData){
        int totalPoints = 0;
        for (String s : tagData.keySet())
            totalPoints += FrameHopperView.findTagByName(s).getValue()*tagData.get(s);
        return totalPoints;
    }

    private double getComplexity(Map<String,Long> tagData,Video video){
        DecimalFormat df = new DecimalFormat("#.###");
        String complexity = df.format((double)getTotalPoints(tagData)/video.getDuration()).replace(',','.');
        return Double.parseDouble(complexity);
    }
}
