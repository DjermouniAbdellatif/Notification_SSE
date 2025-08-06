package com.API.Documents_Management.Direction;

import com.API.Documents_Management.Courriel.Entities.Structure;
import com.API.Documents_Management.Courriel.Enums.TypeStructure;
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
public class Direction implements Structure {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "direction_seq_gen")
    @SequenceGenerator(name = "direction_seq_gen", sequenceName = "direction_seq", allocationSize = 1)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "division_id", nullable = false)
    private Division division;



    @Override
    public TypeStructure getTypeStructure() {
        return TypeStructure.DIRECTION;
    }

    @Override
    public Long getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.name;
    }
}