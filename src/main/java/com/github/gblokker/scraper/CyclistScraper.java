package com.github.gblokker.scraper;

import com.github.gblokker.classes.Cyclist;
import com.github.gblokker.classes.Race;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class CyclistScraper extends FindElement {
    public Cyclist scrapeCyclistData(String cyclistName, int year) throws IOException {
        return new Cyclist(cyclistName, year, parseCyclistRaces(cyclistName, year));
    }

    private Map<String, Race> parseCyclistRaces(String cyclistName, int year) throws IOException {
        Document cyclistResults = Jsoup.connect(String.format("https://www.procyclingstats.com/rider/%s/%d", cyclistName, year))
                .userAgent("Mozilla/5.0 (compatible; pcs-scraper/1.0)")
                .timeout(30000)
                .get();

        // Check if cyclist exists by looking for error messages or empty table
        String pageText = cyclistResults.text().toLowerCase();
        if (pageText.contains("page not found") ||
            cyclistResults.select("table tbody tr").isEmpty()) {
            throw new IOException("Cyclist not found: " + cyclistName + " for year " + year);
        }

        // Use ConcurrentHashMap for thread safety
        Map<String, Race> cyclistRaces = new ConcurrentHashMap<>();
        RaceScraper raceScraper = new RaceScraper();
        
        int threadCount = Runtime.getRuntime().availableProcessors() * 2; // high number of threads since mostly network tasks
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        List<Future<?>> futures = new ArrayList<>();

        Elements rows = cyclistResults.select("table tbody tr");

        for (Element row : rows) {
            Elements cells = row.select("td");
            Element link = row.selectFirst("td a[href]");
            String href = null;
            if (link != null) {
                href = link.attr("href");
            }
            if (cells.size() > 6 && href != null) {
                String[] parts = href.split("/");
                String raceName = parts[1];
                String raceYear = parts[2];
                String stage = parts[parts.length - 1];
                
                if (stage.equals("gc") || (stage.startsWith("stage-") && stage.matches("stage-\\d+")) || stage.equals("result")) {
                    String position = cells.get(1).text();
                    String uniqueKey = position + "/" + raceName + "/" + raceYear + "/" + stage;
                    
                    Future<?> future = executor.submit(() -> {
                        try {
                            Race race = raceScraper.scrapeRaceData(raceName, Integer.parseInt(raceYear), true, stage);
                            cyclistRaces.put(uniqueKey, race);
                        } catch (IOException e) {
                            System.err.println("Error scraping " + uniqueKey + ": " + e.getMessage());
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

        // Shutdown executor
        executor.shutdown();

        return cyclistRaces;
    }
}