package org.example.ui;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import org.example.network.Client;


public class Sidebar {
    private final AnchorPane leftPane = new AnchorPane();;
    private final VBox roomsContainer = new VBox(10);;
    private final ScrollPane leftScroll = new ScrollPane();;
    private Button activeRoomButton = null;
    private ChatPanel chatPanel;
    private static Client client;

    public Sidebar() {
        settingRoomsContainer();

        leftScroll.setContent(roomsContainer);
        settingScroll();

        leftPane.getStyleClass().add("sidebar-pane");
        leftPane.getChildren().add(leftScroll);
    }

    public static void setClient(Client client) { Sidebar.client = client; }
    public VBox getRoomsContainer() { return roomsContainer; }
    public ScrollPane getLeftScroll() { return leftScroll; }
    public AnchorPane getLeftPane() { return leftPane; }
    public void setChatPanel(ChatPanel chatPanel) { this.chatPanel = chatPanel; }


    public Button addChatButton(String roomName, int chatId) {
        Button roonButton = new Button(roomName);
        roonButton.getStyleClass().add("room-item");
        roonButton.setMaxWidth(Double.MAX_VALUE);

        roonButton.setOnAction(event -> {
            if (activeRoomButton != null) {
                activeRoomButton.getStyleClass().remove("active-room");
            }
            roonButton.getStyleClass().add("active-room");
            activeRoomButton = roonButton;

            if (chatPanel != null) {
                RoomStyle roomStyle = new RoomStyle(roomName, chatId);

                roomStyle.setupDefaultSending(); // Активируем кнопки отправки
                chatPanel.displayRoom(roomStyle); // Говорим панели отобразить этот чат
            }
            if (chatPanel != null) {
                RoomStyle roomStyle = new RoomStyle(roomName, chatId);
                roomStyle.setupDefaultSending();
                chatPanel.displayRoom(roomStyle);

                // ЗАПРАШИВАЕМ ИСТОРИЮ У СЕРВЕРА
                client.send("GET_HISTORY:" + chatId);
            }
        });

        return roonButton; // ОБЯЗАТЕЛЬНО возвращаем кнопку
    }


    private void settingRoomsContainer() {
        roomsContainer.setPadding(new Insets(10));
        AnchorPane.setTopAnchor(roomsContainer, 5.0);           // Отступ сверху, чтобы не перекрыть другие кнопки
        AnchorPane.setLeftAnchor(roomsContainer, 0.0);
        AnchorPane.setRightAnchor(roomsContainer, 0.0);
    }

    private void settingScroll() {                                      // Создаем прокрут для чатов
        leftScroll.setFitToWidth(true);
        leftScroll.getStyleClass().add("sidebar-scroll");

        AnchorPane.setTopAnchor(leftScroll, 5.0);               // Оставляем место сверху под заголовок/поиск
        AnchorPane.setBottomAnchor(leftScroll, 80.0);
        AnchorPane.setLeftAnchor(leftScroll, 0.0);
        AnchorPane.setRightAnchor(leftScroll, 0.0);
        leftScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);     // Скрывает полосу прокрутки вертикальную
        leftScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    }
}