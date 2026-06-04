package org.example.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.common.Chat;
import org.example.common.Message;
import org.example.database.DatabaseManager;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChatServer extends WebSocketServer {
    private final DatabaseManager db;
    private final ObjectMapper mapper = new ObjectMapper();

    // WebSocket -> логин онлайн-пользователя
    private final Map<WebSocket, String> activeUsers = new ConcurrentHashMap<>();

    public ChatServer(int port) {
        super(new InetSocketAddress(port));
        this.db = new DatabaseManager();
    }

    // ─── WebSocket события ─────────────────────────────────────────────────────

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        System.out.println("Подключился: " + conn.getRemoteSocketAddress());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        String who = activeUsers.remove(conn);
        System.out.println("Отключился: " + (who != null ? who : conn.getRemoteSocketAddress()));
    }

    @Override
    public void onMessage(WebSocket conn, String raw) {
        try {
            Message msg = mapper.readValue(raw, Message.class);
            switch (msg.type) {
                case LOGIN      -> handleLogin(conn, msg);
                case REGISTER   -> handleRegister(conn, msg);
                case LOGOUT     -> handleLogout(conn);
                case SEARCH_USER -> handleSearch(conn, msg);
                case CREATE_CHAT -> handleCreateChat(conn, msg);
                case ADD_MEMBER  -> handleAddMember(conn, msg);
                case MSG_TEXT    -> handleTextMessage(conn, msg);
                case GET_HISTORY -> handleGetHistory(conn, msg);
                case GET_MEMBERS -> handleGetMembers(conn, msg);
                default -> System.out.println("Неизвестный тип: " + msg.type);
            }
        } catch (Exception e) {
            System.err.println("Ошибка обработки: " + e.getMessage());
        }
    }

    @Override public void onError(WebSocket conn, Exception ex) { ex.printStackTrace(); }
    @Override public void onStart() { System.out.println("Сервер запущен на порту " + getPort()); }

    // ─── Аутентификация ────────────────────────────────────────────────────────

    private void handleLogin(WebSocket conn, Message msg) throws Exception {
        if (db.authenticate(msg.sender, msg.content)) {
            activeUsers.put(conn, msg.sender);
            send(conn, new Message(Message.Type.AUTH_SUCCESS, "OK"));

            // Сразу шлём список чатов
            sendUserChats(conn, msg.sender);
        } else {
            send(conn, new Message(Message.Type.AUTH_ERROR, "Неверный логин или пароль"));
        }
    }

    private void handleRegister(WebSocket conn, Message msg) throws Exception {
        if (db.registerUser(msg.sender, msg.content)) {
            send(conn, new Message(Message.Type.AUTH_SUCCESS, "Регистрация успешна!"));
        } else {
            send(conn, new Message(Message.Type.AUTH_ERROR, "Логин уже занят"));
        }
    }

    private void handleLogout(WebSocket conn) {
        activeUsers.remove(conn);
        // Клиент сам закроет соединение
    }

    // ─── Поиск ─────────────────────────────────────────────────────────────────

    private void handleSearch(WebSocket conn, Message msg) throws Exception {
        String myLogin = activeUsers.get(conn);
        if (myLogin == null) return;

        String query = msg.content != null ? msg.content.trim() : "";
        List<Chat> results;

        if (query.isEmpty()) {
            results = db.getUserChats(myLogin);
        } else {
            results = db.searchUsersAndGroups(query, myLogin);
        }

        Message resp = new Message(Message.Type.UPDATE_LISTS, null);
        resp.chatsList = results;
        send(conn, resp);
    }

    // ─── Создание чата ─────────────────────────────────────────────────────────

    private void handleCreateChat(WebSocket conn, Message msg) throws Exception {
        String creatorLogin = activeUsers.get(conn);
        if (creatorLogin == null) return;

        if ("GROUP".equals(msg.target)) {
            // Создание группового чата
            int chatId = db.createGroupChat(msg.content, creatorLogin);
            if (chatId != -1) {
                Message resp = new Message(Message.Type.SUCCESS, msg.content);
                resp.target = String.valueOf(chatId);
                resp.memberCount = 1;
                send(conn, resp);
            } else {
                send(conn, new Message(Message.Type.ERROR, "Не удалось создать чат"));
            }

        } else {
            // Создание личного чата с пользователем msg.target
            String partnerLogin = msg.target;

            if (!db.userExists(partnerLogin)) {
                send(conn, new Message(Message.Type.ERROR, "Пользователь не найден: " + partnerLogin));
                return;
            }

            int chatId = db.createPrivateChat(creatorLogin, partnerLogin);
            if (chatId != -1) {
                // Отвечаем создателю
                Message resp = new Message(Message.Type.SUCCESS, partnerLogin);
                resp.target = String.valueOf(chatId);
                resp.memberCount = 2;
                send(conn, resp);

                // Если партнёр онлайн — обновляем его список
                notifyUserChatsUpdate(partnerLogin);
            } else {
                send(conn, new Message(Message.Type.ERROR, "Ошибка создания чата"));
            }
        }
    }

    // ─── Добавление участника в группу ────────────────────────────────────────

    private void handleAddMember(WebSocket conn, Message msg) throws Exception {
        String requesterLogin = activeUsers.get(conn);
        if (requesterLogin == null) return;

        int chatId = Integer.parseInt(msg.target);
        String newMemberLogin = msg.content.trim();

        // Проверяем: запрашивающий сам состоит в этом чате
        List<String> members = db.getChatMembers(chatId);
        if (!members.contains(requesterLogin)) {
            send(conn, new Message(Message.Type.ERROR, "Вы не состоите в этом чате"));
            return;
        }

        if (!db.userExists(newMemberLogin)) {
            send(conn, new Message(Message.Type.ERROR, "Пользователь не найден: " + newMemberLogin));
            return;
        }

        if (members.contains(newMemberLogin)) {
            send(conn, new Message(Message.Type.ERROR, newMemberLogin + " уже в чате"));
            return;
        }

        db.addUserToChat(chatId, newMemberLogin);

        // Уведомляем всех текущих участников об обновлении
        broadcastUpdateListsToChat(chatId);

        // Если новый участник онлайн — обновляем его чаты
        notifyUserChatsUpdate(newMemberLogin);

        // Шлём подтверждение запрашивающему
        int count = db.getMemberCount(chatId);
        Message ok = new Message(Message.Type.SUCCESS, newMemberLogin + " добавлен в чат");
        ok.target = String.valueOf(chatId);
        ok.memberCount = count;
        send(conn, ok);
    }

    // ─── Сообщения ─────────────────────────────────────────────────────────────

    private void handleTextMessage(WebSocket conn, Message msg) throws Exception {
        String sender = activeUsers.get(conn);
        if (sender == null) return;

        int chatId = Integer.parseInt(msg.target);
        List<String> members = db.getChatMembers(chatId);

        if (!members.contains(sender)) {
            System.out.println("Нарушение: " + sender + " пишет в чужой чат " + chatId);
            return;
        }

        String time = db.saveMessage(chatId, sender, msg.content);

        Message broadcast = new Message();
        broadcast.type    = Message.Type.MSG_TEXT;
        broadcast.sender  = sender;
        broadcast.target  = String.valueOf(chatId);
        broadcast.content = msg.content + "|" + time;

        String json = mapper.writeValueAsString(broadcast);
        for (Map.Entry<WebSocket, String> entry : activeUsers.entrySet()) {
            if (members.contains(entry.getValue())) {
                entry.getKey().send(json);
            }
        }
    }

    private void handleGetHistory(WebSocket conn, Message msg) throws Exception {
        int chatId = Integer.parseInt(msg.content);

        List<String> history = db.getChatHistory(chatId);
        int memberCount = db.getMemberCount(chatId);

        Message resp = new Message();
        resp.type        = Message.Type.MSG_TEXT;
        resp.target      = String.valueOf(chatId);
        resp.dataList    = history;
        resp.memberCount = memberCount;
        send(conn, resp);
    }

    private void handleGetMembers(WebSocket conn, Message msg) throws Exception {
        int chatId = Integer.parseInt(msg.content);
        List<String> members = db.getChatMembers(chatId);

        Message resp = new Message();
        resp.type     = Message.Type.MEMBERS_LIST;
        resp.target   = String.valueOf(chatId);
        resp.dataList = members;
        send(conn, resp);
    }

    // ─── Вспомогательные ───────────────────────────────────────────────────────

    private void send(WebSocket conn, Message msg) throws Exception {
        conn.send(mapper.writeValueAsString(msg));
    }

    private void sendUserChats(WebSocket conn, String login) throws Exception {
        List<Chat> chats = db.getUserChats(login);
        Message m = new Message(Message.Type.UPDATE_LISTS, null);
        m.chatsList = chats;
        send(conn, m);
    }

    /** Шлёт обновлённый список чатов конкретному онлайн-пользователю. */
    private void notifyUserChatsUpdate(String login) {
        for (Map.Entry<WebSocket, String> e : activeUsers.entrySet()) {
            if (login.equals(e.getValue())) {
                try {
                    sendUserChats(e.getKey(), login);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    /** Шлёт обновление списков всем участникам конкретного чата. */
    private void broadcastUpdateListsToChat(int chatId) {
        List<String> members = db.getChatMembers(chatId);
        for (String member : members) {
            notifyUserChatsUpdate(member);
        }
    }
}
