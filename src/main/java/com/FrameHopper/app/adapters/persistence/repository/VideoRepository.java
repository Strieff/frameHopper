package com.FrameHopper.app.adapters.persistence.repository;

import com.FrameHopper.app.adapters.persistence.entities.VideoEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VideoRepository extends JpaRepository<VideoEntity,Integer> {
    Optional<VideoEntity> getVideoEntityByPath(String path);

    Optional<VideoEntity> findVideoEntitiesById(int id);

    @EntityGraph(attributePaths = {"commentEntities"})
    @Query("select v from VideoEntity v")
    List<VideoEntity> getVideoEntitiesWithNotes();

    List<VideoEntity> getVideoEntitiesByName(String name);

    @Query("select v from VideoEntity v where v.id in :ids")
    List<VideoEntity> getVideoEntitiesById(List<Integer> ids);
}
