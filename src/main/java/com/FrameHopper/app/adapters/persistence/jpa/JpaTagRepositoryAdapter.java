package com.FrameHopper.app.adapters.persistence.jpa;

import com.FrameHopper.app.core.domain.Tag;
import com.FrameHopper.app.core.domain.Video;
import com.FrameHopper.app.core.ports.out.repository.TagRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class JpaTagRepositoryAdapter implements TagRepositoryPort {
    @Override
    public Tag getById(int id) {
        return null;
    }

    @Override
    public Tag getByName(String name) {
        return null;
    }

    @Override
    public List<Tag> getAllByVideo(Video video) {
        return null;
    }

    @Override
    public List<Tag> getAll() {
        return null;
    }

    @Override
    public Tag create(Tag tag) {
        return null;
    }

    @Override
    public Tag update(Tag tag) {
        return null;
    }

    @Override
    public void delete(Tag tag) {

    }
}
