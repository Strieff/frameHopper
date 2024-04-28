package com.example.engineer.View;

import com.example.engineer.FrameProcessor.FrameCache;
import com.example.engineer.FrameProcessor.FrameProcessorClient;
import com.example.engineer.Model.Frame;
import com.example.engineer.Model.Tag;
import com.example.engineer.Model.UserSettings;
import com.example.engineer.Service.FrameService;
import com.example.engineer.Service.TagService;
import com.example.engineer.Service.VideoService;
import com.example.engineer.View.buttonsView.SettingsView;
import com.example.engineer.View.buttonsView.TagManagerView;
import com.example.engineer.View.smallViews.TagDetailsView;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.*;
import java.util.List;

//TODO delete hidden tags that are not assigned to any frame
//TODO fetch all needed data from DB after getting video - remove unnecessary database calls
//TODO display tags on the right in a list - name value
//TODO make a small window for comments under tag list, move left right to find comments
//TODO create DB threads
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

    private JLabel imageLabel;
    private JLabel infoLabel;
    private JTextField jumpTextField;
    private JList<String> userInputList;
    private DefaultListModel<String> userInputListModel;

    private File videoFile;
    private int currentFrameIndex;
    private int maxFrameIndex;
    private int videoHeight;
    private int videoWidth;
    private double videoFramerate;

    private Map<Integer,List<Tag>> tagsOnFramesOnVideo;

    private static final int IFW = JComponent.WHEN_IN_FOCUSED_WINDOW;
    private static final String MOVE_RIGHT = "move right";
    private static final String MOVE_LEFT = "move left";

    private static ApplicationContext ctx;
    public static ApplicationContext getApplicationContext() {
        return ctx;
    }
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        ctx = applicationContext;

        //get global list of tags
        TAG_LIST = tagService.getAllTags();
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

        // New JList for displaying user inputs
        userInputListModel = new DefaultListModel<>();
        userInputList = new JList<>(userInputListModel);
        JScrollPane userInputScrollPane = new JScrollPane(userInputList);
        userInputScrollPane.setPreferredSize(new Dimension(200, getHeight())); // Set preferred size

        // New JPanel for the right section
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(createButtonPanel(), BorderLayout.NORTH);
        rightPanel.add(userInputScrollPane, BorderLayout.CENTER);

        // Use JSplitPane for layout
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, new JPanel(), rightPanel);
        splitPane.setResizeWeight(1.0);
        add(splitPane, BorderLayout.EAST);

        getRootPane().getInputMap(IFW).put(KeyStroke.getKeyStroke('.'), MOVE_RIGHT);
        getRootPane().getInputMap(IFW).put(KeyStroke.getKeyStroke(','), MOVE_LEFT);

        getRootPane().getActionMap().put(MOVE_RIGHT, new MoveRightAction(this));
        getRootPane().getActionMap().put(MOVE_LEFT, new MoveLeftAction(this));

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

                            setUpVideoData(videoFile);

                            infoLabel.setText("Current Frame: " + (currentFrameIndex+1) + "/" + maxFrameIndex + " | Frame Rate: " + videoFramerate + " fps");
                            imageLabel.requestFocusInWindow();
                            videoService.createVideoIfNotExists(videoFile.getName());

                            ctx.getBean(FrameCache.class).setFileName(videoFile);
                            ctx.getBean(FrameCache.class).firstLoad(videoFile);
                            displayCurrentFrame();

                            createTagMapForFile();
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        setVisible(true);
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

            //TODO set up export
        });
    }

    //jump to given frame
    private void jumpToSpecifiedFrame() {
        try {
            int frameToJump = Integer.parseInt(jumpTextField.getText());
            jumpFrame(frameToJump);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid frame number.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    //jump to given frame
    public void jumpFrame(int frame) throws Exception {
        if (frame < 0 || frame > maxFrameIndex) {
            throw new Exception("Cannot display frame: " + frame + ". Frame number does not exist.");
        } else {
            currentFrameIndex = (frame-1);
            displayCurrentFrame();
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
                SwingUtilities.invokeLater(() -> {
                    tagManagerView.setUpData(videoFile.getName(),currentFrameIndex);
                });
            else
                JOptionPane.showMessageDialog(this, "No file was opened!", "No File", JOptionPane.ERROR_MESSAGE);
        });

        JButton settingsButton = new JButton();
        setButtonIcon(settingsButton,"settings.png");
        settingsButton.addActionListener(e -> SwingUtilities.invokeLater(() -> settingsView.setUpData()));

        JButton exportButton = new JButton();
        setButtonIcon(exportButton,"export.png");
        //TODO exportButton.addActionListener(e -> SwingUtilities.invokeLater(() -> new ExportView()));

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

        int forRemoval=-1;

        for (int i = 0; i < tagsOnFramesOnVideo.get(frameNo).size(); i++)
            if(Objects.equals(tagsOnFramesOnVideo.get(frameNo).get(i).getId(), tag.getId()))
                forRemoval = i;

        if(forRemoval==-1)
            return;

        tagsOnFramesOnVideo.get(frameNo).remove(forRemoval);
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

    //loads all necessary information about the video
    private void setUpVideoData(File videoFile){
        try(FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(videoFile)){
            currentFrameIndex = 0;

            grabber.start();

            maxFrameIndex = grabber.getLengthInFrames();
            videoHeight = grabber.getImageHeight();
            videoWidth = grabber.getImageWidth();
            videoFramerate = grabber.getFrameRate();

            grabber.stop();

        }catch (Exception e){
            e.printStackTrace();
        }
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
        for (int i = 0; i < TAG_LIST.size(); i++)
            if(TAG_LIST.get(i).getId().intValue()==id.intValue())
                return i;

        return -1;
    }

    //get amount of not hidden tags
    public synchronized static int getNumberOfVisibleTags(){
        int i = 0;

        for (Tag t : TAG_LIST)
            if(!t.isDeleted())
                i++;

        return i;
    }

    @Override
    public void dispose(){
        ctx.getBean(FrameProcessorClient.class).send("-1;KEEP",false);
        super.dispose();
    }
}
