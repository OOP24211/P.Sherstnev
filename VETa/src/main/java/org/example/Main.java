package org.example;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class Main extends Application {
    private static final String IDLE_BUTTON_STYLE = "-fx-background-color: #6200EE; -fx-background-radius: 25; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 20px;";
    private static final String HOVER_BUTTON_STYLE = "-fx-background-color: #7722FF; -fx-background-radius: 25; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 20px;";

    public void ButtonStyle(Button b, int x, int y){ // Стилизует кнопки
        b.setPrefSize(x, y);
        b.setStyle(
                "-fx-background-color: #6200EE; " +     // Основной цвет
                        "-fx-background-radius: 25; " +         // Скругление (половина высоты для эффекта капсулы)
                        "-fx-text-fill: white; " +              // Белый текст
                        "-fx-font-weight: bold; " +             // Жирный шрифт
                        "-fx-font-size: 20px; " +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 10, 0, 0, 5);" // Тень
        );
    }

    public void ButtonHighlighting(Button b){  // Затемняет кнопки когда наводят мышку
        b.setOnMouseEntered(e -> b.setStyle(HOVER_BUTTON_STYLE));
        b.setOnMouseExited(e -> b.setStyle(IDLE_BUTTON_STYLE));
    }


    public void textFieldStyle(TextField t, String prompt){
        t.setPromptText(prompt);
        t.setStyle(
                "-fx-background-color: #F4F4F4; " +    // Светло-серый фон
                "-fx-background-radius: 5; " +         // Небольшое скругление
                "-fx-border-color: #CCCCCC; " +        // Тонкая серая рамка
                "-fx-border-radius: 5; " +
                "-fx-border-width: 0 0 1 1; " +        // Линия только снизу (стиль Modern)
                "-fx-padding: 10; " +                  // Отступ текста от краев
                "-fx-font-size: 14px;");
    }

    public void OpenChat(String roomName, StackPane rightPane){
        rightPane.getChildren().clear(); // Убираем старый чат

        VBox chatBox = new VBox();
        HBox chatHeader = new HBox();
        chatHeader.setStyle("-fx-background-color: #f5f5f5; -fx-padding: 15; -fx-border-color: #cccccc; -fx-border-width: 0 0 1 0;");

        Text title = new Text(roomName);
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 18px;");
        chatHeader.getChildren().add(title);

        VBox messages = new VBox(10);
        messages.setAlignment(Pos.BOTTOM_LEFT); // Прижимаем всё содержимое к низу
        messages.setFillWidth(true);            // Чтобы сообщения могли растягиваться
        messages.setPadding(new Insets(10, 0, 20, 20));

        ScrollPane scrollPane = new ScrollPane();
        messages.minHeightProperty().bind(scrollPane.heightProperty());
        scrollPane.setContent(messages);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        TextField messageInput = new TextField();
        messageInput.setPromptText("Write message...");
        HBox.setHgrow(messageInput, Priority.ALWAYS);
        messageInput.setStyle("-fx-background-radius: 20; -fx-padding: 10;");

        Button sendButton = new Button("->");
        ButtonStyle(sendButton, 40, 40);

        HBox inputArea = new HBox(4);
        inputArea.setPadding(new Insets(15));
        inputArea.setAlignment(Pos.CENTER);
        inputArea.getChildren().addAll(messageInput, sendButton);

        sendButton.setOnAction( e -> {
            if (!messageInput.getText().isEmpty()){
                Label newMessage = new Label(messageInput.getText());
                newMessage.setWrapText(true); // Разрешает перенос слов на новую строку
                newMessage.setMaxWidth(400);  // Сообщение не будет шире 400 пикселей
                newMessage.setStyle(
                        "-fx-background-color: #BB86FC; " +
                                "-fx-text-fill: black; " +
                                "-fx-padding: 8 15 8 15; " +
                                "-fx-background-radius: 15;" +
                                "-fx-font-size: 14px;"
                );
                messages.getChildren().add(newMessage);
                messageInput.clear();  // Очищает строку ввода после отправки
            }

        });

        messageInput.setOnAction( e -> {
            if (!messageInput.getText().isEmpty()){
                Label newMessage = new Label(messageInput.getText());
                newMessage.setWrapText(true); // Разрешает перенос слов на новую строку
                newMessage.setMaxWidth(400);  // Сообщение не будет шире 400 пикселей
                newMessage.setStyle(
                        "-fx-background-color: #BB86FC; " +
                                "-fx-text-fill: black; " +
                                "-fx-padding: 8 15 8 15; " +
                                "-fx-background-radius: 15;" +
                                "-fx-font-size: 14px;"
                );
                messages.getChildren().add(newMessage);
                messageInput.clear();  // Очищает строку ввода после отправки
            }

        });

        chatBox.getChildren().addAll(chatHeader, scrollPane, inputArea);
        rightPane.getChildren().add(chatBox);
    }

    public void CreateNewRoom(String name, VBox container, StackPane rightPane){
        if (name.isEmpty()) return;

        Button roomButton = new Button(name);
        roomButton.setMaxWidth(Double.MAX_VALUE);

        String buttonStyle = "-fx-background-color: #E0E0E0; -fx-background-radius: 5; -fx-alignment: CENTER_LEFT; -fx-padding: 10; -fx-font-size: 14px;";
        String buttonStyle2 = "-fx-background-color: #c8c8c8; -fx-background-radius: 5; -fx-alignment: CENTER_LEFT; -fx-padding: 10; -fx-font-size: 14px;";
        roomButton.setStyle(buttonStyle);

        roomButton.setOnMouseEntered(e -> {     // Реагирует на наведение
            roomButton.setStyle(buttonStyle2);
        });
        roomButton.setOnMouseExited(e -> {      // Реагирует на наведение
            roomButton.setStyle(buttonStyle);
        });

        roomButton.setOnAction(e -> {           // Создает окно комнаты
            OpenChat(name, rightPane);
        });

        container.getChildren().add(roomButton);
    }


    @Override
    public void start(Stage stage) {
        // Создание кнопок
        Button buttonOpenWindowAddRoom = new Button("+");
        ButtonStyle(buttonOpenWindowAddRoom, 50, 50);
        ButtonHighlighting(buttonOpenWindowAddRoom);

        Button buttonCloseWindowAddRoom = new Button("Close");
        ButtonStyle(buttonCloseWindowAddRoom, 120, 30);
        ButtonHighlighting(buttonCloseWindowAddRoom);

        Button buttonAddRoom = new Button("Add");
        ButtonStyle(buttonAddRoom, 100, 30);
        ButtonHighlighting(buttonAddRoom);

        Text textNameRoom = new Text("Name:");
        textNameRoom.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

        TextField inputNameRoom = new TextField();
        textFieldStyle(inputNameRoom, "Enter name");

        // //////////////////////////////// PANE

        HBox header = new HBox(); // Верхняя плашка
        header.setAlignment(Pos.CENTER_LEFT); // Текст будет слева
        header.setStyle("-fx-background-color: #2C2C2C; -fx-background-radius: 10 10 10 10;"); // Фиолетовый верх
        header.setPadding(new Insets(10, 15, 10, 15));                        // Отступы внутри плашки
        Text headerText = new Text("Create new room");                                   // Текст для плашки
        headerText.setFill(Color.WHITE);
        headerText.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        header.getChildren().add(headerText);


        Region spacer = new Region();  // Создаем "пружину", которая заберет всё свободное место сверху
        VBox.setVgrow(spacer, Priority.ALWAYS);

        HBox buttonInWindowAddRoom = new HBox(15); // Кнопки в ряд с зазором 15
        buttonInWindowAddRoom.setAlignment(Pos.CENTER_RIGHT);
        buttonInWindowAddRoom.getChildren().addAll(buttonAddRoom, buttonCloseWindowAddRoom);

        HBox textInWindowAddRoom = new HBox(15);
        textInWindowAddRoom.setAlignment(Pos.CENTER_LEFT);
        textInWindowAddRoom.getChildren().addAll(textNameRoom, inputNameRoom);


        Region veil = new Region();  // Затемняет весь задний фон и не дает на него нажимать пока открыто окно
        veil.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5);");

        VBox popUpWindowAddRoom = new VBox(15);   // САМОЕ ГЛАВНОЕ ОКНО - СОЗДАНИЕ КОМНАТ
        popUpWindowAddRoom.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 20;");
        popUpWindowAddRoom.setMaxSize(500, 400);
        popUpWindowAddRoom.setAlignment(Pos.CENTER);

        popUpWindowAddRoom.getChildren().addAll(header ,textInWindowAddRoom, spacer, buttonInWindowAddRoom);

        popUpWindowAddRoom.setVisible(false);                    // Делает НЕ видимым
        veil.setVisible(false);                                  // Делает НЕ видимым


        // СОЗДАНИЕ КОНТЕЙНЕРА ДЛЯ КОМНАТ

        VBox roomsContainer = new VBox(10);
        roomsContainer.setPadding(new Insets(10));
        AnchorPane.setTopAnchor(roomsContainer, 5.0); // Отступ сверху, чтобы не перекрыть другие кнопки
        AnchorPane.setLeftAnchor(roomsContainer, 0.0);
        AnchorPane.setRightAnchor(roomsContainer, 0.0);


        // //////////////////////////////// Основная сцена



        StackPane root = new StackPane();  // ГЛАВНЫЙ PANE

        // Создаем левую панель и прикрепляем
        AnchorPane leftPane = new AnchorPane();
        leftPane.setStyle("-fx-background-color: #A5A5A5;");

        // -------СОЗДАЕМ СКРОЛЛ И КЛАДЕМ ЕГО В ЛЕВУЮ ПАНЕЛЬ-------
        ScrollPane leftScroll = new ScrollPane();
        leftScroll.setFitToWidth(true);
        leftScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        leftScroll.setContent(roomsContainer);
        AnchorPane.setTopAnchor(leftScroll, 5.0); // Оставляем место сверху под заголовок/поиск
        AnchorPane.setBottomAnchor(leftScroll, 0.0);
        AnchorPane.setLeftAnchor(leftScroll, 0.0);
        AnchorPane.setRightAnchor(leftScroll, 0.0);
        leftScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);  // Скрывает полосу прокрутки
        leftScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        leftPane.getChildren().add(leftScroll);

        // Создаем правую панель
        StackPane rightPane = new StackPane();
        rightPane.setStyle("-fx-background-color: #ffffff;");

        // Создаем SplitPane и добавляем в него панели
        SplitPane splitPane = new SplitPane();
        splitPane.getItems().addAll(leftPane, rightPane);
        splitPane.setDividerPositions(0.35); // Устанавливаем начальное положение разделителя
        leftPane.setMinWidth(200);// Задаёт минимальные размеры сторон,
        rightPane.setMinWidth(200); // чтобы одну из них нельзя было "схлопнуть" в ноль

        root.getChildren().addAll(splitPane, buttonOpenWindowAddRoom, veil, popUpWindowAddRoom);
        StackPane.setAlignment(buttonOpenWindowAddRoom, Pos.BOTTOM_LEFT); // ДВИГАЮ КНОПКУ +
        StackPane.setMargin(buttonOpenWindowAddRoom, new Insets(0, 0, 20, 20));




        // //////////////////////////////// Действие кнопок

        buttonOpenWindowAddRoom.setOnAction(e -> {    // Делает видимым
            popUpWindowAddRoom.setVisible(true);
            veil.setVisible(true);
        });
        buttonCloseWindowAddRoom.setOnAction(e -> {   // Делает НЕ видимым
            popUpWindowAddRoom.setVisible(false);
            veil.setVisible(false);
        });

        buttonAddRoom.setOnAction(e -> {
            CreateNewRoom(inputNameRoom.getText(), roomsContainer, rightPane);

            inputNameRoom.clear();
            popUpWindowAddRoom.setVisible(false);
            veil.setVisible(false);
        });


        Scene scene = new Scene(root, 800, 600);
        stage.setTitle("VETa");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
