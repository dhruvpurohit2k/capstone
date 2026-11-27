package com.accenture.dhruv.healthdekho.recommendation.controller;

import com.accenture.dhruv.healthdekho.common.ApiResponse;
import com.accenture.dhruv.healthdekho.recommendation.dto.RecommendationRequest;
import com.accenture.dhruv.healthdekho.recommendation.dto.RecommendationResponse;
import com.accenture.dhruv.healthdekho.recommendation.service.PolicyRecommendationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    private final PolicyRecommendationService recommendations;

    public RecommendationController(PolicyRecommendationService recommendations) {
        this.recommendations = recommendations;
    }

    @PostMapping
    public ApiResponse<RecommendationResponse> recommend(Principal customer,
                                                         @Valid @RequestBody RecommendationRequest request) {
        return ApiResponse.success(HttpStatus.OK,
                recommendations.recommend(Long.parseLong(customer.getName()), request));
    }
}
