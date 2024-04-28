package com.example.engineer.FrameProcessor;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.IntStream;


//TODO send and wait instead of sleeping
//TODO send path where to cache - currently when in exe images are cached in User
@Component
public class FrameCache implements ApplicationContextAware {
    private Deque<BufferedImage> cache = new ArrayDeque<>();
    private Deque<Integer> indexCache = new ArrayDeque<>();

    private Map<Integer,Boolean> loadMap = new HashMap<>();

    private String DIR = "cache";
    File videoFile;
    private String fileName;
    private static ApplicationContext ctx;
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        ctx = applicationContext;
    }

    public void move(int currentIndex, int newIndex){


        if (currentIndex > newIndex) { //move left
            cache.addFirst(loadImage(newIndex));
            cache.removeLast();

            indexCache.addFirst(newIndex);
            indexCache.removeLast();
        } else { //move right
            if(currentIndex>=4){
                cache.addLast(loadImage(newIndex));
                cache.removeFirst();

                indexCache.addLast(newIndex);
                indexCache.removeFirst();
            }

            if(currentIndex % 100 > 50 & !loadMap.containsKey((currentIndex+100)/100)) {
                ctx.getBean(FrameProcessorClient.class).send("1;"+(currentIndex+100)/100+";0",false);
                loadMap.put((100+currentIndex)/100,true);
            }
        }
    }

    public void jump(int newIndex){

    }

    public BufferedImage loadImage(int index){
        String path = DIR +
                File.separator +
                fileName +
                File.separator +
                index +
                ".jpg";

        BufferedImage img = null;

        try{
            //check if file exists
            File file = new File(path);

            img = ImageIO.read(file);
        }catch (Exception e){
            e.printStackTrace();
        }

        return img;
    }

    public void firstLoad(File file){
        this.loadMap = new HashMap<>();
        this.videoFile = file;

        String path = DIR + File.separator + fileName;
        try{
            Files.createDirectories(Paths.get(path));
        }catch (Exception e){
            e.printStackTrace();
        }

        ctx.getBean(FrameProcessorClient.class).send("0;0;"+file.getAbsolutePath(),true);

        /*try {
            Thread.sleep(10000);
        } catch (Exception e) {
            e.printStackTrace();
        }*/

        loadMap.put(0, true);


        for (int i = 0; i <= 7; i++)
            try{
                File img = new File(path + File.separator + i + ".jpg");
                BufferedImage bImg = ImageIO.read(img);
                cache.addLast(bImg);
                indexCache.addLast(i);
            }catch (Exception e){
                e.printStackTrace();
            }
    }

    public void setFileName(File file) {
        this.fileName = file.getName();
    }

    public BufferedImage getCurrentFrame(Integer index){
        List<BufferedImage> copy = List.copyOf(cache);
        List<Integer> intCopy = List.copyOf(indexCache);

        return copy.get(intCopy.indexOf(index));
    }
}
