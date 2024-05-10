package com.example.engineer.Model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Table(name = "UserSettings")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserSettings {
    @Id
    @GeneratedValue(generator = "sequence_id")
    private Long id;
    @Column(name = "showDeleted")
    private Boolean showDeleted;
}
