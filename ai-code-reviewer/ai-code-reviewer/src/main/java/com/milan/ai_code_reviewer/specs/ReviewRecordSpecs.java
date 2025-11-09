package com.milan.ai_code_reviewer.specs;


import com.milan.ai_code_reviewer.model.ReviewRecord;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public final class ReviewRecordSpecs {

    private ReviewRecordSpecs() {}

    public static Specification<ReviewRecord> repoEquals(String repo) {
        return (root, q, cb) -> repo == null || repo.isBlank()
                ? cb.conjunction()
                : cb.equal(root.get("repo"), repo);
    }

    public static Specification<ReviewRecord> statusEquals(String status) {
        return (root, q, cb) -> status == null || status.isBlank()
                ? cb.conjunction()
                : cb.equal(root.get("status"), status);
    }

    public static Specification<ReviewRecord> riskBetween(Integer min, Integer max) {
        return (root, q, cb) -> {
            if (min == null && max == null) return cb.conjunction();
            if (min != null && max != null) return cb.between(root.get("riskScore"), min, max);
            if (min != null) return cb.greaterThanOrEqualTo(root.get("riskScore"), min);
            return cb.lessThanOrEqualTo(root.get("riskScore"), max);
        };
    }

    public static Specification<ReviewRecord> createdFrom(LocalDateTime from) {
        return (root, q, cb) -> from == null ? cb.conjunction() : cb.greaterThanOrEqualTo(root.get("createdAt"), from);
    }

    public static Specification<ReviewRecord> createdTo(LocalDateTime to) {
        return (root, q, cb) -> to == null ? cb.conjunction() : cb.lessThanOrEqualTo(root.get("createdAt"), to);
    }

    /** simple full-text-ish contains on aiReview */
    public static Specification<ReviewRecord> search(String qStr) {
        return (root, q, cb) -> (qStr == null || qStr.isBlank())
                ? cb.conjunction()
                : cb.like(cb.lower(root.get("aiReview")), "%" + qStr.toLowerCase() + "%");
    }
}
