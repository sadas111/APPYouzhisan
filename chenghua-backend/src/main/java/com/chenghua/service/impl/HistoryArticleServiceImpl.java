package com.chenghua.service.impl;

import com.chenghua.entity.HistoryArticle;
import com.chenghua.repository.HistoryArticleRepository;
import com.chenghua.service.HistoryArticleService;
import com.chenghua.security.XssSanitizer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class HistoryArticleServiceImpl implements HistoryArticleService {

    private final HistoryArticleRepository historyArticleRepository;

    @Override
    public HistoryArticle createArticle(HistoryArticle article) {
        article.setTitle(XssSanitizer.sanitize(article.getTitle()));
        article.setContent(XssSanitizer.sanitize(article.getContent()));
        article.setAuthor(XssSanitizer.sanitize(article.getAuthor()));
        return historyArticleRepository.save(article);
    }

    @Override
    public Optional<HistoryArticle> getArticleById(Long id) {
        return historyArticleRepository.findById(id);
    }

    @Override
    public List<HistoryArticle> getAllArticles() {
        return historyArticleRepository.findAll();
    }

    @Override
    public List<HistoryArticle> getArticlesByType(String type) {
        return historyArticleRepository.findByType(type);
    }

    @Override
    public HistoryArticle updateArticle(Long id, HistoryArticle articleDetails) {
        return historyArticleRepository.findById(id).map(article -> {
            article.setTitle(XssSanitizer.sanitize(articleDetails.getTitle()));
            article.setContent(XssSanitizer.sanitize(articleDetails.getContent()));
            article.setImageUrl(articleDetails.getImageUrl());
            article.setAuthor(XssSanitizer.sanitize(articleDetails.getAuthor()));
            article.setType(XssSanitizer.sanitize(articleDetails.getType()));
            return historyArticleRepository.save(article);
        }).orElseThrow(() -> new RuntimeException("Article not found with id " + id));
    }

    @Override
    public void deleteArticle(Long id) {
        historyArticleRepository.deleteById(id);
    }
}
