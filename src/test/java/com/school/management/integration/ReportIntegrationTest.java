package com.school.management.integration;

import com.school.management.dto.CourseRequest;
import com.school.management.dto.CourseResponse;
import com.school.management.dto.StudentRequest;
import com.school.management.dto.TeacherRequest;
import com.school.management.entity.CourseType;
import com.school.management.service.CourseService;
import com.school.management.service.StudentService;
import com.school.management.service.TeacherService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ReportIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CourseService courseService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private TeacherService teacherService;

    private CourseResponse math;

    @BeforeEach
    void setUp() {
        math = courseService.create(new CourseRequest("Math", CourseType.MAIN));
        courseService.create(new CourseRequest("Art", CourseType.SECONDARY));

        teacherService.create(new TeacherRequest("Anna", 40, "5A", Set.of("Math")));
        teacherService.create(new TeacherRequest("John", 50, "6B", Set.of("Art")));

        studentService.create(new StudentRequest("Ivan", 19, "5A", Set.of("Math")));
        studentService.create(new StudentRequest("Maria", 25, "5A", Set.of("Math", "Art")));
        studentService.create(new StudentRequest("Peter", 30, "6B", Set.of("Art")));
    }

    @Test
    @DisplayName("GET /api/reports/students/count counts all students in the database")
    void getStudentCount_returnsTotal() throws Exception {
        mockMvc.perform(get("/api/reports/students/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(3));
    }

    @Test
    @DisplayName("GET /api/reports/teachers/count counts all teachers in the database")
    void getTeacherCount_returnsTotal() throws Exception {
        mockMvc.perform(get("/api/reports/teachers/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(2));
    }

    @Test
    @DisplayName("GET /api/reports/courses/count returns counts grouped by course type")
    void getCourseCount_returnsCountsByType() throws Exception {
        mockMvc.perform(get("/api/reports/courses/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[?(@.type=='MAIN')].count").value(1))
                .andExpect(jsonPath("$[?(@.type=='SECONDARY')].count").value(1));
    }

    @Test
    @DisplayName("GET /api/reports/courses/{courseId}/students returns enrolled students")
    void getStudentsByCourse_returnsEnrolledStudents() throws Exception {
        mockMvc.perform(get("/api/reports/courses/" + math.id() + "/students"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("GET /api/reports/courses/{courseId}/students returns 404 for unknown course id")
    void getStudentsByCourse_returns404_whenCourseMissing() throws Exception {
        mockMvc.perform(get("/api/reports/courses/9999/students"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/reports/groups/{groupName}/students returns students of the group")
    void studentsByGroup_returnsGroupStudents() throws Exception {
        mockMvc.perform(get("/api/reports/groups/5A/students"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("GET /api/reports/group-course returns both teachers and students for the group/course combination")
    void byGroupAndCourse_returnsAggregate() throws Exception {
        mockMvc.perform(get("/api/reports/group-course")
                        .param("group", "5A")
                        .param("courseId", String.valueOf(math.id())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.teachers.length()").value(1))
                .andExpect(jsonPath("$.teachers[0].name").value("Anna"))
                .andExpect(jsonPath("$.students.length()").value(2));
    }

    @Test
    @DisplayName("GET /api/reports/courses/{courseId}/students?minAge returns students older than threshold in the course")
    void studentsOlderThan_returnsFilteredStudents() throws Exception {
        mockMvc.perform(get("/api/reports/courses/{courseId}/students", math.id())
                        .param("minAge", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Maria"));
    }

    @Test
    @DisplayName("GET /api/reports/courses/{courseId}/students?minAge returns 404 when course is missing")
    void studentsOlderThan_returns404_whenCourseMissing() throws Exception {
        mockMvc.perform(get("/api/reports/courses/{courseId}/students", 9999L)
                        .param("minAge", "20"))
                .andExpect(status().isNotFound());
    }
}
