package com.API.Documents_Management.Repositories;

import com.API.Documents_Management.Entities.Authority;
import com.API.Documents_Management.Entities.Role;

import com.API.Documents_Management.Enums.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepo extends JpaRepository<Role, Long> {

    @Query("SELECT r FROM Role r LEFT JOIN FETCH r.authorities WHERE r.id = :id")
    Optional<Role> findByIdWithAuthorities(@Param("id") Long id);

    @Query("SELECT r FROM Role r LEFT JOIN FETCH r.authorities WHERE r.name = :name")
    Optional<Role> findByNameWithAuthorities(@Param("name") String name);


    Optional<Role> findById(Long id);

    Optional<Role> findByName(RoleType name);


    void deleteByName(RoleType name);

    void deleteById(Long id);

    List<Role> findAllByNameIn(List<RoleType> names);


    List<Role> findByAuthoritiesContaining(Authority authority);
}
