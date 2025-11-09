package com.milan.ai_code_reviewer;

import com.milan.ai_code_reviewer.service.ReviewRecordService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/webhook")
public class GitHubWebhookController {

    private final SignatureService signatureService;
    private final GitHubClient githubClient;
    private final AiService aiService;
    private final GitHubCommentService commentService;
    private final GitHubLabelService labelService;
    private final PullRequestReviewService reviewService;
    private final ReviewRecordService recordService;

    public GitHubWebhookController(SignatureService signatureService,
                                   GitHubClient githubClient,
                                   AiService aiService,
                                   GitHubCommentService commentService,
                                   GitHubLabelService labelService,
                                   PullRequestReviewService reviewService,
                                   ReviewRecordService recordService) {

        this.signatureService = signatureService;
        this.githubClient = githubClient;
        this.aiService = aiService;
        this.commentService = commentService;
        this.labelService = labelService;
        this.reviewService = reviewService;
        this.recordService = recordService;
    }

    @PostMapping("/github")
    public ResponseEntity<String> handleGitHubWebhook(
            @RequestBody PullRequestEvent event,
            @RequestHeader(value = "X-GitHub-Event", required = false) String eventType
    ) {

        System.out.println("✅ Webhook received!");

        String repo = event.getRepository().getFull_name();
        int prNumber = event.getPull_request().getNumber();

        System.out.println("PR #" + prNumber + " | Repo: " + repo);

        // ✅ 1. Fetch PR files
        List<Map<String, Object>> files = githubClient.getPullRequestFiles(repo, prNumber);

        if (files == null || files.isEmpty()) {
            commentService.postComment(repo, prNumber, "⚠️ Unable to fetch PR files.");
            return ResponseEntity.ok("No files found");
        }

        boolean binaryDetected = false;
        StringBuilder combinedReview = new StringBuilder();

        combinedReview.append("### 🔍 Multi-file AI Review Report\n");
        combinedReview.append("---\n\n");

        // ✅ 2. Loop through each file
        for (Map<String, Object> file : files) {

            String fileName = (String) file.get("filename");
            String patch = (String) file.get("patch");

            combinedReview.append("## 📄 File: ").append(fileName).append("\n");

            if (patch == null) {
                combinedReview.append("⚠️ Skipped — Binary file.\n\n---\n");
                binaryDetected = true;
                continue;
            }

            String review = aiService.generateReview(patch);
            combinedReview.append(review).append("\n\n---\n");
        }

        // ✅ Add label for binary PRs
        if (binaryDetected) {
            labelService.addLabel(repo, prNumber, "binary-files");
        }

        // ✅ 3. Generate PR Risk Score
        int riskScore = aiService.generateRiskScore(combinedReview.toString());

        String riskBanner;
        if (riskScore <= 30) {
            riskBanner = "🟢 **Low Risk (" + riskScore + "/100)** – Safe to merge.";
        } else if (riskScore <= 70) {
            riskBanner = "🟡 **Medium Risk (" + riskScore + "/100)** – Needs human review.";
        } else {
            riskBanner = "🔴 **High Risk (" + riskScore + "/100)** – Fixes required!";
        }

        String finalReview =
                "## 🚨 PR Risk Score\n" +
                        riskBanner +
                        "\n\n---\n" +
                        combinedReview;

        // ✅ 4. Post AI review comment
        commentService.postComment(repo, prNumber, finalReview);

        labelService.addLabel(repo, prNumber, "ai-reviewed");

        // ✅ 5. Approve / Neutral / Request Changes
        String lower = combinedReview.toString().toLowerCase();

        if (riskScore >= 70 || lower.contains("bug") || lower.contains("fix") || lower.contains("issue")) {
            reviewService.requestChanges(
                    repo, prNumber,
                    "❌ High Risk PR (" + riskScore + "/100). Issues detected by AI."
            );
            labelService.addLabel(repo, prNumber, "high-risk");

        } else if (riskScore >= 40) {
            reviewService.commentReview(
                    repo, prNumber,
                    "⚠️ Medium Risk PR (" + riskScore + "/100). Human review recommended."
            );
            labelService.addLabel(repo, prNumber, "medium-risk");

        } else {
            reviewService.approvePR(
                    repo, prNumber,
                    "✅ Low Risk PR (" + riskScore + "/100). Approved automatically."
            );
            labelService.addLabel(repo, prNumber, "low-risk");
        }

        // ✅ 6. Save review history to MySQL
        recordService.saveRecord(
                repo,
                prNumber,
                riskScore,
                (riskScore <= 30 ? "low-risk" : riskScore <= 70 ? "medium-risk" : "high-risk"),
                "ai-reviewed",
                finalReview
        );

        return ResponseEntity.ok("✅ Completed multi-file AI review with risk scoring");
    }
}
