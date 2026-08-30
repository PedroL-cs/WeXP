package com.wexp.controller;

import com.wexp.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.service.annotation.GetExchange;

@RestController
@RequestMapping("/api/v1/images")
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;

    @GetMapping("/games/{gameId}/{imageType}")
    public ResponseEntity<Resource> getImage(@PathVariable String gameId, @PathVariable String imageType) {
        return imageService.getImage(gameId, imageType);
    }

    @GetMapping("/achievements/{achievementId}")
    public ResponseEntity<Resource> getAchievements(@PathVariable String achievementId) {
        return imageService.getAchievementIcon(achievementId);
    }
}
