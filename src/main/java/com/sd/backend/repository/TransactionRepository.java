package com.sd.backend.repository;

import com.sd.backend.model.Transaction;
import com.sd.backend.model.enums.TransactionStatus;
import com.sd.backend.model.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends MongoRepository<Transaction, String> {
    Page<Transaction> findByUserId(String userId, Pageable pageable);

    Page<Transaction> findByUserIdAndType(String userId, TransactionType type, Pageable pageable);

    Page<Transaction> findByUserIdAndStatus(String userId, TransactionStatus status, Pageable pageable);

    Page<Transaction> findByUserIdAndTypeAndStatus(String userId, TransactionType type, TransactionStatus status,
            Pageable pageable);

    void deleteBySubscriptionId(String subscriptionId);

    List<Transaction> findByStatusAndTypeIn(TransactionStatus status, List<TransactionType> types);

    void deleteByUserId(String userId);
}
