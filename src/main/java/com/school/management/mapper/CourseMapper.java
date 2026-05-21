package com.school.management.mapper;

import com.school.management.dto.CourseResponse;
import com.school.management.entity.Course;

public final class CourseMapper {

    private CourseMapper() {
    }

    public static CourseResponse toResponse(Course course) {
        return new CourseResponse(course.getId(), course.getName(), course.getType());
    }
}
