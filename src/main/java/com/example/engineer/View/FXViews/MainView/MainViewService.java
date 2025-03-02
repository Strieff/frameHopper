package com.example.engineer.View.FXViews.MainView;

import com.example.engineer.FrameProcessor.FrameProcessor;
import com.example.engineer.Model.Frame;
import com.example.engineer.Model.Tag;
import com.example.engineer.Model.Video;
import com.example.engineer.Service.FrameService;
import com.example.engineer.Service.VideoService;
import com.example.engineer.View.Elements.Language.Dictionary;
import com.example.engineer.View.Elements.Actions.PasteRecentAction;
import com.example.engineer.View.Elements.Actions.RemoveRecentAction;
import com.example.engineer.View.Elements.Actions.UndoRedoAction;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class MainViewService {
    @Autowired
    private VideoService videoService;
    @Autowired
    private FrameService frameService;
    @Autowired
    private PasteRecentAction pasteRecentAction;
    @Autowired
    private RemoveRecentAction removeRecentAction;
    @Autowired
    private UndoRedoAction undoRedoAction;

    private InformationContainer info;
    private FrameProcessor frameProcessor;


    //get video from DB
    public Video getVideo(File videoFile){
        return videoService.createVideoIfNotExists(videoFile);
    }

    //set up information container
    public void prepareVideo(Video video, File videoFile,Label label){
        info = new InformationContainer(label,video,videoFile,frameService);
    }

    public void prepareProcessor(File file,Video video){
        if(frameProcessor != null)
            frameProcessor.close();

        frameProcessor = FrameProcessor.getFrameProcessor(FilenameUtils.getExtension(file.getAbsolutePath()));
        frameProcessor.loadVideo(file);

        if(video.getTotalFrames() == null){
            video.setTotalFrames(frameProcessor.getInfo().getTotalFrames());
            video.setDuration(frameProcessor.getInfo().getDuration());
            video.setFrameRate(frameProcessor.getInfo().getFramerate());
            video.setVideoWidth(frameProcessor.getInfo().getWidth());
            video.setVideoHeight(frameProcessor.getInfo().getHeight());
            videoService.saveVideo(video);
        }
    }

    public Node jump(int toJump) {
        info.setCurrentIndex(toJump-1);
        return displayCurrentFrame();
    }

    //GETTERS
    public ImageView displayCurrentFrame(){
        var imageLabel = info.getLabel();
        return frameProcessor.getFrame(info.getCurrentIndex(), (int) imageLabel.getWidth(), (int) imageLabel.getHeight());
    }

    public ObservableList<TableEntry> displayCurrentTags(){
        var tagsDTOList = info.getTagsOnFrame();

        ObservableList<TableEntry> data = FXCollections.observableArrayList();
        if(tagsDTOList != null)
            for(var t : tagsDTOList)
                data.add(new TableEntry(t.getName(),t.getValue()));

        return data;
    }

    public int getCurrentIndex(){
        return info.getCurrentIndex();
    }

    public String getCurrentPath() {
        return info.getVideo().getPath();
    }

    public List<Tag> getTagsOnFrame(){
        return info.getTagsOnFrame();
    }

    public int getCurrentId() {
        return info != null ? info.getVideo().getId() : -1;
    }

    public Video getCurrentVideo() {
        return info != null ? info.getVideo() : null;
    }

    public void setCurrentTags(List<Tag> currentTags) {
        info.setCurrentTags(currentTags);
    }

    public boolean isOpen() {
        return info!=null;
    }

    //scale image to fit the application window
    private BufferedImage scaleImage(BufferedImage originalImage,int targetWidth,int targetHeight){
        BufferedImage scaledImage = new BufferedImage(targetWidth,targetHeight,BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = scaledImage.createGraphics();
        g.drawImage(originalImage,0,0,targetWidth,targetHeight,null);
        g.dispose();
        return scaledImage;
    }

    //move right method
    public ImageView moveRight(){
        if(info.getCurrentIndex() < info.getVideo().getTotalFrames()-1)
            info.setCurrentIndex(getCurrentIndex()+1);
        return displayCurrentFrame();
    }

    //move left method
    public ImageView moveLeft(){
        if(info.getCurrentIndex()>0)
            info.setCurrentIndex(getCurrentIndex()-1);
        return displayCurrentFrame();
    }

    //GET CURRENT INFORMATION
    public String displayCurrentInfo() {
        return String.format(
                Dictionary.get("main.fileInfo"),
                isOpen() ?  info.getCurrentIndex() + 1 : 0,
                isOpen() ? info.getVideo().getTotalFrames() : 0,
                isOpen() ? info.getVideo().getFrameRate() : 0f
        );
    }

    //DELETE TAGS FROM FRAME
    public void deleteTag(Tag tag) {
        if(isOpen())
            info.removeTag(tag);
    }

    public void deleteTags(List<Tag> tags) {
        if(isOpen())
            info.removeTags(tags);
    }

    //HANDLE ACTIONS
    public void pasteRecent(){
        if (info.getTagsOnFrame() == null)
            info.setCurrentTags(new ArrayList<>());

        pasteRecentAction.performAction(
                info.getTagsOnFrame(),
                info.getCurrentIndex(),
                info.getVideo()
        );
    }

    public void removeRecent(){
        removeRecentAction.performAction(
                info.getTagsOnFrame() == null ? new ArrayList<>() : info.getTagsOnFrame(),
                info.getCurrentIndex(),
                info.getVideo()
        );
    }

    public void undo(){
        undoRedoAction.undoAction();
        info.setTagsOnFrameOnVideo(undoRedoAction.getOriginalTags(), undoRedoAction.getCurrentFrameIndex());
    }

    public void redo(){
        undoRedoAction.redoAction();
        info.setTagsOnFrameOnVideo(undoRedoAction.getCurrentTags(),undoRedoAction.getCurrentFrameIndex());
    }

    //class to hold necessary variables
    private static class InformationContainer{
        @Getter
        private final Label label;
        @Getter
        private final Video video;
        @Getter
        private final File videoFile;
        @Getter
        @Setter
        private int currentIndex;
        private Map<Integer, List<Tag>> tagsOnFrameOnVideo;

        public InformationContainer(Label label, Video video, File videoFile,FrameService frameService) {
            this.label = label;
            this.video = video;
            this.videoFile = videoFile;

            tagsOnFrameOnVideo = new HashMap<>();
            List<Frame> frames = frameService.getAllByVideo(video);
            for(var f : frames)
                tagsOnFrameOnVideo.put(f.getFrameNumber(),f.getTags());

            this.currentIndex = 0;
        }

        public List<Tag> getTagsOnFrame(){
            return tagsOnFrameOnVideo.get(currentIndex);
        }

        public void setCurrentTags(List<Tag> tags){
            tagsOnFrameOnVideo.put(currentIndex,tags);
        }

        public void setTagsOnFrameOnVideo(List<Tag> tags,int index){
            tagsOnFrameOnVideo.put(index,tags);
        }

        public void removeTag(Tag t){
            for(var tags: tagsOnFrameOnVideo.values())
                tags.remove(t);
        }

        public void removeTags(List<Tag> tags) {
            for(var list : tagsOnFrameOnVideo.values())
                list.removeAll(tags);
        }
    }
}
