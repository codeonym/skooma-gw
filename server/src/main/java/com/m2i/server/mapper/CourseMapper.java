package com.m2i.server.mapper;

import com.m2i.shared.dto.CourseRequestDTO;
import com.m2i.shared.dto.CourseResponseDTO;
import com.m2i.shared.entities.Course;
import com.m2i.shared.entities.Teacher;

public class CourseMapper {
    public static CourseResponseDTO toDTO(Course entity) {
        CourseResponseDTO dto = new CourseResponseDTO();
        dto.setCourseId(entity.getId());
        dto.setName(entity.getName());
        dto.setSemester(entity.getSemester());
        dto.setContent(entity.getContent());
        if(entity.getTeacher() != null) {
            dto.setTeacher(entity.getTeacher().getFirstName() + " " + entity.getTeacher().getLastName());
        }else {
            dto.setTeacher("No teacher assigned");
        }
        return dto;
    }

    public static Course toEntity(CourseRequestDTO dto) {
        Course entity = new Course();
        entity.setId(dto.getCourseId());
        entity.setContent(dto.getContent());
        entity.setTeacher(new Teacher());
        entity.getTeacher().setId((dto.getTeacherId()));
        return entity;
    }
}
