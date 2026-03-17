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
    public void ChangeTagStatus(List<Integer> ids) {
        tagRepositoryPort.updateStatus(ids);
    }

    @Override
    public TagDTO CreateTag(TagDTO tagDto) {
        var message = validateTag(tagDto);
        if(!message.isEmpty())
            throw new IllegalArgumentException(message);

        var tag = TagMapper.toDomain(tagDto);
        var savedTag = tagRepositoryPort.create(tag);

        return TagMapper.fromDomain(savedTag);
    }

    @Override
    public List<TagDTO> CreateTags(List<TagDTO> tags) {
        var domainTags = tags.stream().map(t -> {
            var message = validateTag(t);
            if(!message.isEmpty())
                throw new IllegalArgumentException(message);

            return TagMapper.toDomain(t);
        }).toList();

        domainTags = tagRepositoryPort.create(domainTags);

        return domainTags.stream().map(TagMapper::fromDomain).toList();
    }

    private String validateTag(TagDTO dto) {
        if(dto.getName().isBlank())
            return "name is required";

        if(tagRepositoryPort.getByName(dto.getName()) != null)
            return "Tag with that name already exists";

        if(dto.getValue() == null)
            return "value is required";

        if(dto.getValue().isNaN())
            return "Value must be a number";

        return "";
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
