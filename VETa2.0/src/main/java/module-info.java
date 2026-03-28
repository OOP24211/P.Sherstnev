module org.example {
    requires javafx.controls;
    requires javafx.fxml;
    requires jdk.xml.dom;

    // Подключаем Jackson и WebSocket
    requires com.fasterxml.jackson.databind;
    requires Java.WebSocket;
    requires com.fasterxml.jackson.core;
    requires java.sql;

    // Позволяем JavaFX загружать FXML и контроллеры
    opens org.example to javafx.fxml;
    opens org.example.ui to javafx.fxml;

    // Позволяем Jackson использовать рефлексию для чтения/записи полей в твоем Message
//    opens org.example.models to com.fasterxml.jackson.databind;

    // Экспортируем пакеты, чтобы другие модули могли их видеть
    exports org.example;
    exports org.example.ui;

}