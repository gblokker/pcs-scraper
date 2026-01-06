package com.github.gblokker.scraper;

import com.github.gblokker.classes.Race;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class RaceScraper extends FindElement {

    public Map<String, Race> getAllRacesPerYear(int year, String type, Boolean includeParticipants) throws IOException {
        int circuit = 1;
        if (type.equals("worldtour")) {
            circuit = 1;
        } else if (type.equals("proseries")) {
            circuit = 26;
        } else {
            throw new IOException("Invalid race type: " + type + ". Use 'worldtour' or 'proseries'.");
        }

        Document racesPage = Jsoup.connect(String.format("https://www.procyclingstats.com/races.php?year=%d&circuit=%d&class=&filter=Filter", year, circuit))
                .userAgent("Mozilla/5.0 (compatible; pcs-scraper/1.0)")
                .timeout(30000)
                .get();

        // Find table with class "basic" (handles leading/trailing spaces)
        Element racesTable = null;
        Elements tables = racesPage.select("table");
        for (Element table : tables) {
            if (table.hasClass("basic")) {
                racesTable = table;
                break;
            }
        }

        if (racesTable == null) {
            throw new IOException("Races table not found!");
        }

        Elements rows = racesTable.select("tbody tr");

        int threadCount = Runtime.getRuntime().availableProcessors() * 2; // high number of threads since mostly network tasks
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        List<Future<?>> futures = new ArrayList<>();

        Map<String, Race> races = new ConcurrentHashMap<>();

        for (Element row : rows) {
            Element raceLink = row.selectFirst("td a[href^=race/]");

            if (raceLink != null) {
                String href = raceLink.attr("href");
                String raceName = href.split("/")[1];
                Boolean isGC = isRaceGC(raceName, year);
                if (isGC) {
                    Map<String, String> stages = getAllStages(raceName, year);
                    for (String stage : stages.keySet()) {
                        Future<?> future = executor.submit(() -> {
                            try {
                                races.put(raceName, scrapeRaceData(raceName, year, includeParticipants, stage));
                            } catch (IOException e) {
                                System.err.println("Error scraping " + raceName + " " + stage + " " + year + ": " + e.getMessage());
                            }
                        });
                        futures.add(future);
                    }
                } else {
                    Future<?> future = executor.submit(() -> {
                        try {
                            races.put(raceName, scrapeRaceData(raceName, year, includeParticipants, ""));
                        } catch (IOException e) {
                            System.err.println("Error scraping " + raceName + " " + year + ": " + e.getMessage());
                        }
                    });
                    futures.add(future);
                }
            }
        }

        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (Exception e) {
                System.err.println("Thread execution error: " + e.getMessage());
            }
        }

        executor.shutdown();

        return races;
    }

    private Map<String, String> getAllStages(String raceName, int year) throws IOException {
        Document racePage = Jsoup.connect(String.format("https://www.procyclingstats.com/race/%s/%d", raceName, year))
                .userAgent("Mozilla/5.0 (compatible; pcs-scraper/1.0)")
                .timeout(30000)
                .get();

        Map<String, String> stages = new LinkedHashMap<>();

        Element stagesTable = null;
        Elements tables = racePage.select("table");
        for (Element table : tables) {
            if (table.hasClass("basic")) {
                // Check if this is the stages table by looking for parent <h4>Stages</h4>
                Element prev = table.previousElementSibling();
                while (prev != null && !prev.tagName().equals("h4")) {
                    prev = prev.previousElementSibling();
                }
                if (prev != null && prev.text().contains("Stages")) {
                    stagesTable = table;
                    break;
                }
            }
        }

        if (stagesTable != null) {
            Elements rows = stagesTable.select("tbody tr");
            for (Element row : rows) {
                Element stageLink = row.selectFirst("a[href^=race/]");
                if (stageLink != null) {
                    String href = stageLink.attr("href");
                    String[] parts = href.split("/");
                    if (parts.length > 0) {
                        String stageId = parts[parts.length - 1];
                        stages.put(stageId, href);
                    }
                }
            }
        }

        // Add "gc" as default if not already present
        if (!stages.containsKey("gc")) {
            stages.put("gc", String.format("race/%s/%d/gc", raceName, year));
        }

        return stages;
    }

    public Race scrapeRaceData(String raceName, int year, boolean includeParticipants, String stage) throws IOException {
        boolean isGC = isRaceGC(raceName, year);
        if (!isGC) {
            stage = "";
        }
        Map<String, String> raceData = parseBaseRaceInfo(raceName, year, stage);
        Map<String, String> raceProfile = parseStageProfile(raceName, year, stage);

        Map<String, String> raceResults;
        Map<String, String> raceParticipants;

        if (includeParticipants) {
            raceResults = parseRaceResult(raceName, year, stage);
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
            raceProfile.get("PS final 25k"),
            stage
        );

        return allData;
    }

    public Boolean isRaceGC(String raceName, int year) {
        try {
            Jsoup.connect(String.format("https://www.procyclingstats.com/race/%s/%d/result", raceName, year))
                .userAgent("Mozilla/5.0 (compatible; pcs-scraper/1.0)")
                .timeout(30000)
                .get();
            return false;
        }
        catch (IOException e) {
            return true;
        }
    }

    private Map<String, String> parseBaseRaceInfo(String raceName, int year, String stage) throws IOException {
        Document race = Jsoup.connect(String.format("https://www.procyclingstats.com/race/%s/%d/%s", raceName, year, stage))
                .userAgent("Mozilla/5.0 (compatible; pcs-scraper/1.0)")
                .timeout(30000)
                .get();

        Map<String, String> meta = new HashMap<>();

        addIfFound(race, meta, "Startdate");
        addIfFound(race, meta, "Enddate");
        addIfFound(race, meta, "Classification");
        addIfFound(race, meta, "Category");
        addIfFound(race, meta, "Total Distance");

        return meta;
    }

    private Map<String, String> parseRaceResult(String raceName, int year, String stage) throws IOException {
        String url = String.format("https://www.procyclingstats.com/race/%s/%d/%s/result", raceName, year, stage);
        if (stage.equals("")){
            url = String.format("https://www.procyclingstats.com/race/%s/%d/result", raceName, year);
        }
        Document raceResults = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (compatible; pcs-scraper/1.0)")
                .timeout(30000)
                .get();

        Map<String, String> results = new LinkedHashMap<>();

        // Select results table based on whether it's a stage or overall race
        Element resultsTable;
        if (!stage.equals("")) {
            // For stages, select only the general results table from resultsCont
            resultsTable = raceResults.selectFirst("div#resultsCont div.general table.results");
        } else {
            // For overall race results, select the first results table
            resultsTable = raceResults.selectFirst("table.results");
        }
        
        if (resultsTable == null) {
            throw new IOException("Results table not found!");
        }
        
        Elements rows = resultsTable.select("tbody tr");
        
        for (Element row : rows) {
            Elements cells = row.select("td");

            String rank = cells.get(0).text().trim();
            Element nameCell = row.selectFirst("td.ridername a");
            
            String riderName = "";

            if (nameCell != null) {
                riderName = nameCell.text().trim();
            } else {
                continue; // Skip if no name found
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
                .timeout(30000)
                .get();

        Map<String, String> participants = new LinkedHashMap<>();

        Element startlist = raceParticipants.selectFirst("ul.startlist_v4");
        
        if (startlist == null) {
            throw new IOException("Startlist not found!");
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

    private Map<String, String> parseStageProfile(String raceName, int year, String stage) throws IOException {
        String url = String.format("https://www.procyclingstats.com/race/%s/%d/%s/route/stage-profiles", raceName, year, stage);
        if (stage.equals("")){
            url = String.format("https://www.procyclingstats.com/race/%s/%d/route/stage-profiles", raceName, year);
        }
        Document raceProfile = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (compatible; pcs-scraper/1.0)")
                .timeout(30000)
                .get();

        Map<String, String> raceProfileData = new HashMap<>();

        addIfFound(raceProfile, raceProfileData, "Vertical meters");
        addIfFound(raceProfile, raceProfileData, "ProfileScore");
        addIfFound(raceProfile, raceProfileData, "PS final 25k");
        return raceProfileData;
    }
}