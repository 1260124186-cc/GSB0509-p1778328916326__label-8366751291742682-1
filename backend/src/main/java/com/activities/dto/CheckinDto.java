package com.activities.dto;

import lombok.Data;

public class CheckinDto {

    @Data
    public static class Request {
        private String name;
    }
}
