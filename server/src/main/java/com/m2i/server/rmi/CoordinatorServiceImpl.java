package com.m2i.server.rmi;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

import com.m2i.server.dao.*;
import com.m2i.server.mapper.CourseMapper;
import com.m2i.server.mapper.GradesReportMapper;
import com.m2i.server.mapper.StudentsMapper;
import com.m2i.server.mapper.TeachersMapper;
import com.m2i.server.utils.ServiceLocator;
import com.m2i.shared.dto.*;
import com.m2i.shared.entities.*;
import com.m2i.shared.utils.SecurityUtils;
import jakarta.ejb.EJB;
import com.m2i.shared.interfaces.CoordinatorService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CoordinatorServiceImpl extends UnicastRemoteObject implements CoordinatorService {
    private CoordinatorDAO coordinatorDAO;

    @EJB
    private SecurityUtils securityUtils;
    @EJB
    private GradesReportDAO gradesReportDAO;
    @EJB
    private CourseDAO courseDAO;
    @EJB
    private TeacherDAO teacherDAO;
    @EJB
    private StudentDAO studentDAO;

    public CoordinatorServiceImpl() throws RemoteException {
        super();
        // Lookup EJBs using JNDI names
        this.securityUtils = ServiceLocator.lookup("ejb:/server-1.0-SNAPSHOT/SecurityUtilsBean!com.m2i.shared.utils.SecurityUtils");
        this.coordinatorDAO = ServiceLocator.lookup("ejb:/server-1.0-SNAPSHOT/CoordinatorDAOImpl!com.m2i.server.dao.CoordinatorDAO");
        this.gradesReportDAO = ServiceLocator.lookup("ejb:/server-1.0-SNAPSHOT/GradesReportDAOImpl!com.m2i.server.dao.GradesReportDAO");
        this.courseDAO = ServiceLocator.lookup("ejb:/server-1.0-SNAPSHOT/CourseDAOImpl!com.m2i.server.dao.CourseDAO");
        this.studentDAO = ServiceLocator.lookup("ejb:/server-1.0-SNAPSHOT/StudentDAOImpl!com.m2i.server.dao.StudentDAO");
        this.teacherDAO = ServiceLocator.lookup("ejb:/server-1.0-SNAPSHOT/TeacherDAOImpl!com.m2i.server.dao.TeacherDAO");
    }

    @Override
    public void registerStudent(String sessionId, StudentRequestDTO studentRequestDTO) throws RemoteException {
        Student student = StudentsMapper.toEntity(studentRequestDTO);
        log.info("Registering student: " + student.getUsername());
        try {
            securityUtils.checkAuthorization(sessionId, UserRole.COORDINATOR);

            // Set default role for student
            student.setRole(UserRole.STUDENT);
            student.setUsername(student.getApogee());
            student.setPassword(hashPassword(student.getCne()));
            studentDAO.save(student);
        } catch (SecurityException e) {
            log.error("Authorization failed: " + e.getMessage());
            throw new RemoteException("Authorization failed: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error registering student: " + e.getMessage());
            throw new RemoteException("Error registering student: " + e.getMessage());
        }
    }

    @Override
    public List<StudentResponseDTO> getAllStudents(String sessionId) throws RemoteException {
        try {
            securityUtils.checkAuthorization(sessionId, UserRole.COORDINATOR);
            List<Student> students = studentDAO.findAll();
            return students.stream().map(StudentsMapper::toDTO).toList();
        } catch (SecurityException e) {
            throw new RemoteException("Authorization failed: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error retrieving students: " + e.getMessage());
            throw new RemoteException("Error retrieving students: " + e.getMessage());
        }
    }

    @Override
    public void registerTeacher(String sessionId, TeacherRequestDTO teacherRequestDTO) throws RemoteException {
        try {
            securityUtils.checkAuthorization(sessionId, UserRole.COORDINATOR);
            Teacher teacher = TeachersMapper.toEntity(teacherRequestDTO);
            teacher.setUsername(teacherRequestDTO.getEmail());
            teacher.setPassword(hashPassword(teacherRequestDTO.getEmail()));
            teacherDAO.save(teacher);
        } catch (SecurityException e) {
            throw new RemoteException("Authorization failed: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error registering teacher: " + e.getMessage());
            throw new RemoteException("Error registering teacher: " + e.getMessage());
        }
    }

    @Override
    public List<TeacherResponseDTO> getAllTeachers(String sessionId) throws RemoteException {
        try {
            securityUtils.checkAuthorization(sessionId, UserRole.COORDINATOR);
            List<Teacher> teachers = teacherDAO.findAll();
            return teachers.stream().map(TeachersMapper::toDTO).toList();
        } catch (SecurityException e) {
            throw new RemoteException("Authorization failed: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error retrieving teachers: " + e.getMessage());
            throw new RemoteException("Error retrieving teachers: " + e.getMessage());
        }
    }

    @Override
    public List<CourseResponseDTO> getAllCourses(String sessionId) throws RemoteException {
        try {
            securityUtils.checkAuthorization(sessionId, UserRole.COORDINATOR);
            List<Course> courses = courseDAO.findAll();
            return courses.stream().map(CourseMapper::toDTO).toList();
        } catch (SecurityException e) {
            throw new RemoteException("Authorization failed: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error retrieving courses: " + e.getMessage());
            throw new RemoteException("Error retrieving courses: " + e.getMessage());
        }
    }

    @Override
    public void createCourse(String sessionId, CourseRequestDTO courseRequestDTO) throws RemoteException {
        try {
            securityUtils.checkAuthorization(sessionId, UserRole.COORDINATOR);
            Course course = CourseMapper.toEntity(courseRequestDTO);
            courseDAO.save(course);
        } catch (SecurityException e) {
            throw new RemoteException("Authorization failed: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error creating course: " + e.getMessage());
            throw new RemoteException("Error creating course: " + e.getMessage());
        }
    }

    @Override
    public void updateCourse(String sessionId, CourseRequestDTO courseRequestDTO) throws RemoteException {
        try {
            securityUtils.checkAuthorization(sessionId, UserRole.COORDINATOR);
            Course course = CourseMapper.toEntity(courseRequestDTO);
            courseDAO.save(course);
        } catch (SecurityException e) {
            throw new RemoteException("Authorization failed: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error updating course: " + e.getMessage());
            throw new RemoteException("Error updating course: " + e.getMessage());
        }
    }

    @Override
    public void deleteCourse(String sessionId, Long courseId) throws RemoteException {
        try {
            securityUtils.checkAuthorization(sessionId, UserRole.COORDINATOR);
            courseDAO.delete(courseId);
        } catch (SecurityException e) {
            throw new RemoteException("Authorization failed: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error deleting course: " + e.getMessage());
            throw new RemoteException("Error deleting course: " + e.getMessage());
        }
    }

    @Override
    public void generateGradeReport(String sessionId, GradesReportRequestDTO gradesReportRequestDTO) throws RemoteException {
        try {
            securityUtils.checkAuthorization(sessionId, UserRole.COORDINATOR);
            Coordinator coordinator = coordinatorDAO.findByUsername(sessionId);
            GradesReport gradesReport = GradesReportMapper.toEntity(gradesReportRequestDTO);
            Student student = studentDAO.findByApogee(gradesReport.getStudent().getApogee());
            if (student == null) {
                throw new RemoteException("Student not found");
            }
            gradesReportDAO.generate(gradesReport, coordinator.getFirstName() + " " + coordinator.getLastName());
        } catch (SecurityException e) {
            throw new RemoteException("Authorization failed: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error generating grade report: " + e.getMessage());
            throw new RemoteException("Error generating grade report: " + e.getMessage());
        }
    }

    @Override
    public void approveGradeReport(String sessionId, Long gradesReportId) throws RemoteException {
        try {
            securityUtils.checkAuthorization(sessionId, UserRole.COORDINATOR);
            GradesReport gradesReport = gradesReportDAO.findById(gradesReportId);
            if (gradesReport == null) {
                throw new RemoteException("Grade report not found");
            }
            gradesReportDAO.approve(gradesReport.getId());
        } catch (SecurityException e) {
            throw new RemoteException("Authorization failed: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error approving grade report: " + e.getMessage());
            throw new RemoteException("Error approving grade report: " + e.getMessage());
        }
    }

    @Override
    public void rejectGradeReport(String sessionId, Long gradesReportId) throws RemoteException {
        try {
            securityUtils.checkAuthorization(sessionId, UserRole.COORDINATOR);
            GradesReport gradesReport = gradesReportDAO.findById(gradesReportId);
            if (gradesReport == null) {
                throw new RemoteException("Grade report not found");
            }
            gradesReportDAO.reject(gradesReport.getId());
        } catch (SecurityException e) {
            throw new RemoteException("Authorization failed: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error rejecting grade report: " + e.getMessage());
            throw new RemoteException("Error rejecting grade report: " + e.getMessage());
        }
    }

    @Override
    public void enrollStudent(String sessionId, Long studentId, Semester semester) throws RemoteException {
        try{
            securityUtils.checkAuthorization(sessionId, UserRole.COORDINATOR);
            Student student = studentDAO.findById(studentId);
            if(student == null){
                throw new RemoteException("Student not found");
            }
            courseDAO.findAllBySemester(semester).forEach(course -> {
                course.getEnrolledStudents().add(student);
                courseDAO.save(course);
            });

        }catch (Exception e) {
            log.error("Error enrolling student: " + e.getMessage());
            throw new RemoteException("Error enrolling student: " + e.getMessage());
        }
    }

    @Override
    public void removeStudent(String sessionId, String apogee) throws RemoteException {
        try{
            securityUtils.checkAuthorization(sessionId, UserRole.COORDINATOR);
            Student student = studentDAO.findByApogee(apogee);
            if(student == null){
                throw new RemoteException("Student not found");
            }
            studentDAO.deleteByApogee(student.getApogee());
        }catch (Exception e) {
            log.error("Error removing student: " + e.getMessage());
            throw new RemoteException("Error removing student: " + e.getMessage());
        }
    }

    @Override
    public void removeTeacher(String sessionId, String teacherIdentifier) throws RemoteException {
        try{
            securityUtils.checkAuthorization(sessionId, UserRole.COORDINATOR);
            Teacher teacher = teacherDAO.findByTeacherIdentifier(teacherIdentifier);
            if(teacher == null){
                throw new RemoteException("Teacher not found");
            }
            teacherDAO.deleteByTeacherIdentifier(teacher.getTeacherIdentifier());
        }catch (Exception e) {
            log.error("Error removing teacher: " + e.getMessage());
            throw new RemoteException("Error removing teacher: " + e.getMessage());
        }
    }

    private String hashPassword(String password) {
        // Implement secure password hashing
        return password; // Simplified for example
    }
}