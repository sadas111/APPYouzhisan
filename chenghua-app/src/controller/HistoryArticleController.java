package com.chenghua.controller;

import com.chenghua.entity.HistoryArticle;
import com.chenghua.service.HistoryArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
public class HistoryArticleController {

    private final HistoryArticleService historyArticleService;

    @PostMapping
    public ResponseEntity<HistoryArticle> createArticle(@RequestBody HistoryArticle article) {
        return ResponseEntity.ok(historyArticleService.createArticle(article));
    }

    @GetMapping("/{id}")
    public ResponseEntity<HistoryArticle> getArticleById(@PathVariable Long id) {
        return historyArticleService.getArticleById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<HistoryArticle>> getAllArticles() {
        return ResponseEntity.ok(historyArticleService.getAllArticles());
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<HistoryArticle>> getArticlesByType(@PathVariable String type) {
        return ResponseEntity.ok(historyArticleService.getArticlesByType(type));
    }

    @PutMapping("/{id}")
    public ResponseEntity<HistoryArticle> updateArticle(@PathVariable Long id, @RequestBody HistoryArticle article) {
        try {
            return ResponseEntity.ok(historyArticleService.updateArticle(id, article));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteArticle(@PathVariable Long id) {
        historyArticleService.deleteArticle(id);
        return ResponseEntity.ok().build();
    }
}
