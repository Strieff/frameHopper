package com.FrameHopper.app.core.application.tag;

import com.FrameHopper.app.boundry.dto.TagDTO;
import com.FrameHopper.app.boundry.dto.VideoDTO;
import com.FrameHopper.app.boundry.mappers.BoundaryTagMapper;
import com.FrameHopper.app.core.domain.Tag;
import com.FrameHopper.app.core.domain.Video;
import com.FrameHopper.app.core.ports.in.tag.TagsQuery;
import com.FrameHopper.app.core.ports.out.repository.TagRepositoryPort;
import com.FrameHopper.app.core.ports.out.repository.VideoRepositoryPort;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class TagQueryService implements TagsQuery {
    private final TagRepositoryPort tagRepositoryPort;
    private final VideoRepositoryPort videoRepositoryPort;

    @Override
    public List<TagDTO> getAllTags() {
        var coreTags = tagRepositoryPort.getAll();

        if(coreTags == null || coreTags.isEmpty()) return null;

        return coreTags.stream().map(BoundaryTagMapper::fromDomain).toList();
    }

    @Override
    public List<TagDTO> getAllOnVideo(VideoDTO video) {
        var coreVideo = videoRepositoryPort.getById(video.id());

        if(coreVideo == null) return null;

        var coreTags = tagRepositoryPort.getAllByVideo(coreVideo);

        if(coreTags == null) return null;

        return coreTags.stream().map(BoundaryTagMapper::fromDomain).toList();
    }

    @Override
    public TagDTO getTagById(int id) {
        var tag = tagRepositoryPort.getById(id);

        if(tag == null) return null;

        return BoundaryTagMapper.fromDomain(tag);
    }

    @Override
    public List<TagDTO> getTagsByIds(List<Integer> ids) {
        var tags = tagRepositoryPort.getByIds(ids);

        if(tags == null || tags.isEmpty()) return null;

        return tags.stream().map(BoundaryTagMapper::fromDomain).toList();
    }

    @Override
    public List<TagDTO> getTagsOnVideoFrame(VideoDTO videoDTO, int frame) {
        var video = videoRepositoryPort.getById(videoDTO.id());

        var tags = tagRepositoryPort.getTagsOnVideoFrame(video, frame);

        if(tags == null || tags.isEmpty()) return null;

        return tags.stream().map(BoundaryTagMapper::fromDomain).toList();
    }

    @Override
    public TagDTO getTagByName(String name) {
        var tag = tagRepositoryPort.getByName(name);

        if(tag == null) return null;

        return BoundaryTagMapper.fromDomain(tag);
    }
}
