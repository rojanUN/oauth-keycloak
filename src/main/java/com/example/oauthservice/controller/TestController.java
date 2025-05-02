package com.example.oauthservice.controller;

import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    private final OAuth2AuthorizedClientService oAuth2AuthorizedClientService;

    public TestController(OAuth2AuthorizedClientService oAuth2AuthorizedClientService) {
        this.oAuth2AuthorizedClientService = oAuth2AuthorizedClientService;
    }

    @GetMapping("/")
    public String getHome() {
        return "I am Home";
    }

    @GetMapping("/menu")
    public String getMenu(OAuth2AuthenticationToken token) {
        OAuth2AuthorizedClient authentication = oAuth2AuthorizedClientService.loadAuthorizedClient(
                token.getAuthorizedClientRegistrationId(),
                token.getName()
        );

        OAuth2AccessToken accessToken = authentication.getAccessToken();

        return "I am Menu " + accessToken.getTokenValue();
    }

}
