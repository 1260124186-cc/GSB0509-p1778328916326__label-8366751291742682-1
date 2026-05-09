package com.activities.repository;

import com.activities.entity.CheckinRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CheckinRecordRepository extends JpaRepository<CheckinRecord, Long> {
    List<CheckinRecord> findByActivityIdOrderByCheckinTimeDesc(Long activityId);

    List<CheckinRecord> findByActivityIdOrderByCheckinTimeAsc(Long activityId);
}
