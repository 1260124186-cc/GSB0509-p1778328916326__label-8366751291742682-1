package com.activities.service;

import com.activities.dto.ActivityDto;
import com.activities.entity.Activity;
import com.activities.enums.ActivityStatus;
import com.activities.exception.NotFoundException;
import com.activities.repository.ActivityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ActivityService {

    private final ActivityRepository activityRepository;

    public Activity createActivity(ActivityDto.CreateRequest request) {
        if (request.getEndTime().isBefore(request.getStartTime())) {
            throw new IllegalArgumentException("结束时间不能早于开始时间");
        }

        Activity activity = new Activity();
        activity.setTitle(request.getTitle());
        activity.setStartTime(request.getStartTime());
        activity.setEndTime(request.getEndTime());
        activity.setDescription(request.getDescription());
        activity.setStatus(ActivityStatus.DRAFT);
        activity.setCreatedAt(LocalDateTime.now());

        return activityRepository.save(activity);
    }

    public List<Activity> getAllActivities() {
        return activityRepository.findAll();
    }

    public Activity getActivity(Long id) {
        return activityRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Activity not found with id: " + id));
    }

    @Transactional
    public ActivityDto.PublishResponse publishActivity(Long id) {
        Activity activity = getActivity(id);

        if (activity.getStatus() == ActivityStatus.PUBLISHED) {
            throw new IllegalStateException("Activity is already published");
        }

        String token = UUID.randomUUID().toString();
        activity.setCheckinToken(token);
        activity.setStatus(ActivityStatus.PUBLISHED);

        activityRepository.save(activity);

        ActivityDto.PublishResponse response = new ActivityDto.PublishResponse();
        response.setId(activity.getId());
        response.setCheckinToken(token);
        // URL will be constructed by frontend or controller, but here we provide token
        return response;
    }

    @Transactional
    public Activity closeActivity(Long id) {
        Activity activity = getActivity(id);
        activity.setStatus(ActivityStatus.CLOSED);
        return activityRepository.save(activity);
    }

    public Activity getActivityByToken(String token) {
        return activityRepository.findByCheckinToken(token)
                .orElseThrow(() -> new NotFoundException("Invalid checkin token"));
    }
}
