package com.school.management.repository;

import com.school.management.entity.Teacher;
import org.springframework.stereotype.Repository;

@Repository
public interface TeacherRepository extends PersonRepository<Teacher> {
}
