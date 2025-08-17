package com.API.Documents_Management.Courriel.Entities;


import com.API.Documents_Management.Direction.Direction;
import com.API.Documents_Management.Division.Division;
import com.API.Documents_Management.Entities.AlgerianMinistry;
import com.API.Documents_Management.SousDirection.SousDirection;
import jakarta.persistence.*;
import lombok.*;

@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CourrielDestination {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Courriel courriel;

    @ManyToOne(fetch = FetchType.LAZY)
    private Division toDivision;

    @ManyToOne(fetch = FetchType.LAZY)
    private Direction toDirection;

    @ManyToOne(fetch = FetchType.LAZY)
    private SousDirection toSousDirection;

    @ManyToOne(fetch = FetchType.LAZY)
    private AlgerianMinistry toExternal;
}
