package com.m2i.server.mapper;

import com.m2i.shared.dto.GradeResponseDTO;
import com.m2i.shared.dto.GradeRequestDTO;
import com.m2i.shared.entities.Course;
import com.m2i.shared.entities.Grade;
import com.m2i.shared.entities.Student;

public class GradeMapper {
    public static GradeResponseDTO toDTO(Grade entity) {
        GradeResponseDTO dto = new GradeResponseDTO();
        dto.setId(entity.getId());
        dto.setGrade(entity.getValue());
        dto.setCourse(entity.getCourse().getName());
        if(entity.getSemester() != null){
            dto.setSemester(entity.getSemester().toString());
        }
        dto.setYear(entity.getYear());
        dto.setDate(entity.getDate());
        return dto;
    }

    public static Grade toEntity(GradeRequestDTO dto) {
        Grade entity = new Grade();
        entity.setValue(dto.getGrade());
        entity.setStudent(new Student());
        entity.getStudent().setApogee(dto.getStudentId());
        entity.setCourse(new Course());
        entity.getCourse().setId((dto.getCourseId()));
        entity.setSemester(dto.getSemester());
        return entity;
    }
}
