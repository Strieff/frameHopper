package com.FrameHopper.app.adapters.persistence.repository;

import com.FrameHopper.app.adapters.persistence.entities.FrameEntity;
import com.FrameHopper.app.adapters.persistence.entities.VideoEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FrameRepository extends JpaRepository<FrameEntity,Integer> {
    @EntityGraph(attributePaths = {"tagEntities", "videoEntity"})
    FrameEntity getFrameEntityByFrameNumberAndVideoEntity(int frameNumber, VideoEntity videoEntity);

    @EntityGraph(attributePaths = {"tagEntities", "videoEntity"})
    List<FrameEntity> getFrameEntitiesByVideoEntity(VideoEntity videoEntity);

    @EntityGraph(attributePaths = {"tagEntities", "videoEntity"})
    Optional<FrameEntity> findFrameEntityById(int id);
}
