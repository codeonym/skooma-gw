package com.m2i.client.gui.controllers.student;

import com.m2i.client.utils.ServiceLocator;
import com.m2i.client.utils.SessionManager;
import com.m2i.shared.dto.GradeResponseDTO;
import com.m2i.shared.dto.GradesReportRequestDTO;
import com.m2i.shared.dto.GradesReportResponseDTO;
import com.m2i.shared.entities.Semester;
import com.m2i.shared.entities.Status;
import com.m2i.shared.interfaces.StudentService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.collections.*;
import javafx.geometry.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class GradesViewController implements Initializable {
    @FXML
    private VBox rootContainer;

    @FXML
    private HBox headerBox;

    @FXML
    private TabPane semesterTabPane;

    @FXML
    private HBox reportRequestBox;

    private final StudentService studentService;
    private final Map<String, ObservableList<GradeResponseDTO>> gradesBySemester;
    private final Map<String, TableView<GradeResponseDTO>> tablesBySemester;

    public GradesViewController() {
        this.studentService = ServiceLocator.getInstance().getStudentService();
        this.gradesBySemester = new HashMap<>();
        this.tablesBySemester = new HashMap<>();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupUI();
        loadGrades();
    }

    private void setupUI() {
        // Header setup
        Label titleLabel = new Label("My Grades");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        FontIcon gradeIcon = new FontIcon(FontAwesomeSolid.GRADUATION_CAP);
        gradeIcon.setIconSize(32);

        headerBox.getChildren().addAll(gradeIcon, titleLabel);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setSpacing(15);
        headerBox.setPadding(new Insets(20));

        // Setup report request controls
        setupReportRequestControls();

        // Initialize tab pane
        semesterTabPane = new TabPane();
        semesterTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        semesterTabPane.setStyle("-fx-background-color: transparent;");

        rootContainer.getChildren().add(2, semesterTabPane);
        VBox.setVgrow(semesterTabPane, Priority.ALWAYS);
    }

    private void setupReportRequestControls() {
        ComboBox<String> semesterCombo = new ComboBox<>();
        semesterCombo.setPromptText("Select Semester");
        semesterCombo.setItems(FXCollections.observableArrayList(Arrays.stream(Semester.values())
                .map(Enum::name)
                .collect(Collectors.toList())));

        Button requestReportBtn = new Button("Request Report");
        FontIcon pdfIcon = new FontIcon(FontAwesomeSolid.FILE_PDF);
        requestReportBtn.setGraphic(pdfIcon);
        requestReportBtn.setOnAction(e -> requestGradesReport(
                semesterCombo.getValue(),
                LocalDateTime.now().getYear()
        ));
        requestReportBtn.setStyle("-fx-background-color: #2196f3; -fx-text-fill: white;");

        reportRequestBox.getChildren().addAll(
                new Label("Request Grade Report:"),
                semesterCombo,
                requestReportBtn
        );
        reportRequestBox.setSpacing(10);
        reportRequestBox.setAlignment(Pos.CENTER_LEFT);
        reportRequestBox.setPadding(new Insets(10, 20, 10, 20));
    }

    private TableView<GradeResponseDTO> createGradeTable() {
        TableView<GradeResponseDTO> table = new TableView<>();

        // Course column
        TableColumn<GradeResponseDTO, String> courseColumn = new TableColumn<>("Course");
        courseColumn.setCellValueFactory(new PropertyValueFactory<>("course"));
        courseColumn.setMinWidth(200);

        // Grade column
        TableColumn<GradeResponseDTO, Double> gradeColumn = new TableColumn<>("Grade");
        gradeColumn.setCellValueFactory(new PropertyValueFactory<>("grade"));
        gradeColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double grade, boolean empty) {
                super.updateItem(grade, empty);
                if (empty || grade == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f/20", grade));
                }
            }
        });
        gradeColumn.setMinWidth(100);

        // Date column
        TableColumn<GradeResponseDTO, LocalDateTime> dateColumn = new TableColumn<>("Date");
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        dateColumn.setCellFactory(column -> new TableCell<>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            @Override
            protected void updateItem(LocalDateTime date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(formatter.format(date));
                }
            }
        });
        dateColumn.setMinWidth(150);

        // Year column
        TableColumn<GradeResponseDTO, Integer> yearColumn = new TableColumn<>("Year");
        yearColumn.setCellValueFactory(new PropertyValueFactory<>("year"));
        yearColumn.setMinWidth(100);

        table.getColumns().addAll(courseColumn, gradeColumn, dateColumn, yearColumn);
        return table;
    }

    private VBox createSemesterStats(List<GradeResponseDTO> grades) {
        VBox statsBox = new VBox(10);
        statsBox.setStyle("-fx-background-color: #f5f5f5; -fx-padding: 10; -fx-background-radius: 5;");

        double average = grades.stream()
                .mapToDouble(GradeResponseDTO::getGrade)
                .average()
                .orElse(0.0);

        Label averageLabel = new Label(String.format("Average: %.2f/20", average));
        averageLabel.setStyle("-fx-font-weight: bold;");

        ProgressBar progressBar = new ProgressBar(average / 20.0);
        progressBar.setStyle("-fx-accent: " + (average >= 10 ? "#4caf50" : "#f44336") + ";");
        progressBar.setMaxWidth(Double.MAX_VALUE);

        statsBox.getChildren().addAll(averageLabel, progressBar);
        return statsBox;
    }

    private void organizeGradesBySemester(List<GradeResponseDTO> allGrades) {
        // Clear existing data
        gradesBySemester.clear();
        semesterTabPane.getTabs().clear();

        // Group grades by semester, handling null semesters
        Map<String, List<GradeResponseDTO>> grouped = allGrades.stream()
                .filter(grade -> grade != null && grade.getSemester() != null)  // Filter out null grades and semesters
                .collect(Collectors.groupingBy(
                        grade -> grade.getSemester(),
                        () -> new TreeMap<>(String.CASE_INSENSITIVE_ORDER),  // Use TreeMap for sorted semesters
                        Collectors.toList()
                ));

        // Handle case where no valid grades were found
        if (grouped.isEmpty()) {
            Tab emptyTab = new Tab("No Grades");
            Label emptyLabel = new Label("No grades available");
            emptyLabel.setStyle("-fx-padding: 20;");
            emptyTab.setContent(emptyLabel);
            semesterTabPane.getTabs().add(emptyTab);
            return;
        }

        // Create tabs and tables for each semester
        grouped.forEach((semester, grades) -> {
            ObservableList<GradeResponseDTO> observableGrades = FXCollections.observableArrayList(grades);
            gradesBySemester.put(semester, observableGrades);

            TableView<GradeResponseDTO> table = createGradeTable();
            table.setItems(observableGrades);
            tablesBySemester.put(semester, table);

            // Create stats box
            VBox statsBox = createSemesterStats(grades);

            // Create tab content
            VBox tabContent = new VBox(10);
            tabContent.getChildren().addAll(statsBox, table);
            tabContent.setPadding(new Insets(10));

            Tab tab = new Tab(semester);
            tab.setContent(tabContent);

            semesterTabPane.getTabs().add(tab);
        });
    }
    private void loadGrades() {
        String sessionId = SessionManager.getInstance().getCurrentSession().getSessionId();

        // Show loading indicator
        ProgressIndicator loadingIndicator = new ProgressIndicator();
        loadingIndicator.setMaxSize(50, 50);
        rootContainer.getChildren().add(loadingIndicator);

        CompletableFuture.supplyAsync(() -> {
            try {
                return studentService.getMyGrades(sessionId);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).thenAcceptAsync(grades -> {
            Platform.runLater(() -> {
                rootContainer.getChildren().remove(loadingIndicator);
                organizeGradesBySemester(grades);
            });
        }, Platform::runLater).exceptionally(e -> {
            Platform.runLater(() -> {
                rootContainer.getChildren().remove(loadingIndicator);
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Failed to load grades");
                alert.setContentText(e.getMessage());
                alert.showAndWait();
            });
            return null;
        });
    }

    private void requestGradesReport(String semester, int year) {
        if (semester == null || semester.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Invalid Selection");
            alert.setContentText("Please select a semester");
            alert.showAndWait();
            return;
        }

        String sessionId = SessionManager.getInstance().getCurrentSession().getSessionId();

        // Show loading indicator
        ProgressIndicator loadingIndicator = new ProgressIndicator();
        loadingIndicator.setMaxSize(30, 30);
        reportRequestBox.getChildren().add(loadingIndicator);

        GradesReportRequestDTO request = new GradesReportRequestDTO(
                sessionId,
                year,
                Semester.valueOf(semester)
        );

        CompletableFuture.runAsync(() -> {
            try {
                studentService.requestGradesReport(sessionId, request);
                Platform.runLater(() -> {
                    reportRequestBox.getChildren().remove(loadingIndicator);
                    showSuccessNotification();
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    reportRequestBox.getChildren().remove(loadingIndicator);
                    showErrorNotification(e.getMessage());
                });
            }
        });
    }

    private void showSuccessNotification() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText("Grades report has been requested successfully!");
        alert.showAndWait();
    }

    private void showErrorNotification(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Failed to request grades report");
        alert.setContentText(message);
        alert.showAndWait();
    }
}