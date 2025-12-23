package com.example.engineer.Repository;

import com.example.engineer.Model.Frame;
import com.example.engineer.Model.Video;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNullApi;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FrameRepository extends JpaRepository<Frame,Long> {

    @Query("select f from Frame f where f.frameNumber=:frameNumber and f.video=:video")
    Optional<Frame> findFrameByFrameNumberAndVideo(@Param("frameNumber") int frameNumber, @Param("video") Video video);

    @EntityGraph(attributePaths = {"video","tags"})
    @Query("select f from Frame f")
    List<Frame> findAllWithVideos();

    @EntityGraph(attributePaths = "tags")
    List<Frame> findAllByVideo(Video video);

    @Modifying(clearAutomatically = true)
    @Query(value = "delete from FRAME_TAG where FRAME_ID in :frames", nativeQuery = true)
    void totalFrameDelete(@Param("frames") List<Integer> frames);

    @Modifying(clearAutomatically = true)
    @Query(value = "delete from FRAME where VIDEO_ID=:videoId", nativeQuery = true)
    void totalFrameDelete(@Param("videoId") Integer videoId);

    @Override
    @EntityGraph(attributePaths = "tags")
    Optional<Frame> findById(Long aLong);

    @EntityGraph(attributePaths = "tags")
    @Query("select f from Frame f where f.video=:video and f.frameNumber=:frameNo")
    Optional<Frame> findFrameOnVideo(@Param("video") Video video, @Param("frameNo") int frameNo);
}
