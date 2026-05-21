package com.school.management.integration;

import com.school.management.dto.CourseRequest;
import com.school.management.dto.TeacherRequest;
import com.school.management.dto.TeacherResponse;
import com.school.management.entity.Course;
import com.school.management.entity.CourseType;
import com.school.management.entity.Teacher;
import com.school.management.repository.TeacherRepository;
import com.school.management.service.CourseService;
import com.school.management.service.TeacherService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TeacherIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CourseService courseService;

    @Autowired
    private TeacherService teacherService;

    @Autowired
    private TeacherRepository teacherRepository;

    @BeforeEach
    void setUp() {
        courseService.create(new CourseRequest("Math", CourseType.MAIN));
    }

    @Test
    @DisplayName("POST /api/teachers persists a teacher with course links")
    void createTeacher_persistsToDatabase() throws Exception {
        String body = """
                {"name":"Anna","age":40,"group":"5A","courseNames":["Math"]}
                """;

        mockMvc.perform(post("/api/teachers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Anna"))
                .andExpect(jsonPath("$.courses.length()").value(1));

        assertThat(teacherRepository.findAll())
                .extracting(Teacher::getName)
                .containsExactly("Anna");
    }

    @Test
    @DisplayName("POST /api/teachers returns 404 when referenced course does not exist")
    void createTeacher_returnsNotFound_whenCourseMissing() throws Exception {
        String body = """
                {"name":"Anna","age":40,"group":"5A","courseNames":["Unknown"]}
                """;

        mockMvc.perform(post("/api/teachers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());

        assertThat(teacherRepository.count()).isZero();
    }

    @Test
    @DisplayName("POST /api/teachers returns 400 when age is below the minimum")
    void createTeacher_returnsBadRequest_whenAgeTooLow() throws Exception {
        String body = """
                {"name":"Anna","age":15,"group":"5A","courseNames":["Math"]}
                """;

        mockMvc.perform(post("/api/teachers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[?(@.field=='age')]").exists());
    }

    @Test
    @DisplayName("GET /api/teachers returns all persisted teachers")
    void getAllTeachers_returnsListFromDatabase() throws Exception {
        teacherService.create(new TeacherRequest("Anna", 40, "5A", Set.of("Math")));

        mockMvc.perform(get("/api/teachers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Anna"));
    }

    @Test
    @DisplayName("GET /api/teachers/{id} returns the teacher for an existing id")
    void getTeacherById_returnsTeacher() throws Exception {
        TeacherResponse created = teacherService.create(
                new TeacherRequest("Anna", 40, "5A", Set.of("Math")));

        mockMvc.perform(get("/api/teachers/" + created.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(created.id()))
                .andExpect(jsonPath("$.name").value("Anna"));
    }

    @Test
    @DisplayName("PUT /api/teachers/{id} replaces fields and course assignments")
    void updateTeacher_replacesAllFields() throws Exception {
        courseService.create(new CourseRequest("Art", CourseType.SECONDARY));
        TeacherResponse created = teacherService.create(
                new TeacherRequest("Anna", 40, "5A", Set.of("Math")));

        String body = """
                {"name":"Anna Updated","age":42,"group":"6B","courseNames":["Art"]}
                """;

        mockMvc.perform(put("/api/teachers/" + created.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Anna Updated"))
                .andExpect(jsonPath("$.group").value("6B"))
                .andExpect(jsonPath("$.courses[0].name").value("Art"));

        Teacher reloaded = teacherRepository.findById(created.id()).orElseThrow();
        assertThat(reloaded.getCourses())
                .extracting(Course::getName)
                .containsExactly("Art");
    }

    @Test
    @DisplayName("DELETE /api/teachers/{id} removes the teacher from the database")
    void deleteTeacher_removesFromDatabase() throws Exception {
        TeacherResponse created = teacherService.create(
                new TeacherRequest("Anna", 40, "5A", Set.of("Math")));

        mockMvc.perform(delete("/api/teachers/" + created.id()))
                .andExpect(status().isNoContent());

        assertThat(teacherRepository.findById(created.id())).isEmpty();
    }

    @Test
    @DisplayName("DELETE /api/teachers/{id} returns 404 when teacher is missing")
    void deleteTeacher_returnsNotFound_whenMissing() throws Exception {
        mockMvc.perform(delete("/api/teachers/9999"))
                .andExpect(status().isNotFound());
    }
}
