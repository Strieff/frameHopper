package com.FrameHopper.app.core.application.tag;

import com.FrameHopper.app.core.domain.Tag;
import com.FrameHopper.app.core.domain.Video;
import com.FrameHopper.app.core.ports.in.tag.TagsQuery;
import com.FrameHopper.app.core.ports.out.UserSettingsPort;
import com.FrameHopper.app.core.ports.out.repository.TagRepositoryPort;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class TagQueryService implements TagsQuery {
    private final TagRepositoryPort tagRepositoryPort;

    private final UserSettingsPort userSettingsPort;

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
        var tags = tagRepositoryPort.getAll();

        if(userSettingsPort.showHidden())
            tags = tags.stream().filter(Tag::isVisible).toList();

        return tags;
    }

    @Override
    public List<Tag> getTagsOnVideoFrame(Video video, int frame) {
        var tags = tagRepositoryPort.getTagsOnVideoFrame(video, frame);

        if (!tags.isEmpty() && !userSettingsPort.showHidden())
            tags = tags.stream().filter(Tag::isVisible).toList();

        return tags;
    }

    @Override
    public Tag getTagByName(String name) {
        return tagRepositoryPort.getByName(name);
    }
}
