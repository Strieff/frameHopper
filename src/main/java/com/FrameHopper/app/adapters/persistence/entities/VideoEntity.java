package com.FrameHopper.app.adapters.persistence.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "VideoEntity")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VideoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private int id;
    @Column(name = "name")
    private String name;
    @Column(name = "path")
    private String path;
    @OneToMany(mappedBy = "videoEntity")
    private List<FrameEntity> frameEntities;
    @OneToMany(mappedBy = "videoEntity", cascade = CascadeType.ALL)
    private List<CommentEntity> commentEntities;
    private Integer totalFrames;
    private Double frameRate;
    private Double duration;
    private Integer videoHeight;
    private Integer videoWidth;

    @Override
    public boolean equals(Object o) {
        if(o instanceof VideoEntity)
            return (((VideoEntity) o).getId()) == this.getId();

        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
