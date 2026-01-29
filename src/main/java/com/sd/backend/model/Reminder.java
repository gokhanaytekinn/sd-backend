package com.sd.backend.model;

import com.sd.backend.model.enums.ReminderType;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DBRef;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Objects;

@Document(collection = "reminders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Reminder {

    @Id
    private String id;

    @DBRef
    private User user;

    private ReminderType type;

    private String title;

    private String message;

    private LocalDateTime scheduledAt;

    private LocalDateTime sentAt;

    private Boolean isRead = false;

    private String metadata;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Reminder)) return false;
        Reminder reminder = (Reminder) o;
        return id != null && Objects.equals(id, reminder.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
