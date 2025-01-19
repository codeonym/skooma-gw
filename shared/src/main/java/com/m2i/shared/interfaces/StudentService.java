package com.m2i.shared.interfaces;

import com.m2i.shared.dto.CourseResponseDTO;
import com.m2i.shared.dto.GradeResponseDTO;
import com.m2i.shared.dto.GradesReportRequestDTO;
import com.m2i.shared.dto.GradesReportResponseDTO;
import jakarta.ejb.Remote;
import java.util.List;

@Remote
public interface StudentService {
    List<CourseResponseDTO> getAvailableCourses(String sessionId);
    List<GradeResponseDTO> getMyGrades(String sessionId);
    byte[] downloadCourseContent(String sessionId, Long courseId);
    void requestGradesReport(String sessionId, GradesReportRequestDTO gradesReportRequestDTO);
    List<GradesReportResponseDTO> fetchGradesReport(String sessionId);
}