package com.m2i.client.gui.controllers.coordinator;

import com.m2i.shared.dto.GradesReportResponseDTO;
import io.github.palexdev.materialfx.controls.MFXFilterComboBox;
import io.github.palexdev.materialfx.controls.MFXTableView;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.fxml.FXML;

public class ReportsViewController {
    @FXML
    private MFXTableView<GradesReportResponseDTO> gradesTable;

    @FXML
    private MFXTextField searchField;

    @FXML
    private MFXFilterComboBox<String> filterField;

    private GradesReportTableManager tableManager;

    @FXML
    public void initialize() {
        tableManager = new GradesReportTableManager();
        tableManager.setupTable(gradesTable);
        tableManager.setupFilters(searchField, filterField);
        tableManager.loadData();
    }
}