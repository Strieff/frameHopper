package com.FrameHopper.app.core.application.tag;

import com.FrameHopper.app.boundry.dto.TagDTO;
import com.FrameHopper.app.boundry.mappers.TagMapper;
import com.FrameHopper.app.core.domain.Tag;
import com.FrameHopper.app.core.domain.Video;
import com.FrameHopper.app.core.ports.in.tag.TagsQuery;
import com.FrameHopper.app.core.ports.out.repository.TagRepositoryPort;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class TagQueryService implements TagsQuery {
    private final TagRepositoryPort tagRepositoryPort;

    @Override
    public List<TagDTO> getAllTags() {
        return tagRepositoryPort.getAll().stream().map(TagMapper::fromDomain).toList();
    }

    @Override
    public Tag getTagById(int id) {
        return tagRepositoryPort.getById(id);
    }

    @Override
    public List<Tag> getTagsOnVideoFrame(Video video, int frame) {
        var tags = tagRepositoryPort.getTagsOnVideoFrame(video, frame);

        return tags;
    }

    @Override
    public Tag getTagByName(String name) {
        return tagRepositoryPort.getByName(name);
    }
}
