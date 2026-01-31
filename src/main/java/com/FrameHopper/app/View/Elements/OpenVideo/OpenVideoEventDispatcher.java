package com.FrameHopper.app.View.Elements.OpenVideo;

public class OpenVideoEventDispatcher {
    private static OpenVideoListener listener;

    public static void register(OpenVideoListener listener) {
        OpenVideoEventDispatcher.listener = listener;
    }

    public static void fireEvent(int id) {
        listener.openVideo(id);
    }
}
