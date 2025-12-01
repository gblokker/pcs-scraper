package com.github.gblokker.classes;

import java.util.List;
import java.util.Map;

public class Race {
    public final String name;
    public final int year;
    public final String startDate;
    public final String endDate;
    public final String classification;
    public final String category;
    public final String totalDistance;
    public final Map<String, String> results;
    public final Map<String, String> participants;
    public final String verticalMeters;
    public final String profileScore;
    public final String psFinal25k;

    public Race(String name, int year, String startDate, String endDate, String classification, String category, String totalDistance, Map<String, String> results, Map<String, String> participants, String verticalMeters, String profileScore, String psFinal25k) {
        this.name = name;
        this.year = year;
        this.startDate = startDate;
        this.endDate = endDate;
        this.classification = classification;
        this.category = category;
        this.totalDistance = totalDistance;
        this.results = results;
        this.participants = participants;
        this.verticalMeters = verticalMeters;
        this.profileScore = profileScore;
        this.psFinal25k = psFinal25k;
    }
}
