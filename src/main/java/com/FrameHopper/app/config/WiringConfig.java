package com.FrameHopper.app.config;

import com.FrameHopper.app.adapters.ffmpeg.FfmpegAdapter;
import com.FrameHopper.app.adapters.persistence.jpa.JpaCommentRepositoryAdapter;
import com.FrameHopper.app.adapters.persistence.jpa.JpaFrameRepositoryAdapter;
import com.FrameHopper.app.adapters.persistence.jpa.JpaTagRepositoryAdapter;
import com.FrameHopper.app.adapters.persistence.jpa.JpaVideoRepositoryAdapter;
import com.FrameHopper.app.core.application.FrameBytesQueryService;
import com.FrameHopper.app.core.application.analytics.TagAnalyticsQuery;
import com.FrameHopper.app.core.application.analytics.TagAnalyticsService;
import com.FrameHopper.app.core.application.analytics.VideoAnalyticsQuery;
import com.FrameHopper.app.core.application.analytics.VideoAnalyticsService;
import com.FrameHopper.app.core.application.frame.FrameQueryService;
import com.FrameHopper.app.core.application.comment.CommentCommandService;
import com.FrameHopper.app.core.application.comment.CommentQueryService;
import com.FrameHopper.app.core.application.frame.FrameCommandService;
import com.FrameHopper.app.core.application.tag.TagCommandService;
import com.FrameHopper.app.core.application.tag.TagQueryService;
import com.FrameHopper.app.core.application.video.VideoCommandService;
import com.FrameHopper.app.core.application.video.VideoQueryService;
import com.FrameHopper.app.core.ports.in.FrameBytesQuery;
import com.FrameHopper.app.core.ports.in.comment.*;
import com.FrameHopper.app.core.ports.in.frame.CreateFrameCommand;
import com.FrameHopper.app.core.ports.in.frame.DeleteFrameCommand;
import com.FrameHopper.app.core.ports.in.frame.FrameQuery;
import com.FrameHopper.app.core.ports.in.frame.UpdateFrameCommand;
import com.FrameHopper.app.core.ports.in.tag.*;
import com.FrameHopper.app.core.ports.in.video.*;
import com.FrameHopper.app.core.ports.out.FfmpegPort;
import com.FrameHopper.app.core.ports.out.repository.VideoRepositoryPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;


@Configuration
@DependsOn("entityManagerFactory")
public class WiringConfig {
    // --------------------
    // Inbound ports (core)
    // --------------------

    @Bean
    public FrameBytesQueryService frameBytesQuery(FfmpegPort ffmpegPort) {
        return new FrameBytesQueryService(ffmpegPort);
    }

    //region TAGS
    @Bean
    public TagQueryService tagQuery(
            JpaTagRepositoryAdapter jpaTagRepositoryAdapter,
            JpaVideoRepositoryAdapter jpaVideoRepositoryAdapter
    ) {
        return new TagQueryService(jpaTagRepositoryAdapter, jpaVideoRepositoryAdapter);
    }

    @Bean
    public TagCommandService tagCommand(JpaTagRepositoryAdapter jpaTagRepositoryAdapter) {
        return new TagCommandService(jpaTagRepositoryAdapter);
    }
    //endregion

    //region VIDEOS
    @Bean
    public VideoQueryService videoQuery(
            FfmpegAdapter ffmpegAdapter,
            JpaVideoRepositoryAdapter jpaVideoRepositoryAdapter
    ) {
        return new VideoQueryService(ffmpegAdapter, jpaVideoRepositoryAdapter);
    }

    @Bean
    public VideoCommandService videoCommand(
            FfmpegAdapter ffmpegAdapter,
            JpaVideoRepositoryAdapter jpaVideoRepositoryAdapter
    ) {
        return new VideoCommandService(jpaVideoRepositoryAdapter, ffmpegAdapter);
    }
    //endregion

    //region COMMENTS
    @Bean
    public CommentQueryService commentsQuery(
            JpaCommentRepositoryAdapter jpaCommentRepositoryAdapter,
            JpaVideoRepositoryAdapter jpaVideoRepositoryAdapter
    ) {
        return new CommentQueryService(jpaCommentRepositoryAdapter, jpaVideoRepositoryAdapter);
    }

    @Bean
    public CommentCommandService commentCommand(
            JpaCommentRepositoryAdapter jpaCommentRepositoryAdapter,
            VideoRepositoryPort videoRepositoryPort
    ) {
        return new CommentCommandService(jpaCommentRepositoryAdapter, videoRepositoryPort);
    }
    //endregion

    // FRAMES

    @Bean
    public FrameQueryService frameQuery(JpaFrameRepositoryAdapter jpaFrameRepositoryAdapter) {
        return new FrameQueryService(jpaFrameRepositoryAdapter);
    }

    @Bean
    public FrameCommandService frameCommand(JpaFrameRepositoryAdapter jpaFrameRepositoryAdapter) {
        return new FrameCommandService(jpaFrameRepositoryAdapter);
    }

    // ANALYTICS

    @Bean
    public VideoAnalyticsService videoAnalyticsQuery() {
        return new VideoAnalyticsService();
    }

    @Bean
    public TagAnalyticsService tagAnalyticsQuery() {
        return new TagAnalyticsService();
    }
}
