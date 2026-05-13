package com.chenghua.controller;

import com.chenghua.entity.Tutorial;
import com.chenghua.service.TutorialService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tutorials")
@RequiredArgsConstructor
public class TutorialController {

    private final TutorialService tutorialService;

    @PostMapping
    public ResponseEntity<Tutorial> createTutorial(@RequestBody Tutorial tutorial) {
        return ResponseEntity.ok(tutorialService.createTutorial(tutorial));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Tutorial> getTutorialById(@PathVariable Long id) {
        return tutorialService.getTutorialById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<Tutorial>> getAllTutorials() {
        return ResponseEntity.ok(tutorialService.getAllTutorials());
    }

    @GetMapping("/difficulty/{difficulty}")
    public ResponseEntity<List<Tutorial>> getTutorialsByDifficulty(@PathVariable String difficulty) {
        return ResponseEntity.ok(tutorialService.getTutorialsByDifficulty(difficulty));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Tutorial> updateTutorial(@PathVariable Long id, @RequestBody Tutorial tutorial) {
        try {
            return ResponseEntity.ok(tutorialService.updateTutorial(id, tutorial));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTutorial(@PathVariable Long id) {
        tutorialService.deleteTutorial(id);
        return ResponseEntity.ok().build();
    }
}
