package org.example.ui;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.example.network.Client;

public class WindowRegistration extends CreatePopUpWindow{
    private final Button buttonRegisterAccount = new Button("Register");
    private final Button buttonEnterAccount = new Button("Login");
    private final TextField inputName = new TextField();
    private final PasswordField inputPassword = new PasswordField();

    public WindowRegistration(String nameHeader, int x, int y, Client client){
        super(nameHeader, x, y);
        show();

        Text textName     = new Text("Name:        ");
        Text textPassword = new Text("Password: ");
        customizingElementsWindow(textName, textPassword);
        settingWindowAddRoom(textName, textPassword);

        buttonEnterAccount.setOnAction(e -> {
            String name = inputName.getText();
            String password = inputPassword.getText();

            if (client != null && client.isOpen()) {
                // Отправляем серверу команду на проверку
                client.send("AUTH:" + name + ":" + password);
            } else {
                System.out.println("Ошибка: Нет соединения с сервером!");
            }
        });

        buttonRegisterAccount.setOnAction(e -> {
            String name = inputName.getText();
            String password = inputPassword.getText();

            if (name.isEmpty() || password.isEmpty()) {
                System.out.println("Ошибка: Поля не могут быть пустыми");
                return;
            }
            if (client != null && client.isOpen()) {
                // Отправляем серверу команду на регистрацию
                client.send("REG:" + name + ":" + password);
            } else {
            System.out.println("Ошибка: Нет соединения с сервером!");
            }
        });
    }

    private void customizingElementsWindow(Text textName, Text textPassword){
        // Надпись перед вводом имени комнаты
        textName.getStyleClass().add("popup-label");
        textPassword.getStyleClass().add("popup-label");
        // Место ввода имени комнаты
        inputName.setPromptText("Enter name");
        inputName.getStyleClass().add("func-textFieldStyle");
        inputPassword.setPromptText("Enter password");
        inputPassword.getStyleClass().add("func-textFieldStyle");
        // Кнопка закрытия окна
        buttonRegisterAccount.setPrefSize(120, 30);
        buttonRegisterAccount.getStyleClass().add("func-buttonStyle");
        // Кнопка создания комнаты
        buttonEnterAccount.setPrefSize(100, 30);
        buttonEnterAccount.getStyleClass().add("func-buttonStyle");
    }

    private void settingWindowAddRoom(Text textName, Text textPassword){
        // Создаем "пружину", которая заберет всё свободное место сверху
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        // Две кнопки в ряд с зазором 15
        HBox buttonInWindowAddRoom = new HBox(15);
        buttonInWindowAddRoom.setAlignment(Pos.CENTER_RIGHT);
        buttonInWindowAddRoom.getChildren().addAll(this.buttonRegisterAccount, this.buttonEnterAccount);
        // Текст и место ввода в ряд с зазором 15
        HBox textInWindowRegName = new HBox(15);
        textInWindowRegName.setAlignment(Pos.CENTER_LEFT);
        textInWindowRegName.getChildren().addAll(textName, this.inputName);
        // Текст и место ввода в ряд с зазором 15
        HBox textInWindowRegPassword = new HBox(15);
        textInWindowRegPassword.setAlignment(Pos.CENTER_LEFT);
        textInWindowRegPassword.getChildren().addAll(textPassword, this.inputPassword);
        // Объединение всех компонентов
        getPopUpWindow().getChildren().addAll(getHeader(), textInWindowRegName, textInWindowRegPassword, spacer, buttonInWindowAddRoom);
    }
}
