package com.API.Documents_Management.Services;

import com.API.Documents_Management.Entities.AppUser;
import com.API.Documents_Management.Entities.Role;
import com.API.Documents_Management.Repositories.AppUserRepo;

import com.API.Documents_Management.Repositories.RoleRepo;
import com.API.Documents_Management.Services_Impl.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomeUserService implements UserDetailsService {

    private final AppUserRepo userRepo;
    private final RoleRepo roleRepo;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("Loading user by username: {}", username);

        AppUser user = userRepo.findAppUsersByUsernameWithRoles(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        Set<Role> initializedRoles = new HashSet<>();

        for (Role role : user.getRoles()) {
            Role initializedRole = roleRepo.findByIdWithAuthorities(role.getId())
                    .orElseThrow(() -> new RuntimeException("Role not found: " + role.getId()));
            initializedRoles.add(initializedRole);
        }

        user.setRoles(initializedRoles);

        Set<GrantedAuthority> authorities = getAuthorities(user);


        return new CustomUserDetails(user, authorities);
    }

    private Set<GrantedAuthority> getAuthorities(AppUser user) {
        Set<GrantedAuthority> authorities = new HashSet<>();

        for (Role role : user.getRoles()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));

            if (role.getAuthorities() != null) {
                authorities.addAll(
                        role.getAuthorities()
                                .stream()
                                .map(auth -> new SimpleGrantedAuthority(auth.getName().name()))
                                .collect(Collectors.toSet())
                );
            }
        }

        return authorities;
    }
}
