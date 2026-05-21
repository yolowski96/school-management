package com.school.management.service;

import com.school.management.dto.CountResponse;
import com.school.management.dto.PersonResponse;
import com.school.management.dto.StudentRequest;
import com.school.management.dto.StudentResponse;

import java.util.List;

public interface StudentService {

    StudentResponse create(StudentRequest request);

    List<StudentResponse> findAll();

    StudentResponse findById(Long id);

    StudentResponse update(Long id, StudentRequest request);

    void delete(Long id);

    List<StudentResponse> findOlderThanByCourse(int age, Long courseId);

    List<StudentResponse> getStudentsByCourse(Long courseId);

    List<StudentResponse> getStudentsByGroup(String groupName);

    List<PersonResponse> getStudentsByGroupAndCourse(String group, Long courseId);

    CountResponse countStudents();
}
