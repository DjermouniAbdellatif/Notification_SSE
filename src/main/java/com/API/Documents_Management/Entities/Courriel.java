package com.API.Documents_Management.Entities;

import com.API.Documents_Management.Components.AuditEntityListener;
import com.API.Documents_Management.Direction.Direction;
import com.API.Documents_Management.Division.Division;
import com.API.Documents_Management.Enums.CourrielType;
import com.API.Documents_Management.SousDirection.SousDirection;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EntityListeners(AuditEntityListener.class)
public class Courriel implements AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String courrielPath;
    private String courrielNumber;



    @Enumerated(EnumType.STRING)
    private CourrielType courrielType;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "courriel_id")
    private Set<File> courrielFiles = new HashSet<>();


    @ManyToOne(fetch = FetchType.LAZY)
    private Division fromDivision;

    @ManyToOne(fetch = FetchType.LAZY)
    private Division toDivision;

    @ManyToOne(fetch = FetchType.LAZY)
    private Direction fromDirection;

    @ManyToOne(fetch = FetchType.LAZY)
    private Direction toDirection;

    @ManyToOne(fetch = FetchType.LAZY)
    private SousDirection fromSousDirection;

    @ManyToOne(fetch = FetchType.LAZY)
    private SousDirection toSousDirection;

    // pour un courrier envoyer ou recu  par un ministére

    @ManyToOne(fetch = FetchType.LAZY)
    private Division fromExternal;

    @ManyToOne(fetch = FetchType.LAZY)
    private Division toExternal;



    @Override
    public Long getId() {
        return this.id;
    }

    @Override
    public String getAuditIdentifier() {
        return this.courrielNumber;
    }
}