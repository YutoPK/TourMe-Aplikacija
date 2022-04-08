package com.example.tourme.Model;

import java.util.List;

public class Trie {
    public Node node;

    public Trie() {
        node = new Node();
    }

    public void Insert(List<String> cities){
        for(String f : cities)
            node.Insert(f.toLowerCase(), 0);
    }

    public List<String> Search(String inputText){
        return node.Search(inputText, 0);
    }
}
