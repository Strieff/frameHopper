package com.FrameHopper.app.View.Elements.DataManagers.ViewContainer;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.EnumMap;

@Component
@ViewFlags({
        ViewFlag.FRAME_TAG_MANAGER,
        ViewFlag.SETTINGS,
        ViewFlag.EXPORT,
        ViewFlag.VIDEO_LIST,
        ViewFlag.CREATE_TAG,
        ViewFlag.CHARTS,
        ViewFlag.TAG_MANAGER,
        ViewFlag.NOTES
})
public class OpenViewsInformationContainer {
    private final EnumMap<ViewFlag, Boolean> flags = new EnumMap<>(ViewFlag.class);

    @PostConstruct
    public void init() {
        ViewFlags vf = getClass().getAnnotation(ViewFlags.class);

        if (vf != null)
            for (ViewFlag viewFlag : vf.value()) flags.put(viewFlag, false);
        else
            for (ViewFlag f : ViewFlag.values()) flags.put(f, false);
    }

    public void open(ViewFlag flag) {flags.put(flag, true);}
    public void close(ViewFlag flag) {flags.put(flag, false);}
    public boolean isClosed(ViewFlag flag) {return !flags.get(flag);}
}

