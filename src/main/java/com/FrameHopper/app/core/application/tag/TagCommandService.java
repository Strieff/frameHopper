package com.FrameHopper.app.core.application.tag;

import com.FrameHopper.app.core.domain.Tag;
import com.FrameHopper.app.core.ports.in.tag.ChangeTagStatusCommand;
import com.FrameHopper.app.core.ports.in.tag.CreateTagCommand;
import com.FrameHopper.app.core.ports.in.tag.DeleteTagCommand;
import com.FrameHopper.app.core.ports.in.tag.UpdateTagCommand;
import com.FrameHopper.app.core.ports.out.repository.TagRepositoryPort;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TagCommandService implements
        CreateTagCommand,
        UpdateTagCommand,
        DeleteTagCommand,
        ChangeTagStatusCommand {
    private final TagRepositoryPort tagRepositoryPort;

    @Override
    public Tag ChangeTagStatus(int id) {
        return tagRepositoryPort.updateStatus(id);
    }

    @Override
    public Tag CreateTag(Tag tag) {
        if (tag.getName() == null || tag.getName().isBlank())
            throw new IllegalArgumentException(); //TODO

        return tagRepositoryPort.create(tag);
    }

    @Override
    public void DeleteTag(int id) {
        tagRepositoryPort.delete(id);
    }

    @Override
    public Tag UpdateTag(Tag tag) {
        if (tag.getName() == null || tag.getName().isBlank())
            throw new IllegalArgumentException(); //TODO

        return tagRepositoryPort.update(tag);
    }
}
