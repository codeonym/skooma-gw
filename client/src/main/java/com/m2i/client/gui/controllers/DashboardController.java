package com.m2i.client.gui.controllers;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
import javafx.animation.FadeTransition;
import javafx.util.Duration;

public class DashboardController {
    @FXML private StackPane contentArea;
    private Parent currentContent;

    private static DashboardController instance;

    @FXML
    public void initialize() {
        instance = this;
    }

    public static DashboardController getInstance() {
        return instance;
    }

    public void setContent(Parent content) {
        if (currentContent != null) {
            // Fade out current content
            FadeTransition fadeOut = new FadeTransition(Duration.millis(150), currentContent);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(e -> {
                contentArea.getChildren().remove(currentContent);
                showNewContent(content);
            });
            fadeOut.play();
            System.out.println("Fading out current content");
        } else {
            System.out.println("Showing new content");
            showNewContent(content);
        }
    }

    private void showNewContent(Parent content) {
        // Fade in new content
        content.setOpacity(0);
        contentArea.getChildren().add(content);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(150), content);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();

        currentContent = content;
        System.out.println("Content loaded");
    }
}