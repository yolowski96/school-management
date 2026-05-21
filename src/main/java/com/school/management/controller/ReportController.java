package com.school.management.controller;

import com.school.management.dto.CountResponse;
import com.school.management.dto.CourseTypeCountResponse;
import com.school.management.dto.GroupCourseReportResponse;
import com.school.management.dto.StudentResponse;
import com.school.management.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
@Validated
public class ReportController {

    private final ReportService service;

    public ReportController(ReportService service) {
        this.service = service;
    }

    @GetMapping("/students/count")
    @Operation(summary = "Count students")
    public ResponseEntity<CountResponse> getStudentCount() {
        return ResponseEntity.ok(service.countStudents());
    }

    @GetMapping("/teachers/count")
    @Operation(summary = "Count teachers")
    public ResponseEntity<CountResponse> getTeacherCount() {
        return ResponseEntity.ok(service.countTeachers());
    }

    @GetMapping("/courses/count")
    @Operation(
            summary = "Count courses by type",
            description = "Returns single count for that course type.")
    public ResponseEntity<List<CourseTypeCountResponse>> getCourseCount() {
        return ResponseEntity.ok(service.countCoursesByType());
    }

    @GetMapping("/courses/{courseId}/students")
    @Operation(summary = "Students participating in a specific course")
    public List<StudentResponse> getStudentsByCourse(@PathVariable Long courseId) {
        return service.studentsByCourse(courseId);
    }

    @GetMapping("/groups/{groupName}/students")
    @Operation(summary = "Students participating in a specific group")
    public List<StudentResponse> studentsByGroup(@PathVariable @NotBlank String groupName) {
        return service.studentsByGroup(groupName);
    }

    @GetMapping("/group-course")
    @Operation(summary = "Get report for teachers and students by group and course")
    public GroupCourseReportResponse getGroupCourseReport(
            @RequestParam @NotBlank String group,
            @RequestParam Long courseId) {
        return service.byGroupAndCourse(group, courseId);
    }

    @GetMapping(value = "/courses/{courseId}/students", params = "minAge")
    @Operation(summary = "Get students older than a specific age participating in a course")
    public List<StudentResponse> getStudentsByAgeAndCourse(
            @PathVariable Long courseId,
            @RequestParam @Min(18) int minAge) {
        return service.studentsOlderThan(minAge, courseId);
    }
}
