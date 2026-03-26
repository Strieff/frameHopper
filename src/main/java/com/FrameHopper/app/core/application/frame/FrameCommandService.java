package com.FrameHopper.app.core.application.frame;

import com.FrameHopper.app.adapters.persistence.jpa.JpaFrameRepositoryAdapter;
import com.FrameHopper.app.boundry.dto.FrameDTO;
import com.FrameHopper.app.boundry.mappers.FrameMapper;
import com.FrameHopper.app.core.ports.in.frame.CreateFrameCommand;
import com.FrameHopper.app.core.ports.in.frame.DeleteFrameCommand;
import com.FrameHopper.app.core.ports.in.frame.UpdateFrameCommand;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FrameCommandService implements
        CreateFrameCommand,
        UpdateFrameCommand,
        DeleteFrameCommand
{
    private final JpaFrameRepositoryAdapter jpaFrameRepositoryAdapter;

    @Override
    public FrameDTO createFrame(FrameDTO frame) {
        var toCreate = FrameMapper.toDomain(frame);

        if (toCreate.getFrameNumber() < 0)
            throw new IllegalArgumentException("Frame number must be greater than or equal to 0");

        var created = jpaFrameRepositoryAdapter.create(toCreate);

        return FrameMapper.fromDomain(created);
    }

    @Override
    public void deleteFrame(int id) {
        jpaFrameRepositoryAdapter.delete(id);
    }

    @Override
    public FrameDTO updateFrame(FrameDTO frame) {
        if(frame.frameNumber() < 0)
            throw new IllegalArgumentException("Frame number must be greater than or equal to 0");

        if(jpaFrameRepositoryAdapter.getFrameById(frame.id()) == null)
            throw new IllegalArgumentException("Frame not found");

        var updated = FrameMapper.toDomain(frame);
        updated = jpaFrameRepositoryAdapter.update(updated);

        return FrameMapper.fromDomain(updated);
    }
}
