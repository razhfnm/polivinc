package com.example.polivinc;
public class Message {
    private String userName;
    private String messageText;
    private long timestamp;

    public Message() {
        // Constructor vacío requerido por Firebase
    }

    public Message(String userName, String messageText, long timestamp) {
        this.userName = userName;
        this.messageText = messageText;
        this.timestamp = timestamp;
    }

    public String getUserName() {
        return userName;
    }

    public String getMessageText() {
        return messageText;
    }


}
