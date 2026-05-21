package com.school.management.dto;

import com.school.management.entity.CourseType;

public record CourseTypeCountResponse(
        CourseType type,
        long count) {
}
