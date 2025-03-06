package com.example.engineer.Model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "Video")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Video {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private int id;
    @Column(name = "name")
    private String name;
    @Column(name = "path")
    private String path;
    @OneToMany(mappedBy = "video")
    private List<Frame> frames;
    @OneToMany(mappedBy = "video", cascade = CascadeType.ALL)
    private List<Comment> comments;
    private Integer totalFrames;
    private Double frameRate;
    private Double duration;
    private Integer videoHeight;
    private Integer videoWidth;

    @Override
    public boolean equals(Object o) {
        if(o instanceof Video)
            return (((Video) o).getId()) == this.getId();

        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);  // Use the ID field for hashCode
    }
}
