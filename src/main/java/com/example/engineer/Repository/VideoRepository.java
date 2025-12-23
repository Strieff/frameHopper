package com.example.engineer.Repository;

import com.example.engineer.Model.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VideoRepository extends JpaRepository<Video,Long> {
    Optional<Video> findByPath(String path);

    Optional<Video> findById(Integer id);

    @Query("select v from Video v where v.id in :ids")
    List<Video> findById(@Param("ids") List<Integer> ids);
}
