package com.m2i.client.gui.controllers.teacher;

import com.m2i.client.utils.ServiceLocator;
import com.m2i.client.utils.SessionManager;
import com.m2i.client.utils.UIUtils;
import com.m2i.shared.dto.*;
import com.m2i.shared.interfaces.TeacherService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class GradesManagementViewController implements Initializable {
    @FXML
    private VBox gradeManagementRoot;

    @FXML
    private Label headerLabel;

    @FXML
    private TabPane courseTabPane;

    private final TeacherService teacherService;
    private final Map<Long, List<StudentGradeRow>> courseGradesMap;
    private List<StudentResponseDTO> cachedStudents;
    private Map<String, Map<String, GradeResponseDTO>> cachedGrades; // studentApogee -> courseName -> grade
    private List<CourseResponseDTO> cachedCourses;

    public GradesManagementViewController() {
        this.teacherService = ServiceLocator.getInstance().getTeacherService();
        this.courseGradesMap = new HashMap<>();
        this.cachedGrades = new HashMap<>();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupStyles();
        loadInitialData();
    }

    private void setupStyles() {
        headerLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
    }

    private void loadInitialData() {
        String sessionId = SessionManager.getInstance().getCurrentSession().getSessionId();

        // First, load all courses
        CompletableFuture.supplyAsync(() -> {
            try {
                return teacherService.getAssignedCourses(sessionId);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).thenAccept(courses -> {
            cachedCourses = courses;
            // Then load all students
            CompletableFuture.supplyAsync(() -> {
                try {
                    return teacherService.getAssignedStudents(sessionId);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }).thenAccept(students -> {
                cachedStudents = students;

                // Finally load all grades for all students
                List<CompletableFuture<Void>> gradeFutures = new ArrayList<>();

                for (StudentResponseDTO student : students) {
                    CompletableFuture<Void> gradeFuture = CompletableFuture.runAsync(() -> {
                        try {
                            List<GradeResponseDTO> grades = teacherService.getGradesByStudent(sessionId, student.getApogee());
                            Map<String, GradeResponseDTO> studentGrades = new HashMap<>();
                            for (GradeResponseDTO grade : grades) {
                                studentGrades.put(grade.getCourse(), grade);
                            }
                            cachedGrades.put(student.getApogee(), studentGrades);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    });
                    gradeFutures.add(gradeFuture);
                }

                CompletableFuture.allOf(gradeFutures.toArray(new CompletableFuture[0]))
                        .thenRun(() -> Platform.runLater(this::createCourseTabs))
                        .exceptionally(e -> {
                            Platform.runLater(() ->
                                    UIUtils.showError("Error", "Failed to load grades: " + e.getMessage())
                            );
                            return null;
                        });
            }).exceptionally(e -> {
                Platform.runLater(() ->
                        UIUtils.showError("Error", "Failed to load students: " + e.getMessage())
                );
                return null;
            });
        }).exceptionally(e -> {
            Platform.runLater(() ->
                    UIUtils.showError("Error", "Failed to load courses: " + e.getMessage())
            );
            return null;
        });
    }

    private void createCourseTabs() {
        courseTabPane.getTabs().clear();
        for (CourseResponseDTO course : cachedCourses) {
            Tab tab = new Tab(course.getName());
            VBox content = createCourseContent(course);
            tab.setContent(content);
            courseTabPane.getTabs().add(tab);
        }
    }

    private VBox createCourseContent(CourseResponseDTO course) {
        VBox content = new VBox(20);
        content.setPadding(new Insets(20));

        HBox courseInfo = new HBox(20);
        courseInfo.setAlignment(Pos.CENTER_LEFT);

        Label semesterLabel = new Label("Semester: " + course.getSemester());
        semesterLabel.setStyle("-fx-background-color: #e3f2fd; -fx-padding: 5 10; -fx-background-radius: 15;");

        courseInfo.getChildren().add(semesterLabel);

        VBox studentsList = new VBox(10);
        createStudentRows(course, studentsList);

        ScrollPane scrollPane = new ScrollPane(studentsList);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");

        Button saveButton = new Button("Save All Grades");
        saveButton.setStyle("""
            -fx-background-color: #2196f3;
            -fx-text-fill: white;
            -fx-padding: 10 20;
            -fx-background-radius: 5;
        """);
        saveButton.setOnAction(e -> saveGrades(course));

        content.getChildren().addAll(courseInfo, scrollPane, saveButton);
        return content;
    }

    private void createStudentRows(CourseResponseDTO course, VBox container) {
        List<StudentGradeRow> rows = new ArrayList<>();

        for (StudentResponseDTO student : cachedStudents) {
            Double existingGrade = Optional.ofNullable(cachedGrades.get(student.getApogee()))
                    .map(grades -> grades.get(course.getName()))
                    .map(GradeResponseDTO::getGrade)
                    .orElse(null);

            StudentGradeRow row = new StudentGradeRow(student, existingGrade);
            rows.add(row);
            container.getChildren().add(row);
        }

        courseGradesMap.put(course.getCourseId(), rows);
    }

    private void saveGrades(CourseResponseDTO course) {
        String sessionId = SessionManager.getInstance().getCurrentSession().getSessionId();
        List<StudentGradeRow> rows = courseGradesMap.get(course.getCourseId());

        if (rows == null || rows.isEmpty()) return;

        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (StudentGradeRow row : rows) {
            if (!row.isValid()) {
                UIUtils.showError("Validation Error",
                        "Invalid grade for student: " + row.getStudent().getFirstName() + " " + row.getStudent().getLastName());
                return;
            }

            if (row.hasChanged()) {
                GradeRequestDTO grade = new GradeRequestDTO();
                grade.setCourseId(course.getCourseId());
                grade.setStudentId(row.getStudent().getApogee());
                grade.setGrade(row.getGrade());
                grade.setSemester(course.getSemester());
                grade.setYear(LocalDateTime.now().getYear());

                futures.add(CompletableFuture.runAsync(() -> {
                    try {
                        teacherService.submitGrade(sessionId, grade);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }));
            }
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenRun(() -> Platform.runLater(() -> {
                    UIUtils.showInfo("Success", "All grades have been saved successfully!");
                    loadInitialData(); // Reload data after successful save
                }))
                .exceptionally(e -> {
                    Platform.runLater(() ->
                            UIUtils.showError("Error", "Failed to save grades: " + e.getMessage()));
                    return null;
                });
    }

    private static class StudentGradeRow extends HBox {
        private final StudentResponseDTO student;
        private final TextField gradeField;
        private final Double originalGrade;

        public StudentGradeRow(StudentResponseDTO student, Double existingGrade) {
            this.student = student;
            this.originalGrade = existingGrade;

            setSpacing(20);
            setAlignment(Pos.CENTER_LEFT);
            setPadding(new Insets(10));
            setStyle("-fx-background-color: #f5f5f5; -fx-background-radius: 5;");

            VBox studentInfo = new VBox(5);
            Label nameLabel = new Label(student.getFirstName() + " " + student.getLastName());
            nameLabel.setStyle("-fx-font-weight: bold;");
            Label apogeeLabel = new Label("Apogee: " + student.getApogee());
            apogeeLabel.setStyle("-fx-text-fill: #757575;");
            studentInfo.getChildren().addAll(nameLabel, apogeeLabel);

            gradeField = new TextField();
            gradeField.setPromptText("Grade");
            gradeField.setPrefWidth(100);
            if (existingGrade != null) {
                gradeField.setText(String.format("%.2f", existingGrade));
            }

            gradeField.textProperty().addListener((obs, old, newValue) -> {
                if (!newValue.isEmpty()) {
                    try {
                        double grade = Double.parseDouble(newValue);
                        if (grade < 0 || grade > 20) {
                            gradeField.setStyle("-fx-border-color: red;");
                        } else {
                            gradeField.setStyle("");
                        }
                    } catch (NumberFormatException e) {
                        gradeField.setStyle("-fx-border-color: red;");
                    }
                }
            });

            getChildren().addAll(studentInfo, gradeField);
        }

        public StudentResponseDTO getStudent() {
            return student;
        }

        public double getGrade() {
            return Double.parseDouble(gradeField.getText());
        }

        public boolean isValid() {
            try {
                double grade = Double.parseDouble(gradeField.getText());
                return grade >= 0 && grade <= 20;
            } catch (NumberFormatException e) {
                return false;
            }
        }

        public boolean hasChanged() {
            if (gradeField.getText().isEmpty()) return false;
            try {
                double currentGrade = Double.parseDouble(gradeField.getText());
                return originalGrade == null || Math.abs(currentGrade - originalGrade) > 0.001;
            } catch (NumberFormatException e) {
                return false;
            }
        }
    }
}