package com.example.tourme.Model;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class StaticVars {
    public static String lastSearch = "";

    public static List<Integer> listOfFragments = new ArrayList<>();

    public static boolean isPoruke = false;

    public static String convertMonth(String m){
        switch (m){
            case "Januar": return "01";
            case "Februar": return "02";
            case "Mart": return "03";
            case "April": return "04";
            case "Maj": return "05";
            case "Jun": return "06";
            case "Jul": return "07";
            case "Avgust": return "08";
            case "Septembar": return "09";
            case "Oktobar": return "10";
            case "Novembar": return "11";
            default: return "12";
        }
    }

    public static int numberOfYears(String d1, String m1, String g1, String d2, String m2, String g2){
        int d1Int = Integer.parseInt(d1);
        int m1Int = Integer.parseInt(m1);
        int g1Int = Integer.parseInt(g1);
        int d2Int = Integer.parseInt(d2);
        int m2Int = Integer.parseInt(m2);
        int g2Int = Integer.parseInt(g2);

        int res = Math.max(g2Int - g1Int - 1, 0);
        if(m1Int < m2Int) res++;
        else if(m1Int == m2Int){
            if(d2Int >= d1Int) res++;
        }
        return res;
    }

    public static String getLastSearch() { return lastSearch; }

    public static void setLastSearch(String lastSearch) { StaticVars.lastSearch = lastSearch; }

    public static List<Integer> getListOfFragments() { return listOfFragments; }

    public static void setListOfFragments(List<Integer> listOfFragments) { StaticVars.listOfFragments = listOfFragments; }
}
