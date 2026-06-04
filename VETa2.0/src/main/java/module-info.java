module org.example {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.core;
    requires org.java_websocket;
    requires java.sql;
    requires org.slf4j;

    opens org.example to javafx.fxml;
    opens org.example.ui to javafx.fxml;
    opens org.example.common to com.fasterxml.jackson.databind;

    exports org.example;
    exports org.example.common;
    exports org.example.client;
}
