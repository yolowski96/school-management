package com.school.management.controller;

import com.school.management.dto.CourseRequest;
import com.school.management.dto.CourseResponse;
import com.school.management.service.CourseService;
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
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseService service;

    public CourseController(CourseService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(
            summary = "Create a course",
            description = "Creates a new course identified by its unique name. Returns 409 if the name already exists.")
    public ResponseEntity<CourseResponse> createCourse(@Valid @RequestBody CourseRequest request) {
        CourseResponse created = service.create(request);
        return ResponseEntity.created(URI.create("/api/courses/" + created.id())).body(created);
    }

    @GetMapping
    @Operation(
            summary = "List all courses",
            description = "Returns a list of courses.")
    public ResponseEntity<List<CourseResponse>> getAllCourses() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get a course by id",
            description = "Returns the course with the given id, or 404 if it does not exist.")
    public ResponseEntity<CourseResponse> getCourseById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update a course",
            description = "Renames the course with the given id. Returns 404 if missing, 409 if the new name is already in use.")
    public ResponseEntity<CourseResponse> updateCourse(@PathVariable Long id, @Valid @RequestBody CourseRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete a course",
            description = "Removes the course. Fails with 409 if the course is still referenced by students or teachers.")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
