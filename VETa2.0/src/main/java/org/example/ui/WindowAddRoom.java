package org.example.ui;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.util.function.Consumer;

public class WindowAddRoom extends CreatePopUpWindow{
    private final Button buttonAddRoom = new Button("Add");
    private final Button buttonCloseWindowAddRoom = new Button("Close");;
    private final TextField inputNameRoom = new TextField();
    private final Text textNameRoom = new Text("Name:");

    public WindowAddRoom(String nameHeader, int x, int y) {
        super(nameHeader, x, y);
        customizingElementsWindow();
        settingWindowAddRoom();

        buttonCloseWindowAddRoom.setOnAction(e -> hide());
    }

    public Button getButtonAddRoom() {return buttonAddRoom; }
    public Button getButtonCloseWindowAddRoom() {return buttonCloseWindowAddRoom; }
    public TextField getInputNameRoom() {return inputNameRoom; }
    public Text getTextNameRoom() {return textNameRoom; }

    private void customizingElementsWindow(){
        // Надпись перед вводом имени комнаты
        textNameRoom.getStyleClass().add("popup-label");
        // Место ввода имени комнаты
        inputNameRoom.setPromptText("Enter name");
        inputNameRoom.getStyleClass().add("func-textFieldStyle");
        // Кнопка закрытия окна
        buttonCloseWindowAddRoom.setPrefSize(120, 30);
        buttonCloseWindowAddRoom.getStyleClass().add("func-buttonStyle");
        // Кнопка создания комнаты
        buttonAddRoom.setPrefSize(100, 30);
        buttonAddRoom.getStyleClass().add("func-buttonStyle");
    }

    private void settingWindowAddRoom(){
        // Создаем "пружину", которая заберет всё свободное место сверху
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        // Две кнопки в ряд с зазором 15
        HBox buttonInWindowAddRoom = new HBox(15);
        buttonInWindowAddRoom.setAlignment(Pos.CENTER_RIGHT);
        buttonInWindowAddRoom.getChildren().addAll(this.buttonAddRoom, this.buttonCloseWindowAddRoom);
        // Текст и место ввода в ряд с зазором 15
        HBox textInWindowAddRoom = new HBox(15);
        textInWindowAddRoom.setAlignment(Pos.CENTER_LEFT);
        textInWindowAddRoom.getChildren().addAll(this.textNameRoom, this.inputNameRoom);
        // Объединение всех компонентов
        getPopUpWindow().getChildren().addAll(getHeader() ,textInWindowAddRoom, spacer, buttonInWindowAddRoom);
    }

    public void setOnAddRoomAction(Consumer<String> action) {
        buttonAddRoom.setOnAction(e -> {
            String name = inputNameRoom.getText().trim();
            if (!name.isEmpty()) {
                action.accept(name); // Передаем имя созданной комнаты наружу
                hide();
                inputNameRoom.clear();
            }
        });
    }
}
