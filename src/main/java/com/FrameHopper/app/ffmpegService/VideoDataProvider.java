package com.FrameHopper.app.ffmpegService;

import com.FrameHopper.app.Model.Video;
import javafx.scene.image.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class VideoDataProvider {
    private final static int PRE_LOADING_AMOUNT = 20;
    private final Logger logger = LoggerFactory.getLogger(VideoDataProvider.class);

    private final ExecutorService prefetchExec = Executors.newSingleThreadExecutor();
    private final Set<Integer> inFlight = ConcurrentHashMap.newKeySet();


    private final FfmpegService ffmpegService;
    private final FrameCache cache;

    public VideoDataProvider(FfmpegService ffmpegService, FrameCache cache) {
        this.ffmpegService = ffmpegService;
        this.cache = cache;
    }

    public Image getFrame(Video video, int index)
            throws IOException, InterruptedException {

        byte[] jpegBytes = cache.get(index);
        if(jpegBytes == null) {
            jpegBytes =  ffmpegService.extractFrameBytes(video.getPath(), index);
            cache.put(index, jpegBytes);
        }

        schedulePrefetch(video, index);

        Image fxImage = new Image(new ByteArrayInputStream(jpegBytes));
        if (fxImage.isError()) {
            throw new IOException("JavaFX failed to decode frame image for index " + index);
        }

        return fxImage;
    }

    public VideoInfoDto getVideoData(String path) throws IOException, InterruptedException {
        return ffmpegService.getVideoInfo(path);
    }

    public void clearCache() {
        inFlight.clear();
        cache.clear();
    }

    private void schedulePrefetch(Video video, int currentIndex) {
        int half = PRE_LOADING_AMOUNT / 2;
        int from = Math.max(0, currentIndex - half);
        int to = Math.min(video.getTotalFrames() - 1, currentIndex + half);

        prefetchExec.submit(() -> {
            for (int i = currentIndex + 1; i <= to; i++) singlePreFetch(video.getPath(), i);
            for (int i = currentIndex - 1; i >= from; i--) singlePreFetch(video.getPath(), i);
        });
    }

    private void singlePreFetch(String path, int index) {
        if(cache.containsKey(index)) return;
        if(!inFlight.add(index)) return;

        try {
            var bytes = ffmpegService.extractFrameBytes(path, index);
            cache.put(index, bytes);
        } catch (InterruptedException | IOException e) {
            logger.error("Failed to prefetch video data for index {}", index, e);
        } finally {
            inFlight.remove(index);
        }
    }
}
