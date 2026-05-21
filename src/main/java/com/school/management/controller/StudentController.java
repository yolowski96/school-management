package com.school.management.controller;

import com.school.management.dto.StudentRequest;
import com.school.management.dto.StudentResponse;
import com.school.management.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private static final Logger log = LoggerFactory.getLogger(StudentController.class);

    private final StudentService service;

    public StudentController(StudentService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(
            summary = "Create a student",
            description = "Creates a student with the given personal data and a set of course assignments. Each assignment references a course by name and declares whether it is MAIN or SECONDARY for this student. Returns 404 if a referenced course name does not exist.")
    public ResponseEntity<StudentResponse> createStudent(@Valid @RequestBody StudentRequest request) {
        StudentResponse created = service.create(request);
        return ResponseEntity.created(URI.create("/api/students/" + created.id())).body(created);
    }

    @GetMapping
    @Operation(
            summary = "List all students",
            description = "Returns all students.")
    public ResponseEntity<List<StudentResponse>> getAllStudents() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get a student by id",
            description = "Returns the student with the given id, or 404 if not found.")
    public ResponseEntity<StudentResponse> getStudentById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update a student",
            description = "Replaces the student's name, age, group and the full set of course assignments. The previous course assignments are removed and the new ones inserted (PUT replaces, it does not merge).")
    public ResponseEntity<StudentResponse> updateStudent(@PathVariable Long id, @Valid @RequestBody StudentRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete a student",
            description = "Removes the student and all of their course assignments. Returns 404 if the student does not exist.")
    public ResponseEntity<Void> deleteStudent(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
