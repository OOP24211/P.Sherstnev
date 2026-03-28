package org.example.network;

import javafx.scene.control.Button;
import org.example.ui.ChatPanel;
import org.example.ui.Sidebar;
import org.example.ui.WindowRegistration;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import java.net.URI;

public class Client extends WebSocketClient {
    private int myUserId = -1; // Здесь будем хранить наш ID
    private WindowRegistration registrationWindow;
    private Sidebar sidebar;
    private ChatPanel chatPanel;

    public Client(URI serverUri) {
        super(serverUri);
    }

    public int getMyUserId() { return myUserId; }
    public void setChatPanel(ChatPanel chatPanel) {
        this.chatPanel = chatPanel;
    }
    public void setSidebar(Sidebar sidebar) {
        this.sidebar = sidebar;
    }
    public void setRegistrationWindow(WindowRegistration window) {
        this.registrationWindow = window;
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        System.out.println("Подключено к серверу!");
    }

    @Override
    public void onMessage(String message) {
        System.out.println("Ответ от сервера: " + message);

        if (message.startsWith("AUTH_SUCCESS:") || message.startsWith("REG_SUCCESS:")) {
            // Режем строку по двоеточию
            String[] parts = message.split(":");
            this.myUserId = Integer.parseInt(parts[1]); // Достаем ID

            // СРАЗУ ЗАПРАШИВАЕМ ЧАТЫ
            this.send("GET_CHATS:" + myUserId);

            // Если авторизация успешна, нужно закрыть окно регистрации.
            // Но делать это можно только через Platform.runLater,
            // потому что JavaFX не любит, когда его окна трогают из сетевых потоков.
            javafx.application.Platform.runLater(() -> {
                if (registrationWindow != null) {
                    registrationWindow.hide();
                    System.out.println("Доступ разрешен. Мой ID: " + myUserId);
                }
            });

        } else if (message.equals("AUTH_FAIL") || message.equals("REG_FAIL")) {
            System.out.println("Ошибка: Попробуйте другой логин/пароль.");
        }

        if (message.startsWith("CHATS_LIST:")) {
            String data = message.replace("CHATS_LIST:", "");
            if (data.isEmpty()) return;

            String[] chats = data.split(",");
            javafx.application.Platform.runLater(() -> {
                // Очистим старые кнопки, если они были, чтобы не дублировать
                sidebar.getRoomsContainer().getChildren().clear();

                for (String chat : chats) {
                    String[] parts = chat.split("-"); // "1-Общий"
                    int id = Integer.parseInt(parts[0]);
                    String name = parts[1];

                    Button b = sidebar.addChatButton(name, id);
                    sidebar.getRoomsContainer().getChildren().add(b);
                }
            });
        }

        if (message.startsWith("MSG:")) {
            String[] parts = message.split(":", 4);
            int incomingChatId = Integer.parseInt(parts[1]);
            int senderId = Integer.parseInt(parts[2]);
            String text = parts[3];

            javafx.application.Platform.runLater(() -> {
                // Проверяем, открыт ли у нас сейчас какой-то чат
                // Проверяем, совпадает ли ID пришедшего сообщения с открытым чатом
                if (chatPanel.getCurrentRoom() != null && chatPanel.getCurrentRoom().getChatId() == incomingChatId) {

                    boolean isMe = (senderId == myUserId);
                    chatPanel.getCurrentRoom().appendMessage(text, isMe);
                }
            });
        }
        if (message.startsWith("CHAT_HISTORY:")) {
            String[] parts = message.split(":", 3);
            int historyChatId = Integer.parseInt(parts[1]);

            if (parts.length < 3 || parts[2].isEmpty()) return; // Истории нет

            String[] allMessages = parts[2].split("\\|\\|\\|"); // Делим на отдельные сообщения

            javafx.application.Platform.runLater(() -> {
                // Проверяем, что у нас всё еще открыт тот самый чат
                if (chatPanel.getCurrentRoom() != null && chatPanel.getCurrentRoom().getChatId() == historyChatId) {
                    for (String msgData : allMessages) {
                        String[] msgParts = msgData.split(":::", 2);
                        int senderId = Integer.parseInt(msgParts[0]);
                        String text = msgParts[1];

                        boolean isMe = (senderId == myUserId);
                        chatPanel.getCurrentRoom().appendMessage(text, isMe);
                    }
                }
            });
        }
    }

    @Override
    public void onClose(int i, String s, boolean b) {
        System.out.println("Соединение закрыто");
    }

    @Override
    public void onError(Exception e) {
        e.printStackTrace();
    }
}
