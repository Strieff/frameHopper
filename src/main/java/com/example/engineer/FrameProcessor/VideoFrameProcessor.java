package com.example.engineer.FrameProcessor;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.ImageView;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;

import java.awt.image.BufferedImage;
import java.io.File;

public class VideoFrameProcessor extends FrameProcessor {

    public VideoFrameProcessor() {
        super();
    }

    @Override
    public ImageView getFrame(int index, int frameHeight, int frameWidth) {
        var scaleFactor = getScaleFactor(frameHeight, frameWidth);

        //calculate target dimensions
        int targetWidth = (int) (scaleFactor * info.getWidth());
        int targetHeight = (int) (scaleFactor * info.getHeight());

        //get frame
        var frame = getRawFrame(index);

        if(frame == null) {
            frame = getRawFrame(--index);
        }

        BufferedImage image = converter.convert(frame);
        var scaledImage = drawImage(image, targetWidth, targetHeight);

        var imgView = new ImageView(SwingFXUtils.toFXImage(scaledImage,null));
        imgView.setPreserveRatio(true);

        return imgView;
    }

    private Frame getRawFrame(int index){
        try{
            grabber.setFrameNumber(0); // Ensure we start decoding from the beginning
            grabber.flush(); // Clear buffer
            Frame frame = null;

            // Read frames until we reach the target index
            for (int i = 0; i <= index; i++) {
                frame = grabber.grabImage();
                if (frame == null) break; // Stop if we run out of frames
            }

            return frame;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
