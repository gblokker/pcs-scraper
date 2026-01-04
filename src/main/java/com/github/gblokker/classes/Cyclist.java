package com.github.gblokker.classes;

import java.util.Map;

public class Cyclist {
    public final String name;
    public final int year;
    public Map<String, Race> races;

    public Cyclist(String name, int year, Map<String, Race> races) {
        this.name = name;
        this.year = year;
        this.races = races;
    }
}