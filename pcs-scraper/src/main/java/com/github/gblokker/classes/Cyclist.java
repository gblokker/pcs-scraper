package com.github.gblokker.classes;

import java.util.List;

public class Cyclist {
    public final String name;
    public List<String> races;

    public Cyclist(String name, List<String> races) {
        this.name = name;
        this.races = races;
    }
}