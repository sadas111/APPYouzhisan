package com.chenghua.repository;

import com.chenghua.entity.HistoryArticle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistoryArticleRepository extends JpaRepository<HistoryArticle, Long> {
    List<HistoryArticle> findByType(String type);
}
