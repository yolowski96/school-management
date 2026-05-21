package com.school.management.service;

import com.school.management.dto.CountResponse;
import com.school.management.dto.PersonResponse;
import com.school.management.dto.TeacherRequest;
import com.school.management.dto.TeacherResponse;

import java.util.List;

public interface TeacherService {

    TeacherResponse create(TeacherRequest request);

    List<TeacherResponse> findAll();

    TeacherResponse findById(Long id);

    TeacherResponse update(Long id, TeacherRequest request);

    void delete(Long id);

    List<PersonResponse> getTeachersByGroupAndCourse(String group, Long courseId);

    CountResponse countTeachers();
}
