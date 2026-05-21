package com.school.management.service;

import com.school.management.dto.CountResponse;
import com.school.management.dto.PersonResponse;
import com.school.management.dto.TeacherRequest;
import com.school.management.dto.TeacherResponse;
import com.school.management.entity.Course;
import com.school.management.entity.CourseType;
import com.school.management.entity.PersonRole;
import com.school.management.entity.Teacher;
import com.school.management.exception.ResourceNotFoundException;
import com.school.management.repository.TeacherRepository;
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
class TeacherServiceTest {

    @Mock
    private TeacherRepository teacherRepository;

    @Mock
    private CourseService courseService;

    @InjectMocks
    private TeacherServiceImpl teacherService;

    private Course math;
    private Teacher anna;

    @BeforeEach
    void setUp() {
        math = new Course("Math", CourseType.MAIN);
        math.setId(1L);

        anna = new Teacher("Anna", 40, "5A");
        anna.setId(20L);
        Set<Course> courses = new HashSet<>();
        courses.add(math);
        anna.setCourses(courses);
    }

    @Test
    @DisplayName("create persists a new teacher with resolved courses and returns its response")
    void create_persistsTeacher_andReturnsResponse() {
        TeacherRequest request = new TeacherRequest("Anna", 40, "5A", Set.of("Math"));
        when(courseService.getCoursesByNames(Set.of("Math"))).thenReturn(Set.of(math));
        when(teacherRepository.save(any(Teacher.class))).thenReturn(anna);

        TeacherResponse response = teacherService.create(request);

        assertThat(response.id()).isEqualTo(20L);
        assertThat(response.name()).isEqualTo("Anna");

        ArgumentCaptor<Teacher> captor = ArgumentCaptor.forClass(Teacher.class);
        verify(teacherRepository).save(captor.capture());
        Teacher saved = captor.getValue();
        assertThat(saved.getName()).isEqualTo("Anna");
        assertThat(saved.getCourses()).containsExactly(math);
    }

    @Test
    @DisplayName("findAll returns mapped responses for every teacher")
    void findAll_returnsMappedResponses() {
        when(teacherRepository.findAll()).thenReturn(List.of(anna));

        List<TeacherResponse> result = teacherService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Anna");
    }

    @Test
    @DisplayName("findById returns mapped response when teacher exists")
    void findById_returnsResponse_whenTeacherExists() {
        when(teacherRepository.findById(20L)).thenReturn(Optional.of(anna));

        TeacherResponse response = teacherService.findById(20L);

        assertThat(response.id()).isEqualTo(20L);
    }

    @Test
    @DisplayName("findById throws ResourceNotFoundException when teacher missing")
    void findById_throws_whenTeacherMissing() {
        when(teacherRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> teacherService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Teacher not found: 99");
    }

    @Test
    @DisplayName("update overwrites all fields including courses and returns response")
    void update_overwritesFields_andReturnsResponse() {
        TeacherRequest request = new TeacherRequest("Anna Updated", 41, "6B", Set.of("Math"));
        when(teacherRepository.findById(20L)).thenReturn(Optional.of(anna));
        when(courseService.getCoursesByNames(Set.of("Math"))).thenReturn(Set.of(math));
        when(teacherRepository.save(any(Teacher.class))).thenAnswer(inv -> inv.getArgument(0));

        TeacherResponse response = teacherService.update(20L, request);

        assertThat(response.name()).isEqualTo("Anna Updated");
        assertThat(response.age()).isEqualTo(41);
        assertThat(response.group()).isEqualTo("6B");
    }

    @Test
    @DisplayName("update throws ResourceNotFoundException when teacher missing")
    void update_throws_whenTeacherMissing() {
        TeacherRequest request = new TeacherRequest("Anna", 40, "5A", Set.of("Math"));
        when(teacherRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> teacherService.update(99L, request))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(teacherRepository, never()).save(any());
    }

    @Test
    @DisplayName("delete removes the teacher when found")
    void delete_removesTeacher_whenFound() {
        when(teacherRepository.findById(20L)).thenReturn(Optional.of(anna));

        teacherService.delete(20L);

        verify(teacherRepository, times(1)).delete(anna);
    }

    @Test
    @DisplayName("delete throws ResourceNotFoundException when teacher missing")
    void delete_throws_whenTeacherMissing() {
        when(teacherRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> teacherService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(teacherRepository, never()).delete(any(Teacher.class));
    }

    @Test
    @DisplayName("getTeachersByGroupAndCourse returns mapped PersonResponse with TEACHER role")
    void getTeachersByGroupAndCourse_returnsMappedPersonResponses() {
        when(teacherRepository.findByGroupAndCourseId("5A", 1L)).thenReturn(List.of(anna));

        List<PersonResponse> result = teacherService.getTeachersByGroupAndCourse("5A", 1L);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().name()).isEqualTo("Anna");
        assertThat(result.getFirst().role()).isEqualTo(PersonRole.TEACHER);
    }

    @Test
    @DisplayName("countTeachers returns the repository count wrapped in CountResponse")
    void countTeachers_returnsCount() {
        when(teacherRepository.count()).thenReturn(7L);

        CountResponse response = teacherService.countTeachers();

        assertThat(response.count()).isEqualTo(7L);
    }
}
