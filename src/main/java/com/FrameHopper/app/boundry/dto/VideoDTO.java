package com.FrameHopper.app.boundry.dto;

import java.util.List;

public record VideoDTO(
        int id,
        String name,
        String path,
        MetadataDto metadata
) {
}
