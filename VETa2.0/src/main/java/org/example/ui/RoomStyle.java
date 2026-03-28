package org.example.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.example.network.Client;


public class RoomStyle{
    private final VBox chatBox = new VBox();                        // Создаем пространство для чата
    private final VBox messagesBox = new VBox(7);                // "Хранилище" сообщений
    private final ScrollPane scrollMessage = new ScrollPane();      // Скролл
    private final Button sendButton = new Button("->");          // Кнопка отправки сообщений
    private final TextField messageInput = new TextField();         // Поле ввода текста
    private static Client client;                                   // Клиент
    private final int chatId;                                       // Добавляем это поле
    private final Region spacer = new Region();                     // Пружина чтобы сообщения начинались снизу

    public RoomStyle(String roomName, int chatId){
        this.chatId = chatId;

        HBox chatHeader = new HBox();                               // Заголовок комнаты
        settingChatHeader(roomName, chatHeader);

        settingMessagesBox();
        settingScrollMessage();

        HBox inputArea = new HBox(5);                            // Область ввода и отправки сообщений
        settingInputArea(inputArea);

        chatBox.getChildren().addAll(chatHeader, scrollMessage, inputArea);
    }

    public int getChatId() { return chatId; }
    public static void setClient(Client client) { RoomStyle.client = client; }
    public VBox getChatBox() {return chatBox; }

    public void setupDefaultSending() {
        sendButton.setOnAction(event -> sendMessage());
        messageInput.setOnAction(event -> sendMessage()); // Отправка по Enter
    }

    public void appendMessage(String text, boolean isMyMessage) {
        Label messageLabel = new Label(text);
        messageLabel.setWrapText(true);
        messageLabel.getStyleClass().add("chat-message-bubble");

        HBox messageContainer = new HBox(messageLabel);
        messageContainer.getStyleClass().add("message-container");

        // Растягиваем контейнер на всю ширину
        messageContainer.setMaxWidth(Double.MAX_VALUE);

        if (isMyMessage) {
            // Твои — СПРАВА
            messageContainer.setAlignment(Pos.CENTER_RIGHT);
        } else {
            // Собеседника — СЛЕВА
            messageContainer.setAlignment(Pos.CENTER_LEFT);
        }

        messagesBox.getChildren().add(messageContainer);
        javafx.application.Platform.runLater(() -> scrollMessage.setVvalue(1.0));
    }


    private void settingChatHeader(String roomName, HBox chatHeader){
        chatHeader.getStyleClass().add("chat-header");
        Text title = new Text(roomName);                            // Название комнаты
        title.getStyleClass().add("chat-title");
        chatHeader.getChildren().add(title);
    }

    private void settingMessagesBox(){
        messagesBox.setFillWidth(true);
        messagesBox.setPadding(new Insets(0, 0, 20, 20));
        messagesBox.setSpacing(10);
        VBox.setVgrow(spacer, Priority.ALWAYS);
        messagesBox.getChildren().add(spacer);
    }

    private void settingScrollMessage(){
        scrollMessage.getStyleClass().add("chat-scroll");
        messagesBox.minHeightProperty().bind(scrollMessage.heightProperty());
        scrollMessage.setContent(messagesBox);
        // ДОБАВЬ ВОТ ЭТО:
        scrollMessage.setFitToWidth(true);
        VBox.setVgrow(scrollMessage, Priority.ALWAYS);
        scrollMessage.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);  // Убирает ползунки сбоку и снизу
        scrollMessage.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    }

    private void settingInputArea(HBox inputArea){
        messageInput.setPromptText("Write message...");
        HBox.setHgrow(messageInput, Priority.ALWAYS);
        messageInput.getStyleClass().add("message-input");

        sendButton.setPrefSize(40, 40);
        sendButton.getStyleClass().add("func-buttonStyle");

        inputArea.getStyleClass().add("chat-input-area");
        inputArea.getChildren().addAll(messageInput, sendButton);
    }

    private void sendMessage(){
        String text = messageInput.getText().trim();
        if (!text.isEmpty()) {
            // Формируем сетевую команду
            String messagePayload = "MSG:" + chatId + ":" + client.getMyUserId() + ":" + text;
            client.send(messagePayload);

            messageInput.clear(); // Очищаем поле ввода
        }
    }
}
