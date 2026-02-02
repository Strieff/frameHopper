package com.FrameHopper.app.adapters.persistence.repository;

import com.FrameHopper.app.adapters.persistence.entities.FrameEntity;
import com.FrameHopper.app.adapters.persistence.entities.VideoEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FrameRepository extends JpaRepository<FrameEntity,Integer> {
    @EntityGraph(attributePaths = "tags")
    FrameEntity getFrameEntityByFrameNumberAndVideoEntity(int frameNumber, VideoEntity videoEntity);
}
