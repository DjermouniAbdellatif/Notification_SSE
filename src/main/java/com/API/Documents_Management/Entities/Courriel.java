package com.API.Documents_Management.Entities;

import com.API.Documents_Management.Components.AuditEntityListener;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditEntityListener.class)
public class Courriel implements AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String courrielType;
    private String courrielPath;
    private String courrielNumber;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "courriel_id")
    private Set<File> courrielFiles = new HashSet<>();


    @Override
    public Long getId() {
        return this.id;
    }

    @Override
    public String getAuditIdentifier() {
        return this.courrielNumber;
    }
}