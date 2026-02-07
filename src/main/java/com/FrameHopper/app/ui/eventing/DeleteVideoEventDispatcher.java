package com.FrameHopper.app.ui.eventing;

import com.FrameHopper.app.boundry.dto.VideoDTO;

import java.util.ArrayList;
import java.util.List;

public class DeleteVideoEventDispatcher {
    private static List<DeleteVideoEventListener> listeners = new ArrayList<>();

    public static void register(DeleteVideoEventListener listener) {
        listeners.add(listener);
    }

    public static void unregister(DeleteVideoEventListener listener) {
        listeners.remove(listener);
    }

    public static void dispatch(VideoDTO videoDTO) {
        listeners.forEach(l -> l.onDeleteVideo(videoDTO));
    }
}
