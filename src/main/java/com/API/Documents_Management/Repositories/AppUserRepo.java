package com.API.Documents_Management. Repositories;

import com.API.Documents_Management.Direction.Direction;
import com.API.Documents_Management.Division.Division;
import com.API.Documents_Management. Entities.AppUser;
import com.API.Documents_Management. Entities.Authority;
import com.API.Documents_Management. Entities.Role;
import com.API.Documents_Management. Enums.RoleType;
import com.API.Documents_Management.SousDirection.SousDirection;
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

    List<AppUser> findAllByDirection(Direction direction);

    List<AppUser> findAllByDivision(Division division);

    @Query("""
    SELECT u FROM AppUser u
    JOIN FETCH u.division
    WHERE u.direction.division = :division
""")
    List<AppUser> findAllByDirection_DivisionWithDivisionLoaded(@Param("division") Division division);



}