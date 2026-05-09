package com.activities.controller;

import com.activities.dto.ActivityDto;
import com.activities.entity.Activity;
import com.activities.entity.CheckinRecord;
import com.activities.service.ActivityService;
import com.activities.service.CheckinService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/activities")
@RequiredArgsConstructor
public class AdminController {

    private final ActivityService activityService;
    private final CheckinService checkinService;

    @PostMapping
    public ResponseEntity<Activity> createActivity(@RequestBody ActivityDto.CreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(activityService.createActivity(request));
    }

    @GetMapping
    public ResponseEntity<List<Activity>> getAllActivities() {
        return ResponseEntity.ok(activityService.getAllActivities());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Activity> getActivity(@PathVariable Long id) {
        return ResponseEntity.ok(activityService.getActivity(id));
    }

    @PostMapping("/{id}/publish")
    public ResponseEntity<ActivityDto.PublishResponse> publishActivity(@PathVariable Long id) {
        return ResponseEntity.ok(activityService.publishActivity(id));
    }

    @PostMapping("/{id}/close")
    public ResponseEntity<Activity> closeActivity(@PathVariable Long id) {
        return ResponseEntity.ok(activityService.closeActivity(id));
    }

    @GetMapping("/{id}/checkins")
    public ResponseEntity<Map<String, Object>> getCheckins(@PathVariable Long id) {
        Activity activity = activityService.getActivity(id);
        List<CheckinRecord> checkins = checkinService.getCheckins(id);

        Map<String, Object> response = new HashMap<>();
        response.put("activityTitle", activity.getTitle());
        response.put("totalCount", checkins.size());
        response.put("checkins", checkins);

        return ResponseEntity.ok(response);
    }

}
