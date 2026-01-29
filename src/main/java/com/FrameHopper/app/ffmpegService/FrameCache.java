package com.FrameHopper.app.ffmpegService;

import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class FrameCache {
    private final Map<Integer, byte[]> map;

    public FrameCache() {
        this.map = new LinkedHashMap<>(50, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<Integer, byte[]> eldest) {
                return size() > 50;
            }
        };
    }

    public synchronized byte[] get(int key) {
        return map.get(key);
    }

    public synchronized void put(int key, byte[] value) {
        map.put(key, value);
    }

    public synchronized boolean containsKey(int key) {
        return map.containsKey(key);
    }

    public synchronized void clear() {
        map.clear();
    }
}
