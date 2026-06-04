package org.example.ui;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class CreateChatController {
    @FXML private TextField nameField;
    private MainController mainController;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    private void onCreateClick() {
        String name = nameField.getText().trim();
        if (!name.isEmpty()) {
            mainController.sendCreateGroupChat(name);
            onCancelClick();
        }
    }

    @FXML
    private void onCancelClick() {
        ((Stage) nameField.getScene().getWindow()).close();
    }
}
