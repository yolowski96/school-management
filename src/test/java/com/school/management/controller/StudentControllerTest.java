package com.school.management.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.school.management.dto.CourseResponse;
import com.school.management.dto.StudentRequest;
import com.school.management.dto.StudentResponse;
import com.school.management.entity.CourseType;
import com.school.management.exception.ResourceNotFoundException;
import com.school.management.service.StudentService;
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

@WebMvcTest(StudentController.class)
class StudentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private StudentService studentService;

    private StudentResponse sampleStudent() {
        CourseResponse course = new CourseResponse(1L, "Math", CourseType.MAIN);
        return new StudentResponse(10L, "Ivan", 20, "5A", Set.of(course));
    }

    @Test
    void createStudent_returns201_andLocationHeader() throws Exception {
        StudentRequest request = new StudentRequest("Ivan", 20, "5A", Set.of("Math"));
        when(studentService.create(any(StudentRequest.class))).thenReturn(sampleStudent());

        mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/students/10"))
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.name").value("Ivan"));
    }

    @Test
    void createStudent_returns400_whenNameBlank() throws Exception {
        String body = """
                {"name":"","age":20,"group":"5A","courseNames":["Math"]}
                """;

        mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors[?(@.field=='name')]").exists());
    }

    @Test
    void createStudent_returns400_whenAgeTooLow() throws Exception {
        String body = """
                {"name":"Ivan","age":15,"group":"5A","courseNames":["Math"]}
                """;

        mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[?(@.field=='age')]").exists());
    }

    @Test
    void createStudent_returns404_whenCourseNotFound() throws Exception {
        StudentRequest request = new StudentRequest("Ivan", 20, "5A", Set.of("Unknown"));
        when(studentService.create(any(StudentRequest.class)))
                .thenThrow(new ResourceNotFoundException("Course not found: Unknown"));

        mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Course not found: Unknown"));
    }

    @Test
    void getAllStudents_returnsList() throws Exception {
        when(studentService.findAll()).thenReturn(List.of(sampleStudent()));

        mockMvc.perform(get("/api/students"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Ivan"));
    }

    @Test
    void getStudentById_returns200_whenFound() throws Exception {
        when(studentService.findById(10L)).thenReturn(sampleStudent());

        mockMvc.perform(get("/api/students/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.name").value("Ivan"));
    }

    @Test
    void getStudentById_returns404_whenNotFound() throws Exception {
        when(studentService.findById(99L))
                .thenThrow(new ResourceNotFoundException("Student not found: 99"));

        mockMvc.perform(get("/api/students/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Student not found: 99"));
    }

    @Test
    void updateStudent_returns200_andUpdatedBody() throws Exception {
        StudentRequest request = new StudentRequest("Ivan", 25, "5B", Set.of("Math"));
        StudentResponse updated = new StudentResponse(10L, "Ivan", 25, "5B", Set.of());
        when(studentService.update(eq(10L), any(StudentRequest.class))).thenReturn(updated);

        mockMvc.perform(put("/api/students/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.age").value(25))
                .andExpect(jsonPath("$.group").value("5B"));
    }

    @Test
    void updateStudent_returns404_whenStudentMissing() throws Exception {
        StudentRequest request = new StudentRequest("Ivan", 25, "5B", Set.of("Math"));
        when(studentService.update(eq(99L), any(StudentRequest.class)))
                .thenThrow(new ResourceNotFoundException("Student not found: 99"));

        mockMvc.perform(put("/api/students/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteStudent_returns204() throws Exception {
        doNothing().when(studentService).delete(10L);

        mockMvc.perform(delete("/api/students/10"))
                .andExpect(status().isNoContent());

        verify(studentService).delete(10L);
    }

    @Test
    void deleteStudent_returns404_whenStudentMissing() throws Exception {
        doThrow(new ResourceNotFoundException("Student not found: 99"))
                .when(studentService).delete(99L);

        mockMvc.perform(delete("/api/students/99"))
                .andExpect(status().isNotFound());
    }
}
