package com.FrameHopper.app.adapters.persistence.jpa;

import com.FrameHopper.app.core.domain.Video;
import com.FrameHopper.app.core.ports.out.repository.VideoRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class JpaVideoRepositoryAdapter implements VideoRepositoryPort {

    @Override
    public Video getById(int id) {
        return null;
    }

    @Override
    public Video getByPath(String path) {
        return null;
    }

    @Override
    public List<Video> getAll() {
        return List.of();
    }

    @Override
    public Video create(Video video) {
        return null;
    }

    @Override
    public Video update(Video video) {
        return null;
    }

    @Override
    public void delete(int id) {

    }
}
