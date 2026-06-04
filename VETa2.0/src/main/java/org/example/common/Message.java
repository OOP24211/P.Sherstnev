package org.example.common;

import java.util.List;

public class Message {
    public enum Type {
        LOGIN,
        REGISTER,
        AUTH_SUCCESS,
        AUTH_ERROR,
        MSG_TEXT,
        SEARCH_USER,
        SEARCH_CHAT,
        CREATE_CHAT,
        UPDATE_LISTS,
        SUCCESS,
        ERROR,
        GET_HISTORY,
        ADD_MEMBER,       // Добавить участника в группу
        GET_MEMBERS,      // Запросить список участников чата
        MEMBERS_LIST,     // Ответ со списком участников
        LOGOUT            // Выход из аккаунта
    }

    public Type type;
    public String sender;
    public String content;
    public String target;
    public List<String> dataList;
    public List<Chat> chatsList;
    public int memberCount;   // Кол-во участников (для заголовка)

    public Message() {}

    public Message(Type type, String content) {
        this.type = type;
        this.content = content;
    }
}
