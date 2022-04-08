package com.example.tourme.Model;


import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Gradovi {

    public static class Grad {

        String ime;
        com.google.android.gms.maps.model.LatLng lokacija;

        public Grad(String ime, com.google.android.gms.maps.model.LatLng lokacija){
            this.ime = ime;
            this.lokacija = lokacija;
        }
        public String getIme() {
            return ime;
        }

        public void setIme(String ime) {
            this.ime = ime;
        }

        public LatLng getLokacija() {
            return lokacija;
        }

        public void setLokacija(LatLng lokacija) {
            this.lokacija = lokacija;
        }
    }

    List<String> allCities = Arrays.asList("beograd","novi sad", "niš","kragujevac","priština","subotica",
            "zrenjanin","pančevo","čačak","kruševac","kraljevo","novi pazar","smederevo","leskovac","užice",
            "vranje","valjevo","šabac","sombor","požarevac","pirot","zaječar","kikinda","sremska mitrovica",
            "jagodina","vršac","bor","prokuplje","loznica","zlatibor","zlatar","tara","golija","rtanj",
            "vrnjačka banja","niška banja","sokobanja","prolom banja","ribarska banja","rudnik","kopaonik",
            "aleksinac","knjaževac","ćuprija","paraćin","arandjelovac","lazarevac");

    List<String> allCitiesC = Arrays.asList("Beograd","Novi Sad", "Niš","Kragujevac","Priština","Subotica",
            "Zrenjanin","Pančevo","Čačak","Kruševac","Kraljevo","Novi Pazar","Smederevo","Leskovac","Užice",
            "Vranje","Valjevo","Šabac","Sombor","Požarevac","Pirot","Zaječar","Kikinda","Sremska Mitrovica",
            "Jagodina","Vršac","Bor","Prokuplje","Loznica","Zlatibor","Zlatar","Tara","Golija","Rtanj",
            "Vrnjačka Banja","Niška Banja","Sokobanja","Prolom Banja","Ribarska Banja","Rudnik","Kopaonik",
            "Aleksinac","Knjaževac","Ćuprija","Paraćin","Arandjelovac","Lazarevac");

    Trie trie;

    public Gradovi(){
        trie = new Trie();
        trie.Insert(allCities);
    }

    public List<String> getAllCities() { return allCities; }

    public List<String> getAllCitiesC() { return allCitiesC; }

    public List<String> Search(String inputText){
        return trie.Search(inputText.toLowerCase());
    }


}
