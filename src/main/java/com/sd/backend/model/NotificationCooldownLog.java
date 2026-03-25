package com.sd.backend.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "notification_cooldowns")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationCooldownLog {

    @Id
    private String id;

    /**
     * Keep it as a string to avoid DBRef overhead for simple cooldown tracking.
     */
    private String userId;

    private String notificationType;

    private LocalDateTime lastSentAt;
}

