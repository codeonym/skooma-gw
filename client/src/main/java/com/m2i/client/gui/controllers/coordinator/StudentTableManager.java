package com.m2i.client.gui.controllers.coordinator;

import com.m2i.client.utils.ServiceLocator;
import com.m2i.client.utils.SessionManager;
import com.m2i.client.utils.TableUtils;
import com.m2i.client.utils.UIUtils;
import com.m2i.shared.dto.StudentResponseDTO;
import com.m2i.shared.interfaces.CoordinatorService;
import io.github.palexdev.materialfx.controls.*;
import io.github.palexdev.materialfx.controls.cell.MFXTableRowCell;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.layout.HBox;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class StudentTableManager {
    private final CoordinatorService coordService;
    private ObservableList<StudentResponseDTO> students;
    private FilteredList<StudentResponseDTO> filteredStudents;
    private MFXTableView<StudentResponseDTO> tableView;

    public StudentTableManager() {
        this.coordService = ServiceLocator.getInstance().getCoordinatorService();
    }

    public void setupTable(MFXTableView<StudentResponseDTO> tableView) {
        this.tableView = tableView;
        setupColumns();
        TableUtils.setupTableProperties(tableView);
    }

    private void setupColumns() {
        List<MFXTableColumn<StudentResponseDTO>> columns = List.of(
                createColumn("Apogee", StudentResponseDTO::getApogee),
                createColumn("First Name", StudentResponseDTO::getFirstName),
                createColumn("Last Name", StudentResponseDTO::getLastName),
                createColumn("CNE", StudentResponseDTO::getCne),
                createColumn("Email", StudentResponseDTO::getEmail),
                createColumn("CNI", StudentResponseDTO::getCni),
                createActionsColumn()
        );

        tableView.getTableColumns().addAll(columns);
    }

    private MFXTableColumn<StudentResponseDTO> createColumn(String header, Function<StudentResponseDTO, String> valueExtractor) {
        MFXTableColumn<StudentResponseDTO> column = new MFXTableColumn<>(header, Comparator.comparing(valueExtractor));
        column.setRowCellFactory(student -> new MFXTableRowCell<>(valueExtractor));
        return column;
    }

    private MFXTableColumn<StudentResponseDTO> createActionsColumn() {
        MFXTableColumn<StudentResponseDTO> actionsColumn = new MFXTableColumn<>("Actions", true);
        actionsColumn.setRowCellFactory(student -> new MFXTableRowCell<StudentResponseDTO, String>(s -> "") {
            private final MFXButton editBtn = new MFXButton("");
            private final MFXButton deleteBtn = new MFXButton("");
            private final HBox actions = new HBox(10, editBtn, deleteBtn);

            {
                // Initialize buttons once
                editBtn.setGraphic(new FontIcon(FontAwesomeSolid.EDIT));
                deleteBtn.setGraphic(new FontIcon(FontAwesomeSolid.TRASH));
                editBtn.getStyleClass().add("action-button");
                deleteBtn.getStyleClass().add("action-button");

                // Set the graphic only once
                setGraphic(actions);
            }

            @Override
            public void update(StudentResponseDTO student) {
                super.update(student);
                if (student != null) {
                    // Update button actions for the current student
                    editBtn.setOnAction(e -> {
                        System.out.println("Editing student: " + student.getApogee() +
                                " - " + student.getFirstName() + " " + student.getLastName());
                        editStudent(student);
                    });

                    deleteBtn.setOnAction(e -> deleteStudent(student));
                }
            }
        });
        return actionsColumn;
    }

    private HBox createActionButtons(StudentResponseDTO student) {
        MFXButton editBtn = createActionButton(FontAwesomeSolid.EDIT, () -> editStudent(student));
        System.out.println("Edit button created for student: " + student.getStudentId() + " - " + student.getFirstName() + " " + student.getLastName());
        MFXButton deleteBtn = createActionButton(FontAwesomeSolid.TRASH, () -> deleteStudent(student));

        return new HBox(10, editBtn, deleteBtn);
    }

    private MFXButton createActionButton(FontAwesomeSolid icon, Runnable action) {
        MFXButton button = new MFXButton("");
        button.setGraphic(new FontIcon(icon));
        button.getStyleClass().add("action-button");
        button.setOnAction(e -> action.run());
        return button;
    }

    public void setupFilters(MFXTextField searchField, MFXFilterComboBox<String> filterField) {
        filterField.getItems().addAll("ID", "Name", "CNE", "Email", "CNI", "Apogee");

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.isEmpty()) {
                filteredStudents.setPredicate(null);
            } else {
                applySearchFilter(newVal.toLowerCase(), filterField.getValue());
            }
        });
    }

    private void applySearchFilter(String searchText, String filterType) {
        filteredStudents.setPredicate(student -> {
            return switch (filterType) {
                case "ID" -> student.getStudentId().toLowerCase().contains(searchText);
                case "Name" -> student.getFirstName().toLowerCase().contains(searchText) ||
                        student.getLastName().toLowerCase().contains(searchText);
                case "CNE" -> student.getCne().toLowerCase().contains(searchText);
                case "Email" -> student.getEmail().toLowerCase().contains(searchText);
                case "CNI" -> student.getCni().toLowerCase().contains(searchText);
                case "Apogee" -> student.getApogee().toLowerCase().contains(searchText);
                default -> true;
            };
        });
    }

    public void loadData() {
        CompletableFuture.supplyAsync(() -> {
            try {
                return coordService.getAllStudents(
                        SessionManager.getInstance().getCurrentSession().getSessionId()
                );
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).thenAcceptAsync(studentList -> {
            students = FXCollections.observableArrayList(studentList);
            filteredStudents = new FilteredList<>(students);
            System.out.println("Students loaded: " + students.size());
            System.out.println("Filtered students loaded: " + filteredStudents.size());
            tableView.setItems(filteredStudents);
        }).exceptionally(e -> {
            UIUtils.showError("Error loading students", e.getMessage());
            return null;
        });
    }

    private void editStudent(StudentResponseDTO student) {
        // Implement edit functionality
        System.out.println("Edit student: " + student.getStudentId());
    }

    private void deleteStudent(StudentResponseDTO student) {
        if (UIUtils.showConfirmation("Delete Student",
                String.format("Are you sure you want to delete student: %s %s?",
                        student.getFirstName(), student.getLastName()))) {
            try {
                coordService.removeStudent(
                        SessionManager.getInstance().getCurrentSession().getSessionId(),
                        student.getApogee()
                );
                students.remove(student);
            } catch (Exception e) {
                UIUtils.showError("Error deleting student", e.getMessage());
            }
        }
    }
}
