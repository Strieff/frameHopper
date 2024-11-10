package com.example.engineer.View.FXViews.MainView;

import com.example.engineer.FrameProcessor.Cache;
import com.example.engineer.FrameProcessor.FrameProcessorRequestManager;
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
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
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
    private FrameProcessorRequestManager requestManager;
    @Autowired
    private PasteRecentAction pasteRecentAction;
    @Autowired
    private RemoveRecentAction removeRecentAction;
    @Autowired
    private UndoRedoAction undoRedoAction;

    private InformationContainer info;


    //get video from DB
    public Video getVideo(File videoFile){
        return videoService.createVideoIfNotExists(videoFile);
    }

    //set up cache
    public Cache setCache(File videoFile) {
        if(info != null)
            info.getCache().clearCache();
        return Cache.getCache(videoFile.getAbsolutePath());
    }

    //set up information container
    public void prepareVideo(Cache cache, Video video, File videoFile,Label label){
        cache.firstLoad(videoFile);
        cache.setUpVideoMetadata(videoService,video);

        info = new InformationContainer(cache,label,video,videoFile,frameService);
    }


    //GETTERS
    public ImageView displayCurrentFrame(){
        var cache = info.getCache();
        var imageLabel = info.getLabel();

        //get dimension of the application window
        int appWidth = (int) (imageLabel.getWidth() - 10);
        int appHeight = (int) (imageLabel.getHeight() - 10);

        //calculate scale factor for proportions
        double scaleWidth = (double) appWidth / cache.getWidth();
        double scaleHeight = (double) appHeight / cache.getHeight();
        double scaleFactor = Math.min(scaleWidth,scaleHeight);

        //calculate target dimensions
        int targetWidth = (int) (scaleFactor * cache.getWidth());
        int targetHeight = (int) (scaleFactor * cache.getHeight());

        //get current frame and scale it
        var currentIndex = info.getCurrentIndex();
        BufferedImage frame = cache.getCurrentFrame(currentIndex);
        frame = scaleImage(frame,targetWidth,targetHeight);

        var imageView = new ImageView(SwingFXUtils.toFXImage(frame,null));
        imageView.setPreserveRatio(true); // Preserve aspect ratio

        return imageView;
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

    //TODO: remove later, needed for existing infrastructure
    public String getCurrentName() {
        return info.getVideo().getName();
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
        if(info.getCurrentIndex() != info.video.getTotalFrames()-1)
            info.moveRight();
        return displayCurrentFrame();
    }

    //move left method
    public ImageView moveLeft(){
        if(info.getCurrentIndex()>0)
            info.moveLeft();
        return displayCurrentFrame();
    }

    //jump method
    public ImageView jump(int i){
        if(i-1>=0 && i<info.getVideo().getTotalFrames()){
            info.jump(i);
        }
        return displayCurrentFrame();
    }

    public void close(){
        requestManager.closeServer();
        try{
            Files.walk(Paths.get("cache"))
                    .filter(p -> !p.equals(Paths.get("cache")))
                    .sorted((p1,p2) -> -p1.compareTo(p2))
                    .forEach(p -> {
                        try {
                            Files.delete(p);
                        }catch (Exception ex){
                            throw new RuntimeException(ex);
                        }
                    });
        }catch (Exception e){
            e.printStackTrace();
        }
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
        private final Cache cache;
        @Getter
        private final Label label;
        @Getter
        private final Video video;
        @Getter
        private final File videoFile;
        @Getter
        private int currentIndex;
        private Map<Integer, List<Tag>> tagsOnFrameOnVideo;

        public InformationContainer(Cache cache, Label label, Video video, File videoFile,FrameService frameService) {
            this.cache = cache;
            this.label = label;
            this.video = video;
            this.videoFile = videoFile;

            tagsOnFrameOnVideo = new HashMap<>();
            List<Frame> frames = frameService.getAllByVideo(video);
            for(var f : frames)
                tagsOnFrameOnVideo.put(f.getFrameNumber(),f.getTags());

            this.currentIndex = 0;
        }

        public void moveRight(){
            cache.move(currentIndex,++currentIndex);
        }

        public void moveLeft(){
            cache.move(currentIndex,--currentIndex);
        }

        public void jump(int i){
            currentIndex = i-1;
            cache.jump(currentIndex);
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
