package com.API.Documents_Management.Courriel.Entities;

import com.API.Documents_Management.Components.AuditEntityListener;
import com.API.Documents_Management.Courriel.Enums.CourrielType;
import com.API.Documents_Management.Courriel.Enums.NatureCourriel;
import com.API.Documents_Management.Direction.Direction;
import com.API.Documents_Management.Division.Division;
import com.API.Documents_Management.Entities.AlgerianMinistry;
import com.API.Documents_Management.Entities.AuditableEntity;
import com.API.Documents_Management.SousDirection.SousDirection;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;

import java.time.LocalDateTime;
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

    @CreatedBy
    @Column(updatable = false)
    private String createdBy;

    private String courrielPath;
    private String courrielNumber;

    private String subject;
    private String description;

    private LocalDateTime sentDate;
    private LocalDateTime arrivedDate;
    private LocalDateTime saveDate;
    private LocalDateTime returnDate;

    @Enumerated(EnumType.STRING)
    private CourrielType courrielType; // ARRIVER ou DEPART

    @Enumerated(EnumType.STRING)
    private NatureCourriel nature; // INTERN ou EXTERN

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "courriel_id")
    private Set<File> courrielFiles = new HashSet<>();

    // Expéditeur
    @ManyToOne(fetch = FetchType.LAZY)
    private Division fromDivision;

    @ManyToOne(fetch = FetchType.LAZY)
    private Direction fromDirection;

    @ManyToOne(fetch = FetchType.LAZY)
    private SousDirection fromSousDirection;

    @ManyToOne(fetch = FetchType.LAZY)
    private AlgerianMinistry fromExternal;

    // Destinations
    @OneToMany(mappedBy = "courriel", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CourrielDestination> destinations = new ArrayList<>();

    @Override
    public Long getId() {
        return this.id;
    }

    @Override
    public String getAuditIdentifier() {
        return this.courrielNumber;
    }
}
