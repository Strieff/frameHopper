package com.example.engineer.View.FXViews.MainView;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;
import java.io.File;

@Component
public class TestService {
    private FFmpegFrameGrabber grabber;
    private Java2DFrameConverter converter = new Java2DFrameConverter();
    private int currentFrameIndex = 0;
    private Canvas canvas;

    private int totalFrames;
    private double frameRate;
    private int width, height;
    private double duration;

    public void onInit(File file, Canvas canvas){
        this.canvas = canvas;
        loadVideo(file);

        Platform.runLater(() -> {
            canvas.setWidth(width);
            canvas.setHeight(height);

            System.out.printf("Loaded: %s\nFrames: %d, FPS: %.2f, Duration: %.2fs, Resolution: %dx%d%n",
                    file.getName(), totalFrames, frameRate, duration, width, height);

            displayFrame(currentFrameIndex);
        });
    }

    private void loadVideo(File file){
        try{
            grabber = new FFmpegFrameGrabber(file);
            grabber.start();

            frameRate = grabber.getFrameRate();
            totalFrames = grabber.getLengthInFrames();
            width = grabber.getImageWidth();
            height = grabber.getImageHeight();
            duration = grabber.getLengthInTime() / 1_000_000.0;
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public void displayFrame(int index) {
        try {
            grabber.setFrameNumber(0); // Ensure we start decoding from the beginning
            grabber.flush(); // Clear buffer
            Frame frame = null;

            // Read frames until we reach the target index
            for (int i = 0; i <= index; i++) {
                frame = grabber.grabImage();
                if (frame == null) break; // Stop if we run out of frames
            }

            if (frame != null) {
                BufferedImage image = converter.convert(frame);
                if (image != null) {
                    Image fxImage = SwingFXUtils.toFXImage(image, null);
                    drawImage(fxImage);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void moveLeft(){
        if(currentFrameIndex > 0) {
            currentFrameIndex--;
            displayFrame(currentFrameIndex);
        }
    }

    public void moveRight(){
        if(currentFrameIndex < totalFrames - 1) {
            currentFrameIndex++;
            displayFrame(currentFrameIndex);
        }
    }

    private void drawImage(Image image) {
        Platform.runLater(() -> {
            GraphicsContext gc = canvas.getGraphicsContext2D();
            gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
            gc.drawImage(image, 0, 0, canvas.getWidth(), canvas.getHeight());
        });
    }
}
