package com.chenghua.controller;

import com.chenghua.entity.UserWork;
import com.chenghua.security.AuthenticatedUserService;
import com.chenghua.service.UserWorkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/works")
public class UserWorkController {

    @Autowired
    private UserWorkService userWorkService;

    @Autowired
    private AuthenticatedUserService authenticatedUserService;

    // Save work
    @PostMapping
    public UserWork saveWork(@RequestBody Map<String, Object> payload) {
        Long userId = authenticatedUserService.requireCurrentUserId();
        String imageData = (String) payload.get("imageData");
        return userWorkService.saveWork(userId, imageData);
    }

    // Get user works
    @GetMapping("/user/{userId}")
    public List<UserWork> getUserWorks(@PathVariable Long userId) {
        Long currentUserId = authenticatedUserService.requireCurrentUserId();
        if (!authenticatedUserService.isAdmin() && !userId.equals(currentUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Forbidden");
        }
        return userWorkService.getUserWorks(userId);
    }
}
