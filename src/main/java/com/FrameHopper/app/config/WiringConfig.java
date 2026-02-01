package com.FrameHopper.app.config;

import com.FrameHopper.app.adapters.persistence.jpa.JpaTagRepositoryAdapter;
import com.FrameHopper.app.core.application.FrameQueryService;
import com.FrameHopper.app.core.application.tag.TagCommandService;
import com.FrameHopper.app.core.application.tag.TagQueryService;
import com.FrameHopper.app.core.ports.in.FrameQuery;
import com.FrameHopper.app.core.ports.in.tag.*;
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
    public TagsQuery tagsQuery(JpaTagRepositoryAdapter jpaTagRepositoryAdapter) {
        return new TagQueryService(jpaTagRepositoryAdapter);
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



    // COMMENTS




}
