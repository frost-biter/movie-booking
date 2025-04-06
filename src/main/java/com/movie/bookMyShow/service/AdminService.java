package com.movie.bookMyShow.service;

import com.movie.bookMyShow.dto.CredentialsRequest;
import com.movie.bookMyShow.exception.InvalidCredentialsException;
import com.movie.bookMyShow.exception.UserNotFoundException;
import com.movie.bookMyShow.model.Admin;
import com.movie.bookMyShow.repo.AdminRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AdminService {

    @Autowired
    private AdminRepo adminRepo;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    public String register(CredentialsRequest request) {
        if (adminRepo.findByUsername(request.getUsername()).isPresent()) {
            return "Username already exists";
        }
        Admin admin = new Admin();
        admin.setUsername(request.getUsername());
        admin.setPassword(passwordEncoder.encode(request.getPassword()));
        admin.setRole("ADMIN");
        adminRepo.save(admin);
        return "Admin registered successfully";
    }

    public String login(CredentialsRequest request) {
        Admin admin = adminRepo.findByUsername(request.getUsername())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        boolean passwordMatch = passwordEncoder.matches(request.getPassword(), admin.getPassword());
        if (!passwordMatch) {
            throw new InvalidCredentialsException("Invalid credentials");
        }

        return "Login successful";
    }

    public boolean existsUsername(String username) {
        Optional<Admin> admin = adminRepo.findByUsername(username);
        return admin.isPresent();
    }

    public Admin findByUsername(String username) {
        return adminRepo.findByUsername(username).orElse(null);
    }
}