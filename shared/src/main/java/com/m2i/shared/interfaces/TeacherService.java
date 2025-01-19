package com.m2i.shared.interfaces;

import com.m2i.shared.dto.*;
import jakarta.ejb.Remote;
import com.m2i.shared.entities.Course;
import com.m2i.shared.entities.Grade;
import java.util.List;

@Remote
public interface TeacherService {
    void uploadCourse(String sessionId, CourseRequestDTO course);
    void submitGrade(String sessionId, GradeRequestDTO grade);
    List<StudentResponseDTO> getAssignedStudents(String sessionId);
    List<GradeResponseDTO> getGradesByStudent(String sessionId, String apogee);
    List<CourseResponseDTO> getAssignedCourses(String sessionId);
}