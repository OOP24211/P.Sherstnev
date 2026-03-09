package org.example.ui;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class StyleManager {
    public static void buttonStyle(Button b, int x, int y){     // Стилизует кнопки
        b.setPrefSize(x, y);
        b.getStyleClass().add("func-buttonStyle");
    }

    public static void textFieldStyle(TextField t, String prompt){
        t.setPromptText(prompt);
        t.getStyleClass().add("func-textFieldStyle");
    }

    public static void newMessageStyle(Label newMessage){
        newMessage.setWrapText(true); // Разрешает перенос слов на новую строку
        newMessage.setMaxWidth(400);  // Сообщение не будет шире 400 пикселей
        newMessage.getStyleClass().add("func-messageBubble");
    }
}
