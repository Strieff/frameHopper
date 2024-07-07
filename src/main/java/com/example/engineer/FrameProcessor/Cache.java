package com.example.engineer.FrameProcessor;

import com.example.engineer.Model.Video;
import com.example.engineer.Service.VideoService;
import lombok.Getter;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.LinkedBlockingDeque;

public class Cache {
    protected final LinkedBlockingDeque<BufferedImage> cache = new LinkedBlockingDeque<>();

    protected static GifCache GIF_CACHE;
    protected static FrameCache FRAME_CACHE;

    //video metadata
    @Getter
    protected int maxFrameIndex;
    @Getter
    protected double frameRate;
    @Getter
    protected double duration;
    @Getter
    protected int height;
    @Getter
    protected int width;

    public static Cache getCache(String type){
        if(type.endsWith(".gif"))
            return GIF_CACHE;
        else
            return FRAME_CACHE;
    }

    public void firstLoad(File file){}

    public BufferedImage getCurrentFrame(Integer index){
        return null;
    }

    public void move(int currentIndex, int newIndex){}

    public void jump(int newIndex){}

    public void setUpVideoMetadata(VideoService videoService, Video video){}
}
