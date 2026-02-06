package com.FrameHopper.app.adapters.persistence.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(
        name = "FrameEntity",
        uniqueConstraints = @UniqueConstraint(columnNames = {"frame_number", "video_id"})
)
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FrameEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private int id;
    @Column(name = "frame_number")
    private int frameNumber;
    @ManyToOne
    @JoinColumn(name = "video_id")
    private VideoEntity videoEntity;
    @ManyToMany
    @JoinTable(
            name = "Frame_Tag",
            joinColumns = @JoinColumn(name = "frame_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private List<TagEntity> tagEntities;
}
