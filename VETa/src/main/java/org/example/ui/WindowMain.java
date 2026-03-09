package org.example.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.StackPane;

public class WindowMain {

    private final SplitPane splitPane;
    private final Button buttonOpenWindowAddRoom = new Button("+");;

    public WindowMain(Sidebar sidebar, StackPane rightPane, PopUpWindowAddRoom windowAddRoom){
        this.splitPane = createSplitPane(sidebar, rightPane);
        buttonSettings(windowAddRoom);
    }

    public SplitPane getSplitPane() { return splitPane; }
    public Button getButtonOpenWindowAddRoom() {return buttonOpenWindowAddRoom; }

    private SplitPane createSplitPane(Sidebar sidebar, StackPane rightPane){
        SplitPane splitPane = new SplitPane();
        splitPane.getItems().addAll(sidebar.getLeftPane() , rightPane);
        splitPane.setDividerPositions(0.35);            // Устанавливаем начальное положение разделителя
        sidebar.getLeftPane().setMinWidth(200);         // Задаёт минимальные размеры сторон, чтобы
        rightPane.setMinWidth(200);                     // одну из них нельзя было "схлопнуть" в ноль
        return splitPane;
    }

    private void buttonSettings(PopUpWindowAddRoom windowAddRoom){
        StyleManager.buttonStyle(this.buttonOpenWindowAddRoom, 50, 50);
        this.buttonOpenWindowAddRoom.setOnAction(e -> windowAddRoom.show());
        StackPane.setAlignment(this.buttonOpenWindowAddRoom, Pos.BOTTOM_LEFT);
        StackPane.setMargin(this.buttonOpenWindowAddRoom, new Insets(0, 0, 15, 15));
    }
}
