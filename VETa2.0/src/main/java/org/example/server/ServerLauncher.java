package org.example.server;

public class ServerLauncher {
    public static void main(String[] args) {
        int port = 8887;
        try {
            ChatServer server = new ChatServer(port);
            server.start();
            System.out.println("Сервер мессенджера запущен на порту " + port);
        } catch (Exception e) {
            System.err.println("Не удалось запустить сервер: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
