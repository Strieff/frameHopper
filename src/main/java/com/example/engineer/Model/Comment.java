package com.example.engineer.Model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Comment")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private int id;

    @Column(name = "content", length = 1500)
    private String content;

    @ManyToOne
    @JoinColumn(name = "video_id")
    private Video video;
}
