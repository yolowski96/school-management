package com.school.management.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.school.management.dto.CourseRequest;
import com.school.management.dto.CourseResponse;
import com.school.management.entity.CourseType;
import com.school.management.exception.DuplicateResourceException;
import com.school.management.exception.ResourceInUseException;
import com.school.management.exception.ResourceNotFoundException;
import com.school.management.service.CourseService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CourseController.class)
class CourseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private CourseService courseService;

    private CourseResponse sampleCourse() {
        return new CourseResponse(1L, "Math", CourseType.MAIN);
    }

    @Test
    void createCourse_returns201_andLocationHeader() throws Exception {
        CourseRequest request = new CourseRequest("Math", CourseType.MAIN);
        when(courseService.create(any(CourseRequest.class))).thenReturn(sampleCourse());

        mockMvc.perform(post("/api/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/courses/1"))
                .andExpect(jsonPath("$.name").value("Math"))
                .andExpect(jsonPath("$.type").value("MAIN"));
    }

    @Test
    void createCourse_returns409_whenNameExists() throws Exception {
        CourseRequest request = new CourseRequest("Math", CourseType.MAIN);
        when(courseService.create(any(CourseRequest.class)))
                .thenThrow(new DuplicateResourceException("Course already exists: Math"));

        mockMvc.perform(post("/api/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Course already exists: Math"));
    }

    @Test
    void createCourse_returns400_whenNameBlank() throws Exception {
        String body = """
                {"name":"","type":"MAIN"}
                """;

        mockMvc.perform(post("/api/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[?(@.field=='name')]").exists());
    }

    @Test
    void createCourse_returns400_whenTypeMissing() throws Exception {
        String body = """
                {"name":"Math"}
                """;

        mockMvc.perform(post("/api/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[?(@.field=='type')]").exists());
    }

    @Test
    void getAllCourses_returnsList() throws Exception {
        when(courseService.findAll()).thenReturn(List.of(sampleCourse()));

        mockMvc.perform(get("/api/courses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Math"))
                .andExpect(jsonPath("$[0].type").value("MAIN"));
    }

    @Test
    void getCourseById_returns200_whenFound() throws Exception {
        when(courseService.findById(1L)).thenReturn(sampleCourse());

        mockMvc.perform(get("/api/courses/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Math"));
    }

    @Test
    void getCourseById_returns404_whenNotFound() throws Exception {
        when(courseService.findById(99L))
                .thenThrow(new ResourceNotFoundException("Course not found: 99"));

        mockMvc.perform(get("/api/courses/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Course not found: 99"));
    }

    @Test
    void updateCourse_returns200_andUpdatedBody() throws Exception {
        CourseRequest request = new CourseRequest("Physics", CourseType.SECONDARY);
        CourseResponse updated = new CourseResponse(1L, "Physics", CourseType.SECONDARY);
        when(courseService.update(eq(1L), any(CourseRequest.class))).thenReturn(updated);

        mockMvc.perform(put("/api/courses/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Physics"))
                .andExpect(jsonPath("$.type").value("SECONDARY"));
    }

    @Test
    void updateCourse_returns404_whenCourseMissing() throws Exception {
        CourseRequest request = new CourseRequest("Physics", CourseType.SECONDARY);
        when(courseService.update(eq(99L), any(CourseRequest.class)))
                .thenThrow(new ResourceNotFoundException("Course not found: 99"));

        mockMvc.perform(put("/api/courses/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateCourse_returns409_whenNameAlreadyInUse() throws Exception {
        CourseRequest request = new CourseRequest("Physics", CourseType.SECONDARY);
        when(courseService.update(eq(1L), any(CourseRequest.class)))
                .thenThrow(new DuplicateResourceException("Course already exists: Physics"));

        mockMvc.perform(put("/api/courses/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void deleteCourse_returns204() throws Exception {
        doNothing().when(courseService).delete(1L);

        mockMvc.perform(delete("/api/courses/1"))
                .andExpect(status().isNoContent());

        verify(courseService).delete(1L);
    }

    @Test
    void deleteCourse_returns409_whenCourseInUse() throws Exception {
        doThrow(new ResourceInUseException("Course 'Math' is still referenced by students or teachers"))
                .when(courseService).delete(1L);

        mockMvc.perform(delete("/api/courses/1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Course 'Math' is still referenced by students or teachers"));
    }

    @Test
    void deleteCourse_returns404_whenCourseMissing() throws Exception {
        doThrow(new ResourceNotFoundException("Course not found: 99"))
                .when(courseService).delete(99L);

        mockMvc.perform(delete("/api/courses/99"))
                .andExpect(status().isNotFound());
    }
}
