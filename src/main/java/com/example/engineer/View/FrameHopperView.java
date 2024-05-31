package com.example.engineer.View;

import com.example.engineer.FrameProcessor.FrameCache;
import com.example.engineer.FrameProcessor.FrameProcessorClient;
import com.example.engineer.Model.Frame;
import com.example.engineer.Model.Tag;
import com.example.engineer.Model.UserSettings;
import com.example.engineer.Model.Video;
import com.example.engineer.Service.FrameService;
import com.example.engineer.Service.TagService;
import com.example.engineer.Service.VideoService;
import com.example.engineer.View.Elements.MultilineTableCellRenderer;
import com.example.engineer.View.buttonsView.ExportView;
import com.example.engineer.View.buttonsView.SettingsView;
import com.example.engineer.View.buttonsView.TagManagerView;
import com.example.engineer.View.smallViews.StatusView;
import com.example.engineer.View.smallViews.TagDetailsView;
import org.bytedeco.javacv.FFmpegFrameGrabber;
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
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.Serial;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.stream.IntStream;

//TODO delete hidden tags that are not assigned to any frame
//TODO make a small window for comments under tag list, move left right to find comments
@Component
public class FrameHopperView extends JFrame implements ApplicationContextAware {
    public static List<Tag> TAG_LIST;
    public static UserSettings USER_SETTINGS;

    @Autowired
    private VideoService videoService;
    @Autowired
    private TagService tagService;
    @Autowired
    private FrameService frameService;

    private TagManagerView tagManagerView;
    private SettingsView settingsView;
    private ExportView exportView;

    private final JLabel imageLabel;
    private final JLabel infoLabel;
    private final JTextField jumpTextField;
    private final JTable tagsTableList;

    private File videoFile;
    private int currentFrameIndex;
    private int maxFrameIndex;
    private int videoHeight;
    private int videoWidth;
    private double videoFramerate;
    private double videoDuration;

    private Map<Integer,List<Tag>> tagsOnFramesOnVideo;

    private static final int IFW = JComponent.WHEN_IN_FOCUSED_WINDOW;
    private static final String MOVE_RIGHT = "move right";
    private static final String MOVE_LEFT = "move left";

    Video video;

    private static ApplicationContext ctx;
    public static ApplicationContext getApplicationContext() {
        return ctx;
    }
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        ctx = applicationContext;

