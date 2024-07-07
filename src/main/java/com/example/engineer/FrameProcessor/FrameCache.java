package com.example.engineer.FrameProcessor;

import com.example.engineer.Model.Video;
import com.example.engineer.Service.VideoService;
import jakarta.annotation.PostConstruct;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingDeque;

@Component
public class FrameCache extends Cache{
    private static int CACHE_SIZE = 7;

    @Autowired
    FrameProcessorRequestManager requestManager;

    private final LinkedBlockingDeque<Integer> indexCache = new LinkedBlockingDeque<>();
    private Map<Integer,Boolean> loadMap;

    @Setter
    private String dir;

    @PostConstruct
    private void init(){
        FRAME_CACHE = this;
    }

    @Override
    public void move(int currentIndex, int newIndex){
        if(currentIndex > newIndex){ //move left
            cache.addFirst(loadImage(newIndex));
            cache.removeLast();

            indexCache.addFirst(newIndex);
            indexCache.removeLast();
        }else{ //move right
            if(currentIndex>=(int)Math.ceil(CACHE_SIZE/2f)){
                cache.addLast(loadImage(newIndex));
                cache.removeFirst();

                indexCache.addLast(newIndex);
                indexCache.removeFirst();
            }

            //get next set (100) after crossing 50th frame
            if(currentIndex % 100 > 50 && !loadMap.containsKey((currentIndex+100)/100)){
                requestManager.loadNthSet((currentIndex+100)/100,false);
                loadMap.put((currentIndex+100)/100,true);
            }
        }
    }

    @Override
    public void jump(int newIndex){
        cache.clear();
        indexCache.clear();
        String path = dir + File.separator + "{temp}.jpg";

        //load set if not loaded
        if(!loadMap.containsKey(newIndex/100)){
            requestManager.loadNthSet(newIndex/100,true);
            loadMap.put(newIndex/100,true);
        }

        //load previous set if not loaded
        if(newIndex/100 != 0 && !loadMap.containsKey(newIndex/100-1)){
            requestManager.loadNthSet(newIndex/100-1,true);
            loadMap.put(newIndex/100-1,true);
        }

        for (int i = 0; i < CACHE_SIZE; i++) {
            int index = newIndex + i + (newIndex < 4 ? 0 : -1 * (int)Math.floor(CACHE_SIZE/2f));

            File img = new File(path.replace("{temp}", String.valueOf(index)));
            if(img.exists()){
                try{
                    BufferedImage bImg = ImageIO.read(img);
                    cache.addFirst(bImg);
                    indexCache.addFirst(index);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void firstLoad(File file){
        loadMap = new HashMap<>();
        cache.clear();
        indexCache.clear();

        dir = Paths.get("cache").toAbsolutePath() + File.separator + requestManager.loadFirsSet(file.getAbsolutePath());
        dir = Paths.get(dir).toAbsolutePath().toString();
        System.out.println(dir);
        loadMap.put(0,true);

        for (int i = 0; i < CACHE_SIZE; i++) {
            try{
                String imgPath = dir + File.separator + i + ".jpg";
                System.out.println(imgPath);
                File img = new File(imgPath);
                BufferedImage bImg = ImageIO.read(img);
                cache.addLast(bImg);
                indexCache.add(i);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private BufferedImage loadImage(int index){
        String path = dir + File.separator + index + ".jpg";
        BufferedImage image = null;

        try{
            image = ImageIO.read(new File(path));
        }catch(Exception e){
            e.printStackTrace();
        }

        return image;
    }

    @Override
    public BufferedImage getCurrentFrame(Integer index){
        List<BufferedImage> cacheCopy = List.copyOf(cache);
        List<Integer> indexCacheCopy = List.copyOf(indexCache);

        return cacheCopy.get(indexCacheCopy.indexOf(index));
    }

    @Override
    public void setUpVideoMetadata(VideoService videoService, Video video){
        if(video.getTotalFrames() == null) {
            String[] data = requestManager.getVideoData().split(";");
            maxFrameIndex = Integer.parseInt(data[0]);
            height = Integer.parseInt(data[1]);
            width = Integer.parseInt(data[2]);
            frameRate = Double.parseDouble(data[3]);
            duration = Integer.parseInt(data[4]) / 1000d;

            videoService.addVideoData(video,maxFrameIndex,frameRate,duration,height,width);
        }else {
            maxFrameIndex = video.getTotalFrames();
            height = video.getVideoHeight();
            width = video.getVideoWidth();
            frameRate = video.getFrameRate();
            duration = video.getDuration();
        }
    }
}
