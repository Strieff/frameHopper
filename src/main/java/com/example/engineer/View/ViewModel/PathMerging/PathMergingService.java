package com.example.engineer.View.ViewModel.PathMerging;

import com.example.engineer.FrameProcessor.FrameProcessorRequestManager;
import com.example.engineer.Model.Frame;
import com.example.engineer.Model.Tag;
import com.example.engineer.Model.Video;
import com.example.engineer.Service.FrameService;
import com.example.engineer.Service.VideoService;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.stream.IntStream;

@Component
public class PathMergingService {
    @Autowired
    private FrameProcessorRequestManager requestManager;
    @Autowired
    private FrameService frameService;
    @Autowired
    private VideoService videoService;

    //check if file is video
    private boolean isVideo(String newVideo){
        try{
            return new Tika().detect(new File(newVideo)).startsWith("video");
        }catch (Exception e){
            return false;
        }
    }

    //check if file is gif
    private boolean isGif(String path){
        return path.endsWith(".gif");
    }

    //check if file is valid file format
    public boolean isValidFile(String path){
        return isGif(path) || isVideo(path);
    }

    //check if frame amount is the same
    public boolean hasSameAmountOfFrames(File newVideo,Video oldVideo){
        if(isGif(newVideo.getAbsolutePath())){
            try {
                ImageInputStream stream = ImageIO.createImageInputStream(newVideo);

                Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName("gif");
                if (readers.hasNext()) {
                    ImageReader reader = readers.next();
                    reader.setInput(stream);

                    return reader.getNumImages(true) == oldVideo.getTotalFrames();
                }

                return false;
            } catch (Exception e) {
                return false;
            }
        }else{
            var data = getVideoData(newVideo);
            return Integer.parseInt(data[0]) == oldVideo.getTotalFrames();

        }
    }

    //check if frame amount is the same
    public boolean hasSameAmountOfFrames(Video oldVideo,Video newVideo){
        return oldVideo.getTotalFrames().intValue() == newVideo.getTotalFrames().intValue();
    }

    //check if there exists an entry in DB with the path
    public boolean checkIfExistsInDatabase(String path){
        return videoService.getByPath(path) != null;
    }

    //check if the entry of the new path has any data
    public boolean checkIfNewHasData(Video video){
        return !frameService.getAllByVideo(video).isEmpty();
    }

    //check if the video of the new path has more (true) or less (false) frames
    public boolean checkIfNewHasMoreFrames(Video newVideo, Video oldVideo){
        return newVideo.getTotalFrames() > oldVideo.getTotalFrames();
    }

    //change parameters of the video entry
    public Video updateVideo(Video video,String newVideoPath){
        var data = getVideoData(new File(video.getPath()));
        video.setPath(newVideoPath);
        video.setName(new File(newVideoPath).getName());
        video.setTotalFrames(Integer.parseInt(data[0]));
        video.setFrameRate(Double.parseDouble(data[3]));
        video.setDuration(Integer.parseInt(data[4])/1000d);
        video.setVideoHeight(Integer.parseInt(data[1]));
        video.setVideoWidth(Integer.parseInt(data[2]));

        return videoService.saveVideo(video);
    }

    //change parameters of video entry with the entry of the existing video
    public Video updateVideo(Video oldVideo,Video newVideo){
        oldVideo.setPath(newVideo.getPath());
        oldVideo.setName(newVideo.getName());
        oldVideo.setTotalFrames(newVideo.getTotalFrames());
        oldVideo.setFrameRate(newVideo.getFrameRate());
        oldVideo.setDuration(newVideo.getDuration());
        oldVideo.setVideoHeight(newVideo.getVideoHeight());
        oldVideo.setVideoWidth(newVideo.getVideoWidth());

        return videoService.saveVideo(oldVideo);
    }

    //reassign frames to new video
    //true for new video being shorter
    public void reassignFrames(Video oldVideo,Video newVideo,boolean requiresAction){
        frameService.reassignFrames(newVideo.getId(), oldVideo.getId());

        if(requiresAction){
            var framesOverLimit = frameService.getAllByVideo(newVideo).stream()
                    .filter(f -> f.getFrameNumber()> newVideo.getTotalFrames())
                    .map(Frame::getId)
                    .toList();

            frameService.removeFramesOverLimit(oldVideo.getId(), oldVideo.getTotalFrames(), framesOverLimit);
        }
    }

    //remove frame data and tag associations and a video
    public Video removeVideo(Video keep,Video remove){
        videoService.deleteVideo(remove.getId());

        var updated = updateVideo(keep,remove);
        if(updated.getTotalFrames()<remove.getTotalFrames()) {
            var framesOverLimit = frameService.getAllByVideo(remove).stream()
                    .filter(f -> f.getFrameNumber() > updated.getTotalFrames())
                    .map(Frame::getId)
                    .toList();
            frameService.removeFramesOverLimit(updated.getId(), updated.getTotalFrames(), framesOverLimit);
        }

        return updated;
    }

    //merge video data
    public Video mergeVideos(Video oldVideo,Video newVideo){
        var framesOfOldVideo = frameService.getAllByVideo(oldVideo);
        var framesOfNewVideo = frameService.getAllByVideo(newVideo);

        //map of frame number and a pair? of frame Ids
        var frameMap = new HashMap<Integer,Frame[]>();

        for (int i = 0; i < (oldVideo.getTotalFrames() > newVideo.getTotalFrames() ? oldVideo.getTotalFrames() : newVideo.getTotalFrames()); i++) {
            var frameIds = new Frame[2];
            int finalI = i;

            frameIds[0] = framesOfOldVideo.stream().filter(f -> f.getFrameNumber() == finalI).findFirst().orElse(null);
            frameIds[1] = framesOfNewVideo.stream().filter(f -> f.getFrameNumber() == finalI).findFirst().orElse(null);

            frameMap.put(i,frameIds);
        }

        var frameList = new ArrayList<Frame>();
        for (int i = 0; i < frameMap.size(); i++) {
            var tagSet = new HashSet<Tag>();

            if(frameMap.get(i)[0] != null && !frameMap.get(i)[0].getTags().isEmpty())
                tagSet.addAll(frameMap.get(i)[0].getTags());

            if(frameMap.get(i)[1] != null && !frameMap.get(i)[1].getTags().isEmpty())
                tagSet.addAll(frameMap.get(i)[1].getTags());

            if(!tagSet.isEmpty())
                frameList.add(Frame.builder()
                        .frameNumber(i)
                        .tags(tagSet.stream().toList())
                        .build());
        }

        videoService.deleteVideo(oldVideo.getId());
        videoService.deleteVideo(newVideo.getId());

        if(oldVideo.getTotalFrames() > newVideo.getTotalFrames()) {
            var lastIndex = IntStream.range(0, frameList.size())
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

        for(var frame : frameList) {
            frame.setVideo(mergedVideo);
            frameService.save(frame);
        }

        return mergedVideo;
    }

    //get data of the new video
    public String[] getVideoData(File file){
        return requestManager.getVideoData(file.getAbsolutePath()).split(";");
    }
}
