package com.example.booking.controller;

import com.example.booking.dto.AuthRequest;
import com.example.booking.dto.AuthResponse;
import com.example.booking.service.CustomUserDetailsService;
import com.example.booking.security.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authManager;
    private final CustomUserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;

    public AuthController(AuthenticationManager authManager,
                          CustomUserDetailsService uds,
                          JwtUtil jwtUtil) {
        this.authManager = authManager;
        this.userDetailsService = uds;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest req) {
        try {
        	System.out.println("UserName: "+req.getUsername());
        	System.out.println("Password: "+req.getPassword());
        	Authentication auth = authManager.authenticate(
//        		    new UsernamePasswordAuthenticationToken("admin", "admin123")
        			new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword())
        		);
            
            System.out.println("calling before");

            UserDetails userDetails = userDetailsService.loadUserByUsername(req.getUsername());
            
            System.out.println("Username: " + userDetails.getUsername());
            System.out.println("Password: " + userDetails.getPassword());
            System.out.println("Authorities: " + userDetails.getAuthorities());
            String token = jwtUtil.generateToken(userDetails.getUsername());

            return ResponseEntity.ok(new AuthResponse(token));

        } catch (BadCredentialsException ex) {
        	System.out.println(ex.getMessage());
            return ResponseEntity.status(401).build();
        }
    }
}
