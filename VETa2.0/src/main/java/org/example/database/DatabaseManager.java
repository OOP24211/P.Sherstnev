package org.example.database;

import org.example.common.Chat;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private static final String URL  = "jdbc:postgresql://localhost:5432/veta_db";
    private static final String USER = "veta_user";
    private static final String PASS = "veta_pass";

    private Connection connection;

    public DatabaseManager() {
        try {
            connection = DriverManager.getConnection(URL, USER, PASS);
            System.out.println("Соединение с PostgreSQL установлено.");
            initializeTables();
        } catch (SQLException e) {
            System.err.println("Не удалось подключиться к БД: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void initializeTables() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id       SERIAL PRIMARY KEY,
                    login    VARCHAR(50) UNIQUE NOT NULL,
                    password VARCHAR(100) NOT NULL
                );
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS chats (
                    id       SERIAL PRIMARY KEY,
                    name     VARCHAR(100) NOT NULL,
                    is_group BOOLEAN NOT NULL DEFAULT FALSE
                );
            """);

            // Добавляем колонку is_group если старая БД существует без неё
            try {
                stmt.execute("ALTER TABLE chats ADD COLUMN IF NOT EXISTS is_group BOOLEAN NOT NULL DEFAULT FALSE;");
            } catch (Exception ignored) {}

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS chat_members (
                    chat_id INT REFERENCES chats(id) ON DELETE CASCADE,
                    user_id INT REFERENCES users(id) ON DELETE CASCADE,
                    PRIMARY KEY (chat_id, user_id)
                );
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS messages (
                    id        SERIAL PRIMARY KEY,
                    chat_id   INT REFERENCES chats(id) ON DELETE CASCADE,
                    sender_id INT REFERENCES users(id) ON DELETE CASCADE,
                    content   TEXT NOT NULL,
                    sent_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                );
            """);

            System.out.println("Схема БД проверена/создана.");
        }
    }

    // ─── Аутентификация ────────────────────────────────────────────────────────

    public boolean registerUser(String login, String password) {
        String sql = "INSERT INTO users (login, password) VALUES (?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, login);
            ps.setString(2, password);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Ошибка регистрации: " + e.getMessage());
            return false;
        }
    }

    public boolean authenticate(String login, String password) {
        String sql = "SELECT 1 FROM users WHERE login = ? AND password = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, login);
            ps.setString(2, password);
            return ps.executeQuery().next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ─── Поиск ─────────────────────────────────────────────────────────────────

    /**
     * Возвращает пользователей и публичные группы, подходящие под запрос.
     * Для пользователей id = -1 (личный чат надо создавать).
     * Для групп id = реальный ID чата.
     */
    public List<Chat> searchUsersAndGroups(String query, String myLogin) {
        List<Chat> results = new ArrayList<>();
        String sql = """
            SELECT id, login AS name, 'USER' AS kind FROM users
             WHERE login ILIKE ? AND login != ?
            UNION
            SELECT id, name, 'GROUP' AS kind FROM chats
             WHERE name ILIKE ? AND is_group = TRUE
            ORDER BY name
        """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            String pat = "%" + query + "%";
            ps.setString(1, pat);
            ps.setString(2, myLogin);
            ps.setString(3, pat);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int id = "USER".equals(rs.getString("kind")) ? -1 : rs.getInt("id");
                results.add(new Chat(id, rs.getString("name")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
    }

    // ─── Чаты ──────────────────────────────────────────────────────────────────

    public List<Chat> getUserChats(String login) {
        List<Chat> chats = new ArrayList<>();
        String sql = """
            SELECT c.id,
                   CASE
                     WHEN c.is_group THEN c.name
                     ELSE COALESCE(
                       (SELECT u.login FROM users u
                          JOIN chat_members cm2 ON u.id = cm2.user_id
                         WHERE cm2.chat_id = c.id AND u.login != ?
                         LIMIT 1),
                       'Заметки')
                   END AS display_name
            FROM chats c
            JOIN chat_members cm ON c.id = cm.chat_id
            JOIN users u1        ON cm.user_id = u1.id
            WHERE u1.login = ?
            ORDER BY c.id DESC
        """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, login);
            ps.setString(2, login);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                chats.add(new Chat(rs.getInt("id"), rs.getString("display_name")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return chats;
    }

    /** Создать личный чат (is_group = false). Возвращает ID или -1. */
    public int createPrivateChat(String user1, String user2) {
        // Проверяем, нет ли уже такого чата
        int existing = getExistingChatBetween(user1, user2);
        if (existing != -1) return existing;

        String insertChat = "INSERT INTO chats (name, is_group) VALUES (?, FALSE) RETURNING id";
        try (PreparedStatement ps = connection.prepareStatement(insertChat)) {
            ps.setString(1, ""); // У личных чатов имя не нужно
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int chatId = rs.getInt(1);
                addUserToChat(chatId, user1);
                addUserToChat(chatId, user2);
                return chatId;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /** Создать групповой чат (is_group = true). Возвращает ID или -1. */
    public int createGroupChat(String chatName, String creatorLogin) {
        String insertChat = "INSERT INTO chats (name, is_group) VALUES (?, TRUE) RETURNING id";
        try (PreparedStatement ps = connection.prepareStatement(insertChat)) {
            ps.setString(1, chatName);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int chatId = rs.getInt(1);
                addUserToChat(chatId, creatorLogin);
                return chatId;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public void addUserToChat(int chatId, String login) {
        String findUser    = "SELECT id FROM users WHERE login = ?";
        String insertMember = "INSERT INTO chat_members (chat_id, user_id) VALUES (?, ?) ON CONFLICT DO NOTHING";
        try (PreparedStatement ps1 = connection.prepareStatement(findUser)) {
            ps1.setString(1, login);
            ResultSet rs = ps1.executeQuery();
            if (rs.next()) {
                int userId = rs.getInt("id");
                try (PreparedStatement ps2 = connection.prepareStatement(insertMember)) {
                    ps2.setInt(1, chatId);
                    ps2.setInt(2, userId);
                    ps2.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean isGroupChat(int chatId) {
        String sql = "SELECT is_group FROM chats WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, chatId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getBoolean("is_group");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public int getExistingChatBetween(String user1, String user2) {
        String sql = """
            SELECT cm1.chat_id FROM chat_members cm1
            JOIN chat_members cm2 ON cm1.chat_id = cm2.chat_id
            JOIN users u1 ON cm1.user_id = u1.id
            JOIN users u2 ON cm2.user_id = u2.id
            JOIN chats c  ON cm1.chat_id = c.id
            WHERE u1.login = ? AND u2.login = ? AND c.is_group = FALSE
        """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, user1);
            ps.setString(2, user2);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("chat_id");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    // ─── Участники ─────────────────────────────────────────────────────────────

    public List<String> getChatMembers(int chatId) {
        List<String> members = new ArrayList<>();
        String sql = """
            SELECT u.login FROM users u
            JOIN chat_members cm ON u.id = cm.user_id
            WHERE cm.chat_id = ?
        """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, chatId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) members.add(rs.getString("login"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return members;
    }

    public int getMemberCount(int chatId) {
        String sql = "SELECT COUNT(*) FROM chat_members WHERE chat_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, chatId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // ─── Сообщения ─────────────────────────────────────────────────────────────

    /** Сохраняет сообщение и возвращает время "HH:mm". */
    public String saveMessage(int chatId, String senderLogin, String text) {
        String sql = """
            INSERT INTO messages (chat_id, sender_id, content)
            VALUES (?, (SELECT id FROM users WHERE login = ?), ?)
            RETURNING sent_at
        """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, chatId);
            ps.setString(2, senderLogin);
            ps.setString(3, text);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getTimestamp("sent_at").toLocalDateTime()
                         .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "00:00";
    }

    /** Возвращает историю в формате "login: text|HH:mm". */
    public List<String> getChatHistory(int chatId) {
        List<String> history = new ArrayList<>();
        String sql = """
            SELECT u.login, m.content, m.sent_at FROM messages m
            JOIN users u ON m.sender_id = u.id
            WHERE m.chat_id = ?
            ORDER BY m.sent_at ASC
        """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, chatId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String time = rs.getTimestamp("sent_at").toLocalDateTime()
                                .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
                history.add(rs.getString("login") + ": " + rs.getString("content") + "|" + time);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return history;
    }

    public boolean userExists(String login) {
        String sql = "SELECT 1 FROM users WHERE login = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, login);
            return ps.executeQuery().next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
