package com.school.management.dto;

import java.util.Set;

public record StudentResponse(
        Long id,
        String name,
        int age,
        String group,
        Set<CourseResponse> courses) {
}
