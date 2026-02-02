package com.FrameHopper.app.config;

import com.FrameHopper.app.adapters.ffmpeg.FfmpegAdapter;
import com.FrameHopper.app.adapters.persistence.jpa.JpaCommentRepositoryAdapter;
import com.FrameHopper.app.adapters.persistence.jpa.JpaTagRepositoryAdapter;
import com.FrameHopper.app.adapters.persistence.jpa.JpaVideoRepositoryAdapter;
import com.FrameHopper.app.adapters.settings.UserSettingsAdapter;
import com.FrameHopper.app.core.application.FrameQueryService;
import com.FrameHopper.app.core.application.comment.CommentCommandService;
import com.FrameHopper.app.core.application.comment.CommentQueryService;
import com.FrameHopper.app.core.application.tag.TagCommandService;
import com.FrameHopper.app.core.application.tag.TagQueryService;
import com.FrameHopper.app.core.application.video.VideoCommandService;
import com.FrameHopper.app.core.application.video.VideoQueryService;
import com.FrameHopper.app.core.ports.in.FrameQuery;
import com.FrameHopper.app.core.ports.in.comment.*;
import com.FrameHopper.app.core.ports.in.tag.*;
import com.FrameHopper.app.core.ports.in.video.*;
import com.FrameHopper.app.core.ports.out.FfmpegPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class WiringConfig {
    // --------------------
    // Inbound ports (core)
    // --------------------

    @Bean
    public FrameQuery frameQuery(FfmpegPort ffmpegPort) {
        return new FrameQueryService(ffmpegPort);
    }

    // TAGS

    @Bean
    public TagsQuery tagsQuery(
            JpaTagRepositoryAdapter jpaTagRepositoryAdapter,
            UserSettingsAdapter userSettingsAdapter
    ) {
        return new TagQueryService(jpaTagRepositoryAdapter, userSettingsAdapter);
    }

    @Bean
    public CreateTagCommand createTagCommand(JpaTagRepositoryAdapter jpaTagRepositoryAdapter) {
        return new TagCommandService(jpaTagRepositoryAdapter);
    }

    @Bean
    public UpdateTagCommand updateTagCommand(JpaTagRepositoryAdapter jpaTagRepositoryAdapter) {
        return new TagCommandService(jpaTagRepositoryAdapter);
    }

    @Bean
    public DeleteTagCommand deleteTagCommand(JpaTagRepositoryAdapter jpaTagRepositoryAdapter) {
        return new TagCommandService(jpaTagRepositoryAdapter);
    }

    @Bean
    public ChangeTagStatusCommand changeTagStatusCommand(JpaTagRepositoryAdapter jpaTagRepositoryAdapter) {
        return new TagCommandService(jpaTagRepositoryAdapter);
    }

    // VIDEOS

    @Bean
    public VideoQuery videoQuery(FfmpegAdapter ffmpegAdapter, JpaVideoRepositoryAdapter jpaVideoRepositoryAdapter) {
        return new VideoQueryService(ffmpegAdapter, jpaVideoRepositoryAdapter);
    }

    @Bean
    public VideoMetadataQuery videoMetadataQuery(FfmpegAdapter ffmpegAdapter, JpaVideoRepositoryAdapter jpaVideoRepositoryAdapter) {
        return new VideoQueryService(ffmpegAdapter, jpaVideoRepositoryAdapter);
    }

    @Bean
    public UpdateVideoPathCommand updateVideoPathCommand(FfmpegAdapter ffmpegAdapter, JpaVideoRepositoryAdapter jpaVideoRepositoryAdapter) {
        return new VideoCommandService(jpaVideoRepositoryAdapter, ffmpegAdapter);
    }

    @Bean
    public CreateVideoCommand createVideoCommand(FfmpegAdapter ffmpegAdapter, JpaVideoRepositoryAdapter jpaVideoRepositoryAdapter) {
        return new VideoCommandService(jpaVideoRepositoryAdapter, ffmpegAdapter);
    }

    @Bean
    public DeleteVideoCommand deleteVideoCommand(FfmpegAdapter ffmpegAdapter, JpaVideoRepositoryAdapter jpaVideoRepositoryAdapter) {
        return new VideoCommandService(jpaVideoRepositoryAdapter,ffmpegAdapter);
    }

    @Bean
    public LoadVideoCommand loadVideoCommand(FfmpegAdapter ffmpegAdapter, JpaVideoRepositoryAdapter jpaVideoRepositoryAdapter) {
        return new VideoCommandService(jpaVideoRepositoryAdapter,ffmpegAdapter);
    }

    // COMMENTS

    @Bean
    public CommentsQuery commentsQuery(JpaCommentRepositoryAdapter jpaCommentRepositoryAdapter) {
        return new CommentQueryService(jpaCommentRepositoryAdapter);
    }

    @Bean
    public ChangeCommentContentCommand changeCommentContentCommand(JpaCommentRepositoryAdapter jpaCommentRepositoryAdapter) {
        return new CommentCommandService(jpaCommentRepositoryAdapter);
    }

    @Bean
    public ChangeCommentListingOrderCommand changeCommentListingOrderCommand(JpaCommentRepositoryAdapter jpaCommentRepositoryAdapter) {
        return new CommentCommandService(jpaCommentRepositoryAdapter);
    }

    @Bean
    public CreateCommentCommand createCommentCommand(JpaCommentRepositoryAdapter jpaCommentRepositoryAdapter) {
        return new CommentCommandService(jpaCommentRepositoryAdapter);
    }

    @Bean
    public DeleteCommentCommand deleteCommentCommand(JpaCommentRepositoryAdapter jpaCommentRepositoryAdapter) {
        return new CommentCommandService(jpaCommentRepositoryAdapter);
    }
}
