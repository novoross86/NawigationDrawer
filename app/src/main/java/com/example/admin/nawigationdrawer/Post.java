package com.example.admin.nawigationdrawer;

public class Post {

    private String channel, title, chat_id, username, text, uid;



    public Post(String channel, String title, String chat_id, String username, String text, String uid){
        this.channel = channel;
        this.title = title;
        this.chat_id = chat_id;
        this.username = username;
        this.text = text;
        this.uid = uid;

    }

    public Post(){}

    public String getChatId() {
        return chat_id;
    }

    public String getChat_id() {
        return chat_id;
    }

    public void setChat_id(String chat_id) {
        this.chat_id = chat_id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setChatId(String chat_id) {
        this.chat_id = chat_id;
    }

    public String getChannel(){
        return channel;
    }

    public void setChannel(String channel){
        this.channel = channel;
    }

    public String getTitle(){
        return title;
    }

    public void setTitle(String title){
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

}
