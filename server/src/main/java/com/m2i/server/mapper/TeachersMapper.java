package com.m2i.server.mapper;

import com.m2i.shared.dto.TeacherRequestDTO;
import com.m2i.shared.dto.TeacherResponseDTO;
import com.m2i.shared.entities.Teacher;

public class TeachersMapper {
    public static TeacherResponseDTO toDTO(Teacher teacher) {
        TeacherResponseDTO teacherResponseDTO = new TeacherResponseDTO();
        teacherResponseDTO.setTeacherIdentifier(teacher.getTeacherIdentifier());
        teacherResponseDTO.setFirstName(teacher.getFirstName());
        teacherResponseDTO.setLastName(teacher.getLastName());
        teacherResponseDTO.setOfficeNumber(teacher.getOfficeNumber());
        teacherResponseDTO.setDepartment(teacher.getDepartment());
        teacherResponseDTO.setCni(teacher.getCni());
        teacherResponseDTO.setEmail(teacher.getEmail());
        return teacherResponseDTO;
    }

    public static Teacher toEntity(TeacherRequestDTO teacherRequestDTO) {
        Teacher teacher = new Teacher();
        teacher.setTeacherIdentifier(teacherRequestDTO.getTeacherIdentifier());
        teacher.setFirstName(teacherRequestDTO.getFirstName());
        teacher.setLastName(teacherRequestDTO.getLastName());
        teacher.setOfficeNumber(teacherRequestDTO.getOfficeNumber());
        teacher.setDepartment(teacherRequestDTO.getDepartment());
        teacher.setCni(teacherRequestDTO.getCni());
        teacher.setEmail(teacherRequestDTO.getEmail());
        return teacher;
    }
}
