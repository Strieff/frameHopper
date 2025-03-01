package com.example.engineer.View.FXViews.VideoMerge;

import com.example.engineer.FrameProcessor.FrameProcessor;
import com.example.engineer.Model.Tag;
import com.example.engineer.Model.Video;
import com.example.engineer.Service.FrameService;
import com.example.engineer.Service.VideoService;
import com.example.engineer.View.Elements.Language.Dictionary;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class VideoMergeService {
    @Autowired
    private VideoService videoService;
    @Autowired
    private FrameService frameService;

    //METADATA COMPARISON

    public Video getVideo(int id){
        return videoService.getById(id);
    }

    public Video getVideoFromFile(File file){
        if(videoService.exists(file.getAbsolutePath()))
            return videoService.getByPath(file.getAbsolutePath());

        var data = FrameProcessor.getInstance().getInfo();

        return Video.builder()
                .id(-1)
                .path(file.getAbsolutePath())
                .name(file.getName())
                .totalFrames(data.getTotalFrames())
                .frameRate(data.getFramerate())
                .duration(data.getDuration())
                .videoHeight(data.getHeight())
                .videoWidth(data.getWidth())
                .build();
    }

    public String compareMetadata(Video oldVideo, Video newVideo){
        var conflictAreas = new ArrayList<String>();

        if(compareTotalFrames(oldVideo,newVideo))
            conflictAreas.add(Dictionary.get("conflict.count"));

        if(compareFrameRate(oldVideo, newVideo))
            conflictAreas.add(Dictionary.get("conflict.framerate"));

        if(compareDuration(oldVideo, newVideo))
            conflictAreas.add(Dictionary.get("conflict.duration"));

        if(compareDimensions(oldVideo, newVideo))
            conflictAreas.add(Dictionary.get("conflict.dimensions"));

        if(compareFileType(oldVideo, newVideo))
            conflictAreas.add(Dictionary.get("conflict.fileType"));

        return String.join("/", conflictAreas);
    }

    private boolean compareTotalFrames(Video oldVideo, Video newVideo){
        return !Objects.equals(oldVideo.getTotalFrames(), newVideo.getTotalFrames());
    }

    private boolean compareFrameRate(Video oldVideo, Video newVideo){
        return !Objects.equals(oldVideo.getFrameRate(), newVideo.getFrameRate());
    }

    private boolean compareDuration(Video oldVideo, Video newVideo){
        return !Objects.equals(oldVideo.getDuration(), newVideo.getDuration());
    }

    private boolean compareDimensions(Video oldVideo, Video newVideo){
        return  !(Objects.equals(oldVideo.getVideoHeight(), newVideo.getVideoHeight()))
                || !(Objects.equals(oldVideo.getVideoWidth(), newVideo.getVideoWidth()));
    }

    private boolean compareFileType(Video oldVideo, Video newVideo){
        return !Objects.equals(FilenameUtils.getExtension(oldVideo.getPath()), FilenameUtils.getExtension(newVideo.getPath()));
    }

    //FRAME DATA

    public ObservableList<TableEntry> getFrames(Video video) {
        ObservableList<TableEntry> data = FXCollections.observableArrayList();

        if(video.getId() == -1)
            return data;

        var allTagsDTOList = frameService.getAllByVideo(video);

        if(allTagsDTOList.isEmpty())
            return data;

        allTagsDTOList.forEach(e -> data.add(new TableEntry(
                e.getId(),
                false,
                e.getFrameNumber(),
                e.getTags().size(),
                e.getTags().stream().map(Tag::getName).toList()
        )));

        return data;
    }

    public String compareFrameData(Video oldVideo, Video newVideo){
        if(!compareTotalFrames(oldVideo,newVideo))
            return Dictionary.get("mv.metadata.no-conflict");

        List<String> conflictAreas = new ArrayList<>();

        if(doBothHaveData(oldVideo, newVideo))
            conflictAreas.add(Dictionary.get("fv.conflict.data"));

        if(doesOldHaveMoreFrames(oldVideo, newVideo))
            conflictAreas.add(Dictionary.get("fv.conflict.old"));

        if(doesNewHaveMoreFrames(oldVideo, newVideo))
            conflictAreas.add(Dictionary.get("fv.conflict.new"));

        return String.format(Dictionary.get("fv.metadata.conflict") ,("\n" + String.join("\n",conflictAreas)));
    }

    public boolean doesOldHaveMoreFrames(Video oldVideo, Video newVideo){
        return oldVideo.getTotalFrames() > newVideo.getTotalFrames();
    }

    public boolean doesNewHaveMoreFrames(Video oldVideo, Video newVideo){
        return oldVideo.getTotalFrames() < newVideo.getTotalFrames();
    }

    private boolean doBothHaveData(Video oldVideo, Video newVideo){
        return !newVideo.getFrames().isEmpty() && !oldVideo.getFrames().isEmpty();
    }

    public void blockOverflow(ObservableList<TableEntry> oldFrames, Video newVideo) {
        var lastFrameIndex = newVideo.getTotalFrames();

        oldFrames.forEach(e -> {
            if(e.getNumber() > lastFrameIndex)
                e.setSelectable(false);
        });
    }

    public String getVideoData(Video video, boolean oldVideo) {
        List<String> data = new ArrayList<>();

        data.add(String.format(Dictionary.get("mv.video.path"),video.getPath()));
        data.add(String.format(Dictionary.get("mv.video.type"),FilenameUtils.getExtension(video.getPath())));
        data.add(String.format(Dictionary.get("mv.video.total-frames"),video.getTotalFrames()));
        data.add(String.format(Dictionary.get("mv.video.framerate"),video.getFrameRate()));
        data.add(String.format(Dictionary.get("mv.video.duration"),video.getDuration()));
        data.add(String.format(Dictionary.get("mv.video.dimensions"),String.join(" x ",video.getVideoWidth().toString(),video.getVideoHeight().toString())));

        return String.format(Dictionary.get(oldVideo ? "mv.metadata.old" : "mv.metadata.new"),("\n" + String.join("\n",data)));
    }
}
