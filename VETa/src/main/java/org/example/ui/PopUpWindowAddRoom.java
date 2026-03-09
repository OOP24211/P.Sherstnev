package org.example.ui;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.text.Text;

public class PopUpWindowAddRoom {
    private final Button buttonAddRoom  = new Button("Add");
    private final Button buttonCloseWindowAddRoom  = new Button("Close");
    private final TextField inputNameRoom = new TextField();
    private final VBox popUpWindowAddRoom = new VBox(15);
    private final Region veil = new Region();

    public PopUpWindowAddRoom(Sidebar sidebar, StackPane rightPane){
        Text textNameRoom = new Text("Name:");
        textNameRoom.getStyleClass().add("popup-label");

        StyleManager.textFieldStyle(this.inputNameRoom, "Enter name");
        StyleManager.buttonStyle(this.buttonCloseWindowAddRoom, 120, 30);
        StyleManager.buttonStyle(this.buttonAddRoom, 100, 30);

        this.veil.getStyleClass().add("popup-veil");
        this.popUpWindowAddRoom.getStyleClass().add("popup-window");

        this.popUpWindowAddRoom.setMaxSize(500, 400);
        this.popUpWindowAddRoom.setAlignment(Pos.CENTER);

        HBox header = createHeader();
        fillOutPopUpWindowAddRoom(textNameRoom, header);

        this.popUpWindowAddRoom.setVisible(false);                     // Делает НЕ видимым
        this.veil.setVisible(false);                                   // Делает НЕ видимым

        buttonSettings(sidebar, rightPane);
    }

    public Region getVeil() {return veil; }
    public VBox getPopUpWindowAddRoom() {return popUpWindowAddRoom; }

    public void show(){
        this.veil.setVisible(true);
        this.popUpWindowAddRoom.setVisible(true);
        this.inputNameRoom.requestFocus();
    }

    public void hide(){
        this.veil.setVisible(false);
        this.popUpWindowAddRoom.setVisible(false);
        this.inputNameRoom.clear();
    }

    private void buttonSettings(Sidebar sidebar, StackPane rightPane){
        this.buttonCloseWindowAddRoom.setOnAction(e -> hide());
        this.buttonAddRoom.setOnAction(e -> {
            String name = inputNameRoom.getText().trim();
            if (!name.isEmpty()) {
                RoomComponent room = new RoomComponent(name, rightPane);
                sidebar.getRoomsContainer().getChildren().add(room);

                hide();
            }
        });
    }

    private void fillOutPopUpWindowAddRoom(Text textNameRoom, HBox header){
        Region spacer = new Region();                                  // Создаем "пружину", которая заберет всё свободное место сверху
        VBox.setVgrow(spacer, Priority.ALWAYS);

        HBox buttonInWindowAddRoom = new HBox(15);                  // Кнопки в ряд с зазором 15
        buttonInWindowAddRoom.setAlignment(Pos.CENTER_RIGHT);
        buttonInWindowAddRoom.getChildren().addAll(this.buttonAddRoom, this.buttonCloseWindowAddRoom);

        HBox textInWindowAddRoom = new HBox(15);                    // Текст в ряд с зазором 15
        textInWindowAddRoom.setAlignment(Pos.CENTER_LEFT);
        textInWindowAddRoom.getChildren().addAll(textNameRoom, this.inputNameRoom);

        this.popUpWindowAddRoom.getChildren().addAll(header ,textInWindowAddRoom, spacer, buttonInWindowAddRoom);
    }

    private HBox createHeader() {
        HBox header = new HBox();                                       // Верхняя плашка
        header.setAlignment(Pos.CENTER_LEFT);                           // Текст будет слева
        header.getStyleClass().add("popup-header");                     // Фиолетовый верх

        Text headerText = new Text("Create new room" );              // Текст для плашки
        headerText.getStyleClass().add("popup-header-text");
        header.getChildren().add(headerText);
        return header;
    }
}
