package com.FrameHopper.app.core.application.comment;

import com.FrameHopper.app.boundry.dto.CommentDTO;
import com.FrameHopper.app.boundry.dto.VideoDTO;
import com.FrameHopper.app.boundry.mappers.CommentMapper;
import com.FrameHopper.app.core.domain.Comment;
import com.FrameHopper.app.core.domain.Video;
import com.FrameHopper.app.core.ports.in.comment.CommentsQuery;
import com.FrameHopper.app.core.ports.out.repository.CommentRepositoryPort;
import com.FrameHopper.app.core.ports.out.repository.VideoRepositoryPort;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class CommentQueryService implements CommentsQuery {
    private final CommentRepositoryPort commentRepositoryPort;
    private final VideoRepositoryPort videoRepositoryPort;

    @Override
    public CommentDTO getCommentById(int id) {
        var comment = commentRepositoryPort.getById(id);

        if(comment == null) return null;

        return CommentMapper.fromDomain(comment);
    }

    @Override
    public List<CommentDTO> getAllCommentsByVideo(VideoDTO video) {
        var coreVideo = videoRepositoryPort.getById(video.id());

        if(coreVideo == null) return null;

        var coreComments = commentRepositoryPort.getAllByVideo(coreVideo);

        if(coreComments == null || coreComments.isEmpty()) return new ArrayList<>();

        return coreComments.stream().map(CommentMapper::fromDomain).toList();
    }

    @Override
    public Integer getCommentsCountByVideo(VideoDTO video) {
        var coreVideo = videoRepositoryPort.getById(video.id());

        if(coreVideo == null) return null;

        var coreComments = commentRepositoryPort.getAllByVideo(coreVideo);

        return (coreComments == null || coreComments.isEmpty())
                ? 0
                : coreComments.size();
    }
}
