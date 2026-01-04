package com.example.railwayreservation.service;

import com.example.railwayreservation.entity.User;
import com.example.railwayreservation.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // REGISTER
    public User register(User user) {
        Optional<User> existing = userRepository.findByEmail(user.getEmail());

        if (existing.isPresent()) {
            throw new RuntimeException("User already exists");
        }

        return userRepository.save(user);
    }

    // LOGIN
    public User login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.getPassword().equals(password)) {
            throw new RuntimeException("Invalid password");
        }

        return user;
    }
}

