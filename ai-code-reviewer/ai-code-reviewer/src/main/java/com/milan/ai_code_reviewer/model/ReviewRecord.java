package com.milan.ai_code_reviewer.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "review_records")
public class ReviewRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String repo;

    private int prNumber;

    private int riskScore;

    private String status;  // low-risk, medium-risk, high-risk

    private String labels;  // ai-reviewed, binary-files etc.

    @Column(columnDefinition = "TEXT")
    private String aiReview;  // full AI review text

    private LocalDateTime createdAt = LocalDateTime.now();
}
