package org.example.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.StackPane;

public class WindowMain {
    private final SplitPane mainWindow = new SplitPane();
    private final Button buttonOpenWindowAddRoom = new Button("+");

    public WindowMain(Sidebar sidebar, ChatPanel chatPanel){
        settingMainWindow(sidebar, chatPanel);
        customizingElements();
    }

    public SplitPane getMainWindow() { return mainWindow; }
    public Button getButtonOpenWindowAddRoom() {return buttonOpenWindowAddRoom; }

    private void settingMainWindow(Sidebar sidebar, ChatPanel chatPanel){
        mainWindow.getItems().addAll(sidebar.getLeftPane() , chatPanel.getRightPane());
        mainWindow.setDividerPositions(0.35);               // Устанавливаем начальное положение разделителя
        sidebar.getLeftPane().setMinWidth(200);             // Задаёт минимальные размеры сторон, чтобы
        chatPanel.getRightPane().setMinWidth(200);          // одну из них нельзя было "схлопнуть" в ноль
    }

    private void customizingElements() {
        buttonOpenWindowAddRoom.setPrefSize(50, 50);
        buttonOpenWindowAddRoom.getStyleClass().add("func-buttonStyle");
        StackPane.setAlignment(buttonOpenWindowAddRoom, Pos.BOTTOM_LEFT);
        StackPane.setMargin(buttonOpenWindowAddRoom, new Insets(0, 0, 15, 15));
    }
    // Позволяет Main назначить действие кнопке
    public void setOnAddRoomAction(Runnable action) {
        buttonOpenWindowAddRoom.setOnAction(e -> action.run());
    }
}
