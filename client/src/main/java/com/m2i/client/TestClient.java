package com.m2i.client;

import com.m2i.client.utils.ServiceLocator;
import com.m2i.client.utils.SessionManager;
import com.m2i.shared.auth.UserCredentials;
import com.m2i.shared.auth.UserSession;
import com.m2i.shared.dto.GradeResponseDTO;
import com.m2i.shared.dto.StudentResponseDTO;
import com.m2i.shared.entities.*;
import java.util.List;

public class TestClient {
    public static void main(String[] args) {
        try {
            // Test Coordinator Login
            testCoordinatorFlow();

            // Test Teacher Login
            testTeacherFlow();

            // Test Student Login
            testStudentFlow();

        } catch (Exception e) {
            System.err.println("Test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void testCoordinatorFlow() throws Exception {
        System.out.println("\n=== Testing Coordinator Flow ===");

        // Login
        UserCredentials credentials = new UserCredentials();
        credentials.setUsername("coord1");
        credentials.setPassword("coord123");

        UserSession session = ServiceLocator.getInstance()
                .getAuthService()
                .login(credentials);
        System.out.println("Session ID: " + session.getSessionId());
        SessionManager.getInstance().setCurrentSession(session);

        // Test coordinator operations
        var coordService = ServiceLocator.getInstance().getCoordinatorService();
        List<StudentResponseDTO> students = coordService.getAllStudents(session.getSessionId());

        System.out.println("Found " + students.size() + " students");
        students.forEach(s -> System.out.println("Student: " + s.getFirstName()));

        // Logout
        ServiceLocator.getInstance().getAuthService().logout(session.getSessionId());
    }

    private static void testTeacherFlow() throws Exception {
        System.out.println("\n=== Testing Teacher Flow ===");

        // Login
        UserCredentials credentials = new UserCredentials();
        credentials.setUsername("teacher1");
        credentials.setPassword("teacher123");

        UserSession session = ServiceLocator.getInstance()
                .getAuthService()
                .login(credentials);

        SessionManager.getInstance().setCurrentSession(session);

        // Test teacher operations
        var teacherService = ServiceLocator.getInstance().getTeacherService();
        List<GradeResponseDTO> grades = teacherService.getGradesByStudent(session.getSessionId(), "19032011");

        System.out.println("Found " + grades.size() + " grades");
        grades.forEach(g -> System.out.println("Grade: " + g.getGrade()));

        // Logout
        ServiceLocator.getInstance().getAuthService().logout(session.getSessionId());
    }

    private static void testStudentFlow() throws Exception {
        System.out.println("\n=== Testing Student Flow ===");

        // Login
        UserCredentials credentials = new UserCredentials();
        credentials.setUsername("student1");
        credentials.setPassword("student123");

        UserSession session = ServiceLocator.getInstance()
                .getAuthService()
                .login(credentials);

        SessionManager.getInstance().setCurrentSession(session);

        // Test student operations
        var studentService = ServiceLocator.getInstance().getStudentService();
        List<GradeResponseDTO> grades = studentService.getMyGrades(session.getSessionId());

        System.out.println("Found " + grades.size() + " grades");
        grades.forEach(g -> System.out.println("Grade: " + g.getGrade()));

        // Logout
        ServiceLocator.getInstance().getAuthService().logout(session.getSessionId());
    }
}