package com.school.management.mapper;

import com.school.management.dto.CourseResponse;
import com.school.management.entity.Course;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

public final class CourseSetMapper {

    private CourseSetMapper() {
    }

    public static Set<CourseResponse> map(Set<Course> courses) {
        return courses.stream()
                .map(CourseMapper::toResponse)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
