package com.chenghua.service;

import com.chenghua.entity.Tutorial;
import java.util.List;
import java.util.Optional;

public interface TutorialService {
    Tutorial createTutorial(Tutorial tutorial);
    Optional<Tutorial> getTutorialById(Long id);
    List<Tutorial> getAllTutorials();
    List<Tutorial> getTutorialsByDifficulty(String difficulty);
    Tutorial updateTutorial(Long id, Tutorial tutorial);
    void deleteTutorial(Long id);
}
