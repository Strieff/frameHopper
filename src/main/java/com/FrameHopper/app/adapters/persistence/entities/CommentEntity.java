package com.FrameHopper.app.adapters.persistence.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "CommentEntity")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private int id;

    @Column(name = "content", length = 1500)
    private String content;

    @Column(name= "listingOrder")
    private int listingOrder;

    @ManyToOne
    @JoinColumn(name = "video_id")
    private VideoEntity videoEntity;
}
