package com.chenghua.service.impl;

import com.chenghua.entity.Tutorial;
import com.chenghua.repository.TutorialRepository;
import com.chenghua.service.TutorialService;
import com.chenghua.security.XssSanitizer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TutorialServiceImpl implements TutorialService {

    private final TutorialRepository tutorialRepository;

    @Override
    public Tutorial createTutorial(Tutorial tutorial) {
        tutorial.setTitle(XssSanitizer.sanitize(tutorial.getTitle()));
        tutorial.setDescription(XssSanitizer.sanitize(tutorial.getDescription()));
        return tutorialRepository.save(tutorial);
    }

    @Override
    public Optional<Tutorial> getTutorialById(Long id) {
        return tutorialRepository.findById(id);
    }

    @Override
    public List<Tutorial> getAllTutorials() {
        return tutorialRepository.findAll();
    }

    @Override
    public List<Tutorial> getTutorialsByDifficulty(String difficulty) {
        return tutorialRepository.findByDifficulty(difficulty);
    }

    @Override
    public Tutorial updateTutorial(Long id, Tutorial tutorialDetails) {
        return tutorialRepository.findById(id).map(tutorial -> {
            tutorial.setTitle(XssSanitizer.sanitize(tutorialDetails.getTitle()));
            tutorial.setDescription(XssSanitizer.sanitize(tutorialDetails.getDescription()));
            tutorial.setVideoUrl(tutorialDetails.getVideoUrl());
            tutorial.setCoverUrl(tutorialDetails.getCoverUrl());
            tutorial.setDifficulty(tutorialDetails.getDifficulty());
            tutorial.setDuration(tutorialDetails.getDuration());
            return tutorialRepository.save(tutorial);
        }).orElseThrow(() -> new RuntimeException("Tutorial not found with id " + id));
    }

    @Override
    public void deleteTutorial(Long id) {
        tutorialRepository.deleteById(id);
    }
}
