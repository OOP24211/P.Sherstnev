package org.example.ui;

import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class CreatePopUpWindow {
    private final Region veil;
    private final VBox popUpWindowAddRoom;
    private final HBox header;

    public CreatePopUpWindow(String nameHeader, int x, int y){      // Принимает имя окна и его размеры
        this.veil = new Region();
        veil.getStyleClass().add("popup-veil");

        this.popUpWindowAddRoom = new VBox(15);
        popUpWindowAddRoom.getStyleClass().add("popup-window");
        popUpWindowAddRoom.setMaxSize(x, y);
        popUpWindowAddRoom.setAlignment(Pos.CENTER);

        this.popUpWindowAddRoom.setVisible(false);                  // Делает НЕ видимым
        this.veil.setVisible(false);

        this.header = new HBox();                                   // Заголовок окна
        settingHeader(nameHeader);
    }

    public Region getVeil() {return veil; }
    public VBox getPopUpWindow() {return popUpWindowAddRoom; }
    public HBox getHeader() {return header; }

    public void show(){
        this.veil.setVisible(true);
        this.popUpWindowAddRoom.setVisible(true);
    }

    public void hide() {
        this.veil.setVisible(false);
        this.popUpWindowAddRoom.setVisible(false);
    }

    private void settingHeader(String nameHeader){
        header.setAlignment(Pos.CENTER_LEFT);                        // Текст будет слева
        header.getStyleClass().add("popup-header");                  // Фиолетовый верх

        Text headerText = new Text(nameHeader);                      // Текст для плашки
        headerText.getStyleClass().add("popup-header-text");
        header.getChildren().add(headerText);
    }
}
