package com.FrameHopper.app.Repository;

import com.FrameHopper.app.Model.Comment;
import com.FrameHopper.app.Model.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment,Long> {
    @Override
    Optional<Comment> findById(Long aLong);

    List<Comment> getCommentByVideo(Video video);

    @Query("select c from Comment c order by c.video.id")
    List<Comment> findAllOrdered();
}
