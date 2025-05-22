package com.API.Documents_Management.Services;

import com.API.Documents_Management.Entities.AppUser;
import com.API.Documents_Management.Repositories.AppUserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomeUserService implements UserDetailsService {

    private final AppUserRepo userRepo;


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUser user = userRepo.findAppUsersByUsernameWithRoles(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return new User(user.getUsername(), user.getPassword(), getAuthoriy(user));
    }

    private Collection<? extends GrantedAuthority> getAuthoriy(AppUser user) {

        List<GrantedAuthority> authorities=new ArrayList<>();

        user.getRoles().forEach(role->{
            authorities.add(new SimpleGrantedAuthority("ROLE_"+role.getName()));
        });

        return authorities;
    }
}
