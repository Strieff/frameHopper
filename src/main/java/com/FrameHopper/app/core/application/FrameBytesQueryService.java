package com.FrameHopper.app.core.application;

import com.FrameHopper.app.boundry.dto.VideoDTO;
import com.FrameHopper.app.boundry.mappers.BoundaryVideoMapper;
import com.FrameHopper.app.core.ports.in.FrameBytesQuery;
import com.FrameHopper.app.core.ports.out.FfmpegPort;
import lombok.RequiredArgsConstructor;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

@RequiredArgsConstructor
public class FrameBytesQueryService implements FrameBytesQuery {
    private final FfmpegPort ffmpegPort;

    @Override
    public byte[] getVideoFrame(VideoDTO video, int index) throws IOException, InterruptedException {
        var coreVideo = BoundaryVideoMapper.toDomain(video);

        if (!new File(video.path()).exists())
            throw new IllegalArgumentException("Video not found!");

        if (index < 0)
            throw new IllegalArgumentException("Invalid index!");

        return ffmpegPort.getFrameBytes(coreVideo, index);
    }

    @Override
    public byte[] rotateFrame(VideoDTO video, int index, int angle) throws IOException, InterruptedException {
        var img = getImage(getVideoFrame(video, index));

        int normalizedRotation = (((angle % 360) + 360) % 360 + 45) / 90 * 90 % 360;

        int w = img.getWidth();
        int h = img.getHeight();

        int newW = (normalizedRotation % 180 == 0) ? w : h;
        int newH = (normalizedRotation % 180 == 0) ? h : w;

        BufferedImage result = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = result.createGraphics();

        switch (normalizedRotation) {
            case 90 -> {
                g.translate(newW, 0);
                g.rotate(Math.toRadians(90));
            }
            case 180 -> {
                g.translate(newW, newH);
                g.rotate(Math.toRadians(180));
            }
            case 270 -> {
                g.translate(0, newH);
                g.rotate(Math.toRadians(270));
            }
        }

        g.drawImage(img, 0, 0, null);
        g.dispose();

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(result, "png", baos);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] flipFrame(VideoDTO video, int index, boolean horizontal) throws IOException, InterruptedException {
        var img = getImage(getVideoFrame(video, index));
        var tx = new AffineTransform();

        if(horizontal) {
            tx.scale(-1, 1);
            tx.translate(-img.getWidth(), 0);
        } else {
            tx.scale(1, -1);
            tx.translate(0, -img.getHeight());
        }

        var op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
        var result = op.filter(img, null);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(result, "png", baos);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private BufferedImage getImage(byte[] bytes) {
        try {
            return ImageIO.read(new ByteArrayInputStream(bytes));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
