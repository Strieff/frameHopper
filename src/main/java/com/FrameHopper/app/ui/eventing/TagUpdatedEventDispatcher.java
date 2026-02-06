package com.FrameHopper.app.ui.eventing;

import com.FrameHopper.app.boundry.dto.TagDTO;

import java.util.ArrayList;
import java.util.List;

public class TagUpdatedEventDispatcher {
    private final static List<TagUpdatedEventListener> listeners = new ArrayList<>();

    public static void register(TagUpdatedEventListener listener) {
        listeners.add(listener);
    }

    public static void unregister(TagUpdatedEventListener listener) {
        listeners.remove(listener);
    }

    public static void updateTag(TagDTO tagDTO) {
        listeners.forEach(listener -> listener.onTagUpdated(tagDTO));
    }

    public static void createTag(TagDTO tagDTO) {
        listeners.forEach(listener -> listener.onTagCreated(tagDTO));
    }

    public static void deleteTag(TagDTO tagDTO) {
        listeners.forEach(listener -> listener.onTagDeleted(tagDTO));
    }
}
