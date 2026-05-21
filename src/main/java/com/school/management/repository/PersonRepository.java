package com.school.management.repository;

import com.school.management.entity.Person;
import jakarta.annotation.Nonnull;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

@NoRepositoryBean
public interface PersonRepository<T extends Person> extends JpaRepository<T, Long> {

    @Override
    @EntityGraph(attributePaths = "courses")
    @Nonnull
    List<T> findAll();

    @EntityGraph(attributePaths = "courses")
    Optional<T> findById(@Nonnull Long id);

    @Query("SELECT DISTINCT p FROM #{#entityName} p JOIN p.courses c " +
            "WHERE p.group = :group AND c.id = :courseId")
    List<T> findByGroupAndCourseId(@Param("group") String group,
                                   @Param("courseId") Long courseId);
}
