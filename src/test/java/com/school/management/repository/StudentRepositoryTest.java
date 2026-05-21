package com.school.management.repository;

import com.school.management.entity.Course;
import com.school.management.entity.CourseType;
import com.school.management.entity.Student;
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
class StudentRepositoryTest {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Course math;
    private Course art;

    private Student ivan;
    private Student maria;
    private Student peter;

    @BeforeEach
    void setUp() {
        math = entityManager.persist(new Course("Math", CourseType.MAIN));
        art = entityManager.persist(new Course("Art", CourseType.SECONDARY));

        ivan = persistStudent("Ivan", 18, "5A", math);
        maria = persistStudent("Maria", 31, "5A", math, art);
        peter = persistStudent("Peter", 30, "6B", art);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("findAll returns all students with their courses eagerly loaded")
    void findAll_returnsAllStudents_withCourses() {
        List<Student> students = studentRepository.findAll();

        assertThat(students)
                .extracting(Student::getName)
                .containsExactlyInAnyOrder("Ivan", "Maria", "Peter");
        assertThat(students)
                .allSatisfy(s -> assertThat(s.getCourses()).isNotNull());
    }

    @Test
    @DisplayName("findById returns student when present")
    void findById_returnsStudent_whenPresent() {
        Optional<Student> result = studentRepository.findById(maria.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Maria");
        assertThat(result.get().getCourses()).hasSize(2);
    }

    @Test
    @DisplayName("findById returns empty when id does not exist")
    void findById_returnsEmpty_whenMissing() {
        assertThat(studentRepository.findById(9999L)).isEmpty();
    }

    @Test
    @DisplayName("findByCourseId returns only students enrolled in the given course")
    void findByCourseId_returnsEnrolledStudents() {
        List<Student> result = studentRepository.findByCourseId(math.getId());

        assertThat(result)
                .extracting(Student::getName)
                .containsExactlyInAnyOrder("Ivan", "Maria");
    }

    @Test
    @DisplayName("findByCourseId returns empty when no student enrolled in the course")
    void findByCourseId_returnsEmpty_whenNobodyEnrolled() {
        Course physics = entityManager.persist(new Course("Physics", CourseType.MAIN));
        entityManager.flush();

        assertThat(studentRepository.findByCourseId(physics.getId())).isEmpty();
    }

    @Test
    @DisplayName("findByGroup returns only students in the given group")
    void findByGroup_returnsStudentsInGroup() {
        List<Student> result = studentRepository.findByGroup("5A");

        assertThat(result)
                .extracting(Student::getName)
                .containsExactlyInAnyOrder("Ivan", "Maria");
    }

    @Test
    @DisplayName("findByGroup returns empty when no student in group")
    void findByGroup_returnsEmpty_whenNoStudentInGroup() {
        assertThat(studentRepository.findByGroup("7C")).isEmpty();
    }

    @Test
    @DisplayName("findByGroupAndCourseId returns students who match both group and course")
    void findByGroupAndCourseId_returnsMatchingStudents() {
        List<Student> result = studentRepository.findByGroupAndCourseId("5A", art.getId());

        assertThat(result)
                .extracting(Student::getName)
                .containsExactly("Maria");
    }

    @Test
    @DisplayName("findByGroupAndCourseId returns empty when no student matches both criteria")
    void findByGroupAndCourseId_returnsEmpty_whenNoMatch() {
        List<Student> result = studentRepository.findByGroupAndCourseId("6B", math.getId());

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findOlderThanByCourseId returns students enrolled in course who are older than the threshold")
    void findOlderThanByCourseId_returnsOlderStudents() {
        List<Student> result = studentRepository.findOlderThanByCourseId(25, math.getId());

        assertThat(result)
                .extracting(Student::getName)
                .containsExactly("Maria");
    }

    @Test
    @DisplayName("findOlderThanByCourseId returns empty when nobody older than threshold")
    void findOlderThanByCourseId_returnsEmpty_whenNoOneQualifies() {
        List<Student> result = studentRepository.findOlderThanByCourseId(100, math.getId());

        assertThat(result).isEmpty();
    }

    private Student persistStudent(String name, int age, String group, Course... courses) {
        Student student = new Student(name, age, group);
        Set<Course> courseSet = new HashSet<>();
        for (Course c : courses) {
            courseSet.add(c);
        }
        student.setCourses(courseSet);
        return entityManager.persist(student);
    }
}
