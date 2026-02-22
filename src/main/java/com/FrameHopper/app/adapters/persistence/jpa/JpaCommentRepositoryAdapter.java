package com.FrameHopper.app.adapters.persistence.jpa;

import com.FrameHopper.app.adapters.persistence.mappers.CommentMapper;
import com.FrameHopper.app.adapters.persistence.mappers.VideoMapper;
import com.FrameHopper.app.adapters.persistence.repository.CommentRepository;
import com.FrameHopper.app.core.domain.Comment;
import com.FrameHopper.app.core.domain.Video;
import com.FrameHopper.app.core.ports.out.repository.CommentRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class JpaCommentRepositoryAdapter implements CommentRepositoryPort {
    private final CommentRepository commentRepository;

    @Override
    public Comment getById(int id) {
        return CommentMapper.toDomain(commentRepository.findById(id).orElse(null));
    }

    @Override
    public List<Comment> getAllByVideo(Video video) {
        var entities = commentRepository.findCommentEntitiesByVideoEntity(VideoMapper.fromDomain(video));

        return entities.stream().map(CommentMapper::toDomain).toList();
    }

    @Override
    public Comment create(Comment comment, Video video) {
        var commentEntity = CommentMapper.fromDomain(comment);
        var videoEntity = VideoMapper.fromDomain(video);
        commentEntity.setVideoEntity(videoEntity);

        var savedEntity = commentRepository.save(commentEntity);

        return CommentMapper.toDomain(savedEntity);
    }

    @Override
    public Comment update(Comment comment, Video video) {
        var commentEntity = CommentMapper.fromDomain(comment);
        var videoEntity = VideoMapper.fromDomain(video);
        commentEntity.setVideoEntity(videoEntity);

        var updatedEntity = commentRepository.save(commentEntity);

        return CommentMapper.toDomain(updatedEntity);
    }

    @Override
    public List<Comment> update(List<Comment> comments, Video video) {
        var videoEntity = VideoMapper.fromDomain(video);
        var commentEntities = comments.stream().map(CommentMapper::fromDomain).toList();
        commentEntities.forEach(c -> c.setVideoEntity(videoEntity));

        var updated = commentRepository.saveAll(commentEntities);

        return updated.stream().map(CommentMapper::toDomain).toList();
    }

    @Override
    public void delete(int id) {
        commentRepository.deleteById(id);
    }
}
