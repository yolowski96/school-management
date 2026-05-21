package com.school.management.dto;

import com.school.management.entity.PersonRole;

public record PersonResponse(
        Long id,
        String name,
        int age,
        String group,
        PersonRole role) {
}
