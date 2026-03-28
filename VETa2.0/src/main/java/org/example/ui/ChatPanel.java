package org.example.ui;

import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;

public class ChatPanel {
    private final StackPane rightPane;
    private RoomStyle currentRoom; // Храним текущий открытый чат

    public ChatPanel() {
        this.rightPane = new StackPane();
        settingRightPane();
    }

    public RoomStyle getCurrentRoom() { return currentRoom; }
    public StackPane getRightPane(){ return rightPane; }

    public void displayRoom(RoomStyle room) {
        this.currentRoom = room;
        rightPane.getChildren().clear();
        rightPane.getChildren().add(room.getChatBox());
    }
    private void settingRightPane(){
        rightPane.getStyleClass().add("right-pane");
        Text welcomText = new Text("Select a chat to start messaging");
        welcomText.getStyleClass().add("placeholder-text");
        rightPane.getChildren().add(welcomText);

    }
}
