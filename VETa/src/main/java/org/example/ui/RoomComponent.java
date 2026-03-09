package org.example.ui;

import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;

public class RoomComponent extends Button {
    private static RoomComponent activeRoom = null;

    public RoomComponent(String name,  StackPane rightPane){
        super(name);

        this.setMaxWidth(Double.MAX_VALUE);
        this.getStyleClass().add("room-item");

        this.setOnAction(e -> {           // Создает окно комнаты
            if (activeRoom != null){
                activeRoom.getStyleClass().remove("active-room");
            }
            this.getStyleClass().add("active-room");
            activeRoom = this;

            ChatLogic.OpenChat(name, rightPane);
        });
    }
}
