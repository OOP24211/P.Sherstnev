package org.example.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class ProfileController {
    @FXML private Label loginLabel;

    public void setUserData(String login) {
        loginLabel.setText(login);
    }

    @FXML
    private void onCloseClick() {
        ((Stage) loginLabel.getScene().getWindow()).close();
    }
}
