package com.school.management.controller;

import com.school.management.dto.CountResponse;
import com.school.management.dto.CourseTypeCountResponse;
import com.school.management.dto.GroupCourseReportResponse;
import com.school.management.dto.PersonResponse;
import com.school.management.dto.StudentResponse;
import com.school.management.entity.CourseType;
import com.school.management.entity.PersonRole;
import com.school.management.exception.ResourceNotFoundException;
import com.school.management.service.ReportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReportController.class)
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReportService reportService;

    @Test
    void getStudentCount_returnsCount() throws Exception {
        when(reportService.countStudents()).thenReturn(new CountResponse(42L));

        mockMvc.perform(get("/api/reports/students/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(42));
    }

    @Test
    void getTeacherCount_returnsCount() throws Exception {
        when(reportService.countTeachers()).thenReturn(new CountResponse(7L));

        mockMvc.perform(get("/api/reports/teachers/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(7));
    }

    @Test
    void getCourseCount_returnsCountsByType() throws Exception {
        when(reportService.countCoursesByType()).thenReturn(List.of(
                new CourseTypeCountResponse(CourseType.MAIN, 3L),
                new CourseTypeCountResponse(CourseType.SECONDARY, 2L)));

        mockMvc.perform(get("/api/reports/courses/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[?(@.type=='MAIN')].count").value(3))
                .andExpect(jsonPath("$[?(@.type=='SECONDARY')].count").value(2));
    }

    @Test
    void getStudentsByCourse_returnsList() throws Exception {
        StudentResponse student = new StudentResponse(10L, "Ivan", 12, "5A", Set.of());
        when(reportService.studentsByCourse(1L)).thenReturn(List.of(student));

        mockMvc.perform(get("/api/reports/courses/1/students"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Ivan"));
    }

    @Test
    void getStudentsByCourse_returns404_whenCourseMissing() throws Exception {
        when(reportService.studentsByCourse(99L))
                .thenThrow(new ResourceNotFoundException("Course not found: 99"));

        mockMvc.perform(get("/api/reports/courses/99/students"))
                .andExpect(status().isNotFound());
    }

    @Test
    void studentsByGroup_returnsList() throws Exception {
        StudentResponse student = new StudentResponse(10L, "Ivan", 12, "5A", Set.of());
        when(reportService.studentsByGroup("5A")).thenReturn(List.of(student));

        mockMvc.perform(get("/api/reports/groups/5A/students"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Ivan"));
    }

    @Test
    void byGroupAndCourse_returnsAggregateReport() throws Exception {
        PersonResponse teacher = new PersonResponse(1L, "Anna", 40, "5A", PersonRole.TEACHER);
        PersonResponse student = new PersonResponse(10L, "Ivan", 30, "5A", PersonRole.STUDENT);
        when(reportService.byGroupAndCourse("5A", 1L))
                .thenReturn(new GroupCourseReportResponse(List.of(teacher), List.of(student)));

        mockMvc.perform(get("/api/reports/group-course")
                        .param("group", "5A")
                        .param("courseId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.teachers.length()").value(1))
                .andExpect(jsonPath("$.teachers[0].role").value("TEACHER"))
                .andExpect(jsonPath("$.students.length()").value(1))
                .andExpect(jsonPath("$.students[0].role").value("STUDENT"));
    }

    @Test
    void byGroupAndCourse_returns400_whenGroupMissing() throws Exception {
        mockMvc.perform(get("/api/reports/group-course")
                        .param("courseId", "1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void studentsOlderThan_returnsList() throws Exception {
        StudentResponse student = new StudentResponse(10L, "Maria", 21, "5A", Set.of());
        when(reportService.studentsOlderThan(20, 1L)).thenReturn(List.of(student));

        mockMvc.perform(get("/api/reports/courses/{courseId}/students", 1L)
                        .param("minAge", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Maria"));
    }
}
