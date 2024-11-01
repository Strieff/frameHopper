package com.example.engineer.View.ViewModel.MainApplication;

import com.example.engineer.FrameProcessor.Cache;
import com.example.engineer.FrameProcessor.FrameProcessorRequestManager;
import com.example.engineer.Model.Frame;
import com.example.engineer.Model.Tag;
import com.example.engineer.Model.Video;
import com.example.engineer.Service.FrameService;
import com.example.engineer.Service.VideoService;
import com.example.engineer.View.Elements.DialogProvider;
import com.example.engineer.View.Elements.IconLoader;
import com.example.engineer.View.Elements.Language.Dictionary;
import com.example.engineer.View.Elements.Language.LanguageChangeListener;
import com.example.engineer.View.Elements.Language.LanguageManager;
import com.example.engineer.View.Elements.tableRenderer.MultilineTableCellRenderer;
import com.example.engineer.View.Elements.UserSettingsManager;
import com.example.engineer.View.Elements.actions.PasteRecentAction;
import com.example.engineer.View.Elements.actions.RemoveRecentAction;
import com.example.engineer.View.Elements.actions.UndoRedoAction;
import com.example.engineer.View.ViewModel.Export.ExportView;
import com.example.engineer.View.ViewModel.PathMerging.PathMergingDetailsView;
import com.example.engineer.View.ViewModel.PathMerging.PathMergingListView;
import com.example.engineer.View.ViewModel.Settings.SettingsView;
import com.example.engineer.View.ViewModel.TagDetails.TagDetailsView;
import com.example.engineer.View.ViewModel.TagManagerView.TagManagerView;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.Serial;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component(value = "FrameHopperView")
public class FrameHopperView extends JFrame implements ApplicationContextAware, LanguageChangeListener {
    private static final int IFW = JComponent.WHEN_IN_FOCUSED_WINDOW;

    //needed services
    @Autowired
    private VideoService videoService;
    @Autowired
    private FrameService frameService;

    //actions
    @Autowired
    private PasteRecentAction pasteRecentAction;
    @Autowired
    private RemoveRecentAction removeRecentAction;
    @Autowired
    private UndoRedoAction undoRedoAction;

    //data managers
    @Autowired
    private UserSettingsManager userSettings;
    @Autowired
    private FrameProcessorRequestManager requestManager;
    @Autowired
    LanguageManager languageManager;

    //other views
    @Autowired
    TagManagerView tagManagerView;
    @Autowired
    SettingsView settingsView;
    @Autowired
    ExportView exportView;

    //cache
    private Cache cache;

    //JComponents
    private final JLabel imageLabel;
    private final JLabel infoLabel;
    private final JTextField jumpTextField;
    private final JTable tagsTableList;
    private final JButton jumpButton;

    private final List<JButton> iconButtons = new ArrayList<>();

    //needed data
    private int currentFrameIndex;
    public Boolean loaded = false;
    private File videoFile;
    @Getter
    private Video video;
    private Map<Integer, List<Tag>> tagsOnFramesOnVideo;

