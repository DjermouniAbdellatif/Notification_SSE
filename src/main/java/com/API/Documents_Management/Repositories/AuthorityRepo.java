package com.API.Documents_Management.Repositories;

import com.API.Documents_Management.Entities.Authority;
import com.API.Documents_Management.Enums.AuthorityName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AuthorityRepo extends JpaRepository<Authority, Long> {

   Optional<Authority> findByName(AuthorityName name);

   Optional<Authority> findAuthorityById(Long id);


   void deleteAuthorityByName(AuthorityName name);

   void deleteAuthorityById(Long id);

   List<Authority> findAllByNameIn(List<AuthorityName> names);

}
