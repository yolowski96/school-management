package com.school.management.mapper;

import com.school.management.dto.PersonResponse;
import com.school.management.dto.StudentResponse;
import com.school.management.entity.PersonRole;
import com.school.management.entity.Student;

public final class StudentMapper {

    private StudentMapper() {
    }

    public static StudentResponse toResponse(Student student) {
        return new StudentResponse(
                student.getId(),
                student.getName(),
                student.getAge(),
                student.getGroup(),
                CourseSetMapper.map(student.getCourses()));
    }

    public static PersonResponse toPersonResponse(Student student) {
        return new PersonResponse(
                student.getId(),
                student.getName(),
                student.getAge(),
                student.getGroup(),
                PersonRole.STUDENT);
    }
}
