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
            if (cells.size() > 6) {
                String raceName = cells.get(4).text();
                Race race = raceScraper.scrapeRaceData(raceName, year, true);
                String position = cells.get(1).text();
                cyclistRaces.put(position, race);
            }
        }

        return cyclistRaces;
    }
}