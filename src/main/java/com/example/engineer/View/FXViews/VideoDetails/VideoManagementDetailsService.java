package com.example.engineer.View.FXViews.VideoDetails;

import com.example.engineer.FrameProcessor.FrameProcessorRequestManager;
import com.example.engineer.Model.Frame;
import com.example.engineer.Model.Tag;
import com.example.engineer.Model.Video;
import com.example.engineer.Service.FrameService;
import com.example.engineer.Service.VideoService;
import com.example.engineer.View.Elements.FXElementsProviders.FXDialogProvider;
import com.example.engineer.View.Elements.FXElementsProviders.FXRestartResolver;
import com.example.engineer.View.Elements.FXElementsProviders.FileChooserProvider;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.stream.IntStream;

@Service
public class VideoManagementDetailsService {
    @Autowired
    private VideoService videoService;
    @Autowired
    private FrameService frameService;
    @Autowired
    private FrameProcessorRequestManager requestManager;


    public void changePath(int id, Stage stage) {
        try{
            var pathOfNewFile = FileChooserProvider.videoFileChooser(stage);
            var oldVideo = videoService.getById(id);

            //check if both have same amount of frames
            if(hasSameAmountOfFrames(pathOfNewFile, oldVideo))
                if(!FXDialogProvider.YesNoDialog("VIDEOS HAVE DIFFERENT AMOUNT OF FRAMES"))
                    return;


            if(videoService.exists(pathOfNewFile)){//if new path exists in DB
                var existitngVideo = videoService.getByPath(pathOfNewFile);

                if(!frameService.getAllByVideo(existitngVideo).isEmpty()){//if existing video has data
                    switch(FXDialogProvider.customDialog(
                            "Action required",
                            0,
                            "Cancel",
                            "Discard old",
                            "Merge",
                            "discard new"
                    )){
                        case 0:
                            FXDialogProvider.messageDialog("CANCELLED");
                            return;
                        case 1:
                            if(FXDialogProvider.YesNoDialog("DISCARD OLD?")) {
                                videoService.deleteVideo(oldVideo.getId());

                                var updated = updateVideo(existitngVideo,oldVideo);
                                if(updated.getTotalFrames() < oldVideo.getTotalFrames()){
                                    var framesOverLimit = frameService.getAllByVideo(oldVideo).stream()
                                            .filter(f -> f.getFrameNumber() > updated.getTotalFrames())
                                            .map(Frame::getId)
                                            .toList();
                                    frameService.removeFramesOverLimit(updated.getId(), updated.getTotalFrames(), framesOverLimit);
                                }

                            }else{
                                FXDialogProvider.messageDialog("CANCELLED");
                                return;
                            }
                            break;
                        case 2:
                            if(FXDialogProvider.YesNoDialog("MERGE??")) {
                                mergeVideo(oldVideo,existitngVideo);
                            }else{
                                FXDialogProvider.messageDialog("CANCELLED");
                                return;
                            }
                            break;
                        case 3:
                            if(FXDialogProvider.YesNoDialog("DISCARD NEW?")) {
                                videoService.deleteVideo(existitngVideo.getId());

                                var updated = updateVideo(oldVideo,existitngVideo);
                                if(updated.getTotalFrames() < oldVideo.getTotalFrames()){
                                    var framesOverLimit = frameService.getAllByVideo(oldVideo).stream()
                                            .filter(f -> f.getFrameNumber() > updated.getTotalFrames())
                                            .map(Frame::getId)
                                            .toList();
                                    frameService.removeFramesOverLimit(updated.getId(), updated.getTotalFrames(), framesOverLimit);
                                }

                            }else{
                                FXDialogProvider.messageDialog("CANCELLED");
                                return;
                            }
                            break;
                    }
                }else{//if existing video doesn't have any data
                    //check if frame amount is equal
                    if(hasSameAmountOfFrames(existitngVideo, oldVideo)){
                        videoService.deleteVideo(existitngVideo);
                        var newPathVideo = updateVideo(existitngVideo, oldVideo);
                    }

                    if(hasMoreFrames(existitngVideo, oldVideo)){//new has more frames
                        if(!FXDialogProvider.YesNoDialog("New has more frames. Continue?"))
                            return;

                        reassignFrames(existitngVideo,oldVideo,false);
                    }else{//new has fewer frames
                        if(!FXDialogProvider.YesNoDialog("New has fewer frames. Continue?"))
                            return;

                        reassignFrames(existitngVideo,oldVideo,true);
                    }
                }

            }else{//if new path doesn't exist in DB
                //TODO: check for different length;
                var data = requestManager.getVideoData().split(";");
                var newPathVideo = Video.builder()
                        .id(oldVideo.getId())
                        .path(pathOfNewFile)
                        .name(new File(pathOfNewFile).getName())
                        .totalFrames(Integer.parseInt(data[0]))
                        .frameRate(Double.parseDouble(data[3]))
                        .duration(Integer.parseInt(data[4])/1000d)
                        .videoHeight(Integer.parseInt(data[1]))
                        .videoWidth(Integer.parseInt(data[2]))
                        .build();
                videoService.saveVideo(newPathVideo);
            }

            FXRestartResolver.reset();
        }catch (Exception e){
            FXDialogProvider.errorDialog(e.getMessage());
        }
    }

