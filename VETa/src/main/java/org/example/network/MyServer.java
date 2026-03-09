package org.example.network;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.handshake.ServerHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;

public class MyServer extends WebSocketServer {
    private final ObjectMapper mapper = new ObjectMapper();   // Переводчик: ТЕКСТ <=> ОБЪЕКТ

    public MyServer(int i) {
        // Передаем порт и этот метод делает из него пару: IP-адрес + Порт
        // после чего срабатывает конструктор родителя
        super(new InetSocketAddress(i));
    }

    @Override
    public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) { // Срабатывает когда кто-то новый подключается к серверу
        // webSocket - это объект конкретного соединения (соединение с конкретным пользователем)
        // Печатаем в консоль сервера, что кто-то пришел
        System.out.println("Новое соединение: " + webSocket.getRemoteSocketAddress());
    }

    @Override
    // Срабатывает как только получает пакет от client.send(text); [text = s]
    public void onMessage(WebSocket webSocket, String s) {
        // webSocket - это объект конкретного соединения (соединение с конкретным пользователем)
        // s - сообщение, которое отправил пользователь
        System.out.println(s);
        broadcast(s); // Рассылаем всем клиентам
    }

    @Override
    public void onError(WebSocket webSocket, Exception e) {
        System.err.println("АХТУНГ! Ошибка на сервере: " + e.getMessage());
        e.printStackTrace();
    }

    @Override
    public void onStart() {         // Вызывается один раз при запуске сервера
        System.out.println("Сервер запущен");
    }

    public static void main(String[] args){
        MyServer server = new MyServer(8887);  // Создаем экземпляр нашего класса
        server.start();                          // Запускаем сервер

        // ЭТОТ БЛОК СРАБОТАЕТ ПРИ ВЫКЛЮЧЕНИИ ПРОГРАММЫ
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                System.out.println("Завершаю работу сервера...");
                server.stop(1000); // Даем 1 секунду на закрытие всех соединений
                System.out.println("Сервер остановлен, порт свободен.");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }));
    }
    @Override
    public void onClose(WebSocket webSocket, int i, String s, boolean b) {}
}
