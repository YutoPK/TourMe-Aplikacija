package com.example.tourme.Model;

public class User {

    private String id;
    private String email;
    private String username;
    private String imageurl;
    private String brojOglasa;
    private String status;
    private String ime;
    private String prezime;
    private String opis;
    private String dan, mesec, godina;
    private int brojOcena;
    private double ukupnaProsecnaOcena;

    public User(){

    }

    public User(String id, String email, String username, String imageurl, String brojOglasa, String status, String ime, String prezime, String opis, String dan, String mesec, String godina, int brojOcena, double ukupnaProsecnaOcena){
        this.id = id;
        this.email = email;
        this.username = username;
        this.imageurl = imageurl;
        this.brojOglasa = brojOglasa;
        this.status = status;
        this.ime = ime;
        this.prezime = prezime;
        this.opis = opis;
        this.dan = dan;
        this.mesec = mesec;
        this.godina = godina;
        this.brojOcena = brojOcena;
        this.ukupnaProsecnaOcena = ukupnaProsecnaOcena;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getImageurl() { return imageurl; }

    public void setImageurl(String imageurl) { this.imageurl = imageurl; }

    public void setBrojOglasa(String brojOglasa) { this.brojOglasa = brojOglasa; }

    public String getBrojOglasa() { return brojOglasa; }

    public String getStatus() { return status; }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getIme() { return ime; }

    public void setIme(String ime) { this.ime = ime; }

    public String getPrezime() { return prezime; }

    public void setPrezime(String prezime) { this.prezime = prezime; }

    public String getOpis() { return opis; }

    public void setOpis(String opis) { this.opis = opis; }

    public String getDan() { return dan; }

    public void setDan(String dan) { this.dan = dan; }

    public String getGodina() { return godina; }

    public void setGodina(String godina) { this.godina = godina; }

    public String getMesec() { return mesec; }

    public void setMesec(String mesec) { this.mesec = mesec; }

    public int getBrojOcena() { return brojOcena; }

    public void setBrojOcena(int brojOcena) { this.brojOcena = brojOcena; }

    public double getUkupnaProsecnaOcena() { return ukupnaProsecnaOcena; }

    public void setUkupnaProsecnaOcena(double ukupnaProsecnaOcena) { this.ukupnaProsecnaOcena = ukupnaProsecnaOcena; }
}
