package com.chenghua.repository;

import com.chenghua.entity.UserWork;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UserWorkRepository extends JpaRepository<UserWork, Long> {
    List<UserWork> findByUserIdOrderByCreateTimeDesc(Long userId);
}
