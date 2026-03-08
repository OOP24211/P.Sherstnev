package org.example;

import javafx.geometry.Insets;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

public class Sidebar {
    private final AnchorPane leftPane;
    private final VBox roomsContainer;

    public Sidebar(){
        this.leftPane = new AnchorPane();
        this.leftPane.getStyleClass().add("sidebar-pane");

        this.roomsContainer = createRoomContainer();
        ScrollPane leftScroll = createScroll(roomsContainer);

        this.leftPane.getChildren().add(leftScroll);
    }

    public AnchorPane getLeftPane() { return leftPane; }
    public VBox getRoomsContainer() { return roomsContainer; }

    private static VBox createRoomContainer(){                          // Контейнер для комнат
        VBox roomsContainer = new VBox(10);
        roomsContainer.setPadding(new Insets(10));
        AnchorPane.setTopAnchor(roomsContainer, 5.0);           // Отступ сверху, чтобы не перекрыть другие кнопки
        AnchorPane.setLeftAnchor(roomsContainer, 0.0);
        AnchorPane.setRightAnchor(roomsContainer, 0.0);
        return roomsContainer;
    }

    private static ScrollPane createScroll(VBox roomsContainer){        // Создаем прокрут для чатов
        ScrollPane leftScroll = new ScrollPane();
        leftScroll.setFitToWidth(true);
        leftScroll.getStyleClass().add("sidebar-scroll");

        leftScroll.setContent(roomsContainer);
        AnchorPane.setTopAnchor(leftScroll, 5.0);               // Оставляем место сверху под заголовок/поиск
        AnchorPane.setBottomAnchor(leftScroll, 80.0);
        AnchorPane.setLeftAnchor(leftScroll, 0.0);
        AnchorPane.setRightAnchor(leftScroll, 0.0);
        leftScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);     // Скрывает полосу прокрутки вертикальную
        leftScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);     // Скрывает полосу прокрутки горизонтальную
        return leftScroll;
    }
}

