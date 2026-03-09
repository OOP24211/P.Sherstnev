package org.example.models;

public class Message {
    private String sender;
    private String text;
    private String type;

    public Message(){}
    public Message(String sender, String text, String type) {
        this.sender = sender;
        this.text = text;
        this.type = type;
    }

    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}
