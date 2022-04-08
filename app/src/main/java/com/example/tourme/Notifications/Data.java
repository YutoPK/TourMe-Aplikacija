package com.example.tourme.Notifications;

import com.example.tourme.Model.Oglas;

public class Data {
    private String user;
    private int icon;
    private String body;
    private String title;
    private String sented;

    private String IDOglasa;
    private String NazivGrada;
    private String IDUser;

    public Data(String user, int icon, String body, String title, String sented) {
        this.user = user;
        this.icon = icon;
        this.body = body;
        this.title = title;
        this.sented = sented;
    }

    public Data(String user, int icon, String body, String title, String sented, String IDOglasa, String NazivGrada, String IDUser) {
        this.user = user;
        this.icon = icon;
        this.body = body;
        this.title = title;
        this.sented = sented;
        this.IDOglasa = IDOglasa;
        this.NazivGrada = NazivGrada;
        this.IDUser = IDUser;

    }

    public Data(){

    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSented() {
        return sented;
    }

    public void setSented(String sented) {
        this.sented = sented;
    }

    public String getIDOglasa() {
        return IDOglasa;
    }

    public void setIDOglasa(String IDOglasa) {
        this.IDOglasa = IDOglasa;
    }

    public String getNazivGrada() {
        return NazivGrada;
    }

    public void setNazivGrada(String nazivGrada) {
        NazivGrada = nazivGrada;
    }

    public String getIDUser() {
        return IDUser;
    }

    public void setIDUser(String IDUser) {
        this.IDUser = IDUser;
    }

}
