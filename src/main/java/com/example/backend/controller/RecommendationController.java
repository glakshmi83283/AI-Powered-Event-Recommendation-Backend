package com.example.backend.controller;

import com.example.backend.service.RecommendationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(
    origins = {
        "http://localhost:5173",
        "http://localhost:5174"
    }
)
@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(
            RecommendationService recommendationService) {

        this.recommendationService =
                recommendationService;
    }

    @GetMapping("/{userId}")
    public List<RecommendationService.RecommendationResult>
    getRecommendations(@PathVariable Long userId) {

        return recommendationService
                .getRecommendations(userId);
    }
}