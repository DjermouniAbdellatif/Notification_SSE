package com.API.Documents_Management.Division;

import com.API.Documents_Management.Courriel.Entities.Structure;
import com.API.Documents_Management.Courriel.Enums.TypeStructure;
import com.API.Documents_Management.Direction.Direction;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Division implements Structure {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "division_seq_gen")
    @SequenceGenerator(name = "division_seq_gen", sequenceName = "division_seq", allocationSize = 1)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Override
    public TypeStructure getTypeStructure() {
        return TypeStructure.DIVISION;
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
