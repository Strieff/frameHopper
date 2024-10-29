package com.example.engineer.View.ViewModel.PathMerging;

import com.example.engineer.FrameProcessor.FrameProcessorRequestManager;
import com.example.engineer.Model.Video;
import com.example.engineer.Service.VideoService;
import com.example.engineer.View.Elements.DialogProvider;
import com.example.engineer.View.Elements.Language.Dictionary;
import com.example.engineer.View.Elements.Language.LanguageChangeListener;
import com.example.engineer.View.Elements.Language.LanguageManager;
import com.example.engineer.View.Elements.ProgramResetResolver;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Component
@DependsOn("MergingList")
public class PathMergingDetailsView extends JFrame implements LanguageChangeListener {
    @Autowired
    VideoService videoService;
    @Autowired
    LanguageManager languageManager;
    @Autowired
    FrameProcessorRequestManager requestManager;
    @Autowired
    PathMergingService viewService;

    private final List<JLabel> labels = new ArrayList<>();
    private final List<JButton> buttons = new ArrayList<>();

    private final JTextArea pathTextArea;
    private final JTextArea dataArea;

    //data
    Video current;

    public PathMergingDetailsView() {
        //set needed information
        setSize(300, 400);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        //path label
        var pathLabel = new JLabel(Dictionary.get("merge.path.title"),SwingConstants.CENTER);
        pathLabel.putClientProperty("text","merge.path.title");
        labels.add(pathLabel);

        //text area to display path
        pathTextArea = new JTextArea();
        pathTextArea.setEditable(false);
        pathTextArea.setLineWrap(true);
        pathTextArea.setWrapStyleWord(true);

        //button to change path
        var changePathButton = new JButton(Dictionary.get("merge.path.button.change"));
        changePathButton.putClientProperty("text","merge.path.button.change");
        changePathButton.addActionListener(e -> {
            try{
                //get file
                var fileFromNewPath = getNewFile();

                //check if file is valid
                if(!viewService.isValidFile(fileFromNewPath.getAbsolutePath())) throw new Exception(Dictionary.get("merge.error.fileType"));

                //check if both have same amount of frames
                if(!viewService.hasSameAmountOfFrames(fileFromNewPath,current))
                    //get confirmation
                    if(!DialogProvider.yesNoDialog(String.format(
                        Dictionary.get("merge.warning.differentLen"),
                        fileFromNewPath,
                        current.getPath()
                )))
                        return;

                //check if the new path exists in database
                if(viewService.checkIfExistsInDatabase(fileFromNewPath.getAbsolutePath())){
                    //get video by path
                    var existingVideo = videoService.getByPath(fileFromNewPath.getAbsolutePath());

                    //check if entry of new path has any data
                    if(viewService.checkIfNewHasData(existingVideo)){
                        switch(DialogProvider.customDialog(
                                Dictionary.get("merge.action.actionRequired"),
                                0,
                                Dictionary.get("merge.action.cancel"),
                                Dictionary.get("merge.action.discard.old"),
                                Dictionary.get("merge.action.merge"),
                                Dictionary.get("merge.action.discard.new")
                        )){
                            case 0:
                                DialogProvider.messageDialog(Dictionary.get("merge.message.cancelled"),this);
                                return;
                            case 1:
                                if(DialogProvider.yesNoDialog(Dictionary.get("merge.warning.discard.old")))
                                    viewService.removeVideo(existingVideo,current);
                                else
                                    return;
                                break;
                            case 2:
                                if(DialogProvider.yesNoDialog(Dictionary.get("merge.warning.merge")))
                                    viewService.mergeVideos(current,existingVideo);
                                else
                                    return;
                                break;
                            case 3:
                                if(DialogProvider.yesNoDialog(Dictionary.get("merge.warning.discard.new")))
                                    viewService.removeVideo(current,existingVideo);
                                else
                                    return;
                                break;
                        }
                    }else{
                        //check of both have same amount of frames
                        if(viewService.hasSameAmountOfFrames(existingVideo,current)){
                            videoService.deleteVideo(existingVideo);
                            viewService.updateVideo(current,existingVideo);
                        }

                        //check if entry of new path has more frames
                        if(viewService.checkIfNewHasMoreFrames(existingVideo,current)){
                            //get confirmation
                            if(DialogProvider.yesNoDialog(Dictionary.get("merge.warning.frames.more"))){
                                //reassign data
                                viewService.reassignFrames(current,existingVideo,false);
                                videoService.deleteVideo(current);
                            }else
                                return;
                        }else{
                            //get confirmation
                            if(DialogProvider.yesNoDialog(Dictionary.get("merge.warning.frames.less"))){
                                viewService.reassignFrames(current,existingVideo,true);
                                videoService.deleteVideo(current);
                            }else
                                return;
                        }
                    }

                }else{
                    //doesn't exist in database
                    var saved = viewService.updateVideo(current,fileFromNewPath.getAbsolutePath());
                    if(DialogProvider.yesNoDialog(Dictionary.get("merge.message.ready")))
                        pathTextArea.setText(saved.getPath());
                    else
                        return;
                }

                DialogProvider.messageDialog(Dictionary.get("merge.message.restarting"));
                requestManager.closeServer();
                ProgramResetResolver.reset();
            }catch (Exception ex){
                ex.printStackTrace();
            }
        });
        buttons.add(changePathButton);

        //panel to hold button and text area
        var pathPanel = new JPanel(new BorderLayout());
        pathPanel.add(pathTextArea, BorderLayout.CENTER);
        pathPanel.add(changePathButton, BorderLayout.EAST);

        //panel to hold button, label and text area
        var upperPanel = new JPanel(new BorderLayout());
        upperPanel.add(pathLabel, BorderLayout.NORTH);
        upperPanel.add(pathPanel, BorderLayout.CENTER);

        //metadata label
        var dataLabel = new JLabel(Dictionary.get("merge.data.title"),SwingConstants.CENTER);
        dataLabel.putClientProperty("text","merge.data.title");
        labels.add(dataLabel);

        //metadata text area
        dataArea = new JTextArea();
        dataArea.setEditable(false);

        //panel for metadata label and text area
        var dataPanel = new JPanel(new BorderLayout());
        dataPanel.add(dataLabel, BorderLayout.NORTH);
        dataPanel.add(dataArea, BorderLayout.CENTER);

        //button to close
        var closeButton = new JButton(Dictionary.get("merge.button.close"));
        closeButton.putClientProperty("text","merge.button.close");
        closeButton.addActionListener(e -> close());
        buttons.add(closeButton);

        //add to view
        setLayout(new BorderLayout());
        add(upperPanel, BorderLayout.NORTH);
        add(dataPanel, BorderLayout.CENTER);
        add(closeButton, BorderLayout.SOUTH);

        //operation on close
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                close();
            }
        });

        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_D,KeyEvent.SHIFT_DOWN_MASK,false),"OpenVideoDetails");
        getRootPane().getActionMap().put("OpenVideoDetails", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                close();
            }
        });
    }

    @PostConstruct
    public void init() {
        languageManager.addListener(this);
    }

    public void open(int id){
        current = videoService.getById(id);

        pathTextArea.setText(current.getPath());

        dataArea.setText(String.format(Dictionary.get("merge.data"),current.getTotalFrames(),current.getFrameRate(),current.getDuration()));

        setVisible(true);
    }

    public void close(){
        setVisible(false);
        current = null;
        pathTextArea.setText("");
        dataArea.setText("");
    }

    private File getNewFile() throws Exception{
        var fileChooser = new JFileChooser();
        var userChoice = fileChooser.showOpenDialog(null);

        if(userChoice == JFileChooser.APPROVE_OPTION)
            return fileChooser.getSelectedFile();
        else
            throw new Exception("No file chosen");
    }

    @Override
    public void changeLanguage() {
        for(var l : labels)
            l.setText(Dictionary.get((String)l.getClientProperty("text")));

        for(var b : buttons)
            b.setText(Dictionary.get((String)b.getClientProperty("text")));
    }
}
