package com.github.gblokker;

import com.github.gblokker.classes.Cyclist;
import com.github.gblokker.classes.Race;
import com.github.gblokker.scraper.RaceScraper;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.io.IOException;
/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) {
        RaceScraper scraper = new RaceScraper();
        try {
            Race raceData = scraper.scrapeRaceData("ronde-van-vlaanderen", 2025);
            System.out.println("Race Data:");
            try {
                for (java.lang.reflect.Field field : raceData.getClass().getDeclaredFields()) {
                    field.setAccessible(true);
                    Object value = field.get(raceData);
                    if (value instanceof Iterable) {
                        System.out.println(field.getName() + ":");
                        for (Object item : (Iterable<?>) value) {
                            System.out.println("  - " + item);
                        }
                    } else if (value instanceof java.util.Map) {
                        System.out.println(field.getName() + ": " + value);
                    } else if (value != null && value.getClass().isArray()) {
                        int len = java.lang.reflect.Array.getLength(value);
                        System.out.println(field.getName() + ":");
                        for (int i = 0; i < len; i++) {
                            System.out.println("  - " + java.lang.reflect.Array.get(value, i));
                        }
                    } else {
                        System.out.println(field.getName() + ": " + value);
                    }
                }
            } catch (IllegalAccessException e) {
                System.err.println("Reflection error: " + e.getMessage());
            }
        } catch (IOException e) {
            System.err.println("Error scraping race: " + e.getMessage());
        }
    }
}
