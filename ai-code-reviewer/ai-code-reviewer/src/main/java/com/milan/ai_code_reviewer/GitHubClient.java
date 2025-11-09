package com.milan.ai_code_reviewer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
public class GitHubClient {

    private final WebClient webClient;

    public GitHubClient(@Value("${github.token}") String token) {
        this.webClient = WebClient.builder()
                .baseUrl("https://api.github.com")
                .defaultHeader("Authorization", "Bearer " + token)
                .defaultHeader("Accept", "application/vnd.github+json")
                .build();
    }

    // ✅ OLD: get full diff (still available, unused now)
    public String getPullRequestDiff(String repoFullName, int prNumber) {

        return webClient.get()
                .uri("/repos/" + repoFullName + "/pulls/" + prNumber)
                .header("Accept", "application/vnd.github.v3.diff")
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    // ✅ NEW: Get all files in the PR (patches included)
    public List<Map<String, Object>> getPullRequestFiles(String repoFullName, int prNumber) {

        return webClient.get()
                .uri("/repos/" + repoFullName + "/pulls/" + prNumber + "/files")
                .retrieve()
                .bodyToMono(List.class)
                .block();
    }
}
