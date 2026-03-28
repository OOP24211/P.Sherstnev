package org.example;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.example.network.Client;
import org.example.ui.*;

import java.net.URI;

public class Main extends Application {
    private Client client;

    public void start(Stage stage) throws Exception {
        client = new Client(new URI("ws://localhost:8887"));
        client.connect();

        ChatPanel chatPanel = new ChatPanel();
        Sidebar sidebar = new Sidebar();

        sidebar.setChatPanel(chatPanel);
        RoomStyle.setClient(client);
        Sidebar.setClient(client);
        client.setSidebar(sidebar);
        client.setChatPanel(chatPanel);

        WindowRegistration windowRegistration = new WindowRegistration("Login or register for an account.", 500, 400, client);
        client.setRegistrationWindow(windowRegistration);

        WindowAddRoom windowAddRoom = new WindowAddRoom("Create new room", 500, 400);


        windowAddRoom.setOnAddRoomAction(roomName -> {
            if (client != null && client.isOpen()) {
                // Отправляем запрос серверу: Тип:ИмяЧата:ID-Создателя
                // Мы используем client.getMyUserId(), который получили при логине
                client.send("CREATE_CHAT:" + roomName + ":" + client.getMyUserId());
                System.out.println("Запрос на создание чата отправлен: " + roomName);
            } else {
                System.out.println("Ошибка: Нет соединения с сервером!");
            }
        });


        // Создание главного окна
        WindowMain windowMain = new WindowMain(sidebar, chatPanel);
        windowMain.setOnAddRoomAction(() -> windowAddRoom.show());
        // Основной Pane
        StackPane root = new StackPane();
        root.getChildren().addAll(
                windowMain.getMainWindow(),
                windowMain.getButtonOpenWindowAddRoom(),
                windowAddRoom.getVeil(),
                windowAddRoom.getPopUpWindow(),
                windowRegistration.getVeil(),
                windowRegistration.getPopUpWindow());
        // Вывод
        Scene scene = new Scene(root, 800, 600);
        String css = getClass().getResource("/style.css").toExternalForm();
        scene.getStylesheets().add(css);
        stage.setTitle("VETa");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args){
        launch(args);
    }
}