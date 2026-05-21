package com.school.management.service;

import com.school.management.dto.CountResponse;
import com.school.management.dto.PersonResponse;
import com.school.management.dto.StudentRequest;
import com.school.management.dto.StudentResponse;
import com.school.management.entity.Course;
import com.school.management.entity.CourseType;
import com.school.management.entity.PersonRole;
import com.school.management.entity.Student;
import com.school.management.exception.ResourceNotFoundException;
import com.school.management.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private CourseService courseService;

    @InjectMocks
    private StudentServiceImpl studentService;

    private Course math;
    private Student ivan;

    @BeforeEach
    void setUp() {
        math = new Course("Math", CourseType.MAIN);
        math.setId(1L);

        ivan = new Student("Ivan", 12, "5A");
        ivan.setId(10L);
        Set<Course> courses = new HashSet<>();
        courses.add(math);
        ivan.setCourses(courses);
    }

    @Test
    @DisplayName("create persists a new student with resolved courses and returns its response")
    void create_persistsStudent_andReturnsResponse() {
        StudentRequest request = new StudentRequest("Ivan", 12, "5A", Set.of("Math"));
        when(courseService.getCoursesByNames(Set.of("Math"))).thenReturn(Set.of(math));
        when(studentRepository.save(any(Student.class))).thenReturn(ivan);

        StudentResponse response = studentService.create(request);

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.name()).isEqualTo("Ivan");
        assertThat(response.courses()).hasSize(1);

        ArgumentCaptor<Student> captor = ArgumentCaptor.forClass(Student.class);
        verify(studentRepository).save(captor.capture());
        Student saved = captor.getValue();
        assertThat(saved.getName()).isEqualTo("Ivan");
        assertThat(saved.getAge()).isEqualTo(12);
        assertThat(saved.getGroup()).isEqualTo("5A");
        assertThat(saved.getCourses()).containsExactly(math);
    }

    @Test
    @DisplayName("findAll returns mapped responses for every student")
    void findAll_returnsMappedResponses() {
        when(studentRepository.findAll()).thenReturn(List.of(ivan));

        List<StudentResponse> result = studentService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Ivan");
    }

    @Test
    @DisplayName("findById returns mapped response when student exists")
    void findById_returnsResponse_whenStudentExists() {
        when(studentRepository.findById(10L)).thenReturn(Optional.of(ivan));

        StudentResponse response = studentService.findById(10L);

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.name()).isEqualTo("Ivan");
    }

    @Test
    @DisplayName("findById throws ResourceNotFoundException when student missing")
    void findById_throws_whenStudentMissing() {
        when(studentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studentService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Student not found: 99");
    }

    @Test
    @DisplayName("update overwrites all fields including courses and returns response")
    void update_overwritesFields_andReturnsResponse() {
        StudentRequest request = new StudentRequest("Ivan Updated", 13, "5B", Set.of("Math"));
        when(studentRepository.findById(10L)).thenReturn(Optional.of(ivan));
        when(courseService.getCoursesByNames(Set.of("Math"))).thenReturn(Set.of(math));
        when(studentRepository.save(any(Student.class))).thenAnswer(inv -> inv.getArgument(0));

        StudentResponse response = studentService.update(10L, request);

        assertThat(response.name()).isEqualTo("Ivan Updated");
        assertThat(response.age()).isEqualTo(13);
        assertThat(response.group()).isEqualTo("5B");
        verify(studentRepository).save(ivan);
    }

    @Test
    @DisplayName("update throws ResourceNotFoundException when student missing")
    void update_throws_whenStudentMissing() {
        StudentRequest request = new StudentRequest("Ivan", 12, "5A", Set.of("Math"));
        when(studentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studentService.update(99L, request))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(studentRepository, never()).save(any());
    }

    @Test
    @DisplayName("delete removes the student when found")
    void delete_removesStudent_whenFound() {
        when(studentRepository.findById(10L)).thenReturn(Optional.of(ivan));

        studentService.delete(10L);

        verify(studentRepository, times(1)).delete(ivan);
    }

    @Test
    @DisplayName("delete throws ResourceNotFoundException when student missing")
    void delete_throws_whenStudentMissing() {
        when(studentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studentService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(studentRepository, never()).delete(any(Student.class));
    }

    @Test
    @DisplayName("findOlderThanByCourse delegates to repository and maps results")
    void findOlderThanByCourse_returnsMappedResponses() {
        when(studentRepository.findOlderThanByCourseId(10, 1L)).thenReturn(List.of(ivan));

        List<StudentResponse> result = studentService.findOlderThanByCourse(10, 1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Ivan");
    }

    @Test
    @DisplayName("getStudentsByCourse delegates to repository and maps results")
    void getStudentsByCourse_returnsMappedResponses() {
        when(studentRepository.findByCourseId(1L)).thenReturn(List.of(ivan));

        List<StudentResponse> result = studentService.getStudentsByCourse(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(10L);
        assertThat(result.get(0).name()).isEqualTo("Ivan");
    }

    @Test
    @DisplayName("getStudentsByGroup delegates to repository and maps results")
    void getStudentsByGroup_returnsMappedResponses() {
        when(studentRepository.findByGroup("5A")).thenReturn(List.of(ivan));

        List<StudentResponse> result = studentService.getStudentsByGroup("5A");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(10L);
        assertThat(result.get(0).name()).isEqualTo("Ivan");
    }

    @Test
    @DisplayName("getStudentsByGroupAndCourse returns mapped PersonResponse with STUDENT role")
    void getStudentsByGroupAndCourse_returnsMappedPersonResponses() {
        when(studentRepository.findByGroupAndCourseId("5A", 1L)).thenReturn(List.of(ivan));

        List<PersonResponse> result = studentService.getStudentsByGroupAndCourse("5A", 1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Ivan");
        assertThat(result.get(0).role()).isEqualTo(PersonRole.STUDENT);
    }

    @Test
    @DisplayName("countStudents returns the repository count wrapped in CountResponse")
    void countStudents_returnsCount() {
        when(studentRepository.count()).thenReturn(42L);

        CountResponse response = studentService.countStudents();

        assertThat(response.count()).isEqualTo(42L);
    }
}
