package com.school.management.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.school.management.dto.CourseResponse;
import com.school.management.dto.TeacherRequest;
import com.school.management.dto.TeacherResponse;
import com.school.management.entity.CourseType;
import com.school.management.exception.ResourceNotFoundException;
import com.school.management.service.TeacherService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;

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

@WebMvcTest(TeacherController.class)
class TeacherControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private TeacherService teacherService;

    private TeacherResponse sampleTeacher() {
        CourseResponse course = new CourseResponse(1L, "Math", CourseType.MAIN);
        return new TeacherResponse(20L, "Anna", 40, "5A", Set.of(course));
    }

    @Test
    void createTeacher_returns201_andLocationHeader() throws Exception {
        TeacherRequest request = new TeacherRequest("Anna", 40, "5A", Set.of("Math"));
        when(teacherService.create(any(TeacherRequest.class))).thenReturn(sampleTeacher());

        mockMvc.perform(post("/api/teachers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/teachers/20"))
                .andExpect(jsonPath("$.id").value(20))
                .andExpect(jsonPath("$.name").value("Anna"));
    }

    @Test
    void createTeacher_returns400_whenNameBlank() throws Exception {
        String body = """
                {"name":"","age":40,"group":"5A","courseNames":["Math"]}
                """;

        mockMvc.perform(post("/api/teachers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[?(@.field=='name')]").exists());
    }

    @Test
    void createTeacher_returns400_whenAgeTooLow() throws Exception {
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
    void createTeacher_returns404_whenCourseNotFound() throws Exception {
        TeacherRequest request = new TeacherRequest("Anna", 40, "5A", Set.of("Unknown"));
        when(teacherService.create(any(TeacherRequest.class)))
                .thenThrow(new ResourceNotFoundException("Course not found: Unknown"));

        mockMvc.perform(post("/api/teachers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Course not found: Unknown"));
    }

    @Test
    void getAllTeachers_returnsList() throws Exception {
        when(teacherService.findAll()).thenReturn(List.of(sampleTeacher()));

        mockMvc.perform(get("/api/teachers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Anna"));
    }

    @Test
    void getTeacherById_returns200_whenFound() throws Exception {
        when(teacherService.findById(20L)).thenReturn(sampleTeacher());

        mockMvc.perform(get("/api/teachers/20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(20))
                .andExpect(jsonPath("$.name").value("Anna"));
    }

    @Test
    void getTeacherById_returns404_whenNotFound() throws Exception {
        when(teacherService.findById(99L))
                .thenThrow(new ResourceNotFoundException("Teacher not found: 99"));

        mockMvc.perform(get("/api/teachers/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Teacher not found: 99"));
    }

    @Test
    void updateTeacher_returns200_andUpdatedBody() throws Exception {
        TeacherRequest request = new TeacherRequest("Anna Updated", 41, "6B", Set.of("Math"));
        TeacherResponse updated = new TeacherResponse(20L, "Anna Updated", 41, "6B", Set.of());
        when(teacherService.update(eq(20L), any(TeacherRequest.class))).thenReturn(updated);

        mockMvc.perform(put("/api/teachers/20")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Anna Updated"))
                .andExpect(jsonPath("$.group").value("6B"));
    }

    @Test
    void updateTeacher_returns404_whenTeacherMissing() throws Exception {
        TeacherRequest request = new TeacherRequest("Anna", 40, "5A", Set.of("Math"));
        when(teacherService.update(eq(99L), any(TeacherRequest.class)))
                .thenThrow(new ResourceNotFoundException("Teacher not found: 99"));

        mockMvc.perform(put("/api/teachers/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteTeacher_returns204() throws Exception {
        doNothing().when(teacherService).delete(20L);

        mockMvc.perform(delete("/api/teachers/20"))
                .andExpect(status().isNoContent());

        verify(teacherService).delete(20L);
    }

    @Test
    void deleteTeacher_returns404_whenTeacherMissing() throws Exception {
        doThrow(new ResourceNotFoundException("Teacher not found: 99"))
                .when(teacherService).delete(99L);

        mockMvc.perform(delete("/api/teachers/99"))
                .andExpect(status().isNotFound());
    }
}
