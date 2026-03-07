module org.example {
    requires javafx.controls;
    requires javafx.fxml;
    requires jdk.xml.dom;

    opens org.example to javafx.fxml;
    exports org.example;
}