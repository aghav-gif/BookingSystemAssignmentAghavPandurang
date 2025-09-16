//package com.example.booking.security;
//
//import com.example.booking.model.User;
//import com.example.booking.repository.UserRepository;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.core.userdetails.*;
//import org.springframework.stereotype.Service;
//
//@Service
//public class CustomUserDetailsService implements UserDetailsService {
//    private final UserRepository userRepo;
//    public CustomUserDetailsService(UserRepository userRepo) { this.userRepo = userRepo; }
//
//    @Override
//    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//        User u = userRepo.findByUsername(username)
//                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
//        var authorities = u.getRoles().stream().map(r -> new SimpleGrantedAuthority(r.name())).toList();
//        return new org.springframework.security.core.userdetails.User(u.getUsername(), u.getPassword(), u.isEnabled(), true, true, true, authorities);
//    }
//}
