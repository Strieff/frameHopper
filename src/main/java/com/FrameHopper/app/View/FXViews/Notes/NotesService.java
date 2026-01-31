package com.FrameHopper.app.View.FXViews.Notes;

import com.FrameHopper.app.Model.Comment;
import com.FrameHopper.app.Model.Video;
import com.FrameHopper.app.Service.CommentService;
import com.FrameHopper.app.Service.DataBaseManagementService;
import com.FrameHopper.app.Service.VideoService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class NotesService {
    private final VideoService videoService;
    private final CommentService commentService;
    private final DataBaseManagementService dbService;

    public NotesService(
            VideoService videoService,
            CommentService commentService,
            DataBaseManagementService dbService
    ) {
        this.videoService = videoService;
        this.commentService = commentService;
        this.dbService = dbService;
    }

    public List<TableEntry> getVideosWithNotes() {
        return videoService.getAllWithNotes().stream()
                .map(TableEntry::new)
                .toList();
    }

    public Comment save(Comment comment) {
        return commentService.createOrUpdate(comment);
    }

    public Comment save(String text, int order, Video video) {
        var comment = commentService.createOrUpdate(Comment.builder()
                        .content(text)
                        .listingOrder(order)
                        .video(video)
                        .build());
        video.getComments().add(comment);
        videoService.saveVideo(video);

        return comment;
    }

    @Transactional
    public void delete(Comment note, Video video) {
        int commentId = note.getId();
        int videoId = video.getId();

        // schedule async by IDs only
        dbService.deleteCommentAsync(commentId, videoId);
    }


}
