package com.sd.backend.repository;

import com.sd.backend.model.Transaction;
import com.sd.backend.model.enums.TransactionStatus;
import com.sd.backend.model.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    Page<Transaction> findByUserId(UUID userId, Pageable pageable);
    Page<Transaction> findByUserIdAndType(UUID userId, TransactionType type, Pageable pageable);
    Page<Transaction> findByUserIdAndStatus(UUID userId, TransactionStatus status, Pageable pageable);
    Page<Transaction> findByUserIdAndTypeAndStatus(UUID userId, TransactionType type, TransactionStatus status, Pageable pageable);
    List<Transaction> findByStatusAndTypeIn(TransactionStatus status, List<TransactionType> types);
}
