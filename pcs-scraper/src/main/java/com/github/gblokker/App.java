package com.github.gblokker;

import com.github.gblokker.classes.Cyclist;
import com.github.gblokker.classes.Race;
import com.github.gblokker.scraper.CyclistScraper;

import java.io.IOException;
import java.util.Map;
/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) {
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
