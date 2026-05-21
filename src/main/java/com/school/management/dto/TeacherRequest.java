package com.school.management.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record TeacherRequest(
        @NotBlank @Size(max = 255) String name,
        @NotNull @Min(18) Integer age,
        @NotBlank @Size(max = 255) String group,
        Set<@NotBlank String> courseNames) {
}
