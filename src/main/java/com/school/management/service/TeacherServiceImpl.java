package com.school.management.service;

import com.school.management.dto.CountResponse;
import com.school.management.dto.PersonResponse;
import com.school.management.dto.TeacherRequest;
import com.school.management.dto.TeacherResponse;
import com.school.management.entity.Course;
import com.school.management.entity.Person;
import com.school.management.entity.Teacher;
import com.school.management.exception.ResourceNotFoundException;
import com.school.management.mapper.TeacherMapper;
import com.school.management.repository.TeacherRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@Transactional
public class TeacherServiceImpl implements TeacherService {

    private static final Logger log = LoggerFactory.getLogger(TeacherServiceImpl.class);

    private final TeacherRepository teacherRepository;
    private final CourseService courseService;

    public TeacherServiceImpl(TeacherRepository teacherRepository, CourseService courseService) {
        this.teacherRepository = teacherRepository;
        this.courseService = courseService;
    }

    @Override
    public TeacherResponse create(TeacherRequest request) {
        log.info("Creating teacher: name={}, group={}", request.name(), request.group());
        Teacher teacher = new Teacher(request.name(), request.age(), Person.normalizeGroup(request.group()));
        Set<Course> courses = courseService.getCoursesByNames(request.courseNames());
        teacher.setCourses(courses);
        Teacher saved = teacherRepository.save(teacher);
        log.info("Created teacher id={}", saved.getId());
        return TeacherMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeacherResponse> findAll() {
        log.info("Listing all teachers");
        return teacherRepository.findAll().stream()
                .map(TeacherMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TeacherResponse findById(Long id) {
        log.info("Fetching teacher id={}", id);
        return TeacherMapper.toResponse(findTeacherById(id));
    }

    @Override
    public TeacherResponse update(Long id, TeacherRequest request) {
        log.info("Updating teacher id={}", id);
        Teacher teacher = findTeacherById(id);
        teacher.setName(request.name());
        teacher.setAge(request.age());
        teacher.setGroup(request.group());
        Set<Course> courses = courseService.getCoursesByNames(request.courseNames());
        teacher.setCourses(courses);
        Teacher saved = teacherRepository.save(teacher);
        log.info("Updated teacher id={}", saved.getId());
        return TeacherMapper.toResponse(saved);
    }

    @Override
    public void delete(Long id) {
        log.info("Deleting teacher id={}", id);
        Teacher teacher = findTeacherById(id);
        teacherRepository.delete(teacher);
        log.info("Deleted teacher id={}", id);
    }

    @Override
    public List<PersonResponse> getTeachersByGroupAndCourse(String group, Long courseId) {
        log.info("Fetching teachers for group={} and course id={}", group, courseId);
        return teacherRepository.findByGroupAndCourseId(Person.normalizeGroup(group), courseId).stream()
                .map(TeacherMapper::toPersonResponse)
                .toList();
    }

    @Override
    public CountResponse countTeachers() {
        log.info("Counting teachers");
        return new CountResponse(teacherRepository.count());
    }

    private Teacher findTeacherById(Long id) {
        return teacherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found: " + id));
    }
}
