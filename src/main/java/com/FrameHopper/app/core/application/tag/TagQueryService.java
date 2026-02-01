package com.FrameHopper.app.core.application.tag;

import com.FrameHopper.app.core.domain.Tag;
import com.FrameHopper.app.core.ports.in.tag.TagsQuery;
import com.FrameHopper.app.core.ports.out.repository.TagRepositoryPort;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class TagQueryService implements TagsQuery {
    private final TagRepositoryPort tagRepositoryPort;

    @Override
    public List<Tag> getAllTags() {
        return tagRepositoryPort.getAll();
    }

    @Override
    public Tag getTagById(int id) {
        return tagRepositoryPort.getById(id);
    }

    @Override
    public List<Tag> getAllVisible() {
        return tagRepositoryPort.getAll().stream()
                .filter(Tag::isVisible)
                .toList();
    }

    @Override
    public Tag getTagByName(String name) {
        return tagRepositoryPort.getByName(name);
    }
}
