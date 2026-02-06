package com.FrameHopper.app.adapters.persistence.repository;

import com.FrameHopper.app.adapters.persistence.entities.VideoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VideoRepository extends JpaRepository<VideoEntity,Integer> {
    Optional<VideoEntity> getVideoEntityByPath(String path);
}
