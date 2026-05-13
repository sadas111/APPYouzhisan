package com.chenghua.service;

import com.chenghua.entity.Post;
import com.chenghua.repository.PostRepository;
import com.chenghua.security.XssSanitizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PostService {

    @Autowired
    private PostRepository postRepository;

    public List<Post> getPosts(int page, int size) {
        int safePage = Math.max(0, page);
        int safeSize = Math.min(Math.max(1, size), 100);

        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createTime"));
        return postRepository.findAll(pageable).getContent();
    }

    public Post likePost(Long id) {
        Post post = postRepository.findById(id).orElseThrow(() -> new RuntimeException("Post not found"));
        post.setLikeCount(post.getLikeCount() + 1);
        return postRepository.save(post);
    }

    public Post createPost(Post post) {
        if (post.getLikeCount() == null) {
            post.setLikeCount(0);
        }
        if (post.getUserId() == null) {
            throw new IllegalArgumentException("Missing userId");
        }
        post.setContent(XssSanitizer.sanitize(post.getContent()));
        return postRepository.save(post);
    }
}
