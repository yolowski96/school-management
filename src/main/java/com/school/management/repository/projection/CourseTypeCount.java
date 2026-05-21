package com.school.management.repository.projection;

import com.school.management.entity.CourseType;

public interface CourseTypeCount {
    CourseType getType();
    long getCount();
}
