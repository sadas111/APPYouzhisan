package com.chenghua.service.impl;

import com.chenghua.dto.LoginRequest;
import com.chenghua.entity.User;
import com.chenghua.repository.UserRepository;
import com.chenghua.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@lombok.extern.slf4j.Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User login(LoginRequest request) {
        log.info("Attempting login for user: {}", request.getUsername());
        Optional<User> userOpt = userRepository.findByUsername(request.getUsername());
        
        if (userOpt.isEmpty()) {
            log.warn("Login failed: User not found for username: {}", request.getUsername());
            throw new RuntimeException("Invalid username or password");
        }
        
        User user = userOpt.get();
        String stored = user.getPassword() == null ? "" : user.getPassword();
        boolean ok;
        if (stored.startsWith("$2a$") || stored.startsWith("$2b$") || stored.startsWith("$2y$")) {
            ok = passwordEncoder.matches(request.getPassword(), stored);
        } else {
            ok = stored.equals(request.getPassword());
            if (ok) {
                // Seamless migration: upgrade plaintext password to BCrypt on successful login
                user.setPassword(passwordEncoder.encode(request.getPassword()));
                userRepository.save(user);
            }
        }

        if (!ok) {
            log.warn("Login failed: Incorrect password for user: {}", request.getUsername());
            throw new RuntimeException("Invalid username or password");
        }
        
        log.info("Login successful for user: {}", request.getUsername());
        return user;
    }

    @Override
    public User createUser(User user) {
        if (user.getRole() == null || user.getRole().isBlank()) {
            user.setRole("USER");
        }
        if (user.getPassword() != null && !user.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        return userRepository.save(user);
    }

    @Override
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User updateUser(Long id, User userDetails) {
        return userRepository.findById(id).map(user -> {
            user.setNickname(userDetails.getNickname());
            user.setAvatar(userDetails.getAvatar());
            // In a real app, handle password updates securely
            if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
                user.setPassword(passwordEncoder.encode(userDetails.getPassword()));
            }
            return userRepository.save(user);
        }).orElseThrow(() -> new RuntimeException("User not found with id " + id));
    }

    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    public User login(String username, String password) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            String stored = user.getPassword() == null ? "" : user.getPassword();
            boolean ok;
            if (stored.startsWith("$2a$") || stored.startsWith("$2b$") || stored.startsWith("$2y$")) {
                ok = passwordEncoder.matches(password, stored);
            } else {
                ok = stored.equals(password);
                if (ok) {
                    user.setPassword(passwordEncoder.encode(password));
                    userRepository.save(user);
                }
            }

            if (ok) {
                return user;
            }
        }
        return null;
    }
}
