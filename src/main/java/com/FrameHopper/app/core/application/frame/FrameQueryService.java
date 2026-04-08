package com.FrameHopper.app.core.application.frame;

import com.FrameHopper.app.adapters.persistence.jpa.JpaFrameRepositoryAdapter;
import com.FrameHopper.app.boundry.dto.FrameDTO;
import com.FrameHopper.app.boundry.dto.VideoDTO;
import com.FrameHopper.app.boundry.mappers.FrameMapper;
import com.FrameHopper.app.boundry.mappers.VideoMapper;
import com.FrameHopper.app.core.ports.in.frame.FrameQuery;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class FrameQueryService implements FrameQuery {
    private final JpaFrameRepositoryAdapter jpaFrameRepositoryAdapter;

    @Override
    public FrameDTO get(int id) {
        var frame = jpaFrameRepositoryAdapter.getFrameById(id);

        if (frame == null) return null;

        return FrameMapper.fromDomain(frame);
    }

    @Override
    public FrameDTO get(VideoDTO video, int frameNo) {
        var coreVideo = VideoMapper.toDomain(video);

        var frame = jpaFrameRepositoryAdapter.getFrameByVideoAndFrameNumber(coreVideo, frameNo);

        if (frame == null) return null;

        return FrameMapper.fromDomain(frame);
    }

    @Override
    public List<FrameDTO> getAllFramesOnVideo(VideoDTO video) {
        var coreVideo = VideoMapper.toDomain(video);

        var frames = jpaFrameRepositoryAdapter.getAllFramesOnVideo(coreVideo);

        if(frames == null || frames.isEmpty()) return null;

        return frames.stream().map(FrameMapper::fromDomain).toList();
    }

    @Override
    public List<FrameDTO> getAll() {
        var frames = jpaFrameRepositoryAdapter.getAll();

        if(frames.isEmpty())
            return new ArrayList<>();

        return frames.stream().map(FrameMapper::fromDomain).toList();
    }
}
