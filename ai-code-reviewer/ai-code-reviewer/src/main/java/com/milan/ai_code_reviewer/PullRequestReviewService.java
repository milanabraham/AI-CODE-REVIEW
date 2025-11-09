package com.milan.ai_code_reviewer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
public class PullRequestReviewService {

    private final WebClient webClient;

    public PullRequestReviewService(@Value("${github.token}") String token) {
        this.webClient = WebClient.builder()
                .baseUrl("https://api.github.com")
                .defaultHeader("Authorization", "Bearer " + token)
                .defaultHeader("Accept", "application/vnd.github+json")
                .build();
    }

    // Approve PR
    public void approvePR(String repoFullName, int prNumber, String body) {
        String url = "/repos/" + repoFullName + "/pulls/" + prNumber + "/reviews";
        Map<String, Object> payload = Map.of(
                "event", "APPROVE",
                "body", body
        );
        System.out.println("=== DEBUG APPROVE ===\nURL: " + url + "\nBody: " + payload + "\n=====================");
        webClient.post().uri(url).bodyValue(payload).retrieve().bodyToMono(String.class).block();
    }

    // Request changes
    public void requestChanges(String repoFullName, int prNumber, String body) {
        String url = "/repos/" + repoFullName + "/pulls/" + prNumber + "/reviews";
        Map<String, Object> payload = Map.of(
                "event", "REQUEST_CHANGES",
                "body", body
        );
        System.out.println("=== DEBUG REQUEST CHANGES ===\nURL: " + url + "\nBody: " + payload + "\n=====================");
        webClient.post().uri(url).bodyValue(payload).retrieve().bodyToMono(String.class).block();
    }

    // Neutral comment review (no state change)
    public void commentReview(String repoFullName, int prNumber, String body) {
        String url = "/repos/" + repoFullName + "/pulls/" + prNumber + "/reviews";
        Map<String, Object> payload = Map.of(
                "event", "COMMENT",
                "body", body
        );
        System.out.println("=== DEBUG COMMENT REVIEW ===\nURL: " + url + "\nBody: " + payload + "\n=====================");
        webClient.post().uri(url).bodyValue(payload).retrieve().bodyToMono(String.class).block();
    }
}
