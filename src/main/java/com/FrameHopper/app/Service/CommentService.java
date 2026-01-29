package com.FrameHopper.app.Service;

import com.FrameHopper.app.Model.Comment;
import com.FrameHopper.app.Model.Video;
import com.FrameHopper.app.Repository.CommentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentService {

    private final CommentRepository commentRepository;

    public CommentService(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    public List<Comment> getAll() {
        return commentRepository.findAllOrdered();
    }

    public Comment get(int id) {
        return commentRepository.findById((long) id).orElse(null);
    }

    public List<Comment> getAllByVideo(Video video) {
        return commentRepository.getCommentByVideo(video);
    }

    public Comment createOrUpdate(Comment comment) {
        return commentRepository.save(comment);
    }

    public void delete(Comment comment) {
        commentRepository.delete(comment);
    }

    public void saveAll(List<Comment> comments) {
        commentRepository.saveAll(comments);
    }

    public Comment findById(int commentId) {
        return commentRepository.findById((long) commentId).orElse(null);
    }
}
