package com.FrameHopper.app.ui.eventing;

import com.FrameHopper.app.boundry.dto.FrameDTO;

import java.util.ArrayList;
import java.util.List;

public class FrameUpdatedEventDispatcher {
    private final static List<FrameUpdatedListener> listeners = new ArrayList<>();

    public static void register(FrameUpdatedListener  listener) {
        listeners.add(listener);
    }

    public static void unregister(FrameUpdatedListener  listener) {
        listeners.remove(listener);
    }

    public static void dispatch(int index, FrameDTO frame) {
        listeners.forEach(l -> l.onFrameUpdate(index, frame));
    }
}
