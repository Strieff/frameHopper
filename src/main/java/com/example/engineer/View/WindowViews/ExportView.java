package com.example.engineer.View.WindowViews;

import com.example.engineer.Model.Tag;
import com.example.engineer.Model.Video;
import com.example.engineer.Service.TagService;
import com.example.engineer.Service.VideoService;
import com.example.engineer.View.Elements.MultilineTableCellRenderer;
import com.example.engineer.View.Elements.TagListManager;
import com.example.engineer.View.Elements.UserSettingsManager;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ExportView extends JFrame {
    @Autowired
    private VideoService videoService;
    @Autowired
    private TagService tagService;
    @Autowired
    private TagListManager tagList;
    @Autowired
    private UserSettingsManager userSettings;

    boolean isShiftPressed = false;
    int firstCLickedRow = 0;

    private JTable videoNameTable;

    public void setUpView(){
        setUpKeyBinds();

        setSize(300, 400);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        //set up key bind

        DefaultTableModel model = new DefaultTableModel(null, new String[]{"","",""});
        videoNameTable = new JTable(model){
            @Serial
            private static final long serialVersionUID = 1L;

            @Override
            public Class<?> getColumnClass(int column) {
                return switch (column) {
                    case 0 -> Boolean.class;
                    case 1 -> String.class;
                    default -> Integer.class;
                };
            }
        };

        videoNameTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = videoNameTable.rowAtPoint(e.getPoint());
                int col = videoNameTable.columnAtPoint(e.getPoint());

                if(col == 0){
                    if(!isShiftPressed)
                        firstCLickedRow = row;

                    if(isShiftPressed){
                        int start = Math.min(firstCLickedRow, row);
                        int end = Math.max(firstCLickedRow, row);
                        for(int i = start; i <= end; i++){
                            videoNameTable.setValueAt(true,i,0);
                        }
                    }
                }
            }
        });

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

            if(selected.isEmpty()){
                JOptionPane.showMessageDialog(this, "No videos were selected", "No Videos Selected", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String path = getSaveLocation();
            if(path == null)
                return;
            else {
                userSettings.setExportRecent(path);
                userSettings.save();
            }

            try{
                exportData(selected, path);

                JOptionPane.showMessageDialog(this, "Export complete!", "Export Status", JOptionPane.INFORMATION_MESSAGE);
            }catch (Exception ex){
                JOptionPane.showMessageDialog(this, "Export canceled!", "Export Status", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        //cancel button
        JButton cancelButton = new JButton("CANCEL");
        cancelButton.addActionListener(e -> close());

        JPanel buttonPanel = new JPanel(new GridLayout(1,2));
        buttonPanel.add(cancelButton);
        buttonPanel.add(exportButton);

        JButton clearButton = new JButton("CLEAR");
        clearButton.addActionListener(e -> clearCheckboxes());

        JPanel clearButtonPanel = new JPanel();
        clearButtonPanel.add(clearButton);

        JPanel lowerPanel = new JPanel(new GridLayout(2,1));
        lowerPanel.add(clearButtonPanel);
        lowerPanel.add(buttonPanel);

        add(lowerPanel,BorderLayout.SOUTH);

        setResizable(false);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                close();
            }
        });
    }

    //opens window
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

    //closes window
    public void close() {
        setVisible(false);
        ((DefaultTableModel) videoNameTable.getModel()).setRowCount(0);
    }

    //gets path for saving file
    private String getSaveLocation(){
        if(openRecent())
            return userSettings.getExportPath();

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int userSelection = fileChooser.showSaveDialog(this);

        return userSelection == JFileChooser.APPROVE_OPTION ? fileChooser.getSelectedFile().getAbsolutePath() : null;
    }

    private boolean openRecent(){
        if(userSettings.getExportPath() == null)
            return false;

        return JOptionPane.showConfirmDialog(
                null,
                "Save to: " + userSettings.getExportPath(),
                "EXPORT",
                JOptionPane.YES_NO_OPTION
        ) == JOptionPane.YES_OPTION;
    }

    //get format - either excel or CSV
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

    //get all checked items from the table
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

    //compile raw data and put to export
    private void exportData(List<Integer> indexList, String path) throws Exception{
        List<Object[]> data = tagService.countTagsOnFramesOfVideo(indexList);

        Map<Video,Map<String,Long>> tagAmountOnVideos = new HashMap<>();
        for(Object[] o : data){
            if(!tagAmountOnVideos.containsKey((Video)o[0]))
                tagAmountOnVideos.put((Video)o[0],new HashMap<>());

            tagAmountOnVideos.get((Video)o[0]).put((String)o[1],(Long)o[2]);
        }

        boolean formatChoice = getFormatChoice();
        String fileName = getFileName(path,formatChoice);
        if(fileName == null)
            throw new Exception();

        if(formatChoice)
            exportToExcel(tagAmountOnVideos,path,fileName);
        else
            exportToCsv(tagAmountOnVideos,path,fileName);
    }

    //compiles and exports to excel
    private void exportToExcel(Map<Video,Map<String,Long>> videoTagMap, String path,String name){
        Map<Video,Long> uniqueTagsOnVideos = tagService.getAmountOfUniqueTagsOnVideos(new ArrayList<>(videoTagMap.keySet()));

        String fileName = name+".xlsx";

        try(
                Workbook workbook = WorkbookFactory.create(true);
                FileOutputStream outputStream = new FileOutputStream(path + File.separator + fileName)
        ){
            //create general data
            Sheet sheet = workbook.createSheet("Overview");

            // Create header row
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("NAME");
            headerRow.createCell(1).setCellValue("FRAME COUNT");
            headerRow.createCell(2).setCellValue("CODES");
            headerRow.createCell(3).setCellValue("RUNTIME (SECONDS)");
            headerRow.createCell(4).setCellValue("FRAMERATE");
            headerRow.createCell(5).setCellValue("TOTAL POINTS");
            headerRow.createCell(6).setCellValue("COMPLEXITY");

            //create data row
            for (int row = 0; row < videoTagMap.size(); row++) {
                Video video = new ArrayList<>(videoTagMap.keySet()).get(row);

                Row dataRow = sheet.createRow(row+1);

                dataRow.createCell(0).setCellValue(video.getName());
                dataRow.createCell(1).setCellValue(video.getTotalFrames());
                dataRow.createCell(2).setCellValue(uniqueTagsOnVideos.get(uniqueTagsOnVideos.keySet().stream().filter(v -> v.getId() == video.getId()).findFirst().get()));
                dataRow.createCell(3).setCellValue(video.getDuration());
                dataRow.createCell(4).setCellValue(video.getFrameRate());
                dataRow.createCell(5).setCellValue(getTotalPoints(videoTagMap.get(video)));
                dataRow.createCell(6).setCellValue(getComplexity(getTotalPoints(videoTagMap.get(video)),video));
            }

            Row summaryRow = sheet.createRow(videoTagMap.size()+2);
            summaryRow.createCell(0).setCellValue("TOTAL SHOT AMOUNT");
            summaryRow.createCell(1).setCellValue("TOTAL FRAME COUNT");
            summaryRow.createCell(3).setCellValue("TOTAL RUNTIME");
            summaryRow.createCell(5).setCellValue("TOTAL POINTS");
            summaryRow.createCell(6).setCellValue("OVERALL COMPLEXITY");
            summaryRow.createCell(7).setCellValue("ASL");

            CellStyle decimalStyle = workbook.createCellStyle();
            DataFormat df = workbook.createDataFormat();
            decimalStyle.setDataFormat(df.getFormat("0.000"));

            Row summaryDataRow = sheet.createRow(videoTagMap.size()+3);
            summaryDataRow.createCell(0).setCellValue(videoTagMap.size());
            summaryDataRow.createCell(1).setCellFormula("SUM(B2:B"+ (videoTagMap.size()+1) +")");
            summaryDataRow.createCell(3).setCellFormula("SUM(D2:D"+ (videoTagMap.size()+1) +")");
            summaryDataRow.createCell(5).setCellFormula("SUM(F2:F"+ (videoTagMap.size()+1) +")");
            summaryDataRow.createCell(6).setCellFormula("F" + (videoTagMap.size()+4) + "/D" + (videoTagMap.size()+4));
            sheet.getRow(videoTagMap.size()+3).getCell(6).setCellStyle(decimalStyle);
            summaryDataRow.createCell(7).setCellFormula("D" + (videoTagMap.size()+4) + "/A" + (videoTagMap.size()+4));

            //resize columns
            for (int j = 0; j < 7; j++)
                sheet.autoSizeColumn(j);

            //create tag data info
            Sheet tagSheet = workbook.createSheet("Code data");

            Row tagHeaderRow = tagSheet.createRow(0);
            tagHeaderRow.createCell(0).setCellValue("CODE");
            tagHeaderRow.createCell(1).setCellValue("VALUE");
            tagHeaderRow.createCell(2).setCellValue("AMOUNT");
            tagHeaderRow.createCell(3).setCellValue("TOTAL POINTS");

            //get all amount of tags on all videos
            Map<String,Long> tagData = getTotalAmountOfTags(videoTagMap);

            int i = 1;
            for(String s : tagData.keySet()){
                Tag tag = tagList.getTag(s);

                Row infoRow = tagSheet.createRow(i++);
                infoRow.createCell(0).setCellValue(s);
                infoRow.createCell(1).setCellValue(tag.getValue());
                infoRow.createCell(2).setCellValue(tagData.get(s));
                infoRow.createCell(3).setCellValue(tag.getValue()*tagData.get(s));
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

    //compiles data
    private void exportToCsv(Map<Video,Map<String,Long>> videoTagMap, String path,String name){
        Map<Video,Long> uniqueTagsOnVideos = tagService.getAmountOfUniqueTagsOnVideos(new ArrayList<>(videoTagMap.keySet()));

        String exportDirectory = path + File.separator + name;
        new File(exportDirectory).mkdirs();

        //save overview
        String overviewFileName = File.separator + "Overview.csv";

        List<String[]> overviewData = new ArrayList<>();
        overviewData.add(new String[]{"NAME","FRAME COUNT","CODES","RUNTIME (SECONDS)","FRAMERATE","TOTAL POINTS","COMPLEXITY"});

        for (int row = 0; row < videoTagMap.size();row++) {
            Video video = new ArrayList<>(videoTagMap.keySet()).get(row);

            overviewData.add(new String[]{
                    video.getName(),
                    String.valueOf(video.getTotalFrames()),
                    String.valueOf(uniqueTagsOnVideos.get(uniqueTagsOnVideos.keySet().stream().filter(v -> v.getId() == video.getId()).findFirst().get())),
                    String.valueOf(video.getDuration()),
                    String.valueOf(video.getFrameRate()),
                    String.valueOf(getTotalPoints(videoTagMap.get(video))),
                    String.valueOf(getComplexity(getTotalPoints(videoTagMap.get(video)),video))
            });
        }
        writeToCSV(overviewData,exportDirectory+overviewFileName);

        //save tag data
        String detailsFileName = File.separator + "Code Details.csv";

        Map<String,Long> tagData = getTotalAmountOfTags(videoTagMap);

        List<String[]> tagDetailsData = new ArrayList<>();
        tagDetailsData.add(new String[]{"CODE","VALUE","AMOUNT","TOTAL POINTS"});

        for(String s : tagData.keySet()) {
            Tag tag = tagList.getTag(s);

            tagDetailsData.add(new String[]{
                    s,
                    String.valueOf(tag.getValue()),
                    String.valueOf(tagData.get(s)),
                    String.valueOf(tag.getValue() * tagData.get(s))
            });
        }

        writeToCSV(tagDetailsData,exportDirectory+detailsFileName);
    }

    //writes data to CSV
    private void writeToCSV(List<String[]> data,String path){
        try(FileWriter writer = new FileWriter(path)){
            String csvText = data.stream()
                    .map(record -> String.join(";", record))
                    .collect(Collectors.joining("\n"));

            writer.write(csvText);
            writer.flush();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //gets total amount of points of given tag
    private int getTotalPoints(Map<String,Long> tagData){
        return tagData.entrySet()
                .stream()
                .mapToInt(entry -> (int)(tagList.getTag(entry.getKey()).getValue() * entry.getValue()))
                .sum();
    }

    //gets complexity of given video
    private double getComplexity(int totalPoints,Video video){
        DecimalFormat df = new DecimalFormat("#.###");
        String complexity = df.format((double)totalPoints/video.getDuration()).replace(',','.');
        return Double.parseDouble(complexity);
    }

    //gets total amount of tags
    private Map<String,Long> getTotalAmountOfTags(Map<Video,Map<String,Long>> videoTagMap){
        Map<String,Long> tagData = new HashMap<>();

        videoTagMap.values()
                .stream()
                .flatMap(data -> data.entrySet().stream())
                .forEach(entry -> tagData.merge(entry.getKey(), entry.getValue(), Long::sum));

        return tagData;
    }

    //clears checkboxes
    private void clearCheckboxes(){
        DefaultTableModel model = (DefaultTableModel) videoNameTable.getModel();
        for (int i = 0; i < model.getRowCount(); i++)
            model.setValueAt(false,i,0);
    }

    //sets up keybindings
    private void setUpKeyBinds(){
        int IFW = JComponent.WHEN_IN_FOCUSED_WINDOW;
        getRootPane().getInputMap(IFW).put(KeyStroke.getKeyStroke(KeyEvent.VK_SHIFT,InputEvent.SHIFT_DOWN_MASK,false),"shiftPressed");
        getRootPane().getInputMap(IFW).put(KeyStroke.getKeyStroke(KeyEvent.VK_SHIFT,0,true),"shiftReleased");

        getRootPane().getActionMap().put("shiftPressed", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                isShiftPressed = true;
            }
        });

        getRootPane().getActionMap().put("shiftReleased", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                isShiftPressed = false;
            }
        });

        getRootPane().getInputMap(IFW).put(KeyStroke.getKeyStroke(KeyEvent.VK_E,KeyEvent.SHIFT_DOWN_MASK,false),"OpenExport");
        getRootPane().getActionMap().put("OpenExport", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                close();
            }
        });
    }

    //gets name of the file to save to
    private String getFileName(String path,boolean format) throws Exception{
        String[] options = {"Cancel","Rename","Overwrite"};
        JTextField jTextField = new JTextField();

        JPanel panel = new JPanel(new BorderLayout(0,1));
        panel.add(jTextField);

        String fileName = "";
        while(fileName.isEmpty()){
            int nameChoice = JOptionPane.showConfirmDialog(this,panel,"FILE NAME:",JOptionPane.OK_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE);

            if(nameChoice == JOptionPane.OK_OPTION){
                fileName = jTextField.getText();

                if(new File(path + File.separator + fileName + (format ? ".xlsx" : "")).exists()){
                    int actionChoice = JOptionPane.showOptionDialog(this, fileName+" already exists. Do you want to overwrite it?", "",
                            JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
                            null, options, options[0]);

                    switch(actionChoice){
                        case 0:
                            throw new Exception();
                        case 1:
                            fileName = "";
                            break;
                        case 2:
                            return fileName;
                    }
                }
            }else
                throw new Exception();
        }

        return fileName;
    }
}
