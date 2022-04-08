package com.example.tourme.Model;

import java.util.HashMap;

public class Oglas {

    String idOglasa;
    String grad;
    int brojOcena;
    int cenaOglasa;
    double ocena;
    String userId;
    String opis;

    public Oglas(){

    }

    public Oglas(String idOglasa, String grad, double ocena, int brojOcena, int cenaOglasa, String userId, String opis){
        this.idOglasa = idOglasa;
        this.grad = grad;
        this.ocena = ocena;
        this.brojOcena = brojOcena;
        this.cenaOglasa = cenaOglasa;
        this.userId = userId;
        this.opis = opis;
    }

    public String getIdOglasa() { return idOglasa; }

    public void setIdOglasa(String idOglasa) { this.idOglasa = idOglasa; }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getOpis() {
        return opis;
    }

    public void setOpis(String opis) {
        this.opis = opis;
    }

    public void setGrad(String grad) { this.grad = grad; }

    public void setOcena(double ocena) { this.ocena = ocena; }

    public double getOcena() { return ocena; }

    public String getGrad() {  return grad; }

    public int getBrojOcena() { return brojOcena; }

    public void setBrojOcena(int brojOcena) { this.brojOcena = brojOcena; }

    public int getCenaOglasa() { return cenaOglasa; }

    public void setCenaOglasa(int cenaOglasa) { this.cenaOglasa = cenaOglasa; }

}