    //get context
    private static ApplicationContext ctx;
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        ctx = applicationContext;
    }

    //constructor
    public FrameHopperView(){
        setTitle("FrameHopper");
        setSize(1200, 900);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //CREATE COMPONENTS

        //image display
        imageLabel = new JLabel(Dictionary.get("main.dropHere"), JLabel.CENTER);
        imageLabel.putClientProperty("text","main.dropHere");
        imageLabel.setFont(new Font("Comic Sans",Font.BOLD,36));
        add(imageLabel, BorderLayout.CENTER);

        //label for displaying some metadata
        infoLabel = new JLabel(String.format(Dictionary.get("main.fileInfo"),0,0,0.0));
        infoLabel.putClientProperty("text","main.fileInfo");
        infoLabel.setFont(new Font("Comic Sans",Font.BOLD,24));
        add(infoLabel,BorderLayout.SOUTH);

        //textfield for jump panel
        jumpTextField = new JTextField(5);

        //button for jump panel
        jumpButton = new JButton(Dictionary.get("main.jump"));
        jumpButton.putClientProperty("text","main.jump");
        jumpButton.addActionListener(e -> {
            jumpToSpecifiedFrame();
        });

        //JPanel for jump functionality
        JPanel jumpPanel = new JPanel();
        add(jumpPanel,BorderLayout.NORTH);
        jumpPanel.add(jumpTextField);
        jumpPanel.add(jumpButton);

        //tag list for a frame
        tagsTableList = new JTable(){
            @Serial
            private static final long serialVersionUID = 1L;

            @Override
            public Class<?> getColumnClass(int column) {
                return switch (column) {
                    case 0 -> String.class;
                    default -> Double.class;
                };
            }

            @Override
            public boolean isCellEditable(int row,int column){
                return false;
            }
        };
        tagsTableList.setModel(new DefaultTableModel(new String[][]{},new String[]{
                Dictionary.get("main.tag.name"),
                Dictionary.get("main.tag.value")
        }));
        tagsTableList.getTableHeader().setReorderingAllowed(false);

        //center the value column
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        tagsTableList.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);

        //wrap text in name column
        TableColumn nameColumn = tagsTableList.getColumnModel().getColumn(0);
        nameColumn.setCellRenderer(new MultilineTableCellRenderer());

        //set actions on table
        tagsTableList.setFocusable(false);
        tagsTableList.setRowSelectionAllowed(false);

        //scroll pane for the table
        JScrollPane tagScrollPane = new JScrollPane(tagsTableList);
        tagScrollPane.setPreferredSize(new Dimension(200,getHeight()));

        //tag manager button
        JButton tagMangerButton = new JButton();
        tagMangerButton.setToolTipText(Dictionary.get("tooltip.manager"));
        tagMangerButton.putClientProperty("tooltip","tooltip.manager");
        iconButtons.add(tagMangerButton);
        setButtonIcon(tagMangerButton,"plus.png");
        tagMangerButton.addActionListener(e -> {
            if(videoFile!=null)
                tagManagerView.setUpData(videoFile.getName(),currentFrameIndex);
            else
                JOptionPane.showMessageDialog(
                        this,
                        Dictionary.get("main.noFile"),
                        Dictionary.get("main.NoFile.title"),
                        JOptionPane.ERROR_MESSAGE
                );
        });

        //settings button
        JButton settingsButton = new JButton();
        settingsButton.setToolTipText(Dictionary.get("tooltip.settings"));
        settingsButton.putClientProperty("tooltip","tooltip.settings");
        iconButtons.add(settingsButton);
        setButtonIcon(settingsButton,"settings.png");
        settingsButton.addActionListener(e -> settingsView.open());

        //export button
        JButton exportButton = new JButton();
        exportButton.setToolTipText(Dictionary.get("tooltip.export"));
        exportButton.putClientProperty("tooltip","tooltip.export");
        iconButtons.add(exportButton);
        setButtonIcon(exportButton,"export.png");
        exportButton.addActionListener(e -> exportView.open());

        //panel for view buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(tagMangerButton);
        buttonPanel.add(settingsButton);
        buttonPanel.add(exportButton);

        //panel for table-button section
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(buttonPanel,BorderLayout.NORTH);
        rightPanel.add(tagScrollPane, BorderLayout.CENTER);

        //split pane for the layout
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,true,new JPanel(),rightPanel);
        splitPane.setResizeWeight(1f);
        add(splitPane,BorderLayout.EAST);


        //KEYBINDINGS

        //move right key bind
        getRootPane().getInputMap(IFW).put(KeyStroke.getKeyStroke('.'), "MoveRight");
        getRootPane().getActionMap().put("MoveRight", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(currentFrameIndex != cache.getMaxFrameIndex() - 1){
                    cache.move(currentFrameIndex,++currentFrameIndex);
                    displayCurrentFrame();
                }
            }
        });

        //move left key bind
        getRootPane().getInputMap(IFW).put(KeyStroke.getKeyStroke(','), "MoveLeft");
        getRootPane().getActionMap().put("MoveLeft", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(currentFrameIndex >= 1){
                    cache.move(currentFrameIndex,--currentFrameIndex);
                    displayCurrentFrame();
                }
            }
        });

        //open settings key bind
        getRootPane().getInputMap(IFW).put(KeyStroke.getKeyStroke(KeyEvent.VK_S,KeyEvent.SHIFT_DOWN_MASK,false),"OpenSettings");
        getRootPane().getActionMap().put("OpenSettings", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(!settingsView.isVisible())
                    settingsView.open();
                else
                    settingsView.close();
            }
        });

        //open export key bind
        getRootPane().getInputMap(IFW).put(KeyStroke.getKeyStroke(KeyEvent.VK_E,KeyEvent.SHIFT_DOWN_MASK,false),"OpenExport");
        getRootPane().getActionMap().put("OpenExport", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(!exportView.isVisible())
                    exportView.open();
                else
                    exportView.close();
            }
        });

        //open tag manager key bind
        getRootPane().getInputMap(IFW).put(KeyStroke.getKeyStroke(KeyEvent.VK_M,KeyEvent.SHIFT_DOWN_MASK,false),"OpenManager");
        getRootPane().getActionMap().put("OpenManager", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(video != null)
                    if(!tagManagerView.isVisible())
                        tagManagerView.setUpData(videoFile.getName(),currentFrameIndex);
                    else
                        tagManagerView.close();
                else
                    JOptionPane.showMessageDialog(getRootPane(), Dictionary.get("main.noFile"), Dictionary.get("main.NoFile.title"), JOptionPane.ERROR_MESSAGE);
            }
        });

        //open video  list key bind
        getRootPane().getInputMap(IFW).put(KeyStroke.getKeyStroke(KeyEvent.VK_L,KeyEvent.SHIFT_DOWN_MASK,false),"OpenVideoList");
        getRootPane().getActionMap().put("OpenVideoList", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(!ctx.getBean(PathMergingListView.class).isVisible())
                    ctx.getBean(PathMergingListView.class).open();
                else
                    ctx.getBean(PathMergingListView.class).close();
            }
        });

        //open video details key bind
        getRootPane().getInputMap(IFW).put(KeyStroke.getKeyStroke(KeyEvent.VK_D,KeyEvent.SHIFT_DOWN_MASK,false),"OpenVideoDetails");
        getRootPane().getActionMap().put("OpenVideoDetails", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(video != null)
                    if(!ctx.getBean(PathMergingDetailsView.class).isVisible())
                        ctx.getBean(PathMergingDetailsView.class).open(video.getId());
                    else
                        ctx.getBean(PathMergingDetailsView.class).close();
                else
                    DialogProvider.errorDialog(Dictionary.get("main.noFile"),Dictionary.get("main.NoFile.title"));
            }
        });

        //add last tags key bind
        getRootPane().getInputMap(IFW).put(KeyStroke.getKeyStroke(KeyEvent.VK_V,KeyEvent.CTRL_DOWN_MASK,false),"addLastTag");
        getRootPane().getActionMap().put("addLastTag", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pasteRecentAction.performAction(
                        tagsOnFramesOnVideo.computeIfAbsent(currentFrameIndex, k -> new ArrayList<>()),
                        currentFrameIndex,
                        video
                );
            }
        });

        //remove last tags key bind
        getRootPane().getInputMap(IFW).put(KeyStroke.getKeyStroke(KeyEvent.VK_X,KeyEvent.CTRL_DOWN_MASK,false),"removeLastTag");
        getRootPane().getActionMap().put("removeLastTag", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                removeRecentAction.performAction(
                        tagsOnFramesOnVideo.computeIfAbsent(currentFrameIndex, k -> new ArrayList<>()),
                        currentFrameIndex,
                        video
                );
            }
        });

        //undo key bind
        getRootPane().getInputMap(IFW).put(KeyStroke.getKeyStroke(KeyEvent.VK_Z,KeyEvent.CTRL_DOWN_MASK,false),"undo");
        getRootPane().getActionMap().put("undo", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                undoRedoAction.undoAction();
            }
        });

        //redo action
        getRootPane().getInputMap(IFW).put(KeyStroke.getKeyStroke(KeyEvent.VK_Y,KeyEvent.CTRL_DOWN_MASK,false),"redo");
        getRootPane().getActionMap().put("redo", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                undoRedoAction.redoAction();
            }
        });


        //drag and drop functionality
        new DropTarget(this, new DropTargetAdapter() {
            @Override
            public void drop(DropTargetDropEvent evt) {
                try{
                    evt.acceptDrop(DnDConstants.ACTION_COPY);
                    Transferable t = evt.getTransferable();
                    if(t.isDataFlavorSupported(DataFlavor.javaFileListFlavor)){
                        List<File> fileList = (List<File>) t.getTransferData(DataFlavor.javaFileListFlavor);
                        if(!fileList.isEmpty()){
                            imageLabel.setText("");
                            videoFile = fileList.get(0);
                            video = videoService.createVideoIfNotExists(videoFile);

                            //clear and set cache
                            if(cache != null)
                                cache.clearCache();
                            cache = Cache.getCache(videoFile.getAbsolutePath());

                            //prepare video data
                            prepareVideo();

                            //open video
                            openVideo();
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

        //open window
        setVisible(true);
        getRootPane().requestFocus();

        //actions to take when closing application
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                //send close request to server
                requestManager.closeServer();

                //delete external cache
                try{
                    Files.walk(Paths.get("cache"))
                            .filter(p -> !p.equals(Paths.get("cache")))
                            .sorted((p1,p2) -> -p1.compareTo(p2))
                            .forEach(p -> {
                                try {
                                    Files.delete(p);
                                } catch (IOException ex) {
                                    throw new RuntimeException(ex);
                                }
                            });
                }catch (Exception ex){
                    ex.printStackTrace();
                }

                //dispose of views
                settingsView.dispose();
                exportView.dispose();
                tagManagerView.dispose();
                ctx.getBean(TagDetailsView.class).dispose();
            }
        });

        //TODO: exportButton.setToolTipText("Export");
    }

    @PostConstruct
    public void init(){
        languageManager.addListener(this);
    }

    //add icon to a button
    private void setButtonIcon(JButton button,String name){
        button.setIcon(IconLoader.getLargeIcon(name));
    }

    //open recent video functionality
    public void openRecentVideo(File videoFile){
        //set cache
        cache = Cache.getCache(videoFile.getAbsolutePath());

        //prepare needed data
        this.videoFile = videoFile;
        this.video = videoService.getByPath(videoFile.getAbsolutePath());

        //prepare video data
        prepareVideo();

        //open video
        openVideo();
    }

    //prepare needed data to open video
    private void prepareVideo(){
        //load first cache
        cache.firstLoad(videoFile);
        //load video metadata
        cache.setUpVideoMetadata(videoService,video);
        //set current index to 0
        currentFrameIndex = 0;
    }

    //open video frame and load tags
    public void openVideo(){
        //set up metadata info label
        infoLabel.setText(String.format(Dictionary.get("main.fileInfo"),(currentFrameIndex+1),cache.getMaxFrameIndex(),cache.getFrameRate()));
        imageLabel.requestFocus();

        //create local representation of tag data on video
        tagsOnFramesOnVideo = new HashMap<>();
        tagsOnFramesOnVideo = new HashMap<>();
        List<Frame> frames = frameService.getAllByVideo(video);
        for(Frame f : frames)
            tagsOnFramesOnVideo.put(f.getFrameNumber(),f.getTags());

        //display current frame
        displayCurrentFrame();

        //set loaded to true
        loaded = true;

        //save recent path
        userSettings.setRecentPath(video.getPath());
    }

    //display tag list for given frame
    public void displayTagList(){
        //get and reset model
        DefaultTableModel model = (DefaultTableModel) tagsTableList.getModel();
        model.setRowCount(0);

        //populate table
        if(tagsOnFramesOnVideo.containsKey(currentFrameIndex))
            for(Tag t : tagsOnFramesOnVideo.get(currentFrameIndex))
                model.addRow(new Object[]{
                        t.getName(),
                        t.getValue()
                });

        //set model
        tagsTableList.setModel(model);
        tagsTableList.revalidate();
    }

    //display current frame
    public void displayCurrentFrame(){
        //display tag list
        displayTagList();

        //get dimension of the application window
        int appWidth = imageLabel.getWidth() - 10;
        int appHeight = imageLabel.getHeight() - 10;

        //calculate scale factor for proportions
        double scaleWidth = (double) appWidth / cache.getWidth();
        double scaleHeight = (double) appHeight / cache.getHeight();
        double scaleFactor = Math.min(scaleWidth,scaleHeight);

        //calculate target dimensions
        int targetWidth = (int) (scaleFactor * cache.getWidth());
        int targetHeight = (int) (scaleFactor * cache.getHeight());

        //get current frame and scale it
        BufferedImage frame = cache.getCurrentFrame(currentFrameIndex);
        frame = scaleImage(frame,targetWidth,targetHeight);

        //display frame and update displayed metadata
        imageLabel.setIcon(new ImageIcon(frame));
        infoLabel.setText(String.format(Dictionary.get("main.fileInfo"),(currentFrameIndex+1),cache.getMaxFrameIndex(),cache.getFrameRate()));
    }

    //scale image to fit the application window
    private BufferedImage scaleImage(BufferedImage originalImage,int targetWidth,int targetHeight){
        BufferedImage scaledImage = new BufferedImage(targetWidth,targetHeight,BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = scaledImage.createGraphics();
        g.drawImage(originalImage,0,0,targetWidth,targetHeight,null);
        g.dispose();
        return scaledImage;
    }

    //jump to specified frame
    private void jumpToSpecifiedFrame() {
        try{
            int frameToJump = Integer.parseInt(jumpTextField.getText());
            if(frameToJump < 0 || frameToJump > cache.getMaxFrameIndex()){
                throw new Exception(String.format(Dictionary.get("main.jump.error.outOfBounds"),frameToJump));
            }else{
                cache.jump(frameToJump-1);
                currentFrameIndex = frameToJump-1;
                displayCurrentFrame();
            }
        }catch (NumberFormatException e){
            JOptionPane.showMessageDialog(this, Dictionary.get("main.jump.error.invalid"), Dictionary.get("main.jump.error.invalid.title"), JOptionPane.ERROR_MESSAGE);
        }catch (Exception e){
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    //get all tags of given frame
    public List<Tag> getTagsOfFrame(int frameNo){
        return tagsOnFramesOnVideo.get(frameNo) != null ?
                tagsOnFramesOnVideo.get(frameNo) :
                new ArrayList<>();
    }

    //add tags to frame
    public void putTagsOnFrame(int frameNo, List<Tag> tags){
        tagsOnFramesOnVideo.put(frameNo,tags);
    }

    //remove tag from frame
    public void removeTagFromFrame(Tag tag,int frameNo){
        if(tagsOnFramesOnVideo.get(frameNo)==null)
            return;

        tagsOnFramesOnVideo.get(frameNo).removeIf(t -> t.equals(tag));
    }

    //remove tag from all frames
    public void removeTagFromAllFrames(Tag tag){
        if(!tagsOnFramesOnVideo.isEmpty())
            for(Integer frameNo : tagsOnFramesOnVideo.keySet())
                removeTagFromFrame(tag,frameNo);
    }

    //set tags on given frame in local data representation
    public void setCurrentTags(List<Tag> tags,int frameNo){
        tagsOnFramesOnVideo.put(frameNo,tags);
    }

    @Override
    public void changeLanguage() {
        jumpButton.setText(Dictionary.get((String)jumpButton.getClientProperty("text")));

        if(video == null)
            imageLabel.setText(Dictionary.get((String)imageLabel.getClientProperty("text")));

        for(var b : iconButtons)
            b.setToolTipText(Dictionary.get((String)b.getClientProperty("tooltip")));

        if(cache!=null)
            infoLabel.setText(String.format(Dictionary.get("main.fileInfo"),(currentFrameIndex+1),cache.getMaxFrameIndex(),cache.getFrameRate()));
        else
            infoLabel.setText(String.format(Dictionary.get("main.fileInfo"),0,0,0.0));

        var columnModel = tagsTableList.getColumnModel();
        columnModel.getColumn(0).setHeaderValue(Dictionary.get("main.tag.name"));
        columnModel.getColumn(1).setHeaderValue(Dictionary.get("main.tag.value"));

        repaint();
    }
}
