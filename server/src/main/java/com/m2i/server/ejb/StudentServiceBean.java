package com.m2i.server.ejb;

import com.m2i.server.dao.*;
import com.m2i.server.mapper.CourseMapper;
import com.m2i.server.mapper.GradeMapper;
import com.m2i.server.mapper.GradesReportMapper;
import com.m2i.shared.auth.UserSession;
import com.m2i.shared.dto.CourseResponseDTO;
import com.m2i.shared.dto.GradeResponseDTO;
import com.m2i.shared.dto.GradesReportRequestDTO;
import com.m2i.shared.dto.GradesReportResponseDTO;
import com.m2i.shared.utils.SecurityUtils;
import jakarta.ejb.Stateless;
import jakarta.ejb.EJB;
import com.m2i.shared.entities.*;
import com.m2i.shared.interfaces.StudentService;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Stateless
public class StudentServiceBean implements StudentService {

    @EJB
    private SecurityUtils securityUtils;
    @EJB
    private SessionDAO sessionDAO;
    @EJB
    private CourseDAO courseDAO;
    @EJB
    private StudentDAO studentDAO;
    @EJB
    private GradeDAO gradeDAO;
    @EJB
    private GradesReportDAO gradesReportDAO;

    @Override
    public List<CourseResponseDTO> getAvailableCourses(String sessionId) {
        securityUtils.checkAuthorization(sessionId, UserRole.STUDENT);

        // Get courses.json where the student is enrolled
        UserSession session = sessionDAO.findBySessionId(sessionId);
        Student student = studentDAO.findByUsername(session.getUsername());
        return courseDAO.findByStudentId(student.getId()).stream().map(CourseMapper::toDTO).toList();
    }

    @Override
    public List<GradeResponseDTO> getMyGrades(String sessionId) {
        securityUtils.checkAuthorization(sessionId, UserRole.STUDENT);

        // Get only the logged-in student's grades
        UserSession session = sessionDAO.findBySessionId(sessionId);
        Student student = studentDAO.findByUsername(session.getUsername());
        return gradeDAO.findByStudentId(student.getId())
                .stream()
                .map(GradeMapper::toDTO)
                .toList();
    }

    @Override
    public byte[] downloadCourseContent(String sessionId, Long courseId) {
        securityUtils.checkAuthorization(sessionId, UserRole.STUDENT);

        UserSession session = sessionDAO.findBySessionId(sessionId);
        Course course = courseDAO.findById(courseId);

        if (course == null) {
            throw new IllegalArgumentException("Course not found");
        }

        // Verify student is enrolled in the course
        boolean isEnrolled = course.getEnrolledStudents().stream()
                .anyMatch(student -> student.getUsername().equals(session.getUsername()));

        if (!isEnrolled) {
            throw new SecurityException("Student is not enrolled in this course");
        }

        return course.getContent();
    }

    @Override
    public void requestGradesReport(String sessionId, GradesReportRequestDTO gradesReportRequestDTO) {
        securityUtils.checkAuthorization(sessionId, UserRole.STUDENT);

        UserSession session = sessionDAO.findBySessionId(sessionId);
        Student student = studentDAO.findByUsername(session.getUsername());

        GradesReport gradesReport = GradesReportMapper.toEntity(gradesReportRequestDTO);
        gradesReport.setStudent(student);
        gradesReport.setGenerated(false);
        gradesReport.setStatus(Status.PENDING);
        gradesReport.setRequestDate(LocalDate.now());

        gradesReportDAO.save(gradesReport);
    }

    @Override
    public List<GradesReportResponseDTO> fetchGradesReport(String sessionId) {
        securityUtils.checkAuthorization(sessionId, UserRole.STUDENT);

        UserSession session = sessionDAO.findBySessionId(sessionId);
        Student student = studentDAO.findByUsernameWithReports(session.getUsername());

        return gradesReportDAO.findByStudentId(student.getId())
                .stream()
                .map(GradesReportMapper::toDTO)
                .toList();
    }
}