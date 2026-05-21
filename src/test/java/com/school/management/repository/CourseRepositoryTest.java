package com.school.management.repository;

import com.school.management.entity.Course;
import com.school.management.entity.CourseType;
import com.school.management.entity.Student;
import com.school.management.entity.Teacher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class CourseRepositoryTest {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Course math;
    private Course art;
    private Course music;

    @BeforeEach
    void setUp() {
        math = entityManager.persist(new Course("Math", CourseType.MAIN));
        art = entityManager.persist(new Course("Art", CourseType.SECONDARY));
        music = entityManager.persist(new Course("Music", CourseType.SECONDARY));
        entityManager.flush();
    }

    @Test
    @DisplayName("existsByNameIgnoreCase returns true when course exists regardless of case")
    void existsByNameIgnoreCase_returnsTrue_whenCourseExists() {
        assertThat(courseRepository.existsByNameIgnoreCase("Math")).isTrue();
        assertThat(courseRepository.existsByNameIgnoreCase("math")).isTrue();
        assertThat(courseRepository.existsByNameIgnoreCase("MATH")).isTrue();
    }

    @Test
    @DisplayName("existsByNameIgnoreCase returns false when no course with given name exists")
    void existsByNameIgnoreCase_returnsFalse_whenCourseMissing() {
        assertThat(courseRepository.existsByNameIgnoreCase("Physics")).isFalse();
    }

    @Test
    @DisplayName("findAllByLowerNameIn returns courses for matching names only")
    void findAllByLowerNameIn_returnsMatchingCourses() {
        List<Course> found = courseRepository.findAllByLowerNameIn(Set.of("math", "art", "unknown"));

        assertThat(found)
                .extracting(Course::getName)
                .containsExactlyInAnyOrder("Math", "Art");
    }

    @Test
    @DisplayName("findAllByLowerNameIn returns empty list when no name matches")
    void findAllByLowerNameIn_returnsEmpty_whenNoMatch() {
        List<Course> found = courseRepository.findAllByLowerNameIn(Set.of("unknown"));

        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("isCourseLinkedToAnyStudent returns true when at least one student references the course")
    void isCourseLinkedToAnyStudent_returnsTrue_whenLinked() {
        Student student = new Student("Ivan", 18, "5A");
        Set<Course> courses = new HashSet<>();
        courses.add(math);
        student.setCourses(courses);
        entityManager.persist(student);
        entityManager.flush();

        assertThat(courseRepository.isCourseLinkedToAnyStudent(math.getId())).isTrue();
    }

    @Test
    @DisplayName("isCourseLinkedToAnyStudent returns false when no student references the course")
    void isCourseLinkedToAnyStudent_returnsFalse_whenNotLinked() {
        assertThat(courseRepository.isCourseLinkedToAnyStudent(art.getId())).isFalse();
    }

    @Test
    @DisplayName("isCourseLinkedToAnyTeacher returns true when at least one teacher teaches the course")
    void isCourseLinkedToAnyTeacher_returnsTrue_whenLinked() {
        Teacher teacher = new Teacher("Maria", 35, "5A");
        Set<Course> courses = new HashSet<>();
        courses.add(art);
        teacher.setCourses(courses);
        entityManager.persist(teacher);
        entityManager.flush();

        assertThat(courseRepository.isCourseLinkedToAnyTeacher(art.getId())).isTrue();
    }

    @Test
    @DisplayName("isCourseLinkedToAnyTeacher returns false when no teacher teaches the course")
    void isCourseLinkedToAnyTeacher_returnsFalse_whenNotLinked() {
        assertThat(courseRepository.isCourseLinkedToAnyTeacher(music.getId())).isFalse();
    }
}
