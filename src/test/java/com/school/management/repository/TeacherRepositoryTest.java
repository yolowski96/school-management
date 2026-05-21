package com.school.management.repository;

import com.school.management.entity.Course;
import com.school.management.entity.CourseType;
import com.school.management.entity.Teacher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class TeacherRepositoryTest {

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Course math;
    private Course art;

    private Teacher anna;
    private Teacher john;

    @BeforeEach
    void setUp() {
        math = entityManager.persist(new Course("Math", CourseType.MAIN));
        art = entityManager.persist(new Course("Art", CourseType.SECONDARY));

        anna = persistTeacher("Anna", 40, "5A", math, art);
        john = persistTeacher("John", 50, "6B", math);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("findAll returns all teachers with their courses eagerly loaded")
    void findAll_returnsAllTeachers_withCourses() {
        List<Teacher> teachers = teacherRepository.findAll();

        assertThat(teachers)
                .extracting(Teacher::getName)
                .containsExactlyInAnyOrder("Anna", "John");
    }

    @Test
    @DisplayName("findById returns teacher when present")
    void findById_returnsTeacher_whenPresent() {
        Optional<Teacher> result = teacherRepository.findById(anna.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Anna");
    }

    @Test
    @DisplayName("findById returns empty when id does not exist")
    void findById_returnsEmpty_whenMissing() {
        assertThat(teacherRepository.findById(9999L)).isEmpty();
    }

    @Test
    @DisplayName("findByGroupAndCourseId returns teachers matching both group and course")
    void findByGroupAndCourseId_returnsMatchingTeachers() {
        List<Teacher> result = teacherRepository.findByGroupAndCourseId("5A", math.getId());

        assertThat(result)
                .extracting(Teacher::getName)
                .containsExactly("Anna");
    }

    @Test
    @DisplayName("findByGroupAndCourseId returns empty when no teacher matches both criteria")
    void findByGroupAndCourseId_returnsEmpty_whenNoMatch() {
        List<Teacher> result = teacherRepository.findByGroupAndCourseId("6B", art.getId());

        assertThat(result).isEmpty();
    }

    private Teacher persistTeacher(String name, int age, String group, Course... courses) {
        Teacher teacher = new Teacher(name, age, group);
        Set<Course> courseSet = new HashSet<>();
        for (Course c : courses) {
            courseSet.add(c);
        }
        teacher.setCourses(courseSet);
        return entityManager.persist(teacher);
    }
}
