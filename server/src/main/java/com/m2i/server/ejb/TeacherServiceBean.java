package com.m2i.server.ejb;

import com.m2i.server.dao.*;
import com.m2i.server.mapper.CourseMapper;
import com.m2i.server.mapper.GradeMapper;
import com.m2i.server.mapper.StudentsMapper;
import com.m2i.shared.auth.UserSession;
import com.m2i.shared.dto.*;
import com.m2i.shared.utils.SecurityUtils;
import jakarta.ejb.Stateless;
import jakarta.ejb.EJB;
import com.m2i.shared.entities.*;
import com.m2i.shared.interfaces.TeacherService;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Stateless
@Slf4j
public class TeacherServiceBean implements TeacherService {

    @EJB
    private SecurityUtils securityUtils;
    @EJB
    private StudentDAO studentDAO;
    @EJB
    private TeacherDAO teacherDAO;
    @EJB
    private SessionDAO sessionDAO;
    @EJB
    private CourseDAO courseDAO;
    @EJB
    private GradeDAO gradeDAO;

    @Override
    public void uploadCourse(String sessionId, CourseRequestDTO courseRequestDTO) {
        securityUtils.checkAuthorization(sessionId, UserRole.TEACHER);

        try {
            Course course = CourseMapper.toEntity(courseRequestDTO);
            // Verify that the teacher owns this course
            Teacher teacher = teacherDAO.findById(course.getTeacher().getId());
            if (teacher == null) {
                throw new SecurityException("Teacher not found");
            }

            // Verify the logged-in teacher is the course owner
            UserSession session = sessionDAO.findBySessionId(sessionId);
            if (!teacher.getUsername().equals(session.getUsername())) {
                throw new SecurityException("Not authorized to modify this course");
            }

            if (course.getName() == null) {
                throw new IllegalArgumentException("Invalid course data");
            }

            course.setTeacher(teacher);
            if (course.getId() == 0) {
                courseDAO.save(course);
            } else {
                courseDAO.updateCourseContent(course.getId(), course.getContent());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error uploading course: " + e.getMessage());
        }
    }

    @Override
    public void submitGrade(String sessionId, GradeRequestDTO gradeRequestDTO) {
        securityUtils.checkAuthorization(sessionId, UserRole.TEACHER);

        try {
            Grade grade = GradeMapper.toEntity(gradeRequestDTO);
            // Verify that the teacher owns the course
            Course course = courseDAO.findById(grade.getCourse().getId());
            if (course == null) {
                throw new SecurityException("Course not found");
            }

            // Verify the logged-in teacher is the course owner
            UserSession session = sessionDAO.findBySessionId(sessionId);
            if (!course.getTeacher().getUsername().equals(session.getUsername())) {
                throw new SecurityException("Not authorized to submit grades for this course");
            }

            Student student = studentDAO.findByApogee(grade.getStudent().getApogee());
            grade.setStudent(student);
            grade.setYear(LocalDate.now().getYear());
            grade.setCourse(course);

            if (grade.getId() == null) {
                gradeDAO.save(grade);
            } else {
                gradeDAO.update(grade);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error submitting grade: " + e.getCause());
        }
    }

    @Override
    public List<StudentResponseDTO> getAssignedStudents(String sessionId) {
        securityUtils.checkAuthorization(sessionId, UserRole.TEACHER);

        UserSession session = sessionDAO.findBySessionId(sessionId);
        Teacher teacher = teacherDAO.findByUsername(session.getUsername());
        List <Course> courses = teacherDAO.findTeacherCourses(teacher.getId());
        List <Student> students = new ArrayList<>();
        for (Course course : courses) {
            log.info(" ----------------------------------------- Found {} course for a course", studentDAO.findByEnrolledCourse(course.getId()).size());
            students.addAll(studentDAO.findByEnrolledCourse(course.getId()));
        }
        // remove repeated students
        students = students.stream().distinct().toList();
        log.info(" ----------------------------------------- Found {} Teacher", teacher.getTeacherId());
        log.info(" ----------------------------------------- Found {} courses", courses.size());
        log.info(" ----------------------------------------- Found {} students", students.size());
        return students.stream().map(StudentsMapper::toDTO).toList();
    }

    @Override
    public List<GradeResponseDTO> getGradesByStudent(String sessionId, String apogee) {
        try{
            securityUtils.checkAuthorization(sessionId, UserRole.TEACHER);

            // Get teacher's course grades only
            UserSession session = sessionDAO.findBySessionId(sessionId);
            return gradeDAO.findByStudentApogeeAndTeacherUsername(apogee, session.getUsername()).stream().map(GradeMapper::toDTO).toList();
        }catch (Exception e){
            System.err.println("Detailed error: " + e.getCause());
            throw new RuntimeException("Error getting grades: " + e.getMessage());
        }
    }

    @Override
    public List<CourseResponseDTO> getAssignedCourses(String sessionId) {
        securityUtils.checkAuthorization(sessionId, UserRole.TEACHER);

        UserSession session = sessionDAO.findBySessionId(sessionId);
        Teacher teacher = teacherDAO.findByUsername(session.getUsername());
        return teacherDAO.findTeacherCourses(teacher.getId()).stream().map(CourseMapper::toDTO).toList();
    }
}