package com.chenghua.controller;

import com.chenghua.entity.UserWork;
import com.chenghua.service.UserWorkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/works")
public class UserWorkController {

    @Autowired
    private UserWorkService userWorkService;

    // Save work
    @PostMapping
    public UserWork saveWork(@RequestBody Map<String, Object> payload) {
        Long userId = Long.valueOf(payload.get("userId").toString());
        String imageData = (String) payload.get("imageData");
        return userWorkService.saveWork(userId, imageData);
    }

    // Get user works
    @GetMapping("/user/{userId}")
    public List<UserWork> getUserWorks(@PathVariable Long userId) {
        return userWorkService.getUserWorks(userId);
    }
}
