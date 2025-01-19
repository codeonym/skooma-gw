package com.m2i.client.gui.controllers.coordinator;

import io.github.palexdev.materialfx.controls.MFXTextField;
import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;

import java.net.URL;
import java.rmi.RemoteException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.function.Predicate;

public abstract class AddUserCommon implements Initializable {
    protected static final Map<String, Pattern> VALIDATION_PATTERNS = new HashMap<>();

    static {
        VALIDATION_PATTERNS.put("email", Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$"));
        VALIDATION_PATTERNS.put("cni", Pattern.compile("^[A-Z]{1,2}[0-9]{6}$"));
    }

    @FXML protected Label currentView;
    @FXML protected HBox block1, block2, block3;

    // Common fields
    @FXML protected MFXTextField firstName;
    @FXML protected MFXTextField lastName;
    @FXML protected MFXTextField email;
    @FXML protected MFXTextField cni;
    @FXML protected Label firstNameError;
    @FXML protected Label lastNameError;
    @FXML protected Label emailError;
    @FXML protected Label cniError;

    @FXML protected MFXButton submitButton;
    @FXML protected MFXButton clearButton;

    protected final Map<MFXTextField, ValidationConfig> validationConfigs = new HashMap<>();
    protected record ValidationConfig(Label errorLabel, String errorMessage, Predicate<String> validator) {}

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Platform.runLater(() -> {
            initializeValidationConfigs();
            setupErrorLabels();
            setupValidationListeners();
            hideAllErrorBlocks();
        });
    }

    protected void initializeValidationConfigs() {
        validationConfigs.clear();
        addCommonValidationConfigs();
        initializeSpecificValidationConfigs();
    }

    protected void addCommonValidationConfigs() {
        addValidationConfig(firstName, firstNameError, "First name is required",
                text -> text != null && !text.trim().isEmpty());
        addValidationConfig(lastName, lastNameError, "Last name is required",
                text -> text != null && !text.trim().isEmpty());
        addValidationConfig(email, emailError, "Invalid email format",
                text -> text != null && VALIDATION_PATTERNS.get("email").matcher(text).matches());
        addValidationConfig(cni, cniError, "CNI must be in format: XX123456",
                text -> text != null && VALIDATION_PATTERNS.get("cni").matcher(text.toUpperCase()).matches());
    }

    protected abstract void initializeSpecificValidationConfigs();
    protected abstract void updateSpecificErrorBlocks();
    protected abstract void register() throws RemoteException;

    protected void addValidationConfig(MFXTextField field, Label errorLabel, String errorMessage, Predicate<String> validator) {
        if (field != null && errorLabel != null) {
            validationConfigs.put(field, new ValidationConfig(errorLabel, errorMessage, validator));
        }
    }

    protected void setupErrorLabels() {
        validationConfigs.values().forEach(config -> {
            if (config.errorLabel() != null) {
                config.errorLabel().setTextFill(Color.RED);
                config.errorLabel().setVisible(false);
            }
        });
    }

    protected void setupValidationListeners() {
        validationConfigs.forEach((field, config) -> {
            if (field != null) {
                field.textProperty().addListener((observable, oldValue, newValue) -> {
                    validateField(field);
                    updateErrorBlockVisibility();
                });

                field.focusedProperty().addListener((observable, oldValue, newValue) -> {
                    if (!newValue) {
                        validateField(field);
                        updateErrorBlockVisibility();
                    }
                });
            }
        });
    }

    protected void validateField(MFXTextField field) {
        if (field == null) return;

        ValidationConfig config = validationConfigs.get(field);
        if (config != null) {
            String text = field.getText();
            boolean isValid = config.validator().test(text);
            if (!isValid) {
                showError(field, config.errorMessage());
            } else {
                hideError(field);
            }
        }
    }

    @FXML
    public void OnSubmit(ActionEvent event) {
        initializeValidationConfigs();

        if (!validateForm()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error",
                    "Please correct all errors before submitting.");
            return;
        }

        try {
            register();
            showAlert(Alert.AlertType.INFORMATION, "Success",
                    "User registered successfully!");
            reset();
        } catch (RemoteException e) {
            showAlert(Alert.AlertType.ERROR, "Registration Error",
                    "Failed to register user: " + e.getMessage());
            e.printStackTrace();
        }
    }

    protected boolean validateForm() {
        boolean isValid = true;
        for (MFXTextField field : validationConfigs.keySet()) {
            if (field != null) {
                validateField(field);
                ValidationConfig config = validationConfigs.get(field);
                if (config != null && config.errorLabel().isVisible()) {
                    isValid = false;
                }
            }
        }
        return isValid;
    }

    protected void showError(MFXTextField field, String message) {
        ValidationConfig config = validationConfigs.get(field);
        if (config != null) {
            config.errorLabel().setText(message);
            config.errorLabel().setVisible(true);
            field.setStyle("-fx-border-color: red;");
        }
    }

    protected void hideError(MFXTextField field) {
        ValidationConfig config = validationConfigs.get(field);
        if (config != null) {
            config.errorLabel().setVisible(false);
            field.setStyle("");
        }
    }

    protected void updateErrorBlockVisibility() {
        updateSpecificErrorBlocks();
    }

    @FXML
    public void OnClear(ActionEvent event) {
        confirmAndReset("Are you sure you want to clear all fields?");
    }

    protected void confirmAndReset(String message) {
        Alert confirmAlert = new Alert(
                Alert.AlertType.CONFIRMATION,
                message,
                ButtonType.YES,
                ButtonType.NO
        );
        confirmAlert.setTitle("Clear Form");
        confirmAlert.setHeaderText(null);

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                reset();
            }
        });
    }

    protected void reset() {
        validationConfigs.keySet().forEach(field -> {
            field.clear();
            field.setStyle("");
        });
        validationConfigs.values().forEach(config ->
                config.errorLabel().setVisible(false));
        hideAllErrorBlocks();
    }

    protected void hideAllErrorBlocks() {
        block1.setVisible(false);
        block1.setManaged(false);
        block2.setVisible(false);
        block2.setManaged(false);
        block3.setVisible(false);
        block3.setManaged(false);
    }

    protected void hideBlock(HBox block, Label... labels) {
        boolean anyVisible = Arrays.stream(labels).anyMatch(Node::isVisible);
        block.setVisible(anyVisible);
        block.setManaged(anyVisible);
    }

    protected void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}