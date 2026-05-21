package com.school.management.service;

import com.school.management.dto.CourseRequest;
import com.school.management.dto.CourseResponse;
import com.school.management.dto.CourseTypeCountResponse;
import com.school.management.entity.Course;
import com.school.management.entity.CourseType;
import com.school.management.exception.DuplicateResourceException;
import com.school.management.exception.ResourceInUseException;
import com.school.management.exception.ResourceNotFoundException;
import com.school.management.repository.CourseRepository;
import com.school.management.repository.projection.CourseTypeCount;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private CourseServiceImpl courseService;

    private Course math;

    @BeforeEach
    void setUp() {
        math = new Course("Math", CourseType.MAIN);
        math.setId(1L);
    }

    @Test
    @DisplayName("create persists a new course and returns its response")
    void create_persistsCourse_andReturnsResponse() {
        CourseRequest request = new CourseRequest("Math", CourseType.MAIN);
        when(courseRepository.existsByNameIgnoreCase("Math")).thenReturn(false);
        when(courseRepository.save(any(Course.class))).thenReturn(math);

        CourseResponse response = courseService.create(request);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Math");
        assertThat(response.type()).isEqualTo(CourseType.MAIN);

        ArgumentCaptor<Course> captor = ArgumentCaptor.forClass(Course.class);
        verify(courseRepository).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("Math");
        assertThat(captor.getValue().getType()).isEqualTo(CourseType.MAIN);
    }

    @Test
    @DisplayName("create throws DuplicateResourceException when course name already exists")
    void create_throws_whenNameExists() {
        CourseRequest request = new CourseRequest("Math", CourseType.MAIN);
        when(courseRepository.existsByNameIgnoreCase("Math")).thenReturn(true);

        assertThatThrownBy(() -> courseService.create(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Math");

        verify(courseRepository, never()).save(any());
    }

    @Test
    @DisplayName("findAll returns a list of mapped course responses")
    void findAll_returnsListOfMappedResponses() {
        when(courseRepository.findAll()).thenReturn(List.of(math));

        List<CourseResponse> result = courseService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().id()).isEqualTo(1L);
        assertThat(result.getFirst().name()).isEqualTo("Math");
        assertThat(result.getFirst().type()).isEqualTo(CourseType.MAIN);
    }

    @Test
    @DisplayName("findAll returns empty list when no courses exist")
    void findAll_returnsEmptyList_whenNoCourses() {
        when(courseRepository.findAll()).thenReturn(List.of());

        List<CourseResponse> result = courseService.findAll();

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findById returns mapped response when course exists")
    void findById_returnsResponse_whenCourseExists() {
        when(courseRepository.findById(1L)).thenReturn(Optional.of(math));

        CourseResponse response = courseService.findById(1L);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Math");
    }

    @Test
    @DisplayName("findById throws ResourceNotFoundException when course missing")
    void findById_throws_whenCourseMissing() {
        when(courseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courseService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Course not found: 99");
    }

    @Test
    @DisplayName("update changes the course fields and returns the response")
    void update_changesFields_andReturnsResponse() {
        CourseRequest request = new CourseRequest("Physics", CourseType.SECONDARY);
        when(courseRepository.findById(1L)).thenReturn(Optional.of(math));
        when(courseRepository.existsByNameIgnoreCase("Physics")).thenReturn(false);
        when(courseRepository.save(any(Course.class))).thenAnswer(inv -> inv.getArgument(0));

        CourseResponse response = courseService.update(1L, request);

        assertThat(response.name()).isEqualTo("Physics");
        assertThat(response.type()).isEqualTo(CourseType.SECONDARY);
    }

    @Test
    @DisplayName("update does not check duplicate name when name is unchanged")
    void update_doesNotCheckDuplicate_whenNameUnchanged() {
        CourseRequest request = new CourseRequest("Math", CourseType.SECONDARY);
        when(courseRepository.findById(1L)).thenReturn(Optional.of(math));
        when(courseRepository.save(any(Course.class))).thenAnswer(inv -> inv.getArgument(0));

        CourseResponse response = courseService.update(1L, request);

        assertThat(response.name()).isEqualTo("Math");
        assertThat(response.type()).isEqualTo(CourseType.SECONDARY);
        verify(courseRepository, never()).existsByNameIgnoreCase(any());
    }

    @Test
    @DisplayName("update throws DuplicateResourceException when new name already in use")
    void update_throws_whenNewNameExists() {
        CourseRequest request = new CourseRequest("Physics", CourseType.MAIN);
        when(courseRepository.findById(1L)).thenReturn(Optional.of(math));
        when(courseRepository.existsByNameIgnoreCase("Physics")).thenReturn(true);

        assertThatThrownBy(() -> courseService.update(1L, request))
                .isInstanceOf(DuplicateResourceException.class);

        verify(courseRepository, never()).save(any());
    }

    @Test
    @DisplayName("update throws ResourceNotFoundException when course missing")
    void update_throws_whenCourseMissing() {
        CourseRequest request = new CourseRequest("Physics", CourseType.MAIN);
        when(courseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courseService.update(99L, request))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(courseRepository, never()).save(any());
    }

    @Test
    @DisplayName("delete removes the course when not linked to anyone")
    void delete_removesCourse_whenNotLinked() {
        when(courseRepository.findById(1L)).thenReturn(Optional.of(math));
        when(courseRepository.isCourseLinkedToAnyStudent(1L)).thenReturn(false);
        when(courseRepository.isCourseLinkedToAnyTeacher(1L)).thenReturn(false);

        courseService.delete(1L);

        verify(courseRepository).delete(math);
    }

    @Test
    @DisplayName("delete throws ResourceInUseException when course is linked to a student")
    void delete_throws_whenLinkedToStudent() {
        when(courseRepository.findById(1L)).thenReturn(Optional.of(math));
        when(courseRepository.isCourseLinkedToAnyStudent(1L)).thenReturn(true);

        assertThatThrownBy(() -> courseService.delete(1L))
                .isInstanceOf(ResourceInUseException.class)
                .hasMessageContaining("Math");

        verify(courseRepository, never()).delete(any(Course.class));
    }

    @Test
    @DisplayName("delete throws ResourceInUseException when course is linked to a teacher")
    void delete_throws_whenLinkedToTeacher() {
        when(courseRepository.findById(1L)).thenReturn(Optional.of(math));
        when(courseRepository.isCourseLinkedToAnyStudent(1L)).thenReturn(false);
        when(courseRepository.isCourseLinkedToAnyTeacher(1L)).thenReturn(true);

        assertThatThrownBy(() -> courseService.delete(1L))
                .isInstanceOf(ResourceInUseException.class);

        verify(courseRepository, never()).delete(any(Course.class));
    }

    @Test
    @DisplayName("delete throws ResourceNotFoundException when course missing")
    void delete_throws_whenCourseMissing() {
        when(courseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courseService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("countGroupedByType returns zero for missing course types and the count for present ones")
    void countGroupedByType_returnsCountsIncludingZero() {
        CourseTypeCount mainCount = new CourseTypeCount() {
            @Override
            public CourseType getType() {
                return CourseType.MAIN;
            }

            @Override
            public long getCount() {
                return 3L;
            }
        };
        when(courseRepository.countGroupedByType()).thenReturn(List.of(mainCount));

        List<CourseTypeCountResponse> result = courseService.countGroupedByType();

        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(CourseTypeCountResponse::type, CourseTypeCountResponse::count)
                .containsExactlyInAnyOrder(
                        org.assertj.core.groups.Tuple.tuple(CourseType.MAIN, 3L),
                        org.assertj.core.groups.Tuple.tuple(CourseType.SECONDARY, 0L));
    }

    @Test
    @DisplayName("getCoursesByNames returns the resolved set of courses")
    void getCoursesByNames_returnsResolvedSet() {
        Course art = new Course("Art", CourseType.SECONDARY);
        art.setId(2L);
        when(courseRepository.findAllByLowerNameIn(Set.of("math", "art"))).thenReturn(List.of(math, art));

        Set<Course> result = courseService.getCoursesByNames(Set.of("Math", "Art"));

        assertThat(result).containsExactlyInAnyOrder(math, art);
    }

    @Test
    @DisplayName("getCoursesByNames returns empty set when input is null")
    void getCoursesByNames_returnsEmpty_whenInputNull() {
        Set<Course> result = courseService.getCoursesByNames(null);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getCoursesByNames returns empty set when input is empty")
    void getCoursesByNames_returnsEmpty_whenInputEmpty() {
        Set<Course> result = courseService.getCoursesByNames(new HashSet<>());

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getCoursesByNames throws ResourceNotFoundException when a name is unresolved")
    void getCoursesByNames_throws_whenNameUnresolved() {
        when(courseRepository.findAllByLowerNameIn(Set.of("math", "unknown"))).thenReturn(List.of(math));

        assertThatThrownBy(() -> courseService.getCoursesByNames(Set.of("Math", "Unknown")))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Unknown");
    }

    @Test
    @DisplayName("ensureExists does nothing when course exists")
    void ensureExists_doesNothing_whenCourseExists() {
        when(courseRepository.existsById(1L)).thenReturn(true);

        courseService.ensureExists(1L);
    }

    @Test
    @DisplayName("ensureExists throws ResourceNotFoundException when course missing")
    void ensureExists_throws_whenCourseMissing() {
        when(courseRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> courseService.ensureExists(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Course not found: 99");
    }
}
