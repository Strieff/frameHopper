package com.example.engineer.Model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "Tag")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Tag {
    @Id
    @GeneratedValue(generator = "sequence_id")
    @Column(name = "id")
    private Integer id;
    @Column(name = "name", length = 255)
    private String name;
    @Column(name = "tag_value")
    private Double value;
    @Column(name = "description", length = 1500)
    private String description;
    @Column(name = "deleted")
    private boolean deleted;

    @ManyToMany(mappedBy = "tags")
    private List<Frame> frames;
}
