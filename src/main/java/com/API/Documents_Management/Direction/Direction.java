package com.API.Documents_Management.Direction;

import com.API.Documents_Management.Division.Division;
import com.API.Documents_Management.SousDirection.SousDirection;
import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Direction {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "direction_seq_gen")
    @SequenceGenerator(name = "direction_seq_gen", sequenceName = "direction_seq", allocationSize = 1)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "division_id", nullable = false)
    private Division division;

}