package com.FrameHopper.app.ui.eventing;

import com.FrameHopper.app.boundry.dto.VideoDTO;

import java.util.ArrayList;
import java.util.List;

public class VideoPathUpdatedEventDispatcher {
    private static List<VideoPathUpdatedListener> listeners = new ArrayList<>();

    public static void register(VideoPathUpdatedListener listener) {
        listeners.add(listener);
    }

    public static void unregister(VideoPathUpdatedListener listener) {
        listeners.remove(listener);
    }

    public static void dispatch(VideoDTO video) {
        listeners.forEach(l -> l.onVideoPathUpdated(video));
    }
}
