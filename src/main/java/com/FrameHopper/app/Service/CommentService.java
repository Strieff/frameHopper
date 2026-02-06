package com.FrameHopper.app.Service;

import com.FrameHopper.app.Model.Comment;
import com.FrameHopper.app.Model.Video;
import com.FrameHopper.app.Repository.CommentRepositoryOld;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentService {

    private final CommentRepositoryOld commentRepositoryOld = null;

    public List<Comment> getAll() {
        return commentRepositoryOld.findAllOrdered();
    }

    public Comment get(int id) {
        return commentRepositoryOld.findById((long) id).orElse(null);
    }

    public List<Comment> getAllByVideo(Video video) {
        return commentRepositoryOld.getCommentByVideo(video);
    }

    public Comment createOrUpdate(Comment comment) {
        return commentRepositoryOld.save(comment);
    }

    public void delete(Comment comment) {
        commentRepositoryOld.delete(comment);
    }

    public void saveAll(List<Comment> comments) {
        commentRepositoryOld.saveAll(comments);
    }

    public Comment findById(int commentId) {
        return commentRepositoryOld.findById((long) commentId).orElse(null);
    }
}
