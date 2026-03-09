package org.example.ui;

import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import org.example.network.ConnectionSetup;

import java.net.URISyntaxException;

public class AppController {
    private final StackPane root;
    public static ConnectionSetup client;                 // Создали клиента

    static {
        try {
            client = new ConnectionSetup();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public AppController() {
        Sidebar sidebar = new Sidebar();                               // Левая панель

        StackPane rightPane = new StackPane();                         // Создаем правую панель
        rightPane.setStyle("-fx-background-color: #ffffff;");

        Text welcomText = new Text("Select a chat to start messaging");
        welcomText.getStyleClass().add("placeholder-text");
        rightPane.getChildren().add(welcomText);

        PopUpWindowAddRoom windowAddRoom = new PopUpWindowAddRoom(sidebar, rightPane);
        WindowMain windowMain = new WindowMain(sidebar, rightPane, windowAddRoom);

        this.root = new StackPane();                                    // Основной Pane
        this.root.getChildren().addAll(
                windowMain.getSplitPane(),
                windowMain.getButtonOpenWindowAddRoom(),
                windowAddRoom.getVeil(),
                windowAddRoom.getPopUpWindowAddRoom());
    }
    public StackPane getRoot() { return  root; }

}
