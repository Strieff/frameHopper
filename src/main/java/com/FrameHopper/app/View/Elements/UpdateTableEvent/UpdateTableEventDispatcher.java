package com.FrameHopper.app.View.Elements.UpdateTableEvent;

import java.util.ArrayList;
import java.util.List;

public class UpdateTableEventDispatcher {
    private static final List<UpdateTableListener> listeners = new ArrayList<>();

    public static void register(UpdateTableListener listener) {
        listeners.add(listener);
    }

    public static void unregister(UpdateTableListener listener) {
        listeners.remove(listener);
    }

    public static void fireEvent(){
        for(UpdateTableListener listener : listeners)
            listener.updateTable();
    }
}
