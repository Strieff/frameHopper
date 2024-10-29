package com.example.engineer.View.ViewModel.Export;

import com.example.engineer.Model.Tag;
import com.example.engineer.Model.Video;
import com.example.engineer.Service.TagService;
import com.example.engineer.Service.VideoService;
import com.example.engineer.View.Elements.*;
import com.example.engineer.View.Elements.Language.Dictionary;
import com.example.engineer.View.Elements.Language.LanguageChangeListener;
import com.example.engineer.View.Elements.Language.LanguageManager;
import com.example.engineer.View.Elements.tableRenderer.MultilineTableCellRenderer;
import jakarta.annotation.PostConstruct;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@DependsOn("LanguageManager")
public class ExportView extends JFrame implements LanguageChangeListener {
    private static final int IFW = JComponent.WHEN_IN_FOCUSED_WINDOW;

    //needed services and components
    @Autowired
    private VideoService videoService;
    @Autowired
    private TagService tagService;
    @Autowired
    private TagListManager tagList;
    @Autowired
    private UserSettingsManager userSettings;
    @Autowired
    private LanguageManager languageManager;

    //needed data
    boolean isShiftPressed = false;
    int firstCLickedRow = 0;

    //JComponents
    private final JTable videoNameTable;
    private final List<JButton> buttonList = new ArrayList<>();

