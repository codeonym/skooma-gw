package com.m2i.client.gui.controllers.coordinator;

import com.m2i.client.utils.ServiceLocator;
import com.m2i.client.utils.SessionManager;
import com.m2i.client.utils.TableUtils;
import com.m2i.client.utils.UIUtils;
import com.m2i.shared.dto.GradesReportResponseDTO;
import com.m2i.shared.entities.Status;
import com.m2i.shared.interfaces.CoordinatorService;
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
import org.kordamp.ikonli.javafx.FontIcon;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class GradesReportTableManager {
    private final CoordinatorService coordService;
    private ObservableList<GradesReportResponseDTO> reports;
    private FilteredList<GradesReportResponseDTO> filteredReports;
    private MFXTableView<GradesReportResponseDTO> tableView;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    public GradesReportTableManager() {
        this.coordService = ServiceLocator.getInstance().getCoordinatorService();
    }

    public void setupTable(MFXTableView<GradesReportResponseDTO> tableView) {
        this.tableView = tableView;
        setupColumns();
        TableUtils.setupTableProperties(tableView);
    }

    private void setupColumns() {
        List<MFXTableColumn<GradesReportResponseDTO>> columns = List.of(
                createColumn("Student ID", GradesReportResponseDTO::getStudentId, 120),
                createColumn("Semester", report -> report.getSemester().name(), 100),
                createStatusColumn(),
                createDateColumn("Request Date", GradesReportResponseDTO::getRequestDate),
                createDateColumn("Generation Date", GradesReportResponseDTO::getGenerationDate),
                createActionsColumn()
        );

        tableView.getTableColumns().addAll(columns);
    }

    private MFXTableColumn<GradesReportResponseDTO> createStatusColumn() {
        MFXTableColumn<GradesReportResponseDTO> column = new MFXTableColumn<>("Status", true,
                Comparator.comparing(report -> report.getStatus().name()));
        column.setMinWidth(120);
        column.setRowCellFactory(report -> {
            return new MFXTableRowCell<>(r -> r.getStatus().name()) {
                @Override
                public void update(GradesReportResponseDTO item) {
                    super.update(item);
                    if (item != null) {
                        Label label = new Label(item.getStatus().name());
                        String color = switch (item.getStatus()) {
                            case PENDING -> "#ff9800";
                            case APPROVED -> "#4caf50";
                            case REJECTED -> "#f44336";
                            case GENERATED -> "#2196f3";
                        };
                        label.setStyle(String.format("""
                            -fx-background-color: %s;
                            -fx-background-radius: 15;
                            -fx-padding: 5 10;
                            -fx-text-fill: white;
                            """, color));
                        setGraphic(label);
                        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                    }
                }
            };
        });
        return column;
    }

    private MFXTableColumn<GradesReportResponseDTO> createDateColumn(String header, Function<GradesReportResponseDTO, LocalDate> dateExtractor) {
        MFXTableColumn<GradesReportResponseDTO> column = new MFXTableColumn<>(header, true,
                Comparator.comparing(dateExtractor));
        column.setMinWidth(120);
        column.setRowCellFactory(report -> {
            MFXTableRowCell<GradesReportResponseDTO, String> cell = new MFXTableRowCell<>(
                    r -> dateExtractor.apply(r) != null ? dateExtractor.apply(r).format(DATE_FORMATTER) : "-"
            );
            cell.setAlignment(Pos.CENTER);
            return cell;
        });
        return column;
    }

    private MFXTableColumn<GradesReportResponseDTO> createColumn(String header, Function<GradesReportResponseDTO, String> valueExtractor, double width) {
        MFXTableColumn<GradesReportResponseDTO> column = new MFXTableColumn<>(header, true, Comparator.comparing(valueExtractor));
        column.setMinWidth(width);
        column.setRowCellFactory(report -> {
            MFXTableRowCell<GradesReportResponseDTO, String> cell = new MFXTableRowCell<>(valueExtractor);
            cell.setAlignment(Pos.CENTER_LEFT);
            return cell;
        });
        return column;
    }

    private MFXTableColumn<GradesReportResponseDTO> createActionsColumn() {
        MFXTableColumn<GradesReportResponseDTO> actionsColumn = new MFXTableColumn<>("Actions", true);
        actionsColumn.setMinWidth(200);
        actionsColumn.setRowCellFactory(report -> new MFXTableRowCell<GradesReportResponseDTO, String>(s -> "") {
            private final MFXButton approveBtn = new MFXButton("");
            private final MFXButton rejectBtn = new MFXButton("");
            private final MFXButton generateBtn = new MFXButton("");
            private final HBox actions = new HBox(10, approveBtn, rejectBtn, generateBtn);

            {
                setupActionButton(approveBtn, FontAwesomeSolid.CHECK, "#4caf50");
                setupActionButton(rejectBtn, FontAwesomeSolid.TIMES, "#f44336");
                setupActionButton(generateBtn, FontAwesomeSolid.FILE_PDF, "#2196f3");

                actions.setAlignment(Pos.CENTER);
                setGraphic(actions);
            }

            @Override
            public void update(GradesReportResponseDTO report) {
                super.update(report);
                if (report != null) {
                    // Only show appropriate buttons based on status
                    approveBtn.setVisible(report.getStatus() == Status.PENDING);
                    rejectBtn.setVisible(report.getStatus() == Status.PENDING);
                    generateBtn.setVisible(report.getStatus() == Status.APPROVED && !report.isGenerated());

                    approveBtn.setOnAction(e -> {
                        e.consume();
                        approveReport(report);
                    });
                    rejectBtn.setOnAction(e -> {
                        e.consume();
                        rejectReport(report);
                    });
                    generateBtn.setOnAction(e -> {
                        e.consume();
                        generateReport(report);
                    });
                }
            }
        });
        return actionsColumn;
    }

    private void setupActionButton(MFXButton button, FontAwesomeSolid icon, String color) {
        FontIcon fontIcon = new FontIcon(icon);
        fontIcon.setIconColor(Color.WHITE);
        fontIcon.setIconSize(16);

        button.setGraphic(fontIcon);
        button.setPrefSize(30, 30);
        button.setStyle(String.format("""
            -fx-background-color: %s;
            -fx-background-radius: 15;
            """, color));

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

    private void approveReport(GradesReportResponseDTO report) {
        if (UIUtils.showConfirmation("Approve Report",
                String.format("Are you sure you want to approve the grades report for student %s?", report.getStudentId()))) {
            try {
                coordService.approveGradeReport(
                        SessionManager.getInstance().getCurrentSession().getSessionId(),
                        report.getId()
                );
                loadData();
                UIUtils.showInfo("Report Approved", "The grades report has been approved.");
            } catch (Exception e) {
                UIUtils.showError("Error approving report", e.getMessage());
            }
        }
    }

    private void rejectReport(GradesReportResponseDTO report) {
        if (UIUtils.showConfirmation("Reject Report",
                String.format("Are you sure you want to reject the grades report for student %s?", report.getStudentId()))) {
            try {
                coordService.rejectGradeReport(
                        SessionManager.getInstance().getCurrentSession().getSessionId(),
                        report.getId()
                );
                loadData();
                UIUtils.showInfo("Report Rejected", "The grades report has been rejected.");
            } catch (Exception e) {
                UIUtils.showError("Error rejecting report", e.getMessage());
            }
        }
    }

    private void generateReport(GradesReportResponseDTO report) {
        try {
            coordService.generateGradeReport(
                    SessionManager.getInstance().getCurrentSession().getSessionId(),
                    report.getId()
            );
            loadData();
            UIUtils.showInfo("Report Generated", "The grades report has been generated successfully.");
        } catch (Exception e) {
            UIUtils.showError("Error generating report", e.getMessage());
        }
    }

    public void setupFilters(MFXTextField searchField, MFXFilterComboBox<String> filterField) {
        filterField.getItems().addAll("Student ID", "Status", "Semester");
        filterField.selectFirst();

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.isEmpty()) {
                filteredReports.setPredicate(null);
            } else {
                String filterType = filterField.getValue() != null ? filterField.getValue() : "Student ID";
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
        filteredReports.setPredicate(report -> {
            if (report == null) return false;
            return switch (filterType) {
                case "Student ID" -> report.getStudentId().toLowerCase().contains(searchText);
                case "Status" -> report.getStatus().name().toLowerCase().contains(searchText);
                case "Semester" -> report.getSemester().name().toLowerCase().contains(searchText);
                default -> true;
            };
        });
    }

    public void loadData() {
        CompletableFuture.supplyAsync(() -> {
            try {
                return coordService.getGradeReports(
                        SessionManager.getInstance().getCurrentSession().getSessionId()
                );
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).thenAcceptAsync(reportsList -> {
            reports = FXCollections.observableArrayList(reportsList);
            filteredReports = new FilteredList<>(reports);
            tableView.setItems(filteredReports);
        }, javafx.application.Platform::runLater).exceptionally(e -> {
            UIUtils.showError("Error loading reports", e.getMessage());
            return null;
        });
    }
}