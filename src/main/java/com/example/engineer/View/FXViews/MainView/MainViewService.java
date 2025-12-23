package com.example.engineer.View.FXViews.MainView;

import com.example.engineer.FfmpegService.VideoDataProvider;
import com.example.engineer.Model.Frame;
import com.example.engineer.Model.Tag;
import com.example.engineer.Model.Video;
import com.example.engineer.Service.FrameService;
import com.example.engineer.Service.VideoService;
import com.example.engineer.View.Elements.DataManagers.UserSettingsManager;
import com.example.engineer.View.Elements.Language.Dictionary;
import com.example.engineer.View.Elements.Actions.PasteRecentAction;
import com.example.engineer.View.Elements.Actions.RemoveRecentAction;
import com.example.engineer.View.Elements.Actions.UndoRedoAction;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.*;

@Component
public class MainViewService {
    private final VideoService videoService;
    private final FrameService frameService;
    private final PasteRecentAction pasteRecentAction;
    private final RemoveRecentAction removeRecentAction;
    private final UndoRedoAction undoRedoAction;
    private final VideoDataProvider videoDataProvider;
    private final UserSettingsManager userSettings;

    private InformationContainer info;

    public MainViewService(
            VideoService videoService,
            FrameService frameService,
            PasteRecentAction pasteRecentAction,
            RemoveRecentAction removeRecentAction,
            UndoRedoAction undoRedoAction,
            VideoDataProvider videoDataProvider, UserSettingsManager userSettings
    ) {
        this.videoService = videoService;
        this.frameService = frameService;
        this.pasteRecentAction = pasteRecentAction;
        this.removeRecentAction = removeRecentAction;
        this.undoRedoAction = undoRedoAction;
        this.videoDataProvider = videoDataProvider;
        this.userSettings = userSettings;
    }


    //get video from DB
    public Video getVideo(File videoFile){
        videoDataProvider.clearCache();
        userSettings.setRecentPath(videoFile.getAbsolutePath());
        return videoService.createOrGet(videoFile);
    }

    //set up information container
    public void prepareVideo(Video video, File videoFile,Label label){
        info = new InformationContainer(label,video,videoFile,frameService);
    }

    public Image jump(int toJump) {
        info.setCurrentIndex(toJump-1);

        if(toJump > info.getVideo().getTotalFrames()-1)
            info.setCurrentIndex(info.getVideo().getTotalFrames()-1);

        if(toJump < 1)
            info.setCurrentIndex(0);

        return displayCurrentFrame();
    }

    public Image displayCurrentFrame(){
        try {
            return videoDataProvider.getFrame(
                    info.getVideo(),
                    info.currentIndex
            );
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
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

    //move right method
    public Image moveRight(){
        if(info.getCurrentIndex() < info.getVideo().getTotalFrames()-1)
            info.setCurrentIndex(getCurrentIndex()+1);
        return displayCurrentFrame();
    }

    //move left method
    public Image moveLeft(){
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
        private final Map<Integer, List<Tag>> tagsOnFrameOnVideo;

        public InformationContainer(Label label, Video video, File videoFile, FrameService frameService) {
            this.label = label;
            this.video = video;
            this.videoFile = videoFile;

            tagsOnFrameOnVideo = new HashMap<>();
            List<Frame> frames = frameService.getAllByVideo(video);
            for(var f : frames)
                tagsOnFrameOnVideo.put(f.getFrameNumber(),new ArrayList<>(f.getTags()));

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
