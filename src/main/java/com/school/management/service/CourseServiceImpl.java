package com.school.management.service;

import com.school.management.dto.CourseRequest;
import com.school.management.dto.CourseResponse;
import com.school.management.dto.CourseTypeCountResponse;
import com.school.management.entity.Course;
import com.school.management.entity.CourseType;
import com.school.management.exception.DuplicateResourceException;
import com.school.management.exception.ResourceInUseException;
import com.school.management.exception.ResourceNotFoundException;
import com.school.management.mapper.CourseMapper;
import com.school.management.repository.CourseRepository;
import com.school.management.repository.projection.CourseTypeCount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
public class CourseServiceImpl implements CourseService {

    private static final Logger log = LoggerFactory.getLogger(CourseServiceImpl.class);

    private final CourseRepository courseRepository;

    public CourseServiceImpl(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    @Override
    public CourseResponse create(CourseRequest request) {
        String name = Course.normalizeName(request.name());
        log.info("Creating course: name={}, type={}", name, request.type());
        if (courseRepository.existsByNameIgnoreCase(name)) {
            throw new DuplicateResourceException("Course already exists: " + name);
        }
        Course course = new Course(name, request.type());
        Course saved = courseRepository.save(course);
        log.info("Created course id={}", saved.getId());
        return CourseMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseResponse> findAll() {
        log.info("Fetch all courses.");
        return courseRepository.findAll().stream().map(CourseMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CourseResponse findById(Long id) {
        log.info("Fetching course id={}", id);
        return CourseMapper.toResponse(findCourseById(id));
    }

    @Override
    public CourseResponse update(Long id, CourseRequest request) {
        log.info("Updating course id={}", id);
        Course course = findCourseById(id);
        String newName = Course.normalizeName(request.name());
        if (!course.getName().equalsIgnoreCase(newName) && courseRepository.existsByNameIgnoreCase(newName)) {
            throw new DuplicateResourceException("Course already exists: " + newName);
        }
        course.setName(newName);
        course.setType(request.type());
        Course saved = courseRepository.save(course);
        log.info("Updated course id={}", saved.getId());
        return CourseMapper.toResponse(saved);
    }

    @Override
    public void delete(Long id) {
        log.info("Deleting course id={}", id);
        Course course = findCourseById(id);
        if (courseRepository.isCourseLinkedToAnyStudent(id) || courseRepository.isCourseLinkedToAnyTeacher(id)) {
            throw new ResourceInUseException(
                    "Course '" + course.getName() + "' is still referenced by students or teachers");
        }
        courseRepository.delete(course);
        log.info("Deleted course id={}", id);
    }

    @Override
    public List<CourseTypeCountResponse> countGroupedByType() {
        log.info("Counting courses grouped by type");
        Map<CourseType, Long> counts = courseRepository.countGroupedByType().stream()
                .collect(Collectors.toMap(CourseTypeCount::getType, CourseTypeCount::getCount));
        return Arrays.stream(CourseType.values())
                .map(t -> new CourseTypeCountResponse(t, counts.getOrDefault(t, 0L)))
                .toList();
    }

    @Override
    public Set<Course> getCoursesByNames(Set<String> names) {
        if (names == null || names.isEmpty()) {
            return new HashSet<>();
        }
        Set<String> lowered = names.stream()
                .map(CourseServiceImpl::toLowerKey)
                .collect(Collectors.toSet());
        List<Course> found = courseRepository.findAllByLowerNameIn(lowered);
        Map<String, Course> byLowerName = found.stream()
                .collect(Collectors.toMap(c -> toLowerKey(c.getName()), Function.identity()));
        Set<Course> resolved = new HashSet<>();
        for (String original : names) {
            Course course = byLowerName.get(toLowerKey(original));
            if (course == null) {
                throw new ResourceNotFoundException("Course not found: " + original);
            }
            resolved.add(course);
        }
        return resolved;
    }

    @Override
    public void ensureExists(Long id) {
        if (!courseRepository.existsById(id)) {
            throw new ResourceNotFoundException("Course not found: " + id);
        }
    }

    private Course findCourseById(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found: " + id));
    }

    private static String toLowerKey(String name) {
        return name == null ? null : name.trim().toLowerCase(Locale.ROOT);
    }
}
