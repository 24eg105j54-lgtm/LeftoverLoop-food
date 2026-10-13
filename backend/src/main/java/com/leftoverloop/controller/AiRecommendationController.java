package com.leftoverloop.controller;

import com.leftoverloop.dto.AiMatchRequest;
import com.leftoverloop.dto.AiMatchResponse;
import com.leftoverloop.service.AiRecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class AiRecommendationController {

    @Autowired
    private AiRecommendationService aiRecommendationService;

    @PostMapping("/recommendations")
    public ResponseEntity<AiMatchResponse> getRecommendations(@RequestBody AiMatchRequest request) {
        return ResponseEntity.ok(aiRecommendationService.getRecommendations(request));
    }
}
