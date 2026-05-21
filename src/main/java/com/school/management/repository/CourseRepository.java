package com.school.management.repository;

import com.school.management.entity.Course;
import com.school.management.repository.projection.CourseTypeCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    @Query(value = "SELECT CASE WHEN COUNT(*) > 0 THEN true ELSE false END " +
            "FROM student_courses WHERE course_id = :courseId", nativeQuery = true)
    boolean isCourseLinkedToAnyStudent(@Param("courseId") Long courseId);

    @Query(value = "SELECT CASE WHEN COUNT(*) > 0 THEN true ELSE false END " +
            "FROM teacher_courses WHERE course_id = :courseId", nativeQuery = true)
    boolean isCourseLinkedToAnyTeacher(@Param("courseId") Long courseId);

    @Query("SELECT c.type AS type, COUNT(c) AS count " +
            "FROM Course c GROUP BY c.type")
    List<CourseTypeCount> countGroupedByType();

    @Query("SELECT c FROM Course c WHERE LOWER(c.name) IN :lowerNames")
    List<Course> findAllByLowerNameIn(@Param("lowerNames") Set<String> lowerNames);

    boolean existsByNameIgnoreCase(String courseName);
}
