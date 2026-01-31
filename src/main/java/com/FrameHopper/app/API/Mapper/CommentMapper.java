package com.FrameHopper.app.API.Mapper;

import com.FrameHopper.app.API.Model.CommentDTO;
import com.FrameHopper.app.Model.Comment;
import org.hibernate.LazyInitializationException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class CommentMapper {
    public static List<CommentDTO> mapComments(List<Comment> comments) {
        try {
            if(comments == null || comments.isEmpty())
                return new ArrayList<>();

            return comments.stream()
                    .sorted(Comparator.comparingInt(Comment::getListingOrder))
                    .map(CommentDTO::new)
                    .toList();
        } catch (LazyInitializationException e) {
            return new ArrayList<>();
        }
    }
}
