package com.example.hotelmanagement;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainWindowController {

    private String userRole;

    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }

    @FXML
    protected void onLoginButtonClick() throws IOException {
        Stage stage = (Stage) Stage.getWindows().filtered(window -> window.isShowing()).get(0);
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("login-form.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setScene(scene);
    }

    @FXML
    protected void onRegisterButtonClick() throws IOException {
        Stage stage = (Stage) Stage.getWindows().filtered(window -> window.isShowing()).get(0);
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("register-form.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setScene(scene);
    }

    @FXML
    protected void onContinueButtonClick() throws IOException {
        Stage stage = (Stage) Stage.getWindows().filtered(window -> window.isShowing()).get(0);
        FXMLLoader fxmlLoader = new FXMLLoader();

        String userRole = LoginController.getUserRole();

        if ("Director".equals(userRole) || "Deputy Director".equals(userRole)) {
            fxmlLoader.setLocation(getClass().getResource("manager-main-form.fxml"));
        } else {
            fxmlLoader.setLocation(getClass().getResource("receptionist-main-form.fxml"));
        }

        Parent root = fxmlLoader.load();
        stage.setScene(new Scene(root));
    }


    @FXML
    protected void onExitButtonClick() {
        Stage stage = (Stage) Stage.getWindows().filtered(window -> window.isShowing()).get(0);
        stage.close();
    }
}
