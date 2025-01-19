// TeacherTableManager.java
package com.m2i.client.gui.controllers.coordinator;

import com.m2i.client.utils.ServiceLocator;
import com.m2i.client.utils.SessionManager;
import com.m2i.client.utils.TableUtils;
import com.m2i.client.utils.UIUtils;
import com.m2i.shared.dto.StudentResponseDTO;
import com.m2i.shared.dto.TeacherResponseDTO;
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

public class TeacherTableManager {
    private final CoordinatorService coordService;
    private ObservableList<TeacherResponseDTO> teachers;
    private FilteredList<TeacherResponseDTO> filteredTeachers;
    private MFXTableView<TeacherResponseDTO> tableView;

    public TeacherTableManager() {
        this.coordService = ServiceLocator.getInstance().getCoordinatorService();
    }

    public void setupTable(MFXTableView<TeacherResponseDTO> tableView) {
        this.tableView = tableView;
        setupColumns();
        TableUtils.setupTableProperties(tableView);
    }

    private void setupColumns() {
        List<MFXTableColumn<TeacherResponseDTO>> columns = List.of(
                createColumn("Teacher ID", TeacherResponseDTO::getTeacherIdentifier),
                createColumn("First Name", TeacherResponseDTO::getFirstName),
                createColumn("Last Name", TeacherResponseDTO::getLastName),
                createColumn("Email", TeacherResponseDTO::getEmail),
                createColumn("Department", TeacherResponseDTO::getDepartment),
                createActionsColumn()
        );

        tableView.getTableColumns().addAll(columns);
    }

    private MFXTableColumn<TeacherResponseDTO> createColumn(String header, java.util.function.Function<TeacherResponseDTO, String> valueExtractor) {
        MFXTableColumn<TeacherResponseDTO> column = new MFXTableColumn<>(header, Comparator.comparing(valueExtractor));
        column.setRowCellFactory(teacher -> new MFXTableRowCell<>(valueExtractor));
        return column;
    }

    private MFXTableColumn<TeacherResponseDTO> createActionsColumn() {
        MFXTableColumn<TeacherResponseDTO> actionsColumn = new MFXTableColumn<>("Actions", true);
        actionsColumn.setRowCellFactory(teacher -> new MFXTableRowCell<TeacherResponseDTO, String>(t -> "") {
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
            public void update(TeacherResponseDTO teacher) {
                super.update(teacher);
                if (teacher != null) {
                    // Update button actions for the current student
                    editBtn.setOnAction(e -> {
                        System.out.println("Editing student: " + teacher.getTeacherIdentifier() +
                                " - " + teacher.getFirstName() + " " + teacher.getLastName());
                        editTeacher(teacher);
                    });

                    deleteBtn.setOnAction(e -> deleteTeacher(teacher));
                }
            }
        });
        return actionsColumn;
    }

    private HBox createActionButtons(TeacherResponseDTO teacher) {
        MFXButton editBtn = createActionButton(FontAwesomeSolid.EDIT, () -> editTeacher(teacher));
        MFXButton deleteBtn = createActionButton(FontAwesomeSolid.TRASH, () -> deleteTeacher(teacher));
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
        filterField.getItems().addAll("ID", "Name", "Department", "Email", "CNI");

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.isEmpty()) {
                filteredTeachers.setPredicate(null);
            } else {
                applySearchFilter(newVal.toLowerCase(), filterField.getValue());
            }
        });
    }

    private void applySearchFilter(String searchText, String filterType) {
        filteredTeachers.setPredicate(teacher -> {
            return switch (filterType) {
                case "ID" -> teacher.getTeacherIdentifier().toLowerCase().contains(searchText);
                case "Name" -> teacher.getFirstName().toLowerCase().contains(searchText) ||
                        teacher.getLastName().toLowerCase().contains(searchText);
                case "Department" -> teacher.getDepartment().toLowerCase().contains(searchText);
                case "Email" -> teacher.getEmail().toLowerCase().contains(searchText);
                case "CNI" -> teacher.getCni().toLowerCase().contains(searchText);
                default -> true;
            };
        });
    }

    public void loadData() {
        CompletableFuture.supplyAsync(() -> {
            try {
                return coordService.getAllTeachers(
                        SessionManager.getInstance().getCurrentSession().getSessionId()
                );
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).thenAcceptAsync(teacherList -> {
            teachers = FXCollections.observableArrayList(teacherList);
            filteredTeachers = new FilteredList<>(teachers);
            tableView.setItems(filteredTeachers);
        }).exceptionally(e -> {
            UIUtils.showError("Error loading teachers", e.getMessage());
            return null;
        });
    }

    private void editTeacher(TeacherResponseDTO teacher) {
        // Implement edit functionality
        System.out.println("Edit teacher: " + teacher.getTeacherIdentifier());
    }

    private void deleteTeacher(TeacherResponseDTO teacher) {
        if (UIUtils.showConfirmation("Delete Teacher",
                String.format("Are you sure you want to delete teacher: %s %s?",
                        teacher.getFirstName(), teacher.getLastName()))) {
            try {
                coordService.removeTeacher(
                        SessionManager.getInstance().getCurrentSession().getSessionId(),
                        teacher.getTeacherIdentifier()
                );
                teachers.remove(teacher);
            } catch (Exception e) {
                UIUtils.showError("Error deleting teacher", e.getMessage());
            }
        }
    }
}