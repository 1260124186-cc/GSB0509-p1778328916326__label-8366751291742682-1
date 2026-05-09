package com.activities.controller;

import com.activities.dto.CheckinDto;
import com.activities.entity.Activity;
import com.activities.entity.CheckinRecord;
import com.activities.service.ActivityService;
import com.activities.service.CheckinService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public/activities")
@RequiredArgsConstructor
public class PublicController {

    private final ActivityService activityService;
    private final CheckinService checkinService;

    @GetMapping("/{token}")
    public ResponseEntity<Activity> getActivityByToken(@PathVariable String token) {
        return ResponseEntity.ok(activityService.getActivityByToken(token));
    }

    @PostMapping("/{token}/checkin")
    public ResponseEntity<CheckinRecord> checkin(@PathVariable String token, @RequestBody CheckinDto.Request request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(checkinService.checkin(token, request));
    }
}
