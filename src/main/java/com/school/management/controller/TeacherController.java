package com.school.management.controller;

import com.school.management.dto.TeacherRequest;
import com.school.management.dto.TeacherResponse;
import com.school.management.service.TeacherService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
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
@RequestMapping("/api/teachers")
public class TeacherController {

    private final TeacherService service;

    public TeacherController(TeacherService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(
            summary = "Create a teacher",
            description = "Creates a teacher and links them to the listed courses (by name). Returns 404 if any referenced course name does not exist.")
    public ResponseEntity<TeacherResponse> createTeacher(@Valid @RequestBody TeacherRequest request) {
        TeacherResponse created = service.create(request);
        return ResponseEntity.created(URI.create("/api/teachers/" + created.id())).body(created);
    }

    @GetMapping
    @Operation(
            summary = "List all teachers",
            description = "Returns all teachers.")
    public ResponseEntity<List<TeacherResponse>> getAllTeachers() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get a teacher by id",
            description = "Returns the teacher with the given id, or 404 if not found.")
    public ResponseEntity<TeacherResponse> getTeacherById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update a teacher",
            description = "Replaces the teacher's name, age, group and the full set of taught courses. PUT replaces, it does not merge.")
    public ResponseEntity<TeacherResponse> updateTeacher(@PathVariable Long id, @Valid @RequestBody TeacherRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete a teacher",
            description = "Removes the teacher and their course links. Returns 404 if the teacher does not exist.")
    public ResponseEntity<Void> deleteTeacher(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
