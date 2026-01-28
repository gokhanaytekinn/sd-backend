package com.sd.backend.repository;

import com.sd.backend.model.Reminder;
import com.sd.backend.model.enums.ReminderType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReminderRepository extends JpaRepository<Reminder, UUID> {
    List<Reminder> findByUserId(UUID userId);
    List<Reminder> findByUserIdAndType(UUID userId, ReminderType type);
    List<Reminder> findByUserIdAndIsRead(UUID userId, Boolean isRead);
}
