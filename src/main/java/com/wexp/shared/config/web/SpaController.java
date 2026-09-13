package com.wexp.shared.config.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaController {

    @GetMapping(value = {
            "/",
            "/search",
            "/games/{id}",
            "/games/{gameId}/achievements/{achievementId}",
            "/games/{gameId}/achievements/{achievementId}/guide",
            "/games/{gameId}/achievements/{achievementId}/guide/revise",
            "/games/{gameId}/achievements/{achievementId}/guide/create",
            "/profile",
            "/users/{userId}"
    })
    public String forward() {
        return "forward:/index.html";
    }
}