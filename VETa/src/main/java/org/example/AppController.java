package org.example;

import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;

public class AppController {
    private final StackPane root;

    public AppController(){
        Sidebar sidebar = new Sidebar();    // Левая панель

        StackPane rightPane = new StackPane(); // Создаем правую панель
        rightPane.setStyle("-fx-background-color: #ffffff;");

        Text welcomText = new Text("Select a chat to start messaging");
        welcomText.getStyleClass().add("placeholder-text");
        rightPane.getChildren().add(welcomText);

        PopUpWindowAddRoom windowAddRoom = new PopUpWindowAddRoom(sidebar, rightPane);
        WindowMain windowMain = new WindowMain(sidebar, rightPane, windowAddRoom);

        this.root = new StackPane();   // Основной Pane
        this.root.getChildren().addAll(
                windowMain.getSplitPane(),
                windowMain.getButtonOpenWindowAddRoom(),
                windowAddRoom.getVeil(),
                windowAddRoom.getPopUpWindowAddRoom());
    }
    public StackPane grtRoot() { return  root; }
}
