package org.example.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import org.example.common.Message;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.function.Consumer;

public class ChatClient extends WebSocketClient {
    private final ObjectMapper mapper = new ObjectMapper();
    private Consumer<Message> onMessageCallback;
    private String myLogin;

    public ChatClient(URI serverUri, Consumer<Message> onMessageCallback) {
        super(serverUri);
        this.onMessageCallback = onMessageCallback;
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        System.out.println("Подключено к серверу");
    }

    @Override
    public void onMessage(String rawMessage) {
        try {
            Message msg = mapper.readValue(rawMessage, Message.class);
            Platform.runLater(() -> onMessageCallback.accept(msg));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("Соединение закрыто: " + reason);
    }

    @Override
    public void onError(Exception ex) {
        ex.printStackTrace();
    }

    public void sendMessage(Message msg) {
        try {
            send(mapper.writeValueAsString(msg));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setCallback(Consumer<Message> callback) {
        this.onMessageCallback = callback;
    }

    public String getLogin() { return myLogin; }
    public void setLogin(String login) { this.myLogin = login; }

    /** Отправляет LOGOUT и закрывает сокет. */
    public void disconnect() {
        try {
            sendMessage(new Message(Message.Type.LOGOUT, null));
            close();
        } catch (Exception ignored) {}
    }
}
