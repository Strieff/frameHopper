package com.FrameHopper.app.adapters.persistence.jpa;

import com.FrameHopper.app.adapters.persistence.mappers.CommentMapper;
import com.FrameHopper.app.adapters.persistence.mappers.VideoMapper;
import com.FrameHopper.app.adapters.persistence.repository.CommentRepository;
import com.FrameHopper.app.core.domain.Comment;
import com.FrameHopper.app.core.domain.Video;
import com.FrameHopper.app.core.ports.out.repository.CommentRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class JpaCommentRepositoryAdapter implements CommentRepositoryPort {
    private final CommentRepository commentRepository;

    @Override
    public Comment getById(int id) {
        return CommentMapper.toDomain(commentRepository.findById(id).get());
    }

    @Override
    public List<Comment> getAllByVideo(Video video) {
        var entities = commentRepository.findCommentEntitiesByVideoEntity(VideoMapper.fromDomain(video));

        return entities.stream().map(CommentMapper::toDomain).toList();
    }

    @Override
    public Comment create(Comment comment) {
        var savedEntity = commentRepository.save(CommentMapper.fromDomain(comment));

        return CommentMapper.toDomain(savedEntity);
    }

    @Override
    public Comment update(Comment comment) {
        var updatedEntity = commentRepository.save(CommentMapper.fromDomain(comment));

        return CommentMapper.toDomain(updatedEntity);
    }

    @Override
    public void delete(int id) {
        commentRepository.deleteById(id);
    }
}