    public ExportView(){
        //KEYBINDINGS

        //when shift pressed
        getRootPane().getInputMap(IFW).put(KeyStroke.getKeyStroke(KeyEvent.VK_SHIFT,InputEvent.SHIFT_DOWN_MASK,false),"shiftPressed");
        getRootPane().getActionMap().put("shiftPressed", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                isShiftPressed = true;
            }
        });

        //when shift released
        getRootPane().getInputMap(IFW).put(KeyStroke.getKeyStroke(KeyEvent.VK_SHIFT,0,true),"shiftReleased");
        getRootPane().getActionMap().put("shiftReleased", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                isShiftPressed = false;
            }
        });

        //close window
        getRootPane().getInputMap(IFW).put(KeyStroke.getKeyStroke(KeyEvent.VK_E,KeyEvent.SHIFT_DOWN_MASK,false),"OpenExport");
        getRootPane().getActionMap().put("OpenExport", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                close();
            }
        });

        //set needed information
        setSize(300, 400);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setResizable(false);

        //COMPONENTS

        //create table for holding videos
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

        //listener for multi select
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

        //set checkbox cell size - 1st row
        videoNameTable.getColumnModel().getColumn(0).setPreferredWidth(24);
        videoNameTable.getColumnModel().getColumn(0).setMaxWidth(24);

        //add wrap to name cells - 2nd row
        videoNameTable.getColumnModel().getColumn(1).setCellRenderer(new MultilineTableCellRenderer());

        //set up table properties
        videoNameTable.setTableHeader(null);
        videoNameTable.setFocusable(false);
        videoNameTable.setRowSelectionAllowed(false);

        //scroll pane to hold the table
        JScrollPane scrollPane = new JScrollPane(videoNameTable);
        add(scrollPane);

        //export button
        JButton exportButton = new JButton(Dictionary.get("export.button.export"));
        exportButton.putClientProperty("text","export.button.export");
        exportButton.addActionListener(e -> {
            List<Integer> selected = getExportList();

            if(selected.isEmpty()){
                JOptionPane.showMessageDialog(
                        this,
                        Dictionary.get("export.error.noVideo"),
                        Dictionary.get("export.error.noVideo.title"),
                        JOptionPane.WARNING_MESSAGE
                );
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

                JOptionPane.showMessageDialog(
                        this,
                        Dictionary.get("export.status.success"),
                        Dictionary.get("export.status.title"),
                        JOptionPane.INFORMATION_MESSAGE
                );
            }catch (Exception ex){
                JOptionPane.showMessageDialog(
                        this,
                        Dictionary.get("export.status.cancelled"),
                        Dictionary.get("export.status.title"),
                        JOptionPane.INFORMATION_MESSAGE
                );
            }
        });

        //cancel button
        JButton cancelButton = new JButton(Dictionary.get("export.button.cancel"));
        cancelButton.putClientProperty("text","export.button.cancel");
        cancelButton.addActionListener(e -> close());

        //panel to hold export and cancel buttons
        JPanel buttonPanel = new JPanel(new GridLayout(1,2));
        buttonPanel.add(cancelButton);
        buttonPanel.add(exportButton);

        //clear button
        JButton clearButton = new JButton(Dictionary.get("export.button.clear"));
        clearButton.putClientProperty("text","export.button.clear");
        clearButton.addActionListener(e -> clearCheckboxes());

        //pane to hold clear button
        JPanel clearButtonPanel = new JPanel();
        clearButtonPanel.add(clearButton);

        //panel to hold export panel and clear panel
        JPanel lowerPanel = new JPanel(new GridLayout(2,1));
        lowerPanel.add(clearButtonPanel);
        lowerPanel.add(buttonPanel);
        add(lowerPanel,BorderLayout.SOUTH);

        buttonList.add(exportButton);
        buttonList.add(cancelButton);
        buttonList.add(clearButton);

        //operation on close
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                close();
            }
        });
    }

    @PostConstruct
    public void init(){
        languageManager.addListener(this);
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

    //export to recent directory
    private boolean openRecent(){
        if(userSettings.getExportPath() == null)
            return false;

        Object[] yesNoOptions = {
                Dictionary.get("settings.action.confirmation.yes"),
                Dictionary.get("settings.action.confirmation.no"),
        };

        return JOptionPane.showOptionDialog(
                null,
                 String.format(Dictionary.get("export.saveTo"),userSettings.getExportPath()),
                "EXPORT",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                yesNoOptions,
                yesNoOptions[1]
        ) == 0;
    }

    //get format - either excel or CSV
    private boolean getFormatChoice(){
        return JOptionPane.showOptionDialog(
                this,
                Dictionary.get("export.format.message"),
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
            Sheet sheet = workbook.createSheet(Dictionary.get("data.overview.title"));

            // Create header row
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue(Dictionary.get("data.overview.name"));
            headerRow.createCell(1).setCellValue(Dictionary.get("data.overview.frameCount"));
            headerRow.createCell(2).setCellValue(Dictionary.get("data.overview.codes"));
            headerRow.createCell(3).setCellValue(Dictionary.get("data.overview.runtime"));
            headerRow.createCell(4).setCellValue(Dictionary.get("data.overview.framerate"));
            headerRow.createCell(5).setCellValue(Dictionary.get("data.overview.totalPoints"));
            headerRow.createCell(6).setCellValue(Dictionary.get("data.overview.complexity"));

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
            summaryRow.createCell(0).setCellValue(Dictionary.get("data.overview.summary.totalShotAmount"));
            summaryRow.createCell(1).setCellValue(Dictionary.get("data.overview.summary.totalFrameCount"));
            summaryRow.createCell(3).setCellValue(Dictionary.get("data.overview.summary.totalRuntime"));
            summaryRow.createCell(5).setCellValue(Dictionary.get("data.overview.summary.totalPoints"));
            summaryRow.createCell(6).setCellValue(Dictionary.get("data.overview.summary.overallComplexity"));
            summaryRow.createCell(7).setCellValue(Dictionary.get("data.overview.asl"));

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
            Sheet tagSheet = workbook.createSheet(Dictionary.get("data.tags.title"));

            Row tagHeaderRow = tagSheet.createRow(0);
            tagHeaderRow.createCell(0).setCellValue(Dictionary.get("data.tags.name"));
            tagHeaderRow.createCell(1).setCellValue(Dictionary.get("data.tags.value"));
            tagHeaderRow.createCell(2).setCellValue(Dictionary.get("data.tags.amount"));
            tagHeaderRow.createCell(3).setCellValue(Dictionary.get("data.tags.totalPoints"));

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
        String overviewFileName = File.separator + Dictionary.get("data.overview.title")+".csv";

        List<String[]> overviewData = new ArrayList<>();
        overviewData.add(new String[]{
                Dictionary.get("data.overview.name"),
                Dictionary.get("data.overview.frameCount"),
                Dictionary.get("data.overview.codes"),
                Dictionary.get("data.overview.runtime"),
                Dictionary.get("data.overview.framerate"),
                Dictionary.get("data.overview.totalPoints"),
                Dictionary.get("data.overview.complexity")
        });

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
        String detailsFileName = File.separator + Dictionary.get("data.tags.title") + ".csv";

        Map<String,Long> tagData = getTotalAmountOfTags(videoTagMap);

        List<String[]> tagDetailsData = new ArrayList<>();
        tagDetailsData.add(new String[]{
                Dictionary.get("data.tags.name"),
                Dictionary.get("data.tags.value"),
                Dictionary.get("data.tags.amount"),
                Dictionary.get("data.tags.totalPoints")
        });

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
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path), StandardCharsets.UTF_8))) {
            writer.write('\uFEFF');

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

    //gets name of the file to save to
    private String getFileName(String path,boolean format) throws Exception{
        String[] options = {
                Dictionary.get("export.file.cancel"),
                Dictionary.get("export.file.rename"),
                Dictionary.get("export.file.overwrite"),
        };
        JTextField jTextField = new JTextField();

        JPanel panel = new JPanel(new BorderLayout(0,1));
        panel.add(jTextField);

        String fileName = "";
        while(fileName.isEmpty()){
            int nameChoice = JOptionPane.showConfirmDialog(this,panel,Dictionary.get("export.file.name"),JOptionPane.OK_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE);

            if(nameChoice == JOptionPane.OK_OPTION){
                fileName = jTextField.getText();

                if(new File(path + File.separator + fileName + (format ? ".xlsx" : "")).exists()){
                    int actionChoice = JOptionPane.showOptionDialog(
                            this,
                            String.format(Dictionary.get("export.file.overwrite.message"),fileName),
                            "",
                            JOptionPane.DEFAULT_OPTION,
                            JOptionPane.PLAIN_MESSAGE,
                            null,
                            options,
                            options[0]
                    );

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

    @Override
    public void changeLanguage() {
        for(var button : buttonList)
            button.setText(Dictionary.get((String)button.getClientProperty("text")));
    }
}
