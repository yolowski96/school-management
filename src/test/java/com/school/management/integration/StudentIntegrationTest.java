package com.school.management.integration;

import com.school.management.dto.CourseRequest;
import com.school.management.dto.StudentRequest;
import com.school.management.dto.StudentResponse;
import com.school.management.entity.Course;
import com.school.management.entity.CourseType;
import com.school.management.entity.Student;
import com.school.management.repository.StudentRepository;
import com.school.management.service.CourseService;
import com.school.management.service.StudentService;
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
class StudentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CourseService courseService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private StudentRepository studentRepository;

    @BeforeEach
    void setUp() {
        courseService.create(new CourseRequest("Math", CourseType.MAIN));
        courseService.create(new CourseRequest("Art", CourseType.SECONDARY));
    }

    @Test
    @DisplayName("POST /api/students persists a student with its course links")
    void createStudent_persistsToDatabase() throws Exception {
        String body = """
                {"name":"Ivan","age":20,"group":"5A","courseNames":["Math","Art"]}
                """;

        mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Ivan"))
                .andExpect(jsonPath("$.courses.length()").value(2));

        assertThat(studentRepository.findAll())
                .extracting(Student::getName)
                .containsExactly("Ivan");
    }

    @Test
    @DisplayName("POST /api/students returns 404 when referenced course does not exist")
    void createStudent_returnsNotFound_whenCourseMissing() throws Exception {
        String body = """
                {"name":"Ivan","age":20,"group":"5A","courseNames":["Unknown"]}
                """;

        mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());

        assertThat(studentRepository.count()).isZero();
    }

    @Test
    @DisplayName("POST /api/students returns 400 when validation fails")
    void createStudent_returnsBadRequest_whenValidationFails() throws Exception {
        String body = """
                {"name":"","age":4,"group":"","courseNames":[]}
                """;

        mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors").isArray());
    }

    @Test
    @DisplayName("GET /api/students returns all persisted students")
    void getAllStudents_returnsListFromDatabase() throws Exception {
        studentService.create(new StudentRequest("Ivan", 20, "5A", Set.of("Math")));
        studentService.create(new StudentRequest("Maria", 21, "5A", Set.of("Art")));

        mockMvc.perform(get("/api/students"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("GET /api/students/{id} returns the student for an existing id")
    void getStudentById_returnsStudent() throws Exception {
        StudentResponse created = studentService.create(
                new StudentRequest("Ivan", 19, "5A", Set.of("Math")));

        mockMvc.perform(get("/api/students/" + created.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(created.id()))
                .andExpect(jsonPath("$.name").value("Ivan"))
                .andExpect(jsonPath("$.courses.length()").value(1));
    }

    @Test
    @DisplayName("PUT /api/students/{id} replaces all fields including course assignments")
    void updateStudent_replacesAllFields() throws Exception {
        StudentResponse created = studentService.create(
                new StudentRequest("Ivan", 20, "5A", Set.of("Math")));

        String body = """
                {"name":"Ivan Updated","age":25,"group":"5B","courseNames":["Art"]}
                """;

        mockMvc.perform(put("/api/students/" + created.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Ivan Updated"))
                .andExpect(jsonPath("$.age").value(25))
                .andExpect(jsonPath("$.group").value("5B"))
                .andExpect(jsonPath("$.courses.length()").value(1))
                .andExpect(jsonPath("$.courses[0].name").value("Art"));

        Student reloaded = studentRepository.findById(created.id()).orElseThrow();
        assertThat(reloaded.getCourses())
                .extracting(Course::getName)
                .containsExactly("Art");
    }

    @Test
    @DisplayName("DELETE /api/students/{id} removes the student from the database")
    void deleteStudent_removesFromDatabase() throws Exception {
        StudentResponse created = studentService.create(
                new StudentRequest("Ivan", 19, "5A", Set.of("Math")));

        mockMvc.perform(delete("/api/students/" + created.id()))
                .andExpect(status().isNoContent());

        assertThat(studentRepository.findById(created.id())).isEmpty();
    }

    @Test
    @DisplayName("DELETE /api/students/{id} returns 404 when student is missing")
    void deleteStudent_returnsNotFound_whenMissing() throws Exception {
        mockMvc.perform(delete("/api/students/9999"))
                .andExpect(status().isNotFound());
    }
}
