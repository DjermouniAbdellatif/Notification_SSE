package com.API.Documents_Management.Entities;

import com.API.Documents_Management.Direction.Direction;
import com.API.Documents_Management.Division.Division;
import com.API.Documents_Management.SousDirection.SousDirection;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AppUser  {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "app_user_seq")
    @SequenceGenerator(
            name = "user_seq",
            sequenceName = "app_user_seq",
            allocationSize = 1
    )
    private Long id;
    private String username;
    private String email;
    private String password;


    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_role",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    private Division division;

    @ManyToOne(fetch = FetchType.LAZY)
    private Direction direction;

    @ManyToOne(fetch = FetchType.LAZY)
    private SousDirection sousDirection;

}