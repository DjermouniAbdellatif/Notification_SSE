package com.API.Documents_Management.SousDirection;

import com.API.Documents_Management.Direction.Direction;
import jakarta.persistence.*;
import lombok.*;


@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SousDirection {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sousdirection_seq_gen")
    @SequenceGenerator(name = "sousdirection_seq_gen", sequenceName = "sousdirection_seq", allocationSize = 1)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "direction_id", nullable = false)
    private Direction direction;
}