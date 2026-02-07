package com.FrameHopper.app.ui.eventing;

public class OpenVideoEventDispatcher {
    private static OpenVideoEventListener openVideoEventListener;

    public static void register(OpenVideoEventListener listener) {
        openVideoEventListener = listener;
    }

    public static void dispatch(int id) {
        openVideoEventListener.openVideo(id);
    }
}
