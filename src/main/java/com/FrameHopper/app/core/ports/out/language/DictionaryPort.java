package com.FrameHopper.app.core.ports.out.language;

public interface DictionaryPort {
    public String get(String key);
    public String get(String key, String code);
}
