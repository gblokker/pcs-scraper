package com.github.gblokker.scraper;

import com.github.gblokker.classes.Race;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;

public class RaceScraperTest {
    
    private RaceScraper raceScraper;
    
    // Runs before each test
    @BeforeEach
    public void setUp() {
        raceScraper = new RaceScraper();
    }
    
    // Runs after each test
    @AfterEach
    public void tearDown() {
        raceScraper = null;
    }
    
    // Test method - must be annotated with @Test
    @Test
    public void testIsRaceGC_ShouldReturnTrue() {
        Boolean result = raceScraper.isRaceGC("tour-de-france", 2024);
        assertTrue(result, "Tour de France should be a GC race");
    }
    
    @Test
    public void testIsRaceGC_ShouldReturnFalse() {
        Boolean result = raceScraper.isRaceGC("milano-sanremo", 2024);
        assertFalse(result, "Milano-Sanremo should not be a GC race");
    }
    
    @Test
    public void testScrapeRaceData_ValidRace() throws IOException {
        Race race = raceScraper.scrapeRaceData("strade-bianche", 2024, false, "result");
        
        assertNotNull(race, "Race should not be null");
        assertEquals("strade-bianche", race.name);
        assertEquals(2024, race.year);
        assertTrue(race.participants.isEmpty(), "Participants map should be empty");
    }
    
    @Test
    public void testScrapeRaceData_ValidRace_includeParticipantsTrue() throws IOException {
        Race race = raceScraper.scrapeRaceData("strade-bianche", 2024, true, "result");
        
        assertNotNull(race, "Race should not be null");
        assertEquals("strade-bianche", race.name);
        assertEquals(2024, race.year);
        assertNotNull(race.participants, "Participants should not be null");
        assertFalse(race.participants.isEmpty(), "Participants map should not be empty");
    }

    @Test
    public void testScrapeRaceData_InvalidRace_ShouldThrowException() {
        assertThrows(IOException.class, () -> {
            raceScraper.scrapeRaceData("invalid-race", 2024, false, "result");
        });
    }
}