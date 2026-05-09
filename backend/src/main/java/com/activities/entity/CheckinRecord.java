package com.activities.entity;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "checkin_record")
@EntityListeners(AuditingEntityListener.class)
public class CheckinRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long activityId;

    @Column(nullable = false, length = 100)
    private String name;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime checkinTime;
}
