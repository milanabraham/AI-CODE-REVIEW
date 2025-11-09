package com.milan.ai_code_reviewer.service;

import com.milan.ai_code_reviewer.specs.ReviewRecordSpecs;

import com.milan.ai_code_reviewer.model.ReviewRecord;
import com.milan.ai_code_reviewer.repository.ReviewRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReviewRecordService {

    private final ReviewRecordRepository repository;

    public ReviewRecordService(ReviewRecordRepository repository) {
        this.repository = repository;
    }

    public ReviewRecord saveRecord(String repo,
                                   int prNumber,
                                   int riskScore,
                                   String status,
                                   String labels,
                                   String aiReview) {

        ReviewRecord record = new ReviewRecord();
        record.setRepo(repo);
        record.setPrNumber(prNumber);
        record.setRiskScore(riskScore);
        record.setStatus(status);
        record.setLabels(labels);
        record.setAiReview(aiReview);
        return repository.save(record);
    }

    public Page<ReviewRecord> search(
            String repo,
            String status,
            Integer riskMin,
            Integer riskMax,
            LocalDate from,
            LocalDate to,
            String q,
            Pageable pageable
    ) {
        LocalDateTime fromDt = (from == null) ? null : from.atStartOfDay();
        LocalDateTime toDt   = (to == null)   ? null : to.atTime(23, 59, 59);

        Specification<ReviewRecord> spec = Specification
                .where(ReviewRecordSpecs.repoEquals(repo))
                .and(ReviewRecordSpecs.statusEquals(status))
                .and(ReviewRecordSpecs.riskBetween(riskMin, riskMax))
                .and(ReviewRecordSpecs.createdFrom(fromDt))
                .and(ReviewRecordSpecs.createdTo(toDt))
                .and(ReviewRecordSpecs.search(q));

        return repository.findAll(spec, pageable);
    }

    public Map<String, Object> stats() {
        long total = repository.count();
        long low   = countRiskBucket(0, 30);
        long med   = countRiskBucket(31, 70);
        long high  = countRiskBucket(71, 100);

        Map<String, Object> m = new LinkedHashMap<>();
        m.put("total", total);
        m.put("byStatus", Map.of(
                "low-risk", repository.countByStatus("low-risk"),
                "medium-risk", repository.countByStatus("medium-risk"),
                "high-risk", repository.countByStatus("high-risk")
        ));
        m.put("byRiskBucket", Map.of(
                "low(0-30)", low,
                "medium(31-70)", med,
                "high(71-100)", high
        ));
        return m;
    }

    private long countRiskBucket(int min, int max) {
        // quick in-memory count via spec; for large datasets, add a custom query
        Specification<ReviewRecord> spec = ReviewRecordSpecs.riskBetween(min, max);
        return repository.count(spec);
    }

    public List<ReviewRecord> latest(int limit) {
        return repository.findAll(PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt")))
                .getContent();
    }

    public void delete(long id) {
        repository.deleteById(id);
    }

    public ReviewRecord get(long id) {
        return repository.findById(id).orElse(null);
    }
}
