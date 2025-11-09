package com.milan.ai_code_reviewer.repository;

import com.milan.ai_code_reviewer.model.ReviewRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ReviewRecordRepository
        extends JpaRepository<ReviewRecord, Long>, JpaSpecificationExecutor<ReviewRecord> {

    long countByStatus(String status);
}
