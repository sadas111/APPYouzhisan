package com.chenghua.controller;

import com.chenghua.entity.Post;
import com.chenghua.security.AuthenticatedUserService;
import com.chenghua.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    @Autowired
    private PostService postService;

    @GetMapping
    public List<Post> getAllPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        return postService.getPosts(page, size);
    }

    @Autowired
    private AuthenticatedUserService authenticatedUserService;

    @PostMapping("/{id}/like")
    public Post likePost(@PathVariable Long id) {
        return postService.likePost(id);
    }

    @PostMapping
    public Post createPost(@RequestBody Post post) {
        var user = authenticatedUserService.requireCurrentUser();
        post.setUserId(user.getId());
        post.setUsername(user.getNickname() != null && !user.getNickname().isBlank() ? user.getNickname() : user.getUsername());
        post.setUserAvatar(user.getAvatar());
        return postService.createPost(post);
    }
}
