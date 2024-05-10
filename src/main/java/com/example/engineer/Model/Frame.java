package com.example.engineer.Model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(
        name = "Frame",
        uniqueConstraints = @UniqueConstraint(columnNames = {"frame_number", "video_id"})
)
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Frame {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private int id;
    @Column(name = "frame_number")
    private int frameNumber;
    @ManyToOne
    @JoinColumn(name = "video_id")
    private Video video;
    @ManyToMany
    @JoinTable(
            name = "Frame_Tag",
            joinColumns = @JoinColumn(name = "frame_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private List<Tag> tags;
}
