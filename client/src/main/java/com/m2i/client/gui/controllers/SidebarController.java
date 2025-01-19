package com.m2i.client.gui.controllers;

import com.m2i.client.gui.navigation.MenuItem;
import com.m2i.client.utils.SessionManager;
import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;
import java.io.IOException;
import java.util.Arrays;

public class SidebarController {
    @FXML private VBox menuContainer;
    private Parent currentContent;
    private MFXButton currentButton;

    @FXML
    public void initialize() {
        loadMenuItems();
    }

    private void loadMenuItems() {
        menuContainer.getChildren().clear();
        String userRole = SessionManager.getInstance().getCurrentSession().getRole();

        Arrays.stream(MenuItem.values())
                .filter(item -> item.getAllowedRoles().contains(userRole))
                .forEach(this::createMenuButton);
    }

    private void createMenuButton(MenuItem menuItem) {
        MFXButton button = new MFXButton();  // Don't set text here
        button.getStyleClass().add("sidebar-button");
        button.setMaxWidth(Double.MAX_VALUE);

        // Create and set the icon
        FontIcon icon = new FontIcon(menuItem.getIcon());
        icon.getStyleClass().add("sidebar-icon");

        // Set the graphics and text separately
        button.setGraphic(icon);
        button.setText(menuItem.getTitle());

        button.setOnAction(e -> {
            if (currentButton != null) {
                currentButton.getStyleClass().remove("active");
            }
            button.getStyleClass().add("active");
            currentButton = button;
            System.out.println("Loading content: " + menuItem.getFxmlPath());
            loadContent(menuItem.getFxmlPath());
        });

        menuContainer.getChildren().add(button);
    }

    private void loadContent(String fxmlPath) {
        try {
            System.out.println("Begin Loading content: " + fxmlPath);
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            System.out.println("Loader: " + loader);
            Parent content = loader.load();

            // Get reference to dashboard's content area
            DashboardController dashboardController = getDashboardController();
            if (dashboardController != null) {
                System.out.println("Setting dashboard content: " + fxmlPath);
                dashboardController.setContent(content);
            }

        } catch (IOException e) {
            System.out.println("Error loading content: " + fxmlPath);
            e.printStackTrace();
            // Handle error appropriately
        }
    }

    private DashboardController getDashboardController() {
        // Implement getting reference to dashboard controller
        // This could be through a service locator pattern or other methods
        return DashboardController.getInstance();
    }
}