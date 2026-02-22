package com.FrameHopper.app.adapters.persistence.repository;

import com.FrameHopper.app.adapters.persistence.entities.CommentEntity;
import com.FrameHopper.app.adapters.persistence.entities.VideoEntity;
import lombok.NonNull;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<CommentEntity,Integer> {
    @EntityGraph(attributePaths = {"videoEntity"})
    List<CommentEntity> findCommentEntitiesByVideoEntity(VideoEntity videoEntity);

    @Override
    @EntityGraph(attributePaths = {"videoEntity"})
    Optional<CommentEntity> findById(@NonNull Integer integer);
}
