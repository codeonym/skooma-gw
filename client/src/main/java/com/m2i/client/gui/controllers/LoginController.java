package com.m2i.client.gui.controllers;

import com.m2i.client.utils.SceneManager;
import com.m2i.client.utils.ServiceLocator;
import com.m2i.client.utils.SessionManager;
import com.m2i.shared.auth.UserCredentials;
import com.m2i.shared.auth.UserSession;
import com.m2i.shared.interfaces.AuthenticationService;
import io.github.palexdev.materialfx.controls.MFXPasswordField;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import io.github.palexdev.materialfx.controls.MFXTextField;
public class LoginController {
    @FXML private MFXTextField usernameField;
    @FXML private MFXPasswordField passwordField;
    @FXML private Label errorLabel;

    private AuthenticationService authService;

    @FXML
    public void initialize() {
        authService = ServiceLocator.getInstance().getAuthService();
        errorLabel.setVisible(false);
    }

    @FXML
    public void onLogin() {
        try {
            UserCredentials credentials = new UserCredentials();
            credentials.setUsername(usernameField.getText());
            credentials.setPassword(passwordField.getText());

            UserSession session = authService.login(credentials);
            SessionManager.getInstance().setCurrentSession(session);

            SceneManager.getInstance().loadDashboard();
        } catch (Exception e) {
            errorLabel.setText("Login failed: " + e.getMessage());
            errorLabel.setVisible(true);
        }
    }
}