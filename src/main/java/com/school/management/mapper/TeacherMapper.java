package com.school.management.mapper;

import com.school.management.dto.PersonResponse;
import com.school.management.dto.TeacherResponse;
import com.school.management.entity.PersonRole;
import com.school.management.entity.Teacher;

public final class TeacherMapper {

    private TeacherMapper() {
    }

    public static TeacherResponse toResponse(Teacher teacher) {
        return new TeacherResponse(
                teacher.getId(),
                teacher.getName(),
                teacher.getAge(),
                teacher.getGroup(),
                CourseSetMapper.map(teacher.getCourses()));
    }

    public static PersonResponse toPersonResponse(Teacher teacher) {
        return new PersonResponse(
                teacher.getId(),
                teacher.getName(),
                teacher.getAge(),
                teacher.getGroup(),
                PersonRole.TEACHER);
    }
}
