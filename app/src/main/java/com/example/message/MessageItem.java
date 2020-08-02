package com.example.message;

/**
 * @author Ryan
 * @description
 *  메시지 아이템 클래스
 * @param {String} message : 메세지 내용
 * @param {String} userName : 채팀 메시지 전송한 유저 아이
 */

public class MessageItem {

    String message;
    String userName;


    public MessageItem( String message, String userName) {
        this.message = message;
        this.userName = userName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
