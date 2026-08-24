package com.FrameHopper.app.adapters.persistence.repository;

import com.FrameHopper.app.adapters.persistence.entities.TagEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TagRepository extends JpaRepository<TagEntity,Integer> {
    TagEntity findTagEntityByName(String name);

    TagEntity findTagEntityById(Integer id);

    @Query("select t from TagEntity t where t.id in :ids")
    List<TagEntity> findTagEntitiesById(List<Integer> ids);
}
