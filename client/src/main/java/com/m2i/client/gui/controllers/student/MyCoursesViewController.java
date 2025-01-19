package com.m2i.client.gui.controllers.student;

import com.m2i.shared.dto.CourseResponseDTO;
import io.github.palexdev.materialfx.controls.MFXFilterComboBox;
import io.github.palexdev.materialfx.controls.MFXTableView;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.fxml.FXML;

public class MyCoursesViewController {
    @FXML
    private MFXTableView<CourseResponseDTO> courseTable;
    @FXML
    private MFXTextField searchField;
    @FXML
    private MFXFilterComboBox<String> filterField;

    private CourseTableManager tableManager;

    @FXML
    public void initialize() {
        tableManager = new CourseTableManager();
        tableManager.setupTable(courseTable);
        tableManager.setupFilters(searchField, filterField);
        tableManager.loadData();
    }
}
