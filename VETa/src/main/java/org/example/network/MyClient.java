package org.example.network;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.function.Consumer;

public class MyClient extends WebSocketClient {
    private final ObjectMapper mapper = new ObjectMapper();     // Переводчик: ТЕКСТ <=> ОБЪЕКТ
    private Consumer<String> onMessageCallback;                 // Как поле чтобы для каждого пользователя был один

    public MyClient(String serverUri) throws URISyntaxException {
        super(new URI(serverUri));                              // Принимает строку WebSocket URI и проверяет new URI()
    }

    public void setOnMessageReceived(Consumer<String> callback) {  // Принимаем функцию
        this.onMessageCallback = callback;
    }

    @Override
    // Срабатывает после того как Сервер сделает broadcast(s);
    public void onMessage(String s) {
        System.out.println(s);
        if (onMessageCallback != null) {
            onMessageCallback.accept(s); // Передает строку коду из UI
        }
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {  // Выполняется один раз после подключения
        System.out.println("Клиент подключился к серверу");
    }

    @Override
    public void onClose(int i, String s, boolean b) {

    }

    @Override
    public void onError(Exception e) {
        System.err.println("ОШИБКА КЛИЕНТА: " + e.getMessage());
        e.printStackTrace(); // Покажет в какой строчке всё сломалось

    }
}
