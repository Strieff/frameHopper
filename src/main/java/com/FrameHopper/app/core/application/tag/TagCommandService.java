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
        return null;
    }

    @Override
    public Tag CreateTag(Tag tag) {
        return null;
    }

    @Override
    public void DeleteTag(Tag tag) {

    }

    @Override
    public Tag UpdateTag(Tag tag) {
        return null;
    }
}
