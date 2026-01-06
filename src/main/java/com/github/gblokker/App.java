package com.github.gblokker;

import com.github.gblokker.classes.Cyclist;
import com.github.gblokker.classes.Race;
import com.github.gblokker.scraper.CyclistScraper;
import com.github.gblokker.scraper.RaceScraper;

import java.io.IOException;
import java.util.Map;

public class App 
{
    public static void main( String[] args ) {
        
        RaceScraper raceScraper = new RaceScraper();
        try {
            raceScraper.getAllRacesPerYear(2024, "worldtour", true);
        } catch (IOException e) {
            System.err.println("Error scraping races: " + e.getMessage());
        }

        CyclistScraper cyclistScraper = new CyclistScraper();
        try {
            Cyclist pogi = cyclistScraper.scrapeCyclistData("tadej-pogacar", 2024);
            for (Map.Entry<String, Race> entry : pogi.races.entrySet()) {
                System.out.println("Race: " + entry.getKey());
            }
        } catch (IOException e) {
            System.err.println("Error scraping race: " + e.getMessage());
        }
    }
}
