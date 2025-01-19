package com.m2i.client.gui.controllers.student;

import com.m2i.client.utils.FileUtils;
import com.m2i.client.utils.ServiceLocator;
import com.m2i.client.utils.SessionManager;
import com.m2i.shared.dto.GradesReportResponseDTO;
import com.m2i.shared.entities.Status;
import com.m2i.shared.interfaces.StudentService;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXTableRow;
import io.github.palexdev.materialfx.controls.MFXTableView;
import io.github.palexdev.materialfx.controls.MFXTableColumn;
import io.github.palexdev.materialfx.controls.cell.MFXTableRowCell;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;

import java.io.File;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class GradesReportViewController implements Initializable {
    private static final String TITLE = "Academic Reports";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String ERROR_STYLE = """
            -fx-background-color: #FFF5F5;
            -fx-border-color: #FC8181;
            -fx-border-width: 1px;
            -fx-padding: 10px;
            """;
    private static final String EMPTY_STATE_STYLE = """
            -fx-alignment: center;
            -fx-spacing: 10;
            -fx-padding: 20;
            """;
    private static final Executor BACKGROUND_EXECUTOR = Executors.newCachedThreadPool();

    @FXML private VBox rootContainer;
    @FXML private StackPane contentPane;
    @FXML private MFXTableView<GradesReportResponseDTO> reportsTable;
    @FXML private Label statusLabel;
    @FXML private MFXButton refreshButton;
    private VBox emptyStateContainer;

    private final StudentService studentService;
    private final ObservableList<GradesReportResponseDTO> reports;
    private boolean isLoading;

    public GradesReportViewController() {
        this.studentService = ServiceLocator.getInstance().getStudentService();
        this.reports = FXCollections.observableArrayList();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupLayout();
        setupTable();
        setupEmptyState();
        setupActions();
        loadReports();
    }

    private void setupLayout() {
        // Make rootContainer take full window size
        rootContainer.setPrefWidth(Region.USE_COMPUTED_SIZE);
        rootContainer.setPrefHeight(Region.USE_COMPUTED_SIZE);
        rootContainer.setMaxWidth(Double.MAX_VALUE);
        rootContainer.setMaxHeight(Double.MAX_VALUE);
        rootContainer.setPadding(new Insets(10));
        VBox.setVgrow(rootContainer, Priority.ALWAYS);

        // Header
        Label titleLabel = createStyledLabel(TITLE, "-fx-font-size: 24px; -fx-font-weight: bold;");
        FontIcon reportIcon = new FontIcon(FontAwesomeSolid.GRADUATION_CAP);
        reportIcon.setIconSize(32);

        HBox headerBox = new HBox(15, reportIcon, titleLabel);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setPadding(new Insets(20));
        headerBox.getStyleClass().add("view-header");
        headerBox.setMaxWidth(Double.MAX_VALUE);

        // Status area with loading spinner
        HBox statusBox = new HBox(10);
        statusBox.setAlignment(Pos.CENTER_LEFT);
        statusLabel = createStyledLabel("", "-fx-font-size: 14px;");
        statusBox.getChildren().add(statusLabel);
        HBox.setHgrow(statusBox, Priority.ALWAYS);

        // Toolbar
        refreshButton = new MFXButton("Refresh");
        refreshButton.setGraphic(new FontIcon(FontAwesomeSolid.SYNC));
        refreshButton.getStyleClass().add("mfx-button-primary");

        MFXButton exportButton = new MFXButton("Export All");
        exportButton.setGraphic(new FontIcon(FontAwesomeSolid.FILE_EXPORT));
        exportButton.getStyleClass().add("mfx-button-secondary");
        exportButton.setOnAction(e -> exportAllReports());
        exportButton.disableProperty().bind(Bindings.isEmpty(reports));

        HBox toolbarBox = new HBox(10, refreshButton, exportButton, statusBox);
        toolbarBox.setAlignment(Pos.CENTER_RIGHT);
        toolbarBox.setPadding(new Insets(10, 20, 10, 20));
        toolbarBox.setMaxWidth(Double.MAX_VALUE);

        // Content area - Make table fill available space
        contentPane = new StackPane();
        contentPane.setMaxWidth(Double.MAX_VALUE);
        contentPane.setMaxHeight(Double.MAX_VALUE);
        VBox.setVgrow(contentPane, Priority.ALWAYS);

        // Setup table to grow
        reportsTable.setMaxWidth(Double.MAX_VALUE);
        reportsTable.setMaxHeight(Double.MAX_VALUE);
        reportsTable.setPrefWidth(Region.USE_COMPUTED_SIZE);
        reportsTable.setPrefHeight(Region.USE_COMPUTED_SIZE);

        if(emptyStateContainer != null) {
            // Make empty state container grow as well
            emptyStateContainer.setMaxWidth(Double.MAX_VALUE);
            emptyStateContainer.setMaxHeight(Double.MAX_VALUE);
        }

        // Assembly
        rootContainer.getChildren().setAll(headerBox, toolbarBox, contentPane);
        rootContainer.getStyleClass().add("grades-report-view");
    }

    private void setupTable() {
        reportsTable = new MFXTableView<>(reports);
        reportsTable.setFooterVisible(true);
        reportsTable.getStyleClass().add("reports-table");

        // Make table fill available space
        reportsTable.setMaxWidth(Double.MAX_VALUE);
        reportsTable.setMaxHeight(Double.MAX_VALUE);
        reportsTable.setPadding(new Insets(5));
        StackPane.setAlignment(reportsTable, Pos.CENTER);

        setupTableColumns();
        contentPane.getChildren().add(reportsTable);

        // Bind empty state visibility
        reports.addListener((Observable o) -> updateTableVisibility());
    }

    private void setupEmptyState() {
        FontIcon emptyIcon = new FontIcon(FontAwesomeSolid.FILE_ALT);
        emptyIcon.setIconSize(48);
        emptyIcon.setStyle("-fx-opacity: 0.5;");

        Label emptyLabel = createStyledLabel("No Reports Available", "-fx-font-size: 18px; -fx-font-weight: bold;");
        Label emptyDescription = createStyledLabel(
                "Your academic reports will appear here once they are generated.",
                "-fx-font-size: 14px; -fx-opacity: 0.8;"
        );

        emptyStateContainer = new VBox(20, emptyIcon, emptyLabel, emptyDescription);
        emptyStateContainer.setStyle(EMPTY_STATE_STYLE);
        emptyStateContainer.setAlignment(Pos.CENTER);
        emptyStateContainer.setMaxWidth(Double.MAX_VALUE);
        emptyStateContainer.setMaxHeight(Double.MAX_VALUE);
        emptyStateContainer.setVisible(false);

        contentPane.getChildren().add(emptyStateContainer);
        StackPane.setAlignment(emptyStateContainer, Pos.CENTER);
    }

    private void updateTableVisibility() {
        boolean isEmpty = reports.isEmpty();
        reportsTable.setVisible(!isEmpty);
        emptyStateContainer.setVisible(isEmpty);
    }

    private void setupTableColumns() {
        // Status Column
        MFXTableColumn<GradesReportResponseDTO> statusColumn = new MFXTableColumn<>("Status", true);
        statusColumn.setMinWidth(120);
        statusColumn.setRowCellFactory(reportDto -> {
            MFXTableRowCell<GradesReportResponseDTO, String> cell = new MFXTableRowCell<>(dto -> {
                Status status = dto.getStatus();
                return status != null ? status.name() : "";
            });

            // Initialize cell styling
            cell.setAlignment(Pos.CENTER);
            cell.getStyleClass().add("status-label");

            // Add listener to update styling when value changes
            cell.textProperty().addListener((obs, oldText, newText) -> {
                if (newText != null && !newText.isEmpty()) {
                    try {
                        Status status = Status.valueOf(newText);
                        cell.getStyleClass().removeAll(
                                "status-pending", "status-generated",
                                "status-rejected", "status-approved"
                        );
                        cell.getStyleClass().add(getStatusStyleClass(status));
                    } catch (IllegalArgumentException e) {
                        // Handle invalid status value
                        cell.getStyleClass().removeAll(
                                "status-pending", "status-generated",
                                "status-rejected", "status-approved"
                        );
                    }
                }
            });

            return cell;
        });

        // Semester Column
        MFXTableColumn<GradesReportResponseDTO> semesterColumn = new MFXTableColumn<>("Semester", true);
        semesterColumn.setMinWidth(150);
        semesterColumn.setRowCellFactory(reportDto -> {
            MFXTableRowCell<GradesReportResponseDTO, String> cell = new MFXTableRowCell<>(
                    dto -> dto.getSemester() != null ? dto.getSemester().name() : ""
            );
            cell.setAlignment(Pos.CENTER_LEFT);
            return cell;
        });

        // Request Date Column
        MFXTableColumn<GradesReportResponseDTO> requestDateColumn = new MFXTableColumn<>("Requested On", true);
        requestDateColumn.setMinWidth(120);
        requestDateColumn.setRowCellFactory(reportDto -> {
            MFXTableRowCell<GradesReportResponseDTO, String> cell = new MFXTableRowCell<>(
                    dto -> dto.getRequestDate() != null ? dto.getRequestDate().format(DATE_FORMATTER) : ""
            );
            cell.setAlignment(Pos.CENTER);
            return cell;
        });

        // Generation Date Column
        MFXTableColumn<GradesReportResponseDTO> generationDateColumn = new MFXTableColumn<>("Generated On", true);
        generationDateColumn.setMinWidth(120);
        generationDateColumn.setRowCellFactory(reportDto -> {
            MFXTableRowCell<GradesReportResponseDTO, String> cell = new MFXTableRowCell<>(
                    dto -> dto.getGenerationDate() != null ? dto.getGenerationDate().format(DATE_FORMATTER) : "Pending"
            );
            cell.setAlignment(Pos.CENTER);
            return cell;
        });

        // Actions Column
        MFXTableColumn<GradesReportResponseDTO> actionsColumn = new MFXTableColumn<>("Actions", true);
        actionsColumn.setMinWidth(150);
        actionsColumn.setRowCellFactory(reportDto -> {
            // Create a container for the actions
            HBox actionsContainer = createActionsPane(reportDto);

            // Create the cell with empty text
            MFXTableRowCell<GradesReportResponseDTO, String> cell = new MFXTableRowCell<>(dto -> "");
            cell.setGraphic(actionsContainer);
            cell.setAlignment(Pos.CENTER);

            // Make sure the actions container is visible
            actionsContainer.setVisible(true);
            actionsContainer.setManaged(true);

            return cell;
        });

        reportsTable.getTableColumns().setAll(
                statusColumn,
                semesterColumn,
                requestDateColumn,
                generationDateColumn,
                actionsColumn
        );

        reportsTable.getStyleClass().add("reports-table");
    }
    private HBox createActionsPane(GradesReportResponseDTO report) {
        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER);

        if (report.getStatus() == Status.GENERATED) {
            MFXButton viewBtn = createIconButton("View Report", FontAwesomeSolid.EYE);
            viewBtn.setOnAction(e -> viewReport(report));

            MFXButton downloadBtn = createIconButton("Download", FontAwesomeSolid.DOWNLOAD);
            downloadBtn.setOnAction(e -> downloadReport(report));

            actions.getChildren().addAll(viewBtn, downloadBtn);
        }

        // Make sure the container is visible
        actions.setVisible(true);
        actions.setManaged(true);

        return actions;
    }
    private MFXButton createIconButton(String tooltip, FontAwesomeSolid iconCode) {
        MFXButton button = new MFXButton("");
        button.setGraphic(new FontIcon(iconCode));
        button.getStyleClass().addAll("action-button", "mfx-button-primary");
        button.setTooltip(new Tooltip(tooltip));
        button.setMaxHeight(30);
        button.setMaxWidth(30);
        button.setMinHeight(30);
        button.setMinWidth(30);
        return button;
    }




    private void setupActions() {
        refreshButton.setOnAction(e -> loadReports());
    }

    private void loadReports() {
        if (isLoading) return;

        isLoading = true;
        updateLoadingState(true);
        showStatus("Loading reports...", false);

        String sessionId = SessionManager.getInstance().getCurrentSession().getSessionId();

        CompletableFuture.supplyAsync(
                () -> {
                    try {
                        return studentService.fetchGradesReport(sessionId);
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to fetch reports: " + e.getMessage(), e);
                    }
                },
                BACKGROUND_EXECUTOR
        ).thenAcceptAsync(fetchedReports -> {
            reports.clear();
            reports.addAll(fetchedReports);
            updateLoadingState(false);
            showStatus(fetchedReports.isEmpty() ? "" : "Reports updated successfully", false);
        }, Platform::runLater).exceptionally(e -> {
            updateLoadingState(false);
            showError("Failed to load reports", e.getMessage());
            return null;
        });
    }

    private void viewReport(GradesReportResponseDTO report) {
        try {
            File tempFile = File.createTempFile("academic_report_", ".pdf");
            tempFile.deleteOnExit();

            FileUtils.writeByteArrayToFile(tempFile, report.getReport());
            FileUtils.openFile(tempFile);
        } catch (Exception e) {
            showError("View Error", "Unable to open report: " + e.getMessage());
        }
    }

    private void downloadReport(GradesReportResponseDTO report) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Academic Report");
        fileChooser.setInitialFileName(generateFileName(report));
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
        );

        File file = fileChooser.showSaveDialog(rootContainer.getScene().getWindow());
        if (file != null) {
            CompletableFuture.runAsync(() -> {
                try {
                    FileUtils.writeByteArrayToFile(file, report.getReport());
                    Platform.runLater(() -> showStatus("Report downloaded successfully", false));
                } catch (Exception e) {
                    Platform.runLater(() -> showError("Download Error",
                            "Failed to save report: " + e.getMessage()));
                }
            }, BACKGROUND_EXECUTOR);
        }
    }

    private void exportAllReports() {
        // Implementation for bulk export functionality
        // This would allow users to export all their reports at once
    }

    // Helper methods
    private void updateLoadingState(boolean loading) {
        isLoading = loading;
        refreshButton.setDisable(loading);

        FontIcon icon = new FontIcon(loading ? FontAwesomeSolid.SPINNER : FontAwesomeSolid.SYNC);
        if (loading) {
            icon.getStyleClass().add("fa-spin");
        }
        refreshButton.setGraphic(icon);
    }

    private void showStatus(String message, boolean isError) {
        statusLabel.setText(message);
        statusLabel.setStyle(isError ? ERROR_STYLE : "");
    }

    private void showError(String header, String content) {
        showStatus(content, true);
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private String getStatusStyleClass(Status status) {
        return switch (status) {
            case PENDING -> "status-pending";
            case GENERATED -> "status-generated";
            case REJECTED -> "status-rejected";
            case APPROVED -> "status-approved";
        };
    }

    private String formatSemester(String semester) {
        return semester.replace("_", " ");
    }

    private Label createStyledLabel(String text, String style) {
        Label label = new Label(text);
        label.setStyle(style);
        return label;
    }

    private String generateFileName(GradesReportResponseDTO report) {
        return String.format("academic_report_%s_%s.pdf",
                report.getSemester().name().toLowerCase(),
                report.getRequestDate().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
        );
    }
}