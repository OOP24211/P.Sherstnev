package org.example.network;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;

public class Server extends WebSocketServer {

    public Server (int i) {
        // Передаем порт и этот метод делает из него пару: IP-адрес + Порт
        // после чего срабатывает конструктор родителя
        super(new InetSocketAddress(i));
    }

    @Override
    public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {
        // webSocket - это объект конкретного соединения (соединение с конкретным пользователем)
        // Печатаем в консоль сервера, что кто-то пришел
        System.out.println("Новое соединение: " + webSocket.getRemoteSocketAddress());
    }

    @Override
    public void onMessage(WebSocket webSocket, String message) {
        // webSocket - это объект конкретного соединения (соединение с конкретным пользователем)
        // message - сообщение, которое отправил пользователь
        System.out.println("Получено сообщение: " + message);

        // Проверяем, не попытка ли это авторизации
        // Формат: AUTH:login:password
        if (message.startsWith("AUTH:")) {
            String[] partsMessage = message.split(":");
            if (partsMessage.length == 3) {
                String login = partsMessage[1];
                String password = partsMessage[2];

                // Идем в наш старый добрый DatabaseManager
                int userId = org.example.database.DatabaseManager.authUserAndGetId(login, password);

                if (userId != -1) {
                    // Отправляем успех ВМЕСТЕ с ID
                    webSocket.send("AUTH_SUCCESS:" + userId);
                    System.out.println("Юзер " + login + " вошел под ID: " + userId);
                } else {
                    webSocket.send("AUTH_FAIL");
                }
            }
        }

        else if (message.startsWith("REG:")) {
            String[] partsMessage = message.split(":");
            if (partsMessage.length == 3) {
                // Вызываем метод регистрации из DatabaseManager
                int newUserId = org.example.database.DatabaseManager.registerUserAndGetId(partsMessage[1], partsMessage[2]);

                if (newUserId != -1) {
                    webSocket.send("REG_SUCCESS:" + newUserId);
                    System.out.println("Новый пользователь зарегистрирован! ID: " + newUserId);
                } else {
                    webSocket.send("REG_FAIL");
                }
            }
        }
        else if (message.startsWith("GET_CHATS:")) {
            int userId = Integer.parseInt(message.split(":")[1]);
            String chatsData = org.example.database.DatabaseManager.getUserChats(userId);

            // Отправляем список чатов обратно клиенту
            webSocket.send("CHATS_LIST:" + chatsData);
        }

        else if (message.startsWith("CREATE_CHAT:")) {
            String[] parts = message.split(":");
            if (parts.length == 3) {
                String chatName = parts[1];
                int creatorId = Integer.parseInt(parts[2]);

                //  Создаем чат и добавляем создателя в chat_members
                org.example.database.DatabaseManager.createChat(chatName, creatorId);

                //  Сразу после создания отправляем юзеру обновленный список ЕГО чатов
                String chatsData = org.example.database.DatabaseManager.getUserChats(creatorId);
                webSocket.send("CHATS_LIST:" + chatsData);

                System.out.println("Чат '" + chatName + "' создан юзером " + creatorId);
            }
        }
        else if (message.startsWith("MSG:")) {
            // MSG:ID_ЧАТА:ID_ОТПРАВИТЕЛЯ:ТЕКСТ
            String[] parts = message.split(":", 4); // Режем на 4 части
            if (parts.length == 4) {
                int chatId = Integer.parseInt(parts[1]);
                int senderId = Integer.parseInt(parts[2]);
                String content = parts[3];

                // Сохраняем в базу (метод sendMessage у тебя уже готов в DatabaseManager)
                org.example.database.DatabaseManager.sendMessage(chatId, senderId, content);

                // Рассылаем ВСЕМ подключенным пользователям
                // В будущем мы сделаем рассылку только участникам этого чата,
                // но для теста "broadcast" — самое то.
                broadcast(message);
            }
        }
        else if (message.startsWith("GET_HISTORY:")) {
            int chatId = Integer.parseInt(message.split(":")[1]);
            String history = org.example.database.DatabaseManager.getChatHistory(chatId);

            // Отправляем обратно только тому, кто запросил
            webSocket.send("CHAT_HISTORY:" + chatId + ":" + history);
        }
    }

    @Override
    public void onError(WebSocket webSocket, Exception e) {
        System.err.println("Ошибка на сервере: " + e.getMessage());
        e.printStackTrace();
    }

    @Override
    public void onStart() {
        // Вызывается один раз при запуске сервера
        System.out.println("Сервер запущен");
    }

    @Override
    public void onClose(WebSocket webSocket, int i, String s, boolean b) {}

    public static void main (String[] args) {
        Server server = new Server(8887);
        server.start();

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
}
