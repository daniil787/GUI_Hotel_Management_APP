package com.example.hotelmanagement;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("main-window.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        primaryStage.setScene(scene);
        primaryStage.setTitle("Hotel Management System");
        primaryStage.show();
    }
    public static void main(String[] args) {
        launch();
    }
}
