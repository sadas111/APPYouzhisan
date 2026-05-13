package com.chenghua.service;

import com.chenghua.entity.UserWork;
import com.chenghua.repository.UserWorkRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class UserWorkService {
    @Autowired
    private UserWorkRepository userWorkRepository;

    public UserWork saveWork(Long userId, String imageData) {
        UserWork work = new UserWork();
        work.setUserId(userId);
        work.setImageData(imageData);
        return userWorkRepository.save(work);
    }

    public List<UserWork> getUserWorks(Long userId) {
        return userWorkRepository.findByUserIdOrderByCreateTimeDesc(userId);
    }
}
