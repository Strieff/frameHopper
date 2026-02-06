package com.FrameHopper.app.core.application.frame;

import com.FrameHopper.app.adapters.persistence.jpa.JpaFrameRepositoryAdapter;
import com.FrameHopper.app.boundry.dto.FrameDTO;
import com.FrameHopper.app.boundry.dto.VideoDTO;
import com.FrameHopper.app.boundry.mappers.FrameMapper;
import com.FrameHopper.app.boundry.mappers.VideoMapper;
import com.FrameHopper.app.core.ports.in.frame.FrameQuery;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class FrameQueryService implements FrameQuery {
    private final JpaFrameRepositoryAdapter jpaFrameRepositoryAdapter;

    @Override
    public List<FrameDTO> getAllFramesOnVideo(VideoDTO video) {
        var coreVideo = VideoMapper.toDomain(video);

        var frames = jpaFrameRepositoryAdapter.getAllFramesOnVideo(coreVideo);

        return frames.stream().map(FrameMapper::fromDomain).toList();
    }
}
