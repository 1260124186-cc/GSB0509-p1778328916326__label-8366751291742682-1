package com.activities.service;

import com.activities.dto.CheckinDto;
import com.activities.entity.Activity;
import com.activities.entity.CheckinRecord;
import com.activities.enums.ActivityStatus;
import com.activities.repository.CheckinRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CheckinService {

    private final CheckinRecordRepository checkinRecordRepository;
    private final ActivityService activityService;

    @Transactional
    public CheckinRecord checkin(String token, CheckinDto.Request request) {
        Activity activity = activityService.getActivityByToken(token);

        if (activity.getStatus() == ActivityStatus.CLOSED) {
            throw new IllegalStateException("Activity is closed");
        }
        if (activity.getStatus() != ActivityStatus.PUBLISHED) {
            throw new IllegalStateException("Activity is not published");
        }

        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Name is required");
        }

        CheckinRecord record = new CheckinRecord();
        record.setActivityId(activity.getId());
        record.setName(request.getName().trim());
        record.setCheckinTime(LocalDateTime.now());

        return checkinRecordRepository.save(record);
    }

    public List<CheckinRecord> getCheckins(Long activityId) {
        return checkinRecordRepository.findByActivityIdOrderByCheckinTimeDesc(activityId);
    }

}
