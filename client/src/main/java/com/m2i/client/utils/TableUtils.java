package com.m2i.client.utils;

import io.github.palexdev.materialfx.controls.MFXTableView;
import io.github.palexdev.materialfx.controls.MFXTableRow;

public class TableUtils {

    /**
     * Applies standard table properties and styling to a MaterialFX table
     * @param tableView The table to configure
     * @param <T> The type of data in the table
     */
    public static <T> void setupTableProperties(MFXTableView<T> tableView) {
        setupRowFactory(tableView);
        setupTableConfiguration(tableView);
        setupTableDimensions(tableView);
        applyTableStyling(tableView);
    }

    /**
     * Sets up the row factory with hover and selection effects
     */
    private static <T> void setupRowFactory(MFXTableView<T> tableView) {
        tableView.setTableRowFactory(resource -> {
            MFXTableRow<T> row = new MFXTableRow<>(tableView, resource);

            // Hover effect
            row.setOnMouseEntered(event -> {
                if (!row.isSelected()) {
                    row.setStyle("-fx-background-color: #f0f0f0; -fx-background-radius: 5;");
                }
            });

            row.setOnMouseExited(event -> {
                if (!row.isSelected()) {
                    row.setStyle("-fx-background-radius: 5;");
                }
            });

            // Selection styling
            row.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
                if (isSelected) {
                    row.setStyle("-fx-background-color: #e0e0e0; -fx-background-radius: 5;");
                } else {
                    row.setStyle("-fx-background-radius: 5;");
                }
            });

            return row;
        });
    }

    /**
     * Configures table functionality settings
     */
    private static <T> void setupTableConfiguration(MFXTableView<T> tableView) {
//        tableView.setSelectionMode(SelectionMode.MULTIPLE);
//        tableView.setFooterVisible(true);
//        tableView.setShowFooterLine(true);
//        tableView.setShowVerticalLines(true);
//        tableView.setShowHorizontalLines(true);
//        tableView.setHorizontalScrollBarEnabled(true);
//        tableView.setColumnResizable(true);
//        tableView.setColumnSortable(true);
    }

    /**
     * Sets standard table dimensions
     */
    private static <T> void setupTableDimensions(MFXTableView<T> tableView) {
        tableView.setMinHeight(400);
        tableView.setPrefHeight(600);
        tableView.setMaxHeight(800);
    }

    /**
     * Applies CSS styling to the table
     */
    private static <T> void applyTableStyling(MFXTableView<T> tableView) {
        tableView.getStyleClass().add("mfx-table-view");
    }

    /**
     * Additional customization methods can be added here for specific use cases
     */
}