package com.github.gblokker.scraper;

import com.github.gblokker.classes.Cyclist;
import com.github.gblokker.classes.Race;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Map;

public class CyclistScraper extends FindElement {
    public Cyclist scrapeCyclistData(String cyclistName, int year) throws IOException {
        return new Cyclist(cyclistName, year, parseCyclistRaces(cyclistName, year));
    }

    private Map<String, Race> parseCyclistRaces(String cyclistName, int year) throws IOException {
        Document cyclistResults = Jsoup.connect(String.format("https://www.procyclingstats.com/rider/%s/%d", cyclistName, year))
                .userAgent("Mozilla/5.0 (compatible; pcs-scraper/1.0)")
                .timeout(15000)
                .get();

        Map<String, Race> cyclistRaces = new java.util.HashMap<>();
        RaceScraper raceScraper = new RaceScraper();

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
                    Race race = raceScraper.scrapeRaceData(raceName, Integer.parseInt(raceYear), true, stage);
                    String position = cells.get(1).text();
                    String uniqueKey = position + "/" + raceName + "/" + raceYear + "/" + stage;
                    System.out.println(uniqueKey);
                    cyclistRaces.put(uniqueKey, race);
                }
            }
        }

        return cyclistRaces;
    }
}