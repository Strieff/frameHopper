package com.example.engineer.FrameProcessor;

import com.example.engineer.View.Elements.FXElementsProviders.ViablePathProvider;
import javafx.scene.image.ImageView;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.io.FilenameUtils;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public abstract class FrameProcessor{
    @Getter
    private static FrameProcessor instance;

    @Getter
    @Setter
    protected int currentFrame = 0;
    @Getter
    protected InformationContainer info;
    protected FFmpegFrameGrabber grabber;
    protected Java2DFrameConverter converter = new Java2DFrameConverter();

    public FrameProcessor() {
        instance = this;
    }

    public static FrameProcessor getFrameProcessor(String extension) {
        if (extension.equals("gif"))
            return new GifFrameProcessor();

        return new VideoFrameProcessor();
    }

    public static InformationContainer getTempData(File file){
        FFmpegFrameGrabber grabber;

        try{
            grabber = new FFmpegFrameGrabber(ViablePathProvider.getFile(file));
            grabber.setFormat(FilenameUtils.getExtension(file.getAbsolutePath()));
        }catch (Exception e){
            try {
                grabber = new FFmpegFrameGrabber(ViablePathProvider.getFallbackFile(file));
                grabber.setFormat(FilenameUtils.getExtension(file.getAbsolutePath()));
                grabber.start();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }

        var data = new InformationContainer(grabber);
        try {
            grabber.close();
            grabber.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    public void loadVideo(File file) {
        try{
            grabber = new FFmpegFrameGrabber(ViablePathProvider.getFile(file));
            grabber.setFormat(FilenameUtils.getExtension(file.getAbsolutePath()));
            try{
                grabber.start();
            }catch (Exception e){
                close();
                grabber = new FFmpegFrameGrabber(ViablePathProvider.getFallbackFile(file));
                grabber.setFormat(FilenameUtils.getExtension(file.getAbsolutePath()));
                grabber.start();
            }



            loadMetadata();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public abstract ImageView getFrame(int index, int frameHeight, int frameWidth);

    protected double getScaleFactor(int frameHeight, int frameWidth){
        //get dimension of the application window

        //calculate scale factor for proportions
        double scaleWidth = (double) frameWidth / info.getWidth();
        double scaleHeight = (double) frameHeight / info.getHeight();
        return Math.min(scaleWidth,scaleHeight);
    }

    protected BufferedImage drawImage(BufferedImage originalImage, int targetWidth, int targetHeight){
        BufferedImage scaledImage = new BufferedImage(targetWidth,targetHeight,BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = scaledImage.createGraphics();
        g.drawImage(originalImage,0,0,targetWidth,targetHeight,null);
        g.dispose();
        return scaledImage;
    }

    protected void loadMetadata(){
        info = new InformationContainer(grabber);
    }

    public void close(){
        try {
            grabber.close();
            grabber.release();
        } catch (FrameGrabber.Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void onClose(){
        if(instance != null)
            instance.close();
    }

    @Getter
    public static class InformationContainer {
        private final double framerate;
        private final int totalFrames;
        private final double duration;
        private final int width;
        private final int height;

        public InformationContainer(FFmpegFrameGrabber grabber){
            framerate = grabber.getFrameRate();
            totalFrames = grabber.getLengthInFrames();
            width = grabber.getImageWidth();
            height = grabber.getImageHeight();
            duration = grabber.getLengthInTime() / 1_000_000.0;
        }
    }
}
