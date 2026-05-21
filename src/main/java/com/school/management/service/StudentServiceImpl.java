package com.school.management.service;

import com.school.management.dto.CountResponse;
import com.school.management.dto.PersonResponse;
import com.school.management.dto.StudentRequest;
import com.school.management.dto.StudentResponse;
import com.school.management.entity.Course;
import com.school.management.entity.Person;
import com.school.management.entity.Student;
import com.school.management.exception.ResourceNotFoundException;
import com.school.management.mapper.StudentMapper;
import com.school.management.repository.StudentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@Transactional
public class StudentServiceImpl implements StudentService {

    private static final Logger log = LoggerFactory.getLogger(StudentServiceImpl.class);

    private final StudentRepository studentRepository;
    private final CourseService courseService;

    public StudentServiceImpl(StudentRepository studentRepository, CourseService courseService) {
        this.studentRepository = studentRepository;
        this.courseService = courseService;
    }

    @Override
    public StudentResponse create(StudentRequest request) {
        log.info("Creating student: name={}, group={}", request.name(), request.group());
        Student student = new Student(request.name(), request.age(), Person.normalizeGroup(request.group()));
        Set<Course> courses = courseService.getCoursesByNames(request.courseNames());
        student.setCourses(courses);
        Student saved = studentRepository.save(student);
        log.info("Created student id={}", saved.getId());
        return StudentMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentResponse> findAll() {
        log.info("Listing all students");
        return studentRepository.findAll().stream()
                .map(StudentMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public StudentResponse findById(Long id) {
        log.info("Fetching student id={}", id);
        return StudentMapper.toResponse(findStudentById(id));
    }

    @Override
    public StudentResponse update(Long id, StudentRequest request) {
        log.info("Updating student id={}", id);
        Student student = findStudentById(id);
        student.setName(request.name());
        student.setAge(request.age());
        student.setGroup(request.group());
        Set<Course> courses = courseService.getCoursesByNames(request.courseNames());
        student.setCourses(courses);
        Student saved = studentRepository.save(student);
        log.info("Updated student id={}", saved.getId());
        return StudentMapper.toResponse(saved);
    }

    @Override
    public void delete(Long id) {
        log.info("Deleting student id={}", id);
        Student student = findStudentById(id);
        studentRepository.delete(student);
        log.info("Deleted student id={}", id);
    }

    @Override
    public List<StudentResponse> findOlderThanByCourse(int age, Long courseId) {
        log.info("Finding students older than {} in course id={}", age, courseId);
        return studentRepository.findOlderThanByCourseId(age, courseId).stream()
                .map(StudentMapper::toResponse)
                .toList();
    }

    @Override
    public List<StudentResponse> getStudentsByCourse(Long courseId) {
        log.info("Fetching students for course id={}", courseId);
        return studentRepository.findByCourseId(courseId).stream()
                .map(StudentMapper::toResponse)
                .toList();
    }

    @Override
    public List<StudentResponse> getStudentsByGroup(String groupName) {
        log.info("Fetching students for group={}", groupName);
        return studentRepository.findByGroup(Person.normalizeGroup(groupName)).stream()
                .map(StudentMapper::toResponse)
                .toList();
    }

    @Override
    public List<PersonResponse> getStudentsByGroupAndCourse(String group, Long courseId) {
        log.info("Fetching students for group={} and course id={}", group, courseId);
        return studentRepository.findByGroupAndCourseId(Person.normalizeGroup(group), courseId).stream()
                .map(StudentMapper::toPersonResponse)
                .toList();
    }

    @Override
    public CountResponse countStudents() {
        log.info("Counting students");
        return new CountResponse(studentRepository.count());
    }

    private Student findStudentById(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + id));
    }
}
