package com.school.management.dto;

import java.util.List;

public record GroupCourseReportResponse(
        List<PersonResponse> teachers,
        List<PersonResponse> students) {
}
