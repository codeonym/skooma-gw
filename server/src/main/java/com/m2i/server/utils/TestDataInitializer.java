package com.m2i.server.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.m2i.server.dao.*;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import com.m2i.shared.entities.*;
import java.io.InputStream;
import java.util.*;

@Singleton
@Startup
public class TestDataInitializer {
    @PersistenceContext
    private EntityManager em;

    @EJB
    private UserDAO userDAO;

    @EJB
    private TeacherDAO teacherDAO;

    @EJB
    private StudentDAO studentDAO;

    @EJB
    private CourseDAO courseDAO;
    @EJB
    private CoordinatorDAO coordinatorDAO;
    @EJB
    private GradeDAO gradeDAO;

    private final ObjectMapper mapper = new ObjectMapper();

    @PostConstruct
    public void init() {
        if (isDatabaseEmpty()) {
            createTestData();
        }
    }

    private boolean isDatabaseEmpty() {
        Long count = em.createQuery("SELECT COUNT(u) FROM User u", Long.class)
                .getSingleResult();
        return count == 0;
    }

    private void createTestData() {
        try {
            // Read JSON data
            List<Map<String, Object>> teachersData = readJsonFile("/raw/teachers.json");
            List<Map<String, Object>> coordinatorsData = readJsonFile("/raw/coordinators.json");
            List<Map<String, Object>> coursesData = readJsonFile("/raw/courses.json");
            List<Map<String, Object>> studentsData = readJsonFile("/raw/students.json");

            // Create teachers first
            Map<String, Teacher> teacherMap = new HashMap<>();
            for (Map<String, Object> teacherData : teachersData) {
                Teacher teacher = createTeacher(teacherData);
                teacherMap.put(String.valueOf(teacherData.get("teacherId")), teacher);
            }

            // Create courses and link them to teachers
            List<Course> courses = new ArrayList<>();
            for (Map<String, Object> courseData : coursesData) {
                Course course = createCourse(courseData, teacherMap);
                courses.add(course);
            }

            // Create students
            for (Map<String, Object> studentData : studentsData) {
                createStudent(studentData);
            }
            // Create coordinators
            for (Map<String, Object> coordinatorData : coordinatorsData) {
                createCoordinator(coordinatorData);
            }

            // Enroll students in courses
            List<Student> students = studentDAO.findAll();
            List<Course> savedCourses = courseDAO.findAll();
            for(Course c: savedCourses){
                c.setEnrolledStudents(students);
                courseDAO.save(c);
            }

            // generate random grades
            List<Student> savedStudents = studentDAO.findAll();
            for(Student s: savedStudents){
                for(Course c: savedCourses){
                    if(c.getSemester().equals(Semester.S1) || c.getSemester().equals(Semester.S2)){
                        Grade grade = new Grade();
                        grade.setCourse(c);
                        grade.setStudent(s);
                        grade.setYear(c.getSemester() == Semester.S1 ? 2023 : c.getSemester() == Semester.S2 ? 2024 : 2025);
                        grade.setValue((int) ((Math.random() * 10) + 10));
                        gradeDAO.save(grade);
                    }
                }
            }


        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize test data", e);
        }
    }

    private List<Map<String, Object>> readJsonFile(String filePath) throws Exception {
        try (InputStream is = getClass().getResourceAsStream(filePath)) {
            if (is == null) {
                throw new RuntimeException("Could not find file: " + filePath);
            }
            return mapper.readValue(is, new TypeReference<List<Map<String, Object>>>() {});
        }
    }

    private Teacher createTeacher(Map<String, Object> data) {
        Teacher teacher = new Teacher();

        // Set basic User fields
        teacher.setFirstName((String) data.get("firstName"));
        teacher.setLastName((String) data.get("lastName"));
        teacher.setEmail((String) data.get("email"));
        teacher.setPhone((String) data.get("phone"));
        teacher.setCni((String) data.get("cni"));

        // Set Teacher specific fields
        teacher.setDepartment((String) data.get("department"));
        teacher.setOfficeNumber(String.valueOf(data.get("office")));
        teacher.setTeacherIdentifier(String.valueOf(data.get("teacherId")));

        // Set required User fields
        teacher.setUsername(teacher.getEmail());
        teacher.setPassword(teacher.getTeacherIdentifier());
        teacher.setRole(UserRole.TEACHER);

        return teacherDAO.save(teacher);
    }
    private Coordinator createCoordinator(Map<String, Object> data) {
        Coordinator coordinator = new Coordinator();

        // Set basic User fields
        coordinator.setFirstName((String) data.get("firstName"));
        coordinator.setLastName((String) data.get("lastName"));
        coordinator.setEmail((String) data.get("email"));
        coordinator.setPhone((String) data.get("phone"));
        coordinator.setCni((String) data.get("cni"));

        // Set Teacher specific fields
        coordinator.setDepartment((String) data.get("department"));
        coordinator.setOfficeNumber(String.valueOf(data.get("office")));
        coordinator.setCoordinatorIdentifier(String.valueOf(data.get("teacherId")));

        // Set required User fields
        coordinator.setUsername(coordinator.getEmail());
        coordinator.setPassword(coordinator.getCoordinatorIdentifier());
        coordinator.setRole(UserRole.COORDINATOR);

        return coordinatorDAO.save(coordinator);
    }

    private Course createCourse(Map<String, Object> data, Map<String, Teacher> teacherMap) {
        Course course = new Course();
        course.setName((String) data.get("title"));
        course.setSemester(Semester.valueOf("S" + data.get("semester")));

        String teacherId = String.valueOf(data.get("teacherId"));
        Teacher teacher = teacherMap.get(teacherId);
        if (teacher != null) {
            course.setTeacher(teacher);
        }
        return courseDAO.save(course);
    }

    private Student createStudent(Map<String, Object> data) {
        Student student = new Student();

        // Set basic User fields
        student.setFirstName((String) data.get("firstName"));
        student.setLastName((String) data.get("lastName"));
        student.setEmail((String) data.get("email"));
        student.setPhone((String) data.get("phone"));
        student.setCni((String) data.get("cni"));

        // Set Student specific fields
        student.setApogee((String) data.get("apogee"));
        student.setCne((String) data.get("cne"));
        student.setStudentId("STUD-" + student.getApogee() + System.currentTimeMillis());  // Using apogee as student ID

        // Set required User fields
        student.setUsername(student.getApogee());
        student.setPassword(student.getCne());
        student.setRole(UserRole.STUDENT);

        return studentDAO.save(student);
    }
}