package com.m2i.shared.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import com.m2i.shared.dto.*;
import com.m2i.shared.entities.Semester;

public interface CoordinatorService extends Remote {
    void registerStudent(String sessionId, StudentRequestDTO studentRequestDTO) throws RemoteException;
    List<StudentResponseDTO> getAllStudents(String sessionId) throws RemoteException;
    void registerTeacher(String sessionId, TeacherRequestDTO teacherRequestDTO) throws RemoteException;
    List<TeacherResponseDTO> getAllTeachers(String sessionId) throws RemoteException;
    List<CourseResponseDTO> getAllCourses(String sessionId) throws RemoteException;
    void createCourse(String sessionId, CourseRequestDTO courseRequestDTO) throws RemoteException;
    void updateCourse(String sessionId, CourseRequestDTO courseRequestDTO) throws RemoteException;
    void deleteCourse(String sessionId, Long courseId) throws RemoteException;
    void generateGradeReport(String sessionId, Long gradesReportId) throws RemoteException;
    void approveGradeReport(String sessionId, Long gradesReportId) throws RemoteException;
    void rejectGradeReport(String sessionId, Long gradesReportId) throws RemoteException;
    List<GradesReportResponseDTO> getGradeReports(String sessionId) throws RemoteException;
    void enrollStudent(String sessionId, Long studentId, Semester semester) throws RemoteException;
    void removeStudent(String sessionId, String apogee) throws RemoteException;
    void removeTeacher(String sessionId, String teacherIdentifier) throws RemoteException;
}
