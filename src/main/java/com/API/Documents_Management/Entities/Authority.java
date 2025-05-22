package com.API.Documents_Management.Entities;

import com.API.Documents_Management.Enums.AuthorityName;
import jakarta.persistence.*;

@Entity
public class Authority {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(unique = true, nullable = false)
    @Enumerated(EnumType.STRING)
    private AuthorityName name;

    // Getters et setters...


    public Authority(Long id, String name) {
        this.id = id;
        this.name = AuthorityName.valueOf(name);
    }

    public Authority() {
    }

    public AuthorityName getName() {
        return name;
    }

    public void setName(String name) {
        this.name = AuthorityName.valueOf(name);
    }

    public Long getId() {
        return id;
    }
}