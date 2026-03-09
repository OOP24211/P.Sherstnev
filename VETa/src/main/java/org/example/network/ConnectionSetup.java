package org.example.network;

import java.net.URISyntaxException;

public class ConnectionSetup {
    private final MyClient client;

    public ConnectionSetup() throws URISyntaxException {
        this.client = new MyClient("ws://localhost:8887");      // Создаем экземпляр нашего класса
        client.connect();                                               // Начинает подключаться и подключается
    }

    public MyClient getClient(){
        return this.client;
    }

}
