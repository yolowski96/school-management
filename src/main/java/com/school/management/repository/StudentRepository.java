package com.school.management.repository;

import com.school.management.entity.Student;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentRepository extends PersonRepository<Student> {

    @EntityGraph(attributePaths = "courses")
    @Query("SELECT s FROM Student s JOIN s.courses c WHERE c.id = :courseId")
    List<Student> findByCourseId(@Param("courseId") Long courseId);

    @EntityGraph(attributePaths = "courses")
    List<Student> findByGroup(String group);

    @EntityGraph(attributePaths = "courses")
    @Query("SELECT DISTINCT s FROM Student s JOIN s.courses c " +
            "WHERE s.age > :age AND c.id = :courseId")
    List<Student> findOlderThanByCourseId(@Param("age") int age,
                                          @Param("courseId") Long courseId);
}
