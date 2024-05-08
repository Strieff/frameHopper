package com.example.engineer.Model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "Video")
@Getter
@Setter
@Builder
@AllArgsConstructor
public class Video {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private int id;
    @Column(name = "name", length = 255)
    private String name;
    @OneToMany(mappedBy = "video")
    private List<Frame> frames;
    @OneToMany(mappedBy = "video", cascade = CascadeType.ALL)
    private List<Comment> comments;
    private Integer totalFrames;
    private Double frameRate;
    private Double duration;
    private Integer videoHeight;
    private Integer videoWidth;

    public Video() {

    }
}
