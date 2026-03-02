package com.sd.backend.repository;

import com.sd.backend.model.Reminder;
import com.sd.backend.model.enums.ReminderType;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReminderRepository extends MongoRepository<Reminder, String> {
    List<Reminder> findByUserId(String userId);

    List<Reminder> findByUserIdAndType(String userId, ReminderType type);

    List<Reminder> findByUserIdAndIsRead(String userId, Boolean isRead);

    void deleteByUserId(String userId);
}
