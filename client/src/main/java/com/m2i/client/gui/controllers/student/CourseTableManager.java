package com.m2i.client.gui.controllers.student;

import com.m2i.client.utils.ServiceLocator;
import com.m2i.client.utils.SessionManager;
import com.m2i.client.utils.TableUtils;
import com.m2i.client.utils.UIUtils;
import com.m2i.shared.dto.CourseResponseDTO;
import com.m2i.shared.interfaces.StudentService;
import io.github.palexdev.materialfx.controls.*;
import io.github.palexdev.materialfx.controls.cell.MFXTableRowCell;
import io.github.palexdev.mfxcore.controls.Label;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Pos;
import javafx.scene.control.ContentDisplay;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.materialdesign2.MaterialDesignF;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class CourseTableManager {
    private final StudentService studentService;
    private ObservableList<CourseResponseDTO> courses;
    private FilteredList<CourseResponseDTO> filteredCourses;
    private MFXTableView<CourseResponseDTO> tableView;

    public CourseTableManager() {
        this.studentService = ServiceLocator.getInstance().getStudentService();
    }

    public void setupTable(MFXTableView<CourseResponseDTO> tableView) {
        this.tableView = tableView;
        setupColumns();
        TableUtils.setupTableProperties(tableView);
    }

    private void setupColumns() {
        List<MFXTableColumn<CourseResponseDTO>> columns = List.of(
                createIconColumn(),
                createColumn("Course Name", CourseResponseDTO::getName, 200),
                createSemesterColumn(),
                createColumn("Teacher", CourseResponseDTO::getTeacher, 150),
                createActionsColumn()
        );

        tableView.getTableColumns().addAll(columns);
    }

    private MFXTableColumn<CourseResponseDTO> createIconColumn() {
        MFXTableColumn<CourseResponseDTO> column = new MFXTableColumn<>("", true);
        column.setMinWidth(50);
        column.setMaxWidth(50);
        column.setRowCellFactory(course -> {
            MFXTableRowCell<CourseResponseDTO, String> cell = new MFXTableRowCell<>(c -> "");
            FontIcon icon = new FontIcon(MaterialDesignF.FILE_DOCUMENT);
            icon.setIconColor(Color.valueOf("#1a237e"));
            icon.setIconSize(24);
            cell.setGraphic(icon);
            cell.setAlignment(Pos.CENTER);
            cell.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            return cell;
        });
        return column;
    }

    private MFXTableColumn<CourseResponseDTO> createColumn(String header, Function<CourseResponseDTO, String> valueExtractor, double width) {
        MFXTableColumn<CourseResponseDTO> column = new MFXTableColumn<>(header, true, Comparator.comparing(valueExtractor));
        column.setMinWidth(width);
        column.setRowCellFactory(course -> {
            MFXTableRowCell<CourseResponseDTO, String> cell = new MFXTableRowCell<>(valueExtractor);
            cell.setAlignment(Pos.CENTER_LEFT);
            return cell;
        });
        return column;
    }

    private MFXTableColumn<CourseResponseDTO> createSemesterColumn() {
        MFXTableColumn<CourseResponseDTO> column = new MFXTableColumn<>("Semester", true,
                Comparator.comparing(course -> course.getSemester().name()));
        column.setMinWidth(120);
        column.setRowCellFactory(course -> {
            return new MFXTableRowCell<>(c -> c.getSemester().name()) {
                @Override
                public void update(CourseResponseDTO item) {
                    super.update(item);
                    if (item != null) {
                        Label label = new Label(item.getSemester().name());
                        label.setStyle("""
                            -fx-background-color: #e3f2fd;
                            -fx-background-radius: 15;
                            -fx-padding: 5 10;
                            -fx-text-fill: #1976d2;
                        """);
                        setGraphic(label);
                        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                    }
                }
            };
        });
        return column;
    }

    private MFXTableColumn<CourseResponseDTO> createActionsColumn() {
        MFXTableColumn<CourseResponseDTO> actionsColumn = new MFXTableColumn<>("Actions", true);
        actionsColumn.setMinWidth(150);
        actionsColumn.setRowCellFactory(course -> new MFXTableRowCell<CourseResponseDTO, String>(s -> "") {
            private final MFXButton viewBtn = new MFXButton("");
            private final MFXButton downloadBtn = new MFXButton("");
            private final HBox actions = new HBox(10, viewBtn, downloadBtn);

            {
                // Initialize buttons
                setupActionButton(viewBtn, FontAwesomeSolid.EYE, "#eee");
                setupActionButton(downloadBtn, FontAwesomeSolid.DOWNLOAD, "#eee");

                actions.setAlignment(Pos.CENTER);
                setGraphic(actions);
            }

            @Override
            public void update(CourseResponseDTO course) {
                super.update(course);
                if (course != null) {
                    viewBtn.setOnAction(e -> {
                        e.consume();
                        viewCourse(course);
                    });
                    downloadBtn.setOnAction(e -> {
                        e.consume();
                        downloadCourse(course);
                    });
                }
            }
        });
        return actionsColumn;
    }

    private void setupActionButton(MFXButton button, FontAwesomeSolid icon, String color) {
        FontIcon fontIcon = new FontIcon(icon);
        fontIcon.setIconColor(Color.WHITE);
        fontIcon.setIconSize(12);

        button.setGraphic(fontIcon);
        button.setPrefSize(20, 20);
        button.setStyle(String.format("""
            -fx-background-color: %s;
            -fx-background-radius: 15;
            """, color));

        // Add hover effect using pseudo-classes instead of event handlers
        button.getStyleClass().add("action-button");
        button.setOnMouseEntered(e ->
                button.setStyle(String.format("""
                -fx-background-color: derive(%s, 20%%);
                -fx-background-radius: 15;
                """, color))
        );
        button.setOnMouseExited(e ->
                button.setStyle(String.format("""
                -fx-background-color: %s;
                -fx-background-radius: 15;
                """, color))
        );
    }

    public void setupFilters(MFXTextField searchField, MFXFilterComboBox<String> filterField) {
        filterField.getItems().addAll("Name", "Teacher", "Semester");
        filterField.selectFirst();

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.isEmpty()) {
                filteredCourses.setPredicate(null);
            } else {
                String filterType = filterField.getValue() != null ? filterField.getValue() : "Name";
                applySearchFilter(newVal.toLowerCase(), filterType);
            }
        });

        filterField.selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (searchField.getText() != null && !searchField.getText().isEmpty()) {
                applySearchFilter(searchField.getText().toLowerCase(), newVal);
            }
        });
    }

    private void applySearchFilter(String searchText, String filterType) {
        filteredCourses.setPredicate(course -> {
            if (course == null) return false;
            return switch (filterType) {
                case "Name" -> course.getName().toLowerCase().contains(searchText);
                case "Teacher" -> course.getTeacher().toLowerCase().contains(searchText);
                case "Semester" -> course.getSemester().name().toLowerCase().contains(searchText);
                default -> true;
            };
        });
    }

    public void loadData() {
        CompletableFuture.supplyAsync(() -> {
            try {
                return studentService.getAvailableCourses(SessionManager.getInstance().getCurrentSession().getSessionId());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).thenAcceptAsync(courseList -> {
            courses = FXCollections.observableArrayList(courseList);
            filteredCourses = new FilteredList<>(courses);
            tableView.setItems(filteredCourses);
        }, javafx.application.Platform::runLater).exceptionally(e -> {
            UIUtils.showError("Error loading courses", e.getMessage());
            return null;
        });
    }

    private void viewCourse(CourseResponseDTO course) {
        // Implement view functionality
        System.out.println("Viewing course: " + course.getName());
    }

    private void downloadCourse(CourseResponseDTO course) {
        // Implement download functionality
        System.out.println("Downloading course: " + course.getName());
    }
}