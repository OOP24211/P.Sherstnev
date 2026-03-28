package org.example.database;

import java.sql.*;

public class DatabaseManager {
    private static final String URL = "jdbc:postgresql://localhost:5432/postgres";
    private static final String USER = "postgres";
    private static final String PASS = "4283";

    public static int registerUserAndGetId(String username, String password) {
        // RETURNING id сразу отдаст нам номер новой строки
        String sql = "INSERT INTO users (username, password_hash) VALUES (?, ?) RETURNING id";

        try (Connection conn = connect()) {
             PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            pstmt.setString(2, password);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1); // Возвращаем ID нового юзера
            }
            System.out.println("Юзер " + username + " успешно сохранен в PostgreSQL!");
        } catch (SQLException e) {
            System.out.println("Ошибка регистрации: " + e.getMessage());
        }
        return -1; // Если ник занят или ошибка
    }

    public static int authUserAndGetId(String username, String password) {
        // SQL запрос: "Выбери всё из таблицы users, где имя = ? и пароль = ?"
        String sql = "SELECT * FROM users WHERE username = ? AND password_hash = ?";

        try (Connection conn = connect()) {
             PreparedStatement pstmt = conn.prepareStatement(sql);
            // Заполняем "дырки" данными из интерфейса
            pstmt.setString(1, username);
            pstmt.setString(2, password);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id"); // Возвращаем ID из базы
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // Если не нашли или ошибка — возвращаем -1
    }

    public static void createChat(String nameChat, int creatorId){
        String sqlChat = "INSERT INTO chats (name) VALUES (?) RETURNING id";

        try (Connection conn = connect()){
            PreparedStatement pstmpChat = conn.prepareStatement(sqlChat);
            pstmpChat.setString(1, nameChat);

            // Для получения данных executeQuery()
            var rs = pstmpChat.executeQuery();

            if (rs.next()){
                // Получили ID нового чата
                int chatId = rs.getInt(1);

                // Добавляем создателя в этот чат
                String sqlMember = "INSERT INTO chat_members (user_id, chat_id) VALUES (?, ?)";
                PreparedStatement pstmtMember = conn.prepareStatement(sqlMember);
                pstmtMember.setInt(1, creatorId);
                pstmtMember.setInt(2, chatId);
                pstmtMember.executeUpdate();

                System.out.println("Чат '" + nameChat + "' создан, ID: " + chatId);
            }
        } catch (SQLException e) {
            System.out.println("Ошибка при создании чата: " + e.getMessage());
        }
    }

    public static void  sendMessage(int chatId, int senderId, String text){
        String sql = "INSERT INTO messages (chat_id, sender_id, content) VALUES (?, ?, ?)";

        try (Connection conn = connect()){
            PreparedStatement prtmt = conn.prepareStatement(sql);

            prtmt.setInt(1, chatId);
            prtmt.setInt(2, senderId);
            prtmt.setString(3, text);

            prtmt.executeUpdate();

            System.out.println("Сообщение отправлено!");
        }
        catch (SQLException e) {
            System.out.println("Ошибка отправки: " + e.getMessage());
        }
    }

    public static String getUserChats(int userId) {
        // Соединяем таблицы chats и chat_members, чтобы по ID юзера найти названия чатов
        String sql = "SELECT c.id, c.name FROM chats c " +
                "JOIN chat_members cm ON c.id = cm.chat_id " +
                "WHERE cm.user_id = ?";

        StringBuilder sb = new StringBuilder();
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                if (sb.length() > 0) {
                    sb.append(","); // Разделитель между чатами
                }
                sb.append(rs.getInt("id")).append("-").append(rs.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sb.toString(); // Вернет строку типа "1-Общий,2-Работа"
    }

    public static String getChatHistory(int chatId) {
        // Выбираем отправителя и текст, сортируем по ID (чтобы старые были сверху)
        String sql = "SELECT sender_id, content FROM messages WHERE chat_id = ? ORDER BY id ASC";
        StringBuilder sb = new StringBuilder();

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, chatId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                if (sb.length() > 0) sb.append("|||"); // Разделитель между сообщениями
                sb.append(rs.getInt("sender_id"))
                        .append(":::") // Разделитель внутри сообщения
                        .append(rs.getString("content"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    private static Connection connect() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }
}
