package com.school.management.service;

import com.school.management.dto.CountResponse;
import com.school.management.dto.CourseTypeCountResponse;
import com.school.management.dto.GroupCourseReportResponse;
import com.school.management.dto.PersonResponse;
import com.school.management.dto.StudentResponse;
import com.school.management.entity.CourseType;
import com.school.management.entity.PersonRole;
import com.school.management.entity.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private StudentService studentService;

    @Mock
    private TeacherService teacherService;

    @Mock
    private CourseService courseService;

    @InjectMocks
    private ReportServiceImpl reportService;

    private Student ivan;

    @BeforeEach
    void setUp() {
        ivan = new Student("Ivan", 12, "5A");
        ivan.setId(10L);
    }

    @Test
    @DisplayName("countStudents delegates to studentService.countStudents")
    void countStudents_delegates() {
        when(studentService.countStudents()).thenReturn(new CountResponse(42L));

        CountResponse response = reportService.countStudents();

        assertThat(response.count()).isEqualTo(42L);
        verify(studentService).countStudents();
    }

    @Test
    @DisplayName("countTeachers delegates to teacherService.countTeachers")
    void countTeachers_delegates() {
        when(teacherService.countTeachers()).thenReturn(new CountResponse(7L));

        CountResponse response = reportService.countTeachers();

        assertThat(response.count()).isEqualTo(7L);
        verify(teacherService).countTeachers();
    }

    @Test
    @DisplayName("countCoursesByType delegates to courseService.countGroupedByType")
    void countCoursesByType_delegates() {
        List<CourseTypeCountResponse> counts = List.of(
                new CourseTypeCountResponse(CourseType.MAIN, 3L),
                new CourseTypeCountResponse(CourseType.SECONDARY, 2L));
        when(courseService.countGroupedByType()).thenReturn(counts);

        List<CourseTypeCountResponse> result = reportService.countCoursesByType();

        assertThat(result).containsExactlyElementsOf(counts);
    }

    @Test
    @DisplayName("studentsByCourse ensures course exists and returns mapped students")
    void studentsByCourse_returnsMappedStudents() {
        StudentResponse ivanResponse = new StudentResponse(10L, "Ivan", 12, "5A", java.util.Set.of());
        when(studentService.getStudentsByCourse(1L)).thenReturn(List.of(ivanResponse));

        List<StudentResponse> result = reportService.studentsByCourse(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Ivan");
        verify(courseService).ensureExists(1L);
    }

    @Test
    @DisplayName("studentsByGroup returns mapped students for the group")
    void studentsByGroup_returnsMappedStudents() {
        StudentResponse ivanResponse = new StudentResponse(10L, "Ivan", 12, "5A", java.util.Set.of());
        when(studentService.getStudentsByGroup("5A")).thenReturn(List.of(ivanResponse));

        List<StudentResponse> result = reportService.studentsByGroup("5A");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Ivan");
    }

    @Test
    @DisplayName("byGroupAndCourse ensures course exists and aggregates teacher and student lists")
    void byGroupAndCourse_aggregatesPeople() {
        PersonResponse teacher = new PersonResponse(1L, "Anna", 40, "5A", PersonRole.TEACHER);
        PersonResponse student = new PersonResponse(2L, "Ivan", 12, "5A", PersonRole.STUDENT);
        when(teacherService.getTeachersByGroupAndCourse("5A", 1L)).thenReturn(List.of(teacher));
        when(studentService.getStudentsByGroupAndCourse("5A", 1L)).thenReturn(List.of(student));

        GroupCourseReportResponse response = reportService.byGroupAndCourse("5A", 1L);

        assertThat(response.teachers()).containsExactly(teacher);
        assertThat(response.students()).containsExactly(student);
        verify(courseService).ensureExists(1L);
    }

    @Test
    @DisplayName("studentsOlderThan ensures course exists and delegates to studentService")
    void studentsOlderThan_delegates() {
        StudentResponse response = new StudentResponse(10L, "Maria", 14, "5A", java.util.Set.of());
        when(studentService.findOlderThanByCourse(13, 1L)).thenReturn(List.of(response));

        List<StudentResponse> result = reportService.studentsOlderThan(13, 1L);

        assertThat(result).containsExactly(response);
        verify(courseService).ensureExists(1L);
    }
}
