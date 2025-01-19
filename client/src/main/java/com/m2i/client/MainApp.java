package com.m2i.client;

import com.m2i.client.utils.SceneManager;
import com.m2i.client.utils.ServiceLocator;
import com.m2i.client.utils.SessionManager;
import fr.brouillard.oss.cssfx.CSSFX;
import io.github.palexdev.materialfx.theming.JavaFXThemes;
import io.github.palexdev.materialfx.theming.MaterialFXStylesheets;
import io.github.palexdev.materialfx.theming.UserAgentBuilder;
import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class MainApp extends Application {
    private static final int DEFAULT_WIDTH = 960;
    private static final int DEFAULT_HEIGHT = 640;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            CSSFX.start();

            UserAgentBuilder.builder()
                    .themes(JavaFXThemes.MODENA)
                    .themes(MaterialFXStylesheets.forAssemble(true))
                    .setDeploy(true)
                    .setResolveAssets(true)
                    .build()
                    .setGlobal();
            // Initialize managers
            initializeApplication(primaryStage);

            // Configure primary stage
            configureStage(primaryStage);

            // Load initial scene
            SceneManager.getInstance().loadLogin();

        } catch (Exception e) {
            e.printStackTrace();
            // You might want to show an error dialog here
            System.exit(1);
        }
    }

    private void initializeApplication(Stage primaryStage) {
        // Initialize core managers
        SceneManager.getInstance().initialize(primaryStage);
        ServiceLocator.getInstance(); // Ensure EJB services are initialized
        SessionManager.getInstance(); // Initialize session management
    }

    private void configureStage(Stage primaryStage) {
        // Set stage properties
        primaryStage.setTitle("Skooma-GW - Digital gateway for managing master data");

        // Set minimum size
        primaryStage.setMinWidth(DEFAULT_WIDTH);
        primaryStage.setMinHeight(DEFAULT_HEIGHT);

        // Load application icon
        try {
            String iconPath = "/assets/images/logo-short-light.png";
            var iconUrl = getClass().getResource(iconPath);
            if (iconUrl != null) {
                primaryStage.getIcons().add(new Image(iconUrl.toExternalForm()));
            } else {
                System.err.println("Warning: Application icon not found at " + iconPath);
            }
        } catch (Exception e) {
            System.err.println("Failed to load application icon: " + e.getMessage());
        }

        // Set close request handler
        primaryStage.setOnCloseRequest(event -> {
            // Perform cleanup
            cleanupApplication();
        });
    }

    private void cleanupApplication() {
        try {
            // Cleanup any resources
            SessionManager.getInstance().clearSession();
            //ServiceLocator.getInstance().cleanup();
        } catch (Exception e) {
            System.err.println("Error during application cleanup: " + e.getMessage());
        }
    }

    @Override
    public void stop() {
        // This method is called by the JavaFX runtime when the application closes
        cleanupApplication();
    }
}