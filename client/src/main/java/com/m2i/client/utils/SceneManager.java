// SceneManager.java
package com.m2i.client.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
public class SceneManager {
    private static SceneManager instance;
    private Stage primaryStage;

    private SceneManager() {}

    public static SceneManager getInstance() {
        if (instance == null) {
            instance = new SceneManager();
        }
        return instance;
    }

    public void initialize(Stage stage) {
        this.primaryStage = stage;
    }

    public void loadLogin() {
        loadScene("scenes/login.fxml", "Login");
    }

    public void loadDashboard() {
        loadScene("scenes/dashboard.fxml", "Dashboard");
    }

    private void loadScene(String fxml, String title) {
        try {

            Parent root = FXMLLoader.load(getClass().getResource("/fxml/" + fxml));
            Scene scene = new Scene(root);
            primaryStage.setTitle(title);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            // Handle loading error
        }
    }
}
