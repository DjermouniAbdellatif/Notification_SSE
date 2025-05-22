package com.API.Documents_Management. Repositories;

import com.API.Documents_Management. Entities.AppUser;
import com.API.Documents_Management. Entities.Authority;
import com.API.Documents_Management. Entities.Role;
import com.API.Documents_Management. Enums.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AppUserRepo extends JpaRepository<AppUser, Long> {


    @Query("SELECT u FROM AppUser u LEFT JOIN FETCH u.roles WHERE u.username = :username")
    Optional<AppUser> findAppUsersByUsernameWithRoles(@Param("username") String username);

    Optional<AppUser>  findAppUserByUsername(String username);

    Optional<AppUser> findAppUserById(Long id);

    List<AppUser> findByRolesContaining(Role role);

}