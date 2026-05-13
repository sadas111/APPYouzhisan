package com.chenghua.service;

import com.chenghua.dto.LoginRequest;
import com.chenghua.entity.User;
import java.util.List;
import java.util.Optional;

public interface UserService {
    User login(LoginRequest request);
    User createUser(User user);
    Optional<User> getUserById(Long id);
    Optional<User> getUserByUsername(String username);
    List<User> getAllUsers();
    User updateUser(Long id, User user);
    void deleteUser(Long id);
    User login(String username, String password);
}
