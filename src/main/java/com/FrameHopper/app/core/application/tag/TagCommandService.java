package com.FrameHopper.app.core.application.tag;

import com.FrameHopper.app.core.ports.in.tag.ChangeTagStatusCommand;
import com.FrameHopper.app.core.ports.in.tag.CreateTagCommand;
import com.FrameHopper.app.core.ports.in.tag.DeleteTagCommand;
import com.FrameHopper.app.core.ports.in.tag.UpdateTagCommand;
import com.FrameHopper.app.core.ports.out.repository.TagRepositoryPort;
import com.FrameHopper.app.boundry.dto.TagDTO;
import com.FrameHopper.app.boundry.mappers.TagMapper;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class TagCommandService implements
        CreateTagCommand,
        UpdateTagCommand,
        DeleteTagCommand,
        ChangeTagStatusCommand {
    private final TagRepositoryPort tagRepositoryPort;

    @Override
    public void ChangeTagStatus(int id) {
        tagRepositoryPort.updateStatus(id);
    }

    @Override
    public TagDTO CreateTag(TagDTO tagDto) {
        if (tagDto.getName().isBlank())
            throw new IllegalArgumentException("name is required");

        if(tagRepositoryPort.getByName(tagDto.getName()) != null)
            throw new IllegalArgumentException("Tag with that name already exists");

        if (tagDto.getValue() == null)
            throw new IllegalArgumentException("value is required");

        if (tagDto.getValue().isNaN())
            throw new IllegalArgumentException("Value must be a number");

        var tag = TagMapper.toDomain(tagDto);
        var savedTag = tagRepositoryPort.create(tag);

        return TagMapper.fromDomain(savedTag);
    }

    @Override
    public void DeleteTag(int id) {
        tagRepositoryPort.delete(id);
    }

    @Override
    public TagDTO UpdateTag(TagDTO tagDto) {
        if (tagDto.getName().isBlank())
            throw new IllegalArgumentException("name is required");

        if(tagDto.getValue() == null || tagDto.getValue().isNaN())
            throw new IllegalArgumentException("Value must be a number");

        var tag = TagMapper.toDomain(tagDto);
        var updatedTag = tagRepositoryPort.update(tag);

        return TagMapper.fromDomain(updatedTag);
    }

    @Override
    public void DeleteTags(List<Integer> ids) {
        if (ids == null || ids.isEmpty())
            throw new IllegalArgumentException("ids is required"); //TODO

        tagRepositoryPort.delete(ids);
    }
}
