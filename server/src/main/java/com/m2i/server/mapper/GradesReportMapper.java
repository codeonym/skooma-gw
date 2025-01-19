package com.m2i.server.mapper;

import com.m2i.shared.dto.GradesReportResponseDTO;
import com.m2i.shared.dto.GradesReportRequestDTO;
import com.m2i.shared.entities.GradesReport;
import com.m2i.shared.entities.Student;

public class GradesReportMapper {
    public static GradesReportResponseDTO toDTO(GradesReport gradesReport) {
        GradesReportResponseDTO gradesReportResponseDTO = new GradesReportResponseDTO();
        gradesReportResponseDTO.setStudentId(gradesReport.getStudent().getApogee());
        gradesReportResponseDTO.setReport(gradesReport.getReport());
        gradesReportResponseDTO.setGenerationDate(gradesReport.getGenerationDate());
        gradesReportResponseDTO.setRequestDate(gradesReport.getRequestDate());
        gradesReportResponseDTO.setId(gradesReport.getId());
        gradesReportResponseDTO.setSemester(gradesReport.getSemester());
        gradesReportResponseDTO.setStatus(gradesReport.getStatus());
        gradesReportResponseDTO.setGenerated(gradesReport.isGenerated());
        return gradesReportResponseDTO;
    }

    public static GradesReport toEntity(GradesReportRequestDTO gradesReportRequestDTO) {
        GradesReport gradesReport = new GradesReport();
        Student student = new Student();
        student.setApogee(gradesReportRequestDTO.getStudentId());
        gradesReport.setStudent(student);
        gradesReport.setYear(gradesReportRequestDTO.getYear());
        gradesReport.setSemester(gradesReportRequestDTO.getSemester());
        return gradesReport;
    }
}
