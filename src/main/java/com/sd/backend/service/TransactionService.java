package com.sd.backend.service;

import com.sd.backend.dto.TransactionRequest;
import com.sd.backend.dto.TransactionResponse;
import com.sd.backend.model.Subscription;
import com.sd.backend.model.Transaction;
import com.sd.backend.model.User;
import com.sd.backend.model.enums.TransactionStatus;
import com.sd.backend.model.enums.TransactionType;
import com.sd.backend.repository.SubscriptionRepository;
import com.sd.backend.repository.TransactionRepository;
import com.sd.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionService {
    
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    
    @Transactional(readOnly = true)
    public Page<TransactionResponse> getTransactions(UUID userId, TransactionType type, 
                                                      TransactionStatus status, Pageable pageable) {
        Page<Transaction> transactions;
        
        if (type != null && status != null) {
            transactions = transactionRepository.findByUserIdAndTypeAndStatus(userId, type, status, pageable);
        } else if (type != null) {
            transactions = transactionRepository.findByUserIdAndType(userId, type, pageable);
        } else if (status != null) {
            transactions = transactionRepository.findByUserIdAndStatus(userId, status, pageable);
        } else {
            transactions = transactionRepository.findByUserId(userId, pageable);
        }
        
        return transactions.map(this::toResponse);
    }
    
    @Transactional(readOnly = true)
    public TransactionResponse getTransaction(UUID id, UUID userId) {
        Transaction transaction = transactionRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Transaction not found"));
        
        if (!transaction.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Unauthorized access to transaction");
        }
        
        return toResponse(transaction);
    }
    
    @Transactional
    public TransactionResponse createTransaction(TransactionRequest request, UUID userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setType(request.getType());
        transaction.setAmount(request.getAmount());
        transaction.setCurrency(request.getCurrency());
        transaction.setDescription(request.getDescription());
        transaction.setStatus(TransactionStatus.COMPLETED);
        
        if (request.getSubscriptionId() != null) {
            Subscription subscription = subscriptionRepository.findById(request.getSubscriptionId())
                .orElseThrow(() -> new IllegalArgumentException("Subscription not found"));
            
            if (!subscription.getUser().getId().equals(userId)) {
                throw new IllegalArgumentException("Unauthorized access to subscription");
            }
            
            transaction.setSubscription(subscription);
        }
        
        transaction = transactionRepository.save(transaction);
        return toResponse(transaction);
    }
    
    private TransactionResponse toResponse(Transaction transaction) {
        return new TransactionResponse(
            transaction.getId(),
            transaction.getUser().getId(),
            transaction.getSubscription() != null ? transaction.getSubscription().getId() : null,
            transaction.getType(),
            transaction.getStatus(),
            transaction.getAmount(),
            transaction.getCurrency(),
            transaction.getDescription(),
            transaction.getMetadata(),
            transaction.getCreatedAt(),
            transaction.getUpdatedAt()
        );
    }
}
