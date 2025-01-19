// UsersViewController.java
package com.m2i.client.gui.controllers.coordinator;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import io.github.palexdev.materialfx.controls.MFXTextField;
import io.github.palexdev.materialfx.controls.MFXFilterComboBox;
import io.github.palexdev.materialfx.controls.MFXTableView;
import com.m2i.shared.dto.StudentResponseDTO;
import com.m2i.shared.dto.TeacherResponseDTO;

public class UsersViewController {
    @FXML private Label currentView;
    @FXML private MFXTextField searchField;
    @FXML private MFXFilterComboBox<String> filterField;
    @FXML private MFXTableView<StudentResponseDTO> studentsTable;
    @FXML private MFXTableView<TeacherResponseDTO> teacherTable;

    private final StudentTableManager studentManager;
    private final TeacherTableManager teacherManager;

    public UsersViewController() {
        this.studentManager = new StudentTableManager();
        this.teacherManager = new TeacherTableManager();
    }

    @FXML
    public void initialize() {
        initializeView();
    }

    private void initializeView() {
        switch (currentView.getText().toLowerCase()) {
            case "student" -> initializeStudentView();
            case "teacher" -> initializeTeacherView();
            default -> throw new IllegalStateException("Invalid view type: " + currentView.getText());
        }
    }

    private void initializeStudentView() {
        studentManager.setupTable(studentsTable);
        studentManager.setupFilters(searchField, filterField);
        studentManager.loadData();
    }

    private void initializeTeacherView() {
        teacherManager.setupTable(teacherTable);
        teacherManager.setupFilters(searchField, filterField);
        teacherManager.loadData();
    }
}