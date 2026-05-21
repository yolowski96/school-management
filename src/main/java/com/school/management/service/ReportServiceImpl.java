package com.school.management.service;

import com.school.management.dto.CountResponse;
import com.school.management.dto.CourseTypeCountResponse;
import com.school.management.dto.GroupCourseReportResponse;
import com.school.management.dto.PersonResponse;
import com.school.management.dto.StudentResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ReportServiceImpl implements ReportService {

    private static final Logger log = LoggerFactory.getLogger(ReportServiceImpl.class);

    private final StudentService studentService;
    private final TeacherService teacherService;
    private final CourseService courseService;

    public ReportServiceImpl(StudentService studentService,
                             TeacherService teacherService,
                             CourseService courseService) {
        this.studentService = studentService;
        this.teacherService = teacherService;
        this.courseService = courseService;
    }

    @Override
    public CountResponse countStudents() {
        log.info("Report: count students");
        return studentService.countStudents();
    }

    @Override
    public CountResponse countTeachers() {
        log.info("Report: count teachers");
        return teacherService.countTeachers();
    }

    @Override
    public List<CourseTypeCountResponse> countCoursesByType() {
        log.info("Report: count courses by type");
        return courseService.countGroupedByType();
    }

    @Override
    public List<StudentResponse> studentsByCourse(Long courseId) {
        log.info("Report: students by course id={}", courseId);
        courseService.ensureExists(courseId);
        return studentService.getStudentsByCourse(courseId);
    }

    @Override
    public List<StudentResponse> studentsByGroup(String group) {
        log.info("Report: students by group={}", group);
        return studentService.getStudentsByGroup(group);
    }

    @Override
    public GroupCourseReportResponse byGroupAndCourse(String group, Long courseId) {
        log.info("Report: teachers and students by group={} and course id={}", group, courseId);
        courseService.ensureExists(courseId);
        List<PersonResponse> teachers = teacherService.getTeachersByGroupAndCourse(group, courseId);
        List<PersonResponse> students = studentService.getStudentsByGroupAndCourse(group, courseId);
        return new GroupCourseReportResponse(teachers, students);
    }

    @Override
    public List<StudentResponse> studentsOlderThan(int age, Long courseId) {
        log.info("Report: students older than {} for course id={}", age, courseId);
        courseService.ensureExists(courseId);
        return studentService.findOlderThanByCourse(age, courseId);
    }
}
