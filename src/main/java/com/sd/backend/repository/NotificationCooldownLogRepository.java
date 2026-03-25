package com.sd.backend.repository;

import com.sd.backend.model.NotificationCooldownLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationCooldownLogRepository extends MongoRepository<NotificationCooldownLog, String> {

    List<NotificationCooldownLog> findByUserIdInAndNotificationType(List<String> userIds, String notificationType);

    Optional<NotificationCooldownLog> findByUserIdAndNotificationType(String userId, String notificationType);
}

