package com.example.engineer.API.Mapper;

import com.example.engineer.API.Model.FrameDTO;
import com.example.engineer.Model.Frame;

import java.util.ArrayList;
import java.util.List;

public class FrameMapper {
    public static List<FrameDTO> mapFrames(List<Frame> frames){
        if(frames == null || frames.isEmpty())
            return new ArrayList<>();

        return frames.stream()
                .map(FrameDTO::new)
                .toList();
    }
}
