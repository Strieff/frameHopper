package com.example.engineer.FrameProcessor;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.ImageView;

import java.awt.image.BufferedImage;

public class GifFrameProcessor extends FrameProcessor {

    public GifFrameProcessor() {
        super();
    }

    @Override
    public ImageView getFrame(int index, int frameHeight, int frameWidth) {
        try {
            var scaleFactor = getScaleFactor(frameHeight, frameWidth);

            //calculate target dimensions
            int targetWidth = (int) (scaleFactor * info.getWidth());
            int targetHeight = (int) (scaleFactor * info.getHeight());

            grabber.setFrameNumber(index);

            var frame = grabber.grabImage();

            if(frame == null) {
                grabber.setFrameNumber(--index);
                frame = grabber.grabImage();
            }

            BufferedImage image = converter.convert(frame);
            var scaledImage = drawImage(image, targetWidth, targetHeight);

            var imgView = new ImageView(SwingFXUtils.toFXImage(scaledImage,null));
            imgView.setPreserveRatio(true);

            return imgView;

        }catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
