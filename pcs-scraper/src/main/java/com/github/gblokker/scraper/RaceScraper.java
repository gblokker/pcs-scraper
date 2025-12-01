package com.github.gblokker.scraper;

import com.github.gblokker.classes.Race;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class RaceScraper extends FindElement {

    public Race scrapeRaceData(String raceName, int year, boolean includeParticipants) throws IOException {
        Map<String, String> raceData = parseBaseRaceInfo(raceName, year);
        Map<String, String> raceProfile = parseStrageProfile(raceName, year);

        Map<String, String> raceResults;
        Map<String, String> raceParticipants;

        if (includeParticipants) {
            raceResults = parseRaceResult(raceName, year);
            raceParticipants = parseRaceParticipants(raceName, year);
        } else {
            raceResults = new LinkedHashMap<>();
            raceParticipants = new LinkedHashMap<>();
        }
            
        Race allData = new Race(
            raceName,
            year,
            raceData.get("Startdate"),
            raceData.get("Enddate"),
            raceData.get("Classification"),
            raceData.get("Category"),
            raceData.get("Total Distance"),
            raceResults,
            raceParticipants,
            raceProfile.get("Vertical meters"),
            raceProfile.get("ProfileScore"),
            raceProfile.get("PS final 25k")
        );

        return allData;
    }

    private Map<String, String> parseBaseRaceInfo(String raceName, int year) throws IOException {
        Document race = Jsoup.connect(String.format("https://www.procyclingstats.com/race/%s/%d/", raceName, year))
                .userAgent("Mozilla/5.0 (compatible; pcs-scraper/1.0)")
                .timeout(15000)
                .get();

        Map<String, String> meta = new HashMap<>();

        addIfFound(race, meta, "Startdate");
        addIfFound(race, meta, "Enddate");
        addIfFound(race, meta, "Classification");
        addIfFound(race, meta, "Category");
        addIfFound(race, meta, "Total Distance");

        return meta;
    }

    private Map<String, String> parseRaceResult(String raceName, int year) throws IOException {
        Document raceResults = Jsoup.connect(String.format("https://www.procyclingstats.com/race/%s/%d/result", raceName, year))
                .userAgent("Mozilla/5.0 (compatible; pcs-scraper/1.0)")
                .timeout(15000)
                .get();

        Map<String, String> results = new LinkedHashMap<>();

        // Select table rows
        Elements rows = raceResults.select("table tbody tr");
        
        for (Element row : rows) {
            // Get all td cells in the row
            Elements cells = row.select("td");
            
            if (cells.size() < 6) continue;
            
            String rank = cells.get(0).text().trim();
            
            // Get rider name from 6th cell (index 5)
            Element nameCell = cells.get(5);
            Element riderLink = nameCell.selectFirst("a");
            String riderName = "";
            
            if (riderLink != null) {
                riderName = riderLink.text().trim();
            } else {
                riderName = nameCell.text().trim();
            }
            
            if (!rank.isEmpty() && !riderName.isEmpty()) {
                results.put(rank, riderName);
            }
        }
        return results;
    }

    private Map<String, String> parseRaceParticipants(String raceName, int year) throws IOException {
        Document raceParticipants = Jsoup.connect(String.format("https://www.procyclingstats.com/race/%s/%d/startlist", raceName, year))
                .userAgent("Mozilla/5.0 (compatible; pcs-scraper/1.0)")
                .timeout(15000)
                .get();

        Map<String, String> participants = new LinkedHashMap<>();

        Element startlist = raceParticipants.selectFirst("ul.startlist_v4");
        
        if (startlist == null) {
            System.out.println("No startlist found!");
            return participants;
        }

        Elements teamBlocks = startlist.select("> li");

        for (Element teamBlock : teamBlocks) {
            Elements riderRows = teamBlock.select("div.ridersCont > ul > li");
            
            for (Element riderRow : riderRows) {
                Element bibElem = riderRow.selectFirst("span.bib");
                String bibNumber = bibElem != null ? bibElem.text().trim() : "";
                
                Elements links = riderRow.select("a");
                String riderName = "";
                
                if (links.size() >= 1) {
                    riderName = links.last().text().trim();
                }
                
                if (!bibNumber.isEmpty() && !riderName.isEmpty()) {
                    participants.put(bibNumber, riderName);
                }
            }
        }

        return participants;
    }

    private Map<String, String> parseStrageProfile(String raceName, int year) throws IOException {
        Document raceProfile = Jsoup.connect(String.format("https://www.procyclingstats.com/race/%s/%d/route/stage-profiles", raceName, year))
                .userAgent("Mozilla/5.0 (compatible; pcs-scraper/1.0)")
                .timeout(15000)
                .get();

        Map<String, String> raceProfileData = new HashMap<>();

        addIfFound(raceProfile, raceProfileData, "Vertical meters");
        addIfFound(raceProfile, raceProfileData, "ProfileScore");
        addIfFound(raceProfile, raceProfileData, "PS final 25k");
        return raceProfileData;
    }
}