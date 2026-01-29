package com.FrameHopper.app.View.FXViews.VideoMerge;

import com.FrameHopper.app.ffmpegService.VideoDataProvider;
import com.FrameHopper.app.Model.Frame;
import com.FrameHopper.app.Model.Tag;
import com.FrameHopper.app.Model.Video;
import com.FrameHopper.app.Service.FrameService;
import com.FrameHopper.app.Service.VideoService;
import com.FrameHopper.app.View.Elements.FXElementsProviders.FXDialogProvider;
import com.FrameHopper.app.View.Elements.Language.Dictionary;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Service
public class VideoMergeService {
    private final VideoService videoService;
    private final FrameService frameService;
    private final VideoDataProvider videoDataProvider;

    public VideoMergeService(VideoService videoService, FrameService frameService, VideoDataProvider videoDataProvider) {
        this.videoService = videoService;
        this.frameService = frameService;
        this.videoDataProvider = videoDataProvider;
    }

    //METADATA COMPARISON

    public Video getVideo(int id){
        return videoService.getById(id);
    }

    public Video getVideoFromFile(File file) throws IOException, InterruptedException {
        if(videoService.exists(file.getAbsolutePath()))
            return videoService.getByPath(file.getAbsolutePath());


        var data = videoDataProvider.getVideoData(file.getAbsolutePath());

        return Video.builder()
                .id(-1)
                .path(file.getAbsolutePath())
                .name(file.getName())
                .totalFrames(data.totalFrames())
                .frameRate(data.frameRate())
                .duration(data.durationInSeconds())
                .videoHeight(data.height())
                .videoWidth(data.width())
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

        data.sort(Comparator.comparingInt(TableEntry::getNumber));

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

        return String.format(Dictionary.get("fv.metadata.conflict"),("\n" + String.join("\n",conflictAreas)));
    }

    public boolean doesOldHaveMoreFrames(Video oldVideo, Video newVideo){
        return oldVideo.getTotalFrames() > newVideo.getTotalFrames();
    }

    public boolean doesNewHaveMoreFrames(Video oldVideo, Video newVideo){
        return oldVideo.getTotalFrames() < newVideo.getTotalFrames();
    }

    public boolean doBothHaveData(Video oldVideo, Video newVideo){
        var oldFrames = frameService.getAllByVideo(oldVideo);
        var newFrames = frameService.getAllByVideo(newVideo);

        return !oldFrames.isEmpty() && !newFrames.isEmpty();
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
        data.add(String.format(Dictionary.get("mv.video.total-frames"),video.getTotalFrames()));
        data.add(String.format(Dictionary.get("mv.video.framerate"),video.getFrameRate()));
        data.add(String.format(Dictionary.get("mv.video.duration"),video.getDuration()));
        data.add(String.format(Dictionary.get("mv.video.dimensions"),String.join(" x ",video.getVideoWidth().toString(),video.getVideoHeight().toString())));

        return String.format(Dictionary.get(oldVideo ? "mv.metadata.old" : "mv.metadata.new"),("\n" + String.join("\n",data)));
    }

    public Video mergeData(Video oldVideo, Video newVideo){
        return mergeData(
                FXCollections.observableArrayList(),
                oldVideo,
                FXCollections.observableArrayList(),
                newVideo
        );
    }

    public Video mergeData(ObservableList<TableEntry> oldFrames, Video oldVideo, ObservableList<TableEntry> newFrames, Video newVideo) {
        var oldSelectedEntries = oldFrames.stream().filter(e -> e.selectedProperty().get()).toList();
        var newSelectedEntries = newFrames.stream().filter(e -> e.selectedProperty().get()).toList();

        if(oldSelectedEntries.isEmpty() && newSelectedEntries.isEmpty())
            return saveMergedData(new ArrayList<>(),oldVideo,newVideo);

        if(oldSelectedEntries.isEmpty())
            return saveMergedData(
                    getFilteredFrames(newVideo,newSelectedEntries),
                    oldVideo,
                    newVideo
            );

        if(newSelectedEntries.isEmpty())
            return saveMergedData(
                    getFilteredFrames(oldVideo,oldSelectedEntries),
                    oldVideo,
                    newVideo
            );

        var unifiedFrameList = getUnifiedFrameList(
                doesOldHaveMoreFrames(oldVideo,newVideo) ? oldVideo.getTotalFrames() : newVideo.getTotalFrames(),
                getFilteredFrames(oldVideo,oldSelectedEntries),
                getFilteredFrames(newVideo,newSelectedEntries)
        );

        return saveMergedData(unifiedFrameList,oldVideo,newVideo);
    }

    private List<Frame> getFilteredFrames(Video video,List<TableEntry> entries){
        return frameService.getAllByVideo(video).stream()
                .filter(f -> {
                    for(var e : entries)
                        if(e.getId() == f.getId())
                            return true;

                    return false;
                })
                .toList();
    }

    private List<Frame> getUnifiedFrameList(int total, List<Frame> oldFrames, List<Frame> newFrames){
        Map<Integer,Set<Tag>> frameData = new HashMap<>();

        for (int i = 0; i < total; i++) {
            int finalI = i;
            var oldFrame = oldFrames.stream().filter(f -> f.getFrameNumber() == finalI).findFirst().orElse(null);
            var newFrame = newFrames.stream().filter(f -> f.getFrameNumber() == finalI).findFirst().orElse(null);

            if(oldFrame != null && newFrame != null){
                Set<Tag> tags = new HashSet<>();
                tags.addAll(oldFrame.getTags());
                tags.addAll(newFrame.getTags());
                frameData.put(i,tags);
            }else if(oldFrame != null){
                frameData.put(i,new HashSet<>(oldFrame.getTags()));
            }else if(newFrame != null){
                frameData.put(i,new HashSet<>(newFrame.getTags()));
            }
        }

        return frameData.entrySet().stream().map(e -> Frame.builder()
                .frameNumber(e.getKey())
                .tags(new ArrayList<>(e.getValue()))
                .build()).toList();
    }

    private Video saveMergedData(List<Frame> frames, Video oldVideo, Video newVideo){
        if(newVideo.getId() != -1)
            videoService.deleteVideo(newVideo.getId());

        videoService.deleteVideo(oldVideo.getId());

        newVideo.setId(oldVideo.getId());

        newVideo.setFrames(frames);

        var finalVideo = videoService.saveVideo(newVideo);
        frames.forEach(f -> {
            f.setVideo(finalVideo);
            frameService.save(f);
        });

        return finalVideo;
    }

    public boolean getMergeConfirmation(){
        return FXDialogProvider.customDialog(
                Dictionary.get("fv.final-warning"),
                0,
                Dictionary.get("cancel"),
                Dictionary.get("finish")
        ) == 1;
    }
}
