package org.example.ui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.example.client.ChatClient;
import org.example.common.Chat;
import org.example.common.Message;

import java.io.IOException;

public class MainController {

    @FXML private TextField    searchField;
    @FXML private ListView<String> messageListView;
    @FXML private TextField    messageInputField;
    @FXML private Label        chatNameLabel;
    @FXML private Label        chatInfoLabel;
    @FXML private ListView<Chat> chatListView;
    @FXML private ListView<Chat> searchResultsList;

    private int currentChatId = -1;
    private ChatClient client;

    // ─── Инициализация ─────────────────────────────────────────────────────────

    @FXML
    public void initialize() {
        // Поиск — слушаем текст
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            boolean searching = !newVal.trim().isEmpty();
            searchResultsList.setVisible(searching);
            searchResultsList.setManaged(searching);
            chatListView.setVisible(!searching);
            chatListView.setManaged(!searching);

            if (client != null) {
                Message m = new Message();
                m.type    = Message.Type.SEARCH_USER;
                m.content = newVal;
                client.sendMessage(m);
            }
        });

        // Двойной клик по результату поиска → создать чат
        searchResultsList.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                Chat selected = searchResultsList.getSelectionModel().getSelectedItem();
                if (selected != null) openOrCreateChat(selected);
            }
        });

        // Одиночный клик по чату → открыть
        chatListView.getSelectionModel().selectedItemProperty().addListener((obs, old, cur) -> {
            if (cur != null) selectChat(cur);
        });

        // Enter для отправки
        messageInputField.setOnKeyPressed(e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.ENTER) onSendMessageClick();
        });

        // Авто-скролл
        messageListView.getItems().addListener((javafx.collections.ListChangeListener<String>) c ->
            Platform.runLater(() -> {
                if (!messageListView.getItems().isEmpty())
                    messageListView.scrollTo(messageListView.getItems().size() - 1);
            })
        );

        // Фабрика ячеек — пузыри сообщений
        messageListView.setCellFactory(lv -> new MessageCell());

        // Фабрика ячеек для поиска
        searchResultsList.setCellFactory(lv -> new ListCell<Chat>() {
            @Override
            protected void updateItem(Chat item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setGraphic(null); }
                else {
                    String prefix = item.id == -1 ? "👤 " : "👥 ";
                    setText(prefix + item.name);
                    setStyle("-fx-text-fill: #e0e0e0; -fx-padding: 8 15;");
                }
            }
        });
    }

    // ─── Передача клиента из LoginController ───────────────────────────────────

    public void setClient(ChatClient client) {
        this.client = client;
        this.client.setCallback(this::onMessageReceived);
    }

    // ─── Обработка входящих сообщений ──────────────────────────────────────────

    private void onMessageReceived(Message msg) {
        switch (msg.type) {

            case UPDATE_LISTS -> Platform.runLater(() -> {
                if (msg.chatsList == null) return;
                boolean searching = !searchField.getText().trim().isEmpty();
                if (searching) {
                    searchResultsList.getItems().setAll(msg.chatsList);
                } else {
                    // Запоминаем текущий выбранный чат
                    Chat selected = chatListView.getSelectionModel().getSelectedItem();
                    chatListView.getItems().setAll(msg.chatsList);
                    // Восстанавливаем выбор
                    if (selected != null) {
                        for (Chat c : chatListView.getItems()) {
                            if (c.id == selected.id) {
                                chatListView.getSelectionModel().select(c);
                                break;
                            }
                        }
                    }
                }
            });

            case MSG_TEXT -> {
                if (msg.target != null && msg.target.equals(String.valueOf(currentChatId))) {
                    Platform.runLater(() -> {
                        if (msg.dataList != null) {
                            // Это история — добавляем всё
                            messageListView.getItems().setAll(msg.dataList);
                            // Обновляем счётчик участников
                            if (msg.memberCount > 0)
                                chatInfoLabel.setText("участников: " + msg.memberCount);
                        } else if (msg.content != null) {
                            // Одно новое сообщение
                            messageListView.getItems().add(msg.sender + ": " + msg.content);
                        }
                    });
                }
            }

            case SUCCESS -> {
                int newChatId = Integer.parseInt(msg.target);
                String chatName = msg.content;
                int count = msg.memberCount;

                Platform.runLater(() -> {
                    // Ищем чат в списке, если уже есть — просто выбираем
                    Chat found = null;
                    for (Chat c : chatListView.getItems()) {
                        if (c.id == newChatId) { found = c; break; }
                    }
                    if (found == null) {
                        found = new Chat(newChatId, chatName);
                        chatListView.getItems().add(0, found);
                    }
                    chatListView.getSelectionModel().select(found);
                    searchField.clear();

                    // Обновить счётчик, если это текущий чат
                    if (currentChatId == newChatId && count > 0)
                        chatInfoLabel.setText("участников: " + count);
                });
            }

            case ERROR -> Platform.runLater(() ->
                showAlert(Alert.AlertType.ERROR, "Ошибка", msg.content)
            );

            case MEMBERS_LIST -> Platform.runLater(() -> {
                if (msg.dataList != null)
                    showMembersDialog(msg.dataList, Integer.parseInt(msg.target));
            });
        }
    }

    // ─── Выбор чата ────────────────────────────────────────────────────────────

    private void selectChat(Chat chat) {
        this.currentChatId = chat.id;
        chatNameLabel.setText(chat.name);
        chatInfoLabel.setText("загрузка...");
        messageListView.getItems().clear();

        // Запрашиваем историю (сервер вернёт и memberCount)
        Message req = new Message();
        req.type    = Message.Type.GET_HISTORY;
        req.content = String.valueOf(currentChatId);
        client.sendMessage(req);
    }

    private void openOrCreateChat(Chat selected) {
        Message m = new Message();
        m.type = Message.Type.CREATE_CHAT;

        if (selected.id == -1) {
            // Пользователь — создаём личный чат
            m.target  = selected.name;
            m.content = selected.name;
        } else {
            // Группа уже существует — просто открываем
            chatListView.getItems().stream()
                .filter(c -> c.id == selected.id)
                .findFirst()
                .ifPresentOrElse(
                    c -> chatListView.getSelectionModel().select(c),
                    () -> {
                        chatListView.getItems().add(0, selected);
                        chatListView.getSelectionModel().select(selected);
                    }
                );
            searchField.clear();
            return;
        }
        client.sendMessage(m);
        searchField.clear();
    }

    // ─── Отправка сообщения ────────────────────────────────────────────────────

    @FXML
    private void onSendMessageClick() {
        String text = messageInputField.getText().trim();
        if (text.isEmpty() || currentChatId == -1) return;

        Message m = new Message();
        m.type    = Message.Type.MSG_TEXT;
        m.content = text;
        m.target  = String.valueOf(currentChatId);
        client.sendMessage(m);
        messageInputField.clear();
    }

    // ─── Кнопка "+" (создать групповой чат) ───────────────────────────────────

    @FXML
    private void onCreateChatClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/ui/create_chat.fxml"));
            Parent root = loader.load();

            CreateChatController ctrl = loader.getController();
            ctrl.setMainController(this);

            Stage owner = (Stage) chatListView.getScene().getWindow();
            Stage stage = buildModal(owner, "Новый групповой чат", root, 320, 180);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendCreateGroupChat(String chatName) {
        Message m = new Message();
        m.type    = Message.Type.CREATE_CHAT;
        m.content = chatName;
        m.target  = "GROUP";
        client.sendMessage(m);
    }

    // ─── Добавить участника в группу ──────────────────────────────────────────

    @FXML
    private void onAddMemberClick() {
        if (currentChatId == -1) {
            showAlert(Alert.AlertType.INFORMATION, "Подсказка", "Сначала выберите групповой чат");
            return;
        }
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Добавить участника");
        dialog.setHeaderText("Введите логин пользователя:");
        dialog.setContentText("Логин:");
        applyDarkStyle(dialog.getDialogPane());

        dialog.showAndWait().ifPresent(login -> {
            if (!login.trim().isEmpty()) {
                Message m = new Message();
                m.type    = Message.Type.ADD_MEMBER;
                m.target  = String.valueOf(currentChatId);
                m.content = login.trim();
                client.sendMessage(m);
            }
        });
    }

    // ─── Посмотреть участников ─────────────────────────────────────────────────

    @FXML
    private void onViewMembersClick() {
        if (currentChatId == -1) return;
        Message m = new Message();
        m.type    = Message.Type.GET_MEMBERS;
        m.content = String.valueOf(currentChatId);
        client.sendMessage(m);
    }

    private void showMembersDialog(java.util.List<String> members, int chatId) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Участники чата");
        alert.setHeaderText("Участники (" + members.size() + "):");
        alert.setContentText(String.join("\n", members));
        applyDarkStyle(alert.getDialogPane());
        alert.show();
    }

    // ─── Профиль ───────────────────────────────────────────────────────────────

    @FXML
    private void onProfileClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/ui/profile.fxml"));
            Parent root = loader.load();

            ProfileController ctrl = loader.getController();
            ctrl.setUserData(client.getLogin());

            Stage owner = (Stage) chatListView.getScene().getWindow();
            buildModal(owner, "Профиль — " + client.getLogin(), root, 300, 400).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ─── Выход ─────────────────────────────────────────────────────────────────

    @FXML
    private void onLogoutClick() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Выход");
        confirm.setHeaderText(null);
        confirm.setContentText("Вы уверены, что хотите выйти?");
        applyDarkStyle(confirm.getDialogPane());

        confirm.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                client.disconnect();
                try {
                    Stage stage = (Stage) chatListView.getScene().getWindow();
                    FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/org/example/ui/login.fxml"));
                    Scene scene = new Scene(loader.load(), 400, 300);
                    stage.setScene(scene);
                    stage.centerOnScreen();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // ─── Утилиты ───────────────────────────────────────────────────────────────

    private Stage buildModal(Stage owner, String title, Parent root, double w, double h) {
        Stage stage = new Stage();
        stage.setTitle(title);
        stage.initOwner(owner);
        stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        stage.setResizable(false);

        Scene scene = new Scene(root, w, h);
        String css = getClass().getResource("/org/example/ui/style.css") != null
            ? getClass().getResource("/org/example/ui/style.css").toExternalForm() : null;
        if (css != null) scene.getStylesheets().add(css);
        stage.setScene(scene);

        stage.setOnShown(e -> {
            stage.setX(owner.getX() + owner.getWidth()  / 2 - stage.getWidth()  / 2);
            stage.setY(owner.getY() + owner.getHeight() / 2 - stage.getHeight() / 2);
        });
        return stage;
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        applyDarkStyle(alert.getDialogPane());
        alert.show();
    }

    private void applyDarkStyle(DialogPane pane) {
        pane.setStyle("-fx-background-color: #1e1e1e; -fx-text-fill: white;");
        String css = getClass().getResource("/org/example/ui/style.css") != null
            ? getClass().getResource("/org/example/ui/style.css").toExternalForm() : null;
        if (css != null) pane.getStylesheets().add(css);
    }

    // ─── Внутренний класс — ячейка с пузырём ──────────────────────────────────

    private class MessageCell extends ListCell<String> {
        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
                setText(null);
                setStyle("-fx-background-color: transparent;");
                return;
            }

            // Разделяем "login: text|HH:mm"
            String textPart = item;
            String timePart = "";
            int sep = item.lastIndexOf("|");
            if (sep != -1 && item.length() - sep == 6) {
                textPart = item.substring(0, sep);
                timePart = item.substring(sep + 1);
            }

            String myLogin = client.getLogin();
            boolean isMine = textPart.startsWith(myLogin + ":");

            // Текст без "login: " для своих сообщений
            String displayText = isMine
                ? textPart.replaceFirst("^" + java.util.regex.Pattern.quote(myLogin) + ": ", "")
                : textPart;

            // Имя отправителя (для чужих)
            String senderName = "";
            if (!isMine && textPart.contains(": ")) {
                senderName = textPart.substring(0, textPart.indexOf(": "));
            }

            // Пузырь
            VBox bubble = new VBox(2);
            bubble.getStyleClass().addAll("bubble", isMine ? "my-bubble" : "other-bubble");
            bubble.setMaxWidth(460);

            if (!isMine && !senderName.isEmpty()) {
                Label sender = new Label(senderName);
                sender.getStyleClass().add("sender-label");
                bubble.getChildren().add(sender);
            }

            Label msgLbl = new Label(displayText);
            msgLbl.setWrapText(true);
            msgLbl.setMaxWidth(400);
            msgLbl.getStyleClass().add("message-label");

            HBox bottom = new HBox();
            bottom.setAlignment(Pos.BOTTOM_RIGHT);
            Region spring = new Region();
            HBox.setHgrow(spring, Priority.ALWAYS);
            Label timeLbl = new Label(timePart + (isMine ? " ✓" : ""));
            timeLbl.getStyleClass().add("time-label");
            bottom.getChildren().addAll(spring, timeLbl);

            bubble.getChildren().addAll(msgLbl, bottom);

            HBox row = new HBox(bubble);
            row.setPadding(new javafx.geometry.Insets(2, 8, 2, 8));
            row.setAlignment(isMine ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

            setGraphic(row);
            setText(null);
            setStyle("-fx-background-color: transparent;");
        }
    }
}
