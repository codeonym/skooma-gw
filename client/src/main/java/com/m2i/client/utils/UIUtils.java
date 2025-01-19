package com.m2i.client.utils;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import java.util.Optional;

public class UIUtils {

    /**
     * Shows an error dialog with the specified title and message.
     * Ensures execution on the JavaFX Application Thread.
     *
     * @param title The dialog title
     * @param message The error message
     */
    public static void showError(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    /**
     * Shows a confirmation dialog and returns true if the user clicks OK.
     * Ensures execution on the JavaFX Application Thread.
     *
     * @param title The dialog title
     * @param message The confirmation message
     * @return true if user confirms, false otherwise
     */
    public static boolean showConfirmation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(message);

        Optional<ButtonType> response = alert.showAndWait();
        return response.isPresent() && response.get() == ButtonType.OK;
    }

    /**
     * Shows an information dialog with the specified title and message.
     * Ensures execution on the JavaFX Application Thread.
     *
     * @param title The dialog title
     * @param message The information message
     */
    public static void showInfo(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    /**
     * Shows a warning dialog with the specified title and message.
     * Ensures execution on the JavaFX Application Thread.
     *
     * @param title The dialog title
     * @param message The warning message
     */
    public static void showWarning(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    /**
     * Executes the given runnable on the JavaFX Application Thread.
     * If already on FX thread, executes immediately, otherwise uses Platform.runLater
     *
     * @param action The runnable to execute
     */
    public static void runOnFXThread(Runnable action) {
        if (Platform.isFxApplicationThread()) {
            action.run();
        } else {
            Platform.runLater(action);
        }
    }
}