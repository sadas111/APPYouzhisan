package com.chenghua.service;

import com.chenghua.entity.HistoryArticle;
import java.util.List;
import java.util.Optional;

public interface HistoryArticleService {
    HistoryArticle createArticle(HistoryArticle article);
    Optional<HistoryArticle> getArticleById(Long id);
    List<HistoryArticle> getAllArticles();
    List<HistoryArticle> getArticlesByType(String type);
    HistoryArticle updateArticle(Long id, HistoryArticle article);
    void deleteArticle(Long id);
}
