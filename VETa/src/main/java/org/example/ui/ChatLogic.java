package org.example.ui;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.example.network.MyClient;

import java.util.function.Consumer;

public class ChatLogic {

    public static void OpenChat(String roomName, StackPane rightPane){
        rightPane.getChildren().clear();                            // Убираем старый чат
        VBox chatBox = new VBox();                                  // Создаем пространство для чата

        HBox chatHeader = createHeader(roomName);                   // Заголовок комнаты
        final VBox messages = createMessagesContainer();            // "Хранилище" сообщений
        final ScrollPane scrollPane = setupScrollPane(messages);    // Скролл
        HBox inputArea = createInputArea(messageInput -> sendMessage(messageInput));

        chatBox.getChildren().addAll(chatHeader, scrollPane, inputArea);
        rightPane.getChildren().add(chatBox);

        // --- MyClient ---
        MyClient client = AppController.client.getClient();
        client.setOnMessageReceived(text -> {
            Platform.runLater(() -> {   //ГОВОРИТ ПОТОКУ ИНТЕРФЕЙСА "КАК ЗАКОНЧИШЬ СВОИ ДЕЛА, СДЕЛАЙ ЭТО"
                Label newMessage = new Label(text);
                StyleManager.newMessageStyle(newMessage);
                messages.getChildren().add(newMessage);
                Platform.runLater(() -> scrollPane.setVvalue(1.0));   // Прокрутка вниз
            });
        });
    }

    private static void sendMessage(TextField messageInput){
        String text = messageInput.getText().trim();
        if (!text.isEmpty()) {
            // --- MyClient ---
            MyClient client = AppController.client.getClient();
            client.send(text);                                       // Отправляем строку в сокет
            messageInput.clear();                                    // Очищает строку ввода после отправки
        }
    }

    private static HBox createHeader(String roomName){
        HBox chatHeader = new HBox();
        chatHeader.getStyleClass().add("chat-header");

        Text title = new Text(roomName);                        // Название чата(комнаты)
        title.getStyleClass().add("chat-title");
        chatHeader.getChildren().add(title);
        return chatHeader;
    }

    private static VBox createMessagesContainer(){
        VBox messages = new VBox(10);                        // "Хранилище" сообщений
        messages.setAlignment(Pos.BOTTOM_LEFT);                 // Прижимаем всё содержимое к низу
        messages.setFillWidth(false);                           // Чтобы сообщения могли растягиваться
        messages.setPadding(new Insets(10, 0, 20, 20));
        return messages;
    }

    private static ScrollPane setupScrollPane(VBox messages){
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.getStyleClass().add("chat-scroll");
        messages.minHeightProperty().bind(scrollPane.heightProperty());
        scrollPane.setContent(messages);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        return scrollPane;
    }

    private static HBox createInputArea(Consumer<TextField> onSendAction){
        TextField messageInput = new TextField();           // Поле ввода текста
        messageInput.setPromptText("Write message...");
        HBox.setHgrow(messageInput, Priority.ALWAYS);
        messageInput.getStyleClass().add("message-input");

        Button sendButton = new Button("->");            // Кнопка отправить
        StyleManager.buttonStyle(sendButton, 40, 40);

        sendButton.setOnAction( e -> onSendAction.accept(messageInput));
        messageInput.setOnAction( e -> onSendAction.accept(messageInput));

        HBox inputArea = new HBox(5);                   // Нижняя панель
        inputArea.getStyleClass().add("chat-input-area");
        inputArea.getChildren().addAll(messageInput, sendButton);

        return inputArea;
    }
}
