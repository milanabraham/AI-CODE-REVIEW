package com.milan.ai_code_reviewer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.Map;

@Service
public class GitHubCommentService {

    private final WebClient webClient;

    public GitHubCommentService(@Value("${github.token}") String token) {
        this.webClient = WebClient.builder()
                .baseUrl("https://api.github.com")
                .defaultHeader("Authorization", "Bearer " + token)
                .defaultHeader("Accept", "application/vnd.github.v3+json")
                .build();
    }

    public void postComment(String repoFullName, int prNumber, String comment) {

        String url = "/repos/" + repoFullName + "/issues/" + prNumber + "/comments";

        Map<String, Object> body = Map.of(
                "body", comment
        );

        System.out.println("=== DEBUG: POSTING COMMENT ===");
        System.out.println("URL: " + url);
        System.out.println("Body: " + body);
        System.out.println("================================");

        webClient.post()
                .uri(url)
                .bodyValue(body) // ✅ Let Jackson encode JSON safely
                .retrieve()
                .bodyToMono(String.class)
                .doOnError(err -> {
                    System.out.println("🔥 ERROR posting comment:");
                    err.printStackTrace();
                })
                .block();
    }
}
