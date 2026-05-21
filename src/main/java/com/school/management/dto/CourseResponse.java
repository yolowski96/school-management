package com.school.management.dto;

import com.school.management.entity.CourseType;

public record CourseResponse(Long id, String name, CourseType type) {
}