        //get global list of tags
        TAG_LIST = Collections.synchronizedList(tagService.getAllTags());
    }

    public FrameHopperView(){
        setTitle("Video Frame Viewer");
        setSize(1200, 900);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Initialize components
        imageLabel = new JLabel();
        add(imageLabel, BorderLayout.CENTER);

        // New JLabel for displaying current frame and frame rate
        infoLabel = new JLabel("Current Frame: 0/0 | Frame Rate: 0 fps");
        infoLabel.setFont(new Font("Comic Sans", Font.BOLD, 24));
        add(infoLabel, BorderLayout.SOUTH);

        // New JPanel to hold the JTextField and JButton
        JPanel jumpPanel = new JPanel();
        jumpTextField = new JTextField(5);
        jumpPanel.add(jumpTextField);

        // New JButton for jumping to the specified frame
        JButton jumpButton = new JButton("Jump to Frame");
        jumpButton.addActionListener((e) -> jumpToSpecifiedFrame());
        jumpPanel.add(jumpButton);

        add(jumpPanel, BorderLayout.NORTH);

        //create tag list for frame
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
        Object[] columnNames = {"NAME","VALUE"};
        Object[][] data = new Object[0][];

        DefaultTableModel model = new DefaultTableModel(data,columnNames);
        tagsTableList.setModel(model);
        tagsTableList.getTableHeader().setReorderingAllowed(false);

        // Center align the cells under the "VALUE" column
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        tagsTableList.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);

        // Wrap text in the "NAME" column
        TableColumn nameColumn = tagsTableList.getColumnModel().getColumn(0);
        nameColumn.setCellRenderer(new MultilineTableCellRenderer());

        tagsTableList.setFocusable(false);
        tagsTableList.setRowSelectionAllowed(false);

        JScrollPane tagsScrollPane = new JScrollPane(tagsTableList);

        tagsScrollPane.setPreferredSize(new Dimension(200, getHeight())); // Set preferred size

        // New JPanel for the right section
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(createButtonPanel(), BorderLayout.NORTH);
        rightPanel.add(tagsScrollPane, BorderLayout.CENTER);

        // Use JSplitPane for layout
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, new JPanel(), rightPanel);
        splitPane.setResizeWeight(1.0);
        add(splitPane, BorderLayout.EAST);

        //movement key binds
        getRootPane().getInputMap(IFW).put(KeyStroke.getKeyStroke('.'), MOVE_RIGHT);
        getRootPane().getInputMap(IFW).put(KeyStroke.getKeyStroke(','), MOVE_LEFT);

        getRootPane().getActionMap().put(MOVE_RIGHT, new MoveRightAction(this));
        getRootPane().getActionMap().put(MOVE_LEFT, new MoveLeftAction(this));

        //shortcut key binds
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
                    JOptionPane.showMessageDialog(getRootPane(), "No file was opened!", "No File", JOptionPane.ERROR_MESSAGE);
            }
        });


        // Add drag and drop support
        new DropTarget(this, new DropTargetAdapter() {
            public void drop(DropTargetDropEvent evt) {
                try {
                    evt.acceptDrop(DnDConstants.ACTION_COPY);
                    Transferable t = evt.getTransferable();
                    if (t.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                        java.util.List<File> files = (java.util.List<File>) t.getTransferData(DataFlavor.javaFileListFlavor);
                        if (!files.isEmpty()) {
                            videoFile = files.get(0);

                            video = videoService.createVideoIfNotExists(videoFile.getName());
                            if(video.getTotalFrames() == null)
                                setUpVideoData(videoFile);
                            else
                                setUpVideoData(video);

                            infoLabel.setText("Current Frame: " + (currentFrameIndex+1) + "/" + maxFrameIndex + " | Frame Rate: " + videoFramerate + " fps");
                            imageLabel.requestFocusInWindow();

                            ctx.getBean(FrameCache.class).setFileName(videoFile);
                            ctx.getBean(FrameCache.class).firstLoad(videoFile);

                            createTagMapForFile();

                            displayCurrentFrame();
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        setVisible(true);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                ctx.getBean(FrameProcessorClient.class).send("-1;0;0",false);

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
                ctx.getBean(SettingsView.class).dispose();
                ctx.getBean(TagManagerView.class).dispose();
                ctx.getBean(TagDetailsView.class).dispose();
                ctx.getBean(StatusView.class).dispose();
            }
        });

        getRootPane().requestFocus();
    }

    //initialize buttons
    public void setUpButtonViews(){
        //initialize tagManagerView
        SwingUtilities.invokeLater(() -> {
            //set up tag manager
            tagManagerView = getApplicationContext().getBean(TagManagerView.class);
            tagManagerView.setUpView(this);

            //set up settings
            settingsView = getApplicationContext().getBean(SettingsView.class);
            settingsView.setUpView();

            //set up tag details
            getApplicationContext().getBean(TagDetailsView.class).setUpView();

            exportView = ctx.getBean(ExportView.class);
            exportView.setUpView();

        });
    }

    //jump to given frame
    private void jumpToSpecifiedFrame() {
        try {
            int frameToJump = Integer.parseInt(jumpTextField.getText());
            if (frameToJump < 0 || frameToJump > maxFrameIndex) {
                throw new Exception("Cannot display frame: " + frameToJump + ". Frame number does not exist.");
            } else {
                ctx.getBean(FrameCache.class).jump(frameToJump-1);
                currentFrameIndex = (frameToJump-1);
                displayCurrentFrame();
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid frame number.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);

        }
    }

    //move to the frame on the right
    public void moveRight() {
        if (currentFrameIndex != maxFrameIndex - 1) {
            ctx.getBean(FrameCache.class).move(currentFrameIndex,currentFrameIndex+1);
            currentFrameIndex++;
            displayCurrentFrame();
        }
    }

    //move to the frame on the left
    public void moveLeft() {
        if (currentFrameIndex >= 1) {
            ctx.getBean(FrameCache.class).move(currentFrameIndex,currentFrameIndex-1);
            currentFrameIndex--;
            displayCurrentFrame();
        }
    }

    //display frame
    private void displayCurrentFrame() {
        //display tag list
        displayTagList();

        // Decode the current frame of the video from cache
        BufferedImage bufferedImage = ctx.getBean(FrameCache.class).getCurrentFrame(currentFrameIndex);

        // Get the dimensions of the JFrame
        int frameWidth = imageLabel.getWidth() - 10;
        int frameHeight = imageLabel.getHeight() - 10;

        // Calculate scaling factors to maintain proportionality
        double scaleWidth = (double) frameWidth / videoWidth;
        double scaleHeight = (double) frameHeight / videoHeight;
        double scaleFactor = Math.min(scaleWidth, scaleHeight);

        // Calculate the target dimensions
        int targetWidth = (int) (scaleFactor * videoWidth);
        int targetHeight = (int) (scaleFactor * videoHeight);

        // Scale the image to fit inside the JLabel
        BufferedImage scaledImage = scaleImage(bufferedImage, targetWidth, targetHeight);

        // Display the scaled image in the JLabel
        imageLabel.setIcon(new ImageIcon(scaledImage));

        infoLabel.setText("Current Frame: " + (currentFrameIndex + 1) + "/" + maxFrameIndex + " | Frame Rate: " + videoFramerate + " fps");
    }

    //scale image to window size
    private BufferedImage scaleImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
        BufferedImage scaledImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = scaledImage.createGraphics();
        g.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
        g.dispose();
        return scaledImage;
    }

    //create buttons
    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout());

        JButton tagManagerButton = new JButton();
        setButtonIcon(tagManagerButton,"plus.png");
        tagManagerButton.addActionListener(e -> {
            if(videoFile != null)
                tagManagerView.setUpData(videoFile.getName(),currentFrameIndex);
            else
                JOptionPane.showMessageDialog(this, "No file was opened!", "No File", JOptionPane.ERROR_MESSAGE);
        });

        JButton settingsButton = new JButton();
        setButtonIcon(settingsButton,"settings.png");
        settingsButton.addActionListener(e -> SwingUtilities.invokeLater(() -> settingsView.open()));

        JButton exportButton = new JButton();
        setButtonIcon(exportButton,"export.png");
        exportButton.addActionListener(e -> SwingUtilities.invokeLater(() -> exportView.open()));

        buttonPanel.add(tagManagerButton);
        buttonPanel.add(settingsButton);
        buttonPanel.add(exportButton);

        return buttonPanel;
    }

    //add icons to button
    private void setButtonIcon(JButton button,String icon){
        URL iconURL = getClass().getResource("/icons/"+icon);
        if(iconURL != null) {
            ImageIcon imageIcon = new ImageIcon(iconURL);
            Image scaledIcon = imageIcon.getImage().getScaledInstance(32,32,Image.SCALE_SMOOTH);
            button.setIcon(new ImageIcon(scaledIcon));
        }
    }

    //create local representation of data in the database
    private void createTagMapForFile(){
        tagsOnFramesOnVideo = new HashMap<>();

        List<Frame> frames = frameService.getAllByVideo(videoService.getByName(videoFile.getName()));

        for(Frame f : frames)
            tagsOnFramesOnVideo.put(f.getFrameNumber(),f.getTags());
    }

    //get all tags of given frame
    public List<Tag> getTagsOfFrame(int frameNo){
        return tagsOnFramesOnVideo.get(frameNo) != null ?
                tagsOnFramesOnVideo.get(frameNo) :
                new ArrayList<>();
    }

    public Tag getTagByIdOnFrame(int id,int frameNo){
        if(tagsOnFramesOnVideo.get(frameNo)==null)
            return null;

        for(Tag t : tagsOnFramesOnVideo.get(frameNo))
            if(t.getId() ==id)
                return t;

        return null;
    }

    //remove tag from frame
    public void removeTagFromFrame(Tag tag,int frameNo){
        if(tagsOnFramesOnVideo.get(frameNo)==null)
            return;

        tagsOnFramesOnVideo.get(frameNo).removeIf(t -> Objects.equals(t.getId(),tag.getId()));
    }

    //remove tag from all frames
    public void removeTagFromAllFrames(Tag tag){
        if(!tagsOnFramesOnVideo.isEmpty())
            for(Integer frameNo : tagsOnFramesOnVideo.keySet())
                removeTagFromFrame(tag,frameNo);
    }

    public void displayTagList(){
        DefaultTableModel model = (DefaultTableModel) tagsTableList.getModel();
        model.setRowCount(0);

        if(tagsOnFramesOnVideo.containsKey(currentFrameIndex))
            for(Tag tag : tagsOnFramesOnVideo.get(currentFrameIndex))
                model.addRow(new Object[]{
                        tag.getName(),
                        tag.getValue(),
                });

        tagsTableList.setModel(model);
        tagsTableList.revalidate();
    }

    //set tags on given frame in local data representation
    public void setCurrentTags(List<Tag> tags,int frameNo){
        tagsOnFramesOnVideo.put(frameNo,tags);
    }

    //loads all necessary information about the video from file
    private void setUpVideoData(File videoFile){
        try(FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(videoFile)){
            currentFrameIndex = 0;

            grabber.start();

            maxFrameIndex = grabber.getLengthInFrames();
            videoHeight = grabber.getImageHeight();
            videoWidth = grabber.getImageWidth();
            videoFramerate = grabber.getFrameRate();
            videoDuration = grabber.getLengthInTime() / 1000000d;

            grabber.stop();

            videoService.addVideoData(video,maxFrameIndex,videoFramerate,videoDuration,videoHeight,videoWidth);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //sets up data if video in database
    private void setUpVideoData(Video video){
        currentFrameIndex = 0;
        maxFrameIndex = video.getTotalFrames();
        videoHeight = video.getVideoHeight();
        videoWidth = video.getVideoWidth();
        videoFramerate = video.getFrameRate();
        videoDuration = video.getDuration();
    }

    //move left class
    private static class MoveLeftAction extends AbstractAction {
        FrameHopperView app;

        public MoveLeftAction(FrameHopperView app) {
            this.app = app;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            app.moveLeft();
        }
    }

    //move right class
    private static class MoveRightAction extends AbstractAction {
        FrameHopperView app;

        public MoveRightAction(FrameHopperView app) {
            this.app = app;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            app.moveRight();
        }
    }

    //get index of tag in global list
    public synchronized static int findTagIndexById(Integer id){
        return IntStream.range(0, TAG_LIST.size())
                .filter(i -> TAG_LIST.get(i).getId().equals(id))
                .findFirst()
                .orElse(-1);
    }

    //get tag by name
    public static Tag findTagByName(String name){
        return TAG_LIST.stream()
                .filter(t -> t.getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    //get amount of not hidden tags
    public synchronized static int getNumberOfVisibleTags(){
        return (int) TAG_LIST.stream()
                .filter(t -> !t.isDeleted())
                .count();
    }
}
