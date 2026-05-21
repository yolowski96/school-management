package com.school.management.integration;

import com.school.management.dto.CourseRequest;
import com.school.management.dto.CourseResponse;
import com.school.management.dto.StudentRequest;
import com.school.management.entity.CourseType;
import com.school.management.repository.CourseRepository;
import com.school.management.service.CourseService;
import com.school.management.service.StudentService;
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
class CourseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CourseService courseService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private CourseRepository courseRepository;

    @Test
    @DisplayName("POST /api/courses persists course to the database")
    void createCourse_persistsToDatabase() throws Exception {
        String body = """
                {"name":"Math","type":"MAIN"}
                """;

        mockMvc.perform(post("/api/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Math"))
                .andExpect(jsonPath("$.type").value("MAIN"));

        assertThat(courseRepository.existsByNameIgnoreCase("Math")).isTrue();
    }

    @Test
    @DisplayName("POST /api/courses returns 409 when course name is duplicated")
    void createCourse_returnsConflict_whenDuplicate() throws Exception {
        courseService.create(new CourseRequest("Math", CourseType.MAIN));

        String body = """
                {"name":"Math","type":"SECONDARY"}
                """;

        mockMvc.perform(post("/api/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("GET /api/courses returns all courses from the database")
    void getAllCourses_returnsListFromDatabase() throws Exception {
        courseService.create(new CourseRequest("Math", CourseType.MAIN));
        courseService.create(new CourseRequest("Art", CourseType.SECONDARY));

        mockMvc.perform(get("/api/courses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("GET /api/courses/{id} returns the course for an existing id")
    void getCourseById_returnsCourse() throws Exception {
        CourseResponse created = courseService.create(new CourseRequest("Math", CourseType.MAIN));

        mockMvc.perform(get("/api/courses/" + created.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(created.id()))
                .andExpect(jsonPath("$.name").value("Math"));
    }

    @Test
    @DisplayName("GET /api/courses/{id} returns 404 for a missing id")
    void getCourseById_returns404_whenMissing() throws Exception {
        mockMvc.perform(get("/api/courses/9999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/courses/{id} updates the course in the database")
    void updateCourse_persistsChanges() throws Exception {
        CourseResponse created = courseService.create(new CourseRequest("Math", CourseType.MAIN));

        String body = """
                {"name":"Physics","type":"SECONDARY"}
                """;

        mockMvc.perform(put("/api/courses/" + created.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Physics"))
                .andExpect(jsonPath("$.type").value("SECONDARY"));

        CourseResponse reloaded = courseService.findById(created.id());
        assertThat(reloaded.name()).isEqualTo("Physics");
        assertThat(reloaded.type()).isEqualTo(CourseType.SECONDARY);
    }

    @Test
    @DisplayName("DELETE /api/courses/{id} removes the course from the database")
    void deleteCourse_removesFromDatabase() throws Exception {
        CourseResponse created = courseService.create(new CourseRequest("Math", CourseType.MAIN));

        mockMvc.perform(delete("/api/courses/" + created.id()))
                .andExpect(status().isNoContent());

        assertThat(courseRepository.findById(created.id())).isEmpty();
    }

    @Test
    @DisplayName("DELETE /api/courses/{id} returns 409 when course is linked to a student")
    void deleteCourse_returnsConflict_whenLinkedToStudent() throws Exception {
        CourseResponse course = courseService.create(new CourseRequest("Math", CourseType.MAIN));
        studentService.create(new StudentRequest("Ivan", 19, "5A", Set.of("Math")));

        mockMvc.perform(delete("/api/courses/" + course.id()))
                .andExpect(status().isConflict());

        assertThat(courseRepository.findById(course.id())).isPresent();
    }
}
