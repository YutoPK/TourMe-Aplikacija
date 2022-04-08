package com.example.tourme.Model;

import java.util.ArrayList;
import java.util.List;

public class Node {

    Node[] letters;
    List<String> cities;

    public Node(){
        letters = new Node[32];
        cities = new ArrayList<>();
    }

    public void addToList(String city){
        cities.add(city);
    }

    public int getPosition(char c){
        int position = (int)(c - 'a');
        switch(c){
            case ' ': position = 26; break;
            case 'š': position = 27; break;
            case 'đ': position = 28; break;
            case 'č': position = 29; break;
            case 'ć': position = 30; break;
            case 'ž': position = 31; break;
        }
        return position;
    }

    public void Insert(String s, int index){
        if(index == s.length())
            return;
        char c = s.charAt(index);
        int position = getPosition(c);

        if(letters[position] == null)
            letters[position] = new Node();

        letters[position].addToList(s);
        letters[position].Insert(s, index + 1);
    }

    public List<String> Search(String inputText, int index){
        if(index == inputText.length())
            return cities;
        char c = inputText.charAt(index);
        int position = getPosition(c);

        if(letters[position] == null)
            letters[position] = new Node();

        return letters[position].Search(inputText, index + 1);

    }
}
