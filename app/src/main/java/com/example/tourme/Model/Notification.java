package com.example.tourme.Model;

import com.example.tourme.Adapters.NotificationAdapter;

public class Notification {

    String to;
    String title;
    String body;

    public Notification(String to, String title, String body) {
        this.to = to;
        this.title = title;
        this.body = body;
    }

    public Notification(){

    };

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
