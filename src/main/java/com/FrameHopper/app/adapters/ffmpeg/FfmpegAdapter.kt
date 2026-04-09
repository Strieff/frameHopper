package com.FrameHopper.app.adapters.ffmpeg;

import com.FrameHopper.app.core.domain.Video;
import com.FrameHopper.app.core.ports.out.FfmpegPort;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
@RequiredArgsConstructor
public class FfmpegAdapter implements FfmpegPort {
    private final static int PRE_LOADING_AMOUNT = 20;
    private final Logger logger = LoggerFactory.getLogger(FfmpegAdapter.class);

    private final ExecutorService prefetchExec = Executors.newSingleThreadExecutor();
    private final Set<Integer> inFlight = ConcurrentHashMap.newKeySet();

    private final FfmpegService ffmpegService;
    private final FrameCache frameCache;

    @Override
    public void loadVideo(Video video){
        inFlight.clear();
        frameCache.clear();

        schedulePrefetch(video, 0);
    }

    @Override
    public byte[] getFrameBytes(Video video, int index) throws InterruptedException, IOException {
        if(frameCache.containsKey(index))
            return frameCache.get(index);

        schedulePrefetch(video, index);

        return ffmpegService.extractFrameBytes(video.getPath(), index);
    }

    @Override
    public Video.VideoMetadata getVideoMetadata(String path) throws InterruptedException, IOException {
        return ffmpegService.getVideoInfo(path);
    }

    //region [Cache filling]

    @Async
    protected void schedulePrefetch(Video video, int currentIndex) {
        int half = PRE_LOADING_AMOUNT / 2;
        int from = Math.max(0, currentIndex - half);
        int to = Math.min(video.getMetadata().totalFrames() - 1, currentIndex + half);

        prefetchExec.submit(() -> {
            for (int i = currentIndex + 1; i <= to; i++) singlePreFetch(video.getPath(), i);
            for (int i = currentIndex - 1; i >= from; i--) singlePreFetch(video.getPath(), i);
        });
    }

    private void singlePreFetch(String path, int index) {
        if(frameCache.containsKey(index)) return;
        if(!inFlight.add(index)) return;

        try {
            var bytes = ffmpegService.extractFrameBytes(path, index);
            frameCache.put(index, bytes);
        } catch (InterruptedException | IOException e) {
            logger.error("Failed to prefetch video data for index {}", index, e);
        } finally {
            inFlight.remove(index);
        }
    }

    //endregion
}
