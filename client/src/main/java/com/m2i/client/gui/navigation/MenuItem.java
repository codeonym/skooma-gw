package com.m2i.client.gui.navigation;

import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;

import java.util.Set;

public enum MenuItem {
    // Student Menu Items
    MY_COURSES("My Courses", FontAwesomeSolid.BOOK, "/fxml/views/student/courses.fxml", Set.of("STUDENT")),
    MY_GRADES("My Grades", FontAwesomeSolid.CHART_BAR, "/fxml/views/student/grades.fxml", Set.of("STUDENT")),
    MY_ORDERS("My Orders", FontAwesomeSolid.PAPER_PLANE, "/fxml/views/student/orders.fxml", Set.of("STUDENT")),


    // Teacher Menu Items
    GRADES_MANAGEMENT("Grades Management", FontAwesomeSolid.TASKS, "/fxml/views/teacher/grades-management.fxml", Set.of("TEACHER")),
    COURSES_MANAGEMENT("My Courses", FontAwesomeSolid.BOOK, "/fxml/views/teacher/courses-management.fxml", Set.of("TEACHER")),


    // Coordinator Menu Items
    STUDENT_MANAGEMENT("Students Management", FontAwesomeSolid.USERS_COG, "/fxml/views/coordinator/students.fxml", Set.of("COORDINATOR")),
    TEACHER_MANAGEMENT("Teachers Management", FontAwesomeSolid.USERS_COG, "/fxml/views/coordinator/teachers.fxml", Set.of("COORDINATOR")),
    COURSE_MANAGEMENT("Course Management", FontAwesomeSolid.BOOK_OPEN, "/fxml/views/coordinator/course-management.fxml", Set.of("COORDINATOR")),
    REPORTS("Grade Reports Orders", FontAwesomeSolid.CHART_LINE, "/fxml/views/coordinator/reports.fxml", Set.of("COORDINATOR")),
    ADD_STUDENT("Add new student", FontAwesomeSolid.USER_EDIT, "/fxml/views/coordinator/add-student.fxml", Set.of("COORDINATOR")),
    ADD_TEACHER("Add new teacher", FontAwesomeSolid.USER_EDIT, "/fxml/views/coordinator/add-teacher.fxml", Set.of("COORDINATOR")),
    ADD_COURSE("Add new course", FontAwesomeSolid.BOOK, "/fxml/views/coordinator/add-course.fxml", Set.of("COORDINATOR")),
    ADD_SCHEDULE("Add new schedule", FontAwesomeSolid.CALENDAR, "/fxml/views/coordinator/add-schedule.fxml", Set.of("COORDINATOR")),

    // Common Menu Items
    SCHEDULE("Schedule", FontAwesomeSolid.CALENDAR_ALT, "/fxml/views/common/schedule.fxml", Set.of("STUDENT", "TEACHER")),
    ACCOUNT_SETTINGS("Account settings", FontAwesomeSolid.USER, "/fxml/views/common/account-settings.fxml", Set.of("STUDENT", "TEACHER"));


    private final String title;
    private final FontAwesomeSolid icon;
    private final String fxmlPath;
    private final Set<String> allowedRoles;

    MenuItem(String title, FontAwesomeSolid icon, String fxmlPath, Set<String> allowedRoles) {
        this.title = title;
        this.icon = icon;
        this.fxmlPath = fxmlPath;
        this.allowedRoles = allowedRoles;
    }

    public String getTitle() { return title; }
    public FontAwesomeSolid getIcon() { return icon; }
    public String getFxmlPath() { return fxmlPath; }
    public Set<String> getAllowedRoles() { return allowedRoles; }
}