package com.github.gblokker.scraper;

import com.github.gblokker.classes.Cyclist;
import com.github.gblokker.classes.Race;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.Map;

public class CyclistScraperTest {
    
    private CyclistScraper cyclistScraper;
    
    @BeforeEach
    public void setUp() {
        cyclistScraper = new CyclistScraper();
    }
    
    @AfterEach
    public void tearDown() {
        cyclistScraper = null;
    }
    
    @Test
    public void testScrapeCyclistData_ValidCyclist() throws IOException {
        Cyclist cyclist = cyclistScraper.scrapeCyclistData("tadej-pogacar", 2024);
        
        assertNotNull(cyclist, "Cyclist should not be null");
        assertEquals("tadej-pogacar", cyclist.name);
        assertEquals(2024, cyclist.year);
        assertNotNull(cyclist.races, "Races map should not be null");
        assertFalse(cyclist.races.isEmpty(), "Races map should not be empty");
        Map<String, Race> races = cyclist.races;        
        boolean hasValidKey = races.keySet().stream()
            .anyMatch(key -> key.contains("tour-de-france") || key.contains("giro-d-italia"));
        assertTrue(hasValidKey, "Should contain at least one grand tour");
    }
    
    @Test
    public void testScrapeCyclistData_InvalidCyclist_ShouldThrowException() {
        assertThrows(IOException.class, () -> {
            cyclistScraper.scrapeCyclistData("invalid-cyclist-name-12345", 2024);
        });
    }
}
