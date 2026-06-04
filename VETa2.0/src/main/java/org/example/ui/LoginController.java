package org.example.ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.client.ChatClient;
import org.example.common.Message;

import java.net.URI;

public class LoginController {
    @FXML private TextField     loginField;
    @FXML private PasswordField passwordField;
    @FXML private Label         statusLabel;

    private ChatClient client;

    @FXML
    public void initialize() {
        try {
            client = new ChatClient(new URI("ws://localhost:8887"), this::handleResponse);
            client.connect();
        } catch (Exception e) {
            statusLabel.setText("Сервер недоступен");
        }
    }

    @FXML private void onLoginClick()    { sendAuth(Message.Type.LOGIN);    }
    @FXML private void onRegisterClick() { sendAuth(Message.Type.REGISTER); }

    private void sendAuth(Message.Type type) {
        String login = loginField.getText().trim();
        String pass  = passwordField.getText().trim();

        if (login.isEmpty() || pass.isEmpty()) {
            statusLabel.setText("Заполните все поля");
            return;
        }

        Message msg = new Message();
        msg.type    = type;
        msg.sender  = login;
        msg.content = pass;
        client.sendMessage(msg);
    }

    private void handleResponse(Message msg) {
        if (msg.type == Message.Type.AUTH_SUCCESS) {
            if (msg.content != null && msg.content.contains("Регистрация")) {
                javafx.application.Platform.runLater(() -> {
                    statusLabel.setStyle("-fx-text-fill: #55ff55;");
                    statusLabel.setText("Аккаунт создан! Теперь войдите.");
                });
            } else {
                String login = loginField.getText().trim();
                client.setLogin(login);
                javafx.application.Platform.runLater(() -> {
                    try { switchToMain(); }
                    catch (Exception e) {
                        statusLabel.setText("Ошибка загрузки главного окна");
                    }
                });
            }
        } else if (msg.type == Message.Type.AUTH_ERROR) {
            javafx.application.Platform.runLater(() -> {
                statusLabel.setStyle("-fx-text-fill: #ff5555;");
                statusLabel.setText(msg.content);
            });
        }
    }

    private void switchToMain() throws Exception {
        FXMLLoader loader = new FXMLLoader(
            getClass().getResource("/org/example/ui/main_window.fxml"));
        Parent root = loader.load();

        MainController ctrl = loader.getController();
        ctrl.setClient(client);

        Stage stage = (Stage) loginField.getScene().getWindow();
        Scene scene = new Scene(root, 960, 620);
        String css = getClass().getResource("/org/example/ui/style.css") != null
            ? getClass().getResource("/org/example/ui/style.css").toExternalForm() : null;
        if (css != null) scene.getStylesheets().add(css);

        stage.setScene(scene);
        stage.setTitle("VETa Messenger");
        stage.centerOnScreen();
        stage.show();
    }
}
