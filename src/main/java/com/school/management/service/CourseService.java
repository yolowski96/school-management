package com.school.management.service;

import com.school.management.dto.CourseRequest;
import com.school.management.dto.CourseResponse;
import com.school.management.dto.CourseTypeCountResponse;
import com.school.management.entity.Course;

import java.util.List;
import java.util.Set;

public interface CourseService {

    CourseResponse create(CourseRequest request);

    List<CourseResponse> findAll();

    CourseResponse findById(Long id);

    CourseResponse update(Long id, CourseRequest request);

    void delete(Long id);

    List<CourseTypeCountResponse> countGroupedByType();

    Set<Course> getCoursesByNames(Set<String> names);

    void ensureExists(Long id);
}
