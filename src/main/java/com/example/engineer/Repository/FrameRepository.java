package com.example.engineer.Repository;

import com.example.engineer.Model.Frame;
import com.example.engineer.Model.Tag;
import com.example.engineer.Model.Video;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FrameRepository extends JpaRepository<Frame,Long> {

    @Query("select f from Frame f where f.frameNumber=:frameNumber and f.video=:video")
    Optional<Frame> findFrameByFrameNumberAndVideo(@Param("frameNumber") int frameNumber, @Param("video") Video video);

    @Query("select f from Frame f join f.tags t where t=:tag")
    List<Frame> getAllFramesWithTag(@Param("tag") Tag tag);

    @EntityGraph(attributePaths = "tags")
    List<Frame> findAllByVideo(Video video);
}
