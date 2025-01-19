package com.m2i.client.gui.controllers;

import com.m2i.client.utils.ServiceLocator;
import com.m2i.shared.auth.UserSession;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXComboBox;
import javafx.fxml.FXML;
import javafx.scene.control.MenuButton;
import com.m2i.client.utils.SessionManager;
import com.m2i.client.utils.SceneManager;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import org.kordamp.ikonli.javafx.FontIcon;

public class HeaderController {
    public MFXComboBox languageSelector;
    public MFXButton themeToggle;
    public FontIcon themeIcon;
    public Circle profileAvatar;
    public Text userNameText;
    private UserSession session;
    @FXML
    private void initialize() {
        session = SessionManager.getInstance().getCurrentSession();
        userNameText.setText(session.getUsername().substring(0, 2).toUpperCase());
        
        // Setup language selector
        languageSelector.getItems().addAll("English", "Français", "العربية");
        languageSelector.selectFirst();
        languageSelector.setOnAction(e -> onLanguageChange());

        // Setup theme toggle
        themeToggle.setOnAction(e -> toggleTheme());
    }

    private void onLanguageChange() {
        String selectedLanguage = languageSelector.getValue().toString();
        // Implement language change logic
    }

    private void toggleTheme() {
        boolean isDark = themeIcon.getIconLiteral().equals("far-moon");
        themeIcon.setIconLiteral(isDark ? "far-moon" : "far-sun");
        // Implement theme change logic
    }

    @FXML
    public void onViewProfile() {
        // Load profile view into main content area
    }

    @FXML
    public void onEditProfile() {
        // Load profile edit view into main content area
    }

    @FXML
    public void onLogout() {
        try {
            ServiceLocator.getInstance().getAuthService().logout(session.getSessionId());
            SessionManager.getInstance().clearSession();
            SceneManager.getInstance().loadLogin();
        } catch (Exception e) {
            // Handle logout error
        }
    }
}
