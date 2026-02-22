package com.FrameHopper.app.core.application.comment;

import com.FrameHopper.app.boundry.dto.CommentDTO;
import com.FrameHopper.app.boundry.mappers.CommentMapper;
import com.FrameHopper.app.core.ports.in.comment.ChangeCommentContentCommand;
import com.FrameHopper.app.core.ports.in.comment.ChangeCommentListingOrderCommand;
import com.FrameHopper.app.core.ports.in.comment.CreateCommentCommand;
import com.FrameHopper.app.core.ports.in.comment.DeleteCommentCommand;
import com.FrameHopper.app.core.ports.out.repository.CommentRepositoryPort;
import com.FrameHopper.app.core.ports.out.repository.VideoRepositoryPort;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class CommentCommandService implements
        ChangeCommentContentCommand,
        ChangeCommentListingOrderCommand,
        CreateCommentCommand,
        DeleteCommentCommand {
    private final CommentRepositoryPort commentRepositoryPort;
    private final VideoRepositoryPort videoRepositoryPort;

    @Override
    public CommentDTO updateCommentContent(CommentDTO comment) {
        var coreComment = CommentMapper.toDomain(comment);
        var coreVideo = videoRepositoryPort.getById(comment.getVideoId());

        coreComment = commentRepositoryPort.update(coreComment, coreVideo);

        return CommentMapper.fromDomain(coreComment);
    }

    @Override
    public List<CommentDTO> changeCommentListingOrder(List<CommentDTO> comments) {
        if(comments == null || comments.isEmpty())
            throw new IllegalArgumentException("comments cannot be null or empty");

        if(comments.stream().anyMatch(c -> c.getListingOrder() == -1))
            throw new IllegalArgumentException("Comment listing order must be greater than or equal to 0");

        var videoId = comments.getFirst().getVideoId();
        var coreVideo = videoRepositoryPort.getById(videoId);

        var coreComments = comments.stream().map(CommentMapper::toDomain).toList();
        coreComments = commentRepositoryPort.update(coreComments, coreVideo);

        return coreComments.stream().map(CommentMapper::fromDomain).toList();
    }

    @Override
    public CommentDTO CreateComment(CommentDTO comment) {
        var coreComment = CommentMapper.toDomain(comment);
        var coreVideo = videoRepositoryPort.getById(comment.getVideoId());

        var createdComment = commentRepositoryPort.create(coreComment,coreVideo);

        return CommentMapper.fromDomain(createdComment);
    }

    @Override
    public void DeleteComment(int id) {
        commentRepositoryPort.delete(id);
    }
}
