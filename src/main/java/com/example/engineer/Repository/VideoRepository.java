package com.example.engineer.Repository;

import com.example.engineer.Model.Video;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VideoRepository extends JpaRepository<Video,Long> {
    @Query("select v from Video v where v.name=:name")
    Optional<Video> findByName(@Param("name") String name);

    @EntityGraph(attributePaths = "frames") // Eagerly load frames
    List<Video> findAllByIdIn(List<Integer> videoIds);

    //@EntityGraph(attributePaths = "frames") // Eagerly load frames
    Optional<Video> findFirstById(Integer id);
}
