package com.activities.dto;

import lombok.Data;
import java.time.LocalDateTime;

public class ActivityDto {

    @Data
    public static class CreateRequest {
        private String title;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private String description;
    }

    @Data
    public static class PublishResponse {
        private Long id;
        private String checkinToken;
        private String checkinUrl;
    }
}
