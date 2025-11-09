package com.milan.ai_code_reviewer.controller;

import com.milan.ai_code_reviewer.model.ReviewRecord;
import org.springframework.data.domain.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.milan.ai_code_reviewer.service.ReviewRecordService;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reviews")
public class ReviewRecordController {

    private final ReviewRecordService service;

    public ReviewRecordController(ReviewRecordService service) {
        this.service = service;
    }

    /** GET /api/reviews?repo=&status=&riskMin=&riskMax=&from=&to=&q=&page=0&size=20&sort=createdAt,desc */
    @GetMapping
    public Page<ReviewRecord> list(
            @RequestParam(required = false) String repo,
            @RequestParam(required = false) String status,           // low-risk | medium-risk | high-risk
            @RequestParam(required = false) Integer riskMin,
            @RequestParam(required = false) Integer riskMax,
            @RequestParam(required = false) LocalDate from,          // ISO date: 2025-11-08
            @RequestParam(required = false) LocalDate to,
            @RequestParam(required = false, name = "q") String query, // full-text-ish on aiReview
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort
    ) {
        String[] sortParts = sort.split(",");
        Sort s = (sortParts.length == 2)
                ? Sort.by(Sort.Direction.fromString(sortParts[1]), sortParts[0])
                : Sort.by(Sort.Direction.DESC, "createdAt");

        Pageable pageable = PageRequest.of(page, Math.min(size, 200), s);
        return service.search(repo, status, riskMin, riskMax, from, to, query, pageable);
    }

    /** GET /api/reviews/stats */
    @GetMapping("/stats")
    public Map<String, Object> stats() {
        return service.stats();
    }

    /** GET /api/reviews/recent?limit=10 */
    @GetMapping("/recent")
    public List<ReviewRecord> recent(@RequestParam(defaultValue = "10") int limit) {
        return service.latest(Math.min(limit, 100));
    }

    /** GET /api/reviews/{id} */
    @GetMapping("/{id}")
    public ResponseEntity<ReviewRecord> get(@PathVariable long id) {
        ReviewRecord rr = service.get(id);
        return (rr == null) ? ResponseEntity.notFound().build() : ResponseEntity.ok(rr);
    }

    /** DELETE /api/reviews/{id} */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    /** GET /api/reviews/export.csv (applies same filters as list) */
    @GetMapping(value = "/export", produces = "text/csv")
    public ResponseEntity<String> exportCsv(
            @RequestParam(required = false) String repo,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer riskMin,
            @RequestParam(required = false) Integer riskMax,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            @RequestParam(required = false, name = "q") String query
    ) {
        // export up to 5k rows to be safe
        List<ReviewRecord> all = service.search(
                repo, status, riskMin, riskMax, from, to, query,
                PageRequest.of(0, 5000, Sort.by(Sort.Direction.DESC, "createdAt"))
        ).getContent();

        String header = "id,repo,prNumber,riskScore,status,labels,createdAt\n";
        String rows = all.stream()
                .map(r -> String.join(",",
                        String.valueOf(r.getId()),
                        csv(r.getRepo()),
                        String.valueOf(r.getPrNumber()),
                        String.valueOf(r.getRiskScore()),
                        csv(r.getStatus()),
                        csv(r.getLabels()),
                        csv(String.valueOf(r.getCreatedAt()))
                ))
                .collect(Collectors.joining("\n"));

        String csv = header + rows + "\n";

        String filename = URLEncoder.encode("reviews_export.csv", StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + filename)
                .contentType(MediaType.TEXT_PLAIN)
                .body(csv);
    }

    private static String csv(String v) {
        if (v == null) return "";
        String escaped = v.replace("\"", "\"\"");
        if (escaped.contains(",") || escaped.contains("\"") || escaped.contains("\n")) {
            return "\"" + escaped + "\"";
        }
        return escaped;
    }
}
