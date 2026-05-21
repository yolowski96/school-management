package com.school.management.dto;

import com.school.management.entity.CourseType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CourseRequest(
        @NotBlank @Size(max = 255) String name,
        @NotNull CourseType type) {
}
