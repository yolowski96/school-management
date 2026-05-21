package com.school.management.service;

import com.school.management.dto.CountResponse;
import com.school.management.dto.CourseTypeCountResponse;
import com.school.management.dto.GroupCourseReportResponse;
import com.school.management.dto.StudentResponse;

import java.util.List;

public interface ReportService {

    CountResponse countStudents();

    CountResponse countTeachers();

    List<CourseTypeCountResponse> countCoursesByType();

    List<StudentResponse> studentsByCourse(Long courseId);

    List<StudentResponse> studentsByGroup(String group);

    GroupCourseReportResponse byGroupAndCourse(String group, Long courseId);

    List<StudentResponse> studentsOlderThan(int age, Long courseId);
}
