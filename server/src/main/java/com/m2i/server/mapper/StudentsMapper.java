package com.m2i.server.mapper;

import com.m2i.shared.dto.StudentRequestDTO;
import com.m2i.shared.dto.StudentResponseDTO;
import com.m2i.shared.entities.Student;

public class StudentsMapper {
    public static StudentResponseDTO toDTO(Student student) {
        StudentResponseDTO studentResponseDTO = new StudentResponseDTO();
        studentResponseDTO.setStudentId(student.getStudentId());
        studentResponseDTO.setFirstName(student.getFirstName());
        studentResponseDTO.setLastName(student.getLastName());
        studentResponseDTO.setCne(student.getCne());
        studentResponseDTO.setEmail(student.getEmail());
        studentResponseDTO.setCni(student.getCni());
        studentResponseDTO.setApogee(student.getApogee());
        return studentResponseDTO;
    }

    public static Student toEntity(StudentRequestDTO studentRequestDTO) {
        Student student = new Student();
        student.setStudentId(studentRequestDTO.getStudentId());
        student.setFirstName(studentRequestDTO.getFirstName());
        student.setLastName(studentRequestDTO.getLastName());
        student.setCne(studentRequestDTO.getCne());
        student.setEmail(studentRequestDTO.getEmail());
        student.setCni(studentRequestDTO.getCni());
        student.setApogee(studentRequestDTO.getApogee());
        return student;
    }
}