    private boolean hasSameAmountOfFrames(String newPath, Video oldVideo){
        if(isGif(newPath)){
            try(ImageInputStream stream = ImageIO.createImageInputStream(newPath)){
                Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName("gif");
                if(readers.hasNext()){
                    ImageReader reader = readers.next();
                    reader.setInput(stream);

                    return reader.getNumImages(true) == oldVideo.getTotalFrames();
                }

                return false;
            }catch (Exception e){
                FXDialogProvider.errorDialog(e.getMessage());
                return false;
            }
        }else{
            var newVideo = videoService.getByPath(newPath);
            if(newVideo != null)
                return newVideo.getTotalFrames().intValue() == oldVideo.getTotalFrames().intValue();
            else{
                var frameAmount = requestManager.getVideoData(newPath).split(";")[0];
                return Integer.parseInt(frameAmount) == oldVideo.getTotalFrames();
            }
        }
    }

    private boolean hasSameAmountOfFrames(Video newVideo, Video oldVideo){
        return newVideo.getTotalFrames().intValue() == oldVideo.getTotalFrames().intValue();
    }

    private boolean hasMoreFrames(Video newVideo, Video oldVideo){
        return newVideo.getTotalFrames() < oldVideo.getTotalFrames();//true if left is bigger
    }

    private boolean isGif(String path){
        return path.endsWith(".gif");
    }

    private void reassignFrames(Video newVideo, Video oldVideo, boolean requiresAction){
        frameService.reassignFrames(newVideo.getId(), oldVideo.getId());

        if(requiresAction){
            var framesOverLimit = frameService.getAllByVideo(newVideo).stream()
                    .filter(f -> f.getFrameNumber() > newVideo.getTotalFrames())
                    .map(Frame::getId)
                    .toList();

            frameService.removeFramesOverLimit(oldVideo.getId(), oldVideo.getTotalFrames(), framesOverLimit);
        }
    }

    private Video updateVideo(Video newVideo, Video oldVideo){
        oldVideo.setPath(newVideo.getPath());
        oldVideo.setName(newVideo.getName());
        oldVideo.setTotalFrames(newVideo.getTotalFrames());
        oldVideo.setFrameRate(newVideo.getFrameRate());
        oldVideo.setDuration(newVideo.getDuration());
        oldVideo.setVideoHeight(newVideo.getVideoHeight());
        oldVideo.setVideoWidth(newVideo.getVideoWidth());

        return videoService.saveVideo(oldVideo);
    }

    private Video mergeVideo(Video oldVIdeo, Video newVideo){
        var framesOnOldVideo = frameService.getAllByVideo(oldVIdeo);
        var framesOnNewVideo = frameService.getAllByVideo(newVideo);

        //map of frame number and a pair? of Frames
        var frameMap = new HashMap<Integer,Frame[]>();

        //get pairs
        for (int i = 0; i < (oldVIdeo.getTotalFrames() > newVideo.getTotalFrames() ? oldVIdeo.getTotalFrames() : newVideo.getTotalFrames()); i++) {
            var frames = new Frame[2];
            int finalI = i;

            frames[0] = framesOnOldVideo.stream().filter(f -> f.getFrameNumber() == finalI).findFirst().orElse(null);
            frames[1] = framesOnNewVideo.stream().filter(f -> f.getFrameNumber() == finalI).findFirst().orElse(null);

            frameMap.put(finalI, frames);
        }

        var frameList = new ArrayList<Frame>();
        for (int i = 0; i < frameMap.size(); i++) {
            var tagSet = new HashSet<Tag>();

            if(frameMap.get(i)[0] != null && !frameMap.get(i)[0].getTags().isEmpty())
                tagSet.addAll(frameMap.get(i)[0].getTags());

            if(frameMap.get(i)[1] != null && !frameMap.get(i)[1].getTags().isEmpty())
                tagSet.addAll(frameMap.get(i)[1].getTags());

            if(!tagSet.isEmpty())
                frameList.add(
                    Frame.builder()
                        .frameNumber(i)
                        .tags(tagSet.stream().toList())
                        .build()
                );
        }

        videoService.deleteVideo(oldVIdeo.getId());
        videoService.deleteVideo(newVideo.getId());

        if(oldVIdeo.getTotalFrames() > newVideo.getTotalFrames()){
            var lastIndex = IntStream.range(0,frameList.size())
                    .filter(i -> i > newVideo.getTotalFrames()-1)
                    .findFirst()
                    .orElse(-1);

            if(lastIndex != -1)
                frameList = new ArrayList<>(frameList.subList(0, lastIndex));
        }

        var mergedVideo = videoService.saveVideo(Video.builder()
                .path(newVideo.getPath())
                .name(newVideo.getName())
                .totalFrames(newVideo.getTotalFrames())
                .frameRate(newVideo.getFrameRate())
                .duration(newVideo.getDuration())
                .videoHeight(newVideo.getVideoHeight())
                .videoWidth(newVideo.getVideoWidth())
                .frames(frameList)
                .build());

        for(var frame: frameList){
            frame.setVideo(mergedVideo);
            frameService.save(frame);
        }

        return mergedVideo;
    }
}
