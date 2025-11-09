package com.milan.ai_code_reviewer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
public class GitHubLabelService {

    private final WebClient webClient;

    public GitHubLabelService(@Value("${github.token}") String token) {
        this.webClient = WebClient.builder()
                .baseUrl("https://api.github.com")
                .defaultHeader("Authorization", "Bearer " + token)
                .defaultHeader("Accept", "application/vnd.github+json")
                .build();
    }

    public void addLabel(String repo, int prNumber, String label) {

        String url = "/repos/" + repo + "/issues/" + prNumber + "/labels";

        System.out.println("=== DEBUG: ADDING LABEL ===");
        System.out.println("URL: " + url);
        System.out.println("Label: " + label);

        Map<String, Object> body = Map.of(
                "labels", new String[]{label}
        );

        webClient.post()
                .uri(url)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .doOnError(e -> System.out.println("❌ ERROR adding label: " + e.getMessage()))
                .block();
    }
}
