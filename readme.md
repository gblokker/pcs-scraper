# ProCycling Predictor

A Java-based web scraper for collecting professional cycling race data from ProcyclingStats.com. I build this as a way to get a bit more comfortable with java, so do not expect perfect code and if you have any improvements please let me know.

## Components

### CyclistScraper
Scrapes cyclist data for a given year, including:
- All races participated in
- Results for each race (stage results, GC positions, one-day race results)
- Threaded scraping for improved performance

**Usage:**
```java
CyclistScraper cyclistScraper = new CyclistScraper();
Cyclist pogacar = cyclistScraper.scrapeCyclistData("tadej-pogacar", 2024);
```

**Race Key Structure:**
The `Cyclist.races` Map uses keys in the format: `position/race-name/year/stage`

Examples:
- `"1/tour-de-france/2024/gc"` - 1st place in Tour de France 2024 GC
- `"1/tour-de-france/2024/stage-15"` - 1st place in stage 15
- `"3/milano-sanremo/2024/result"` - 3rd place in Milano-Sanremo one-day race

### RaceScraper
Scrapes detailed race information, including:
- Race metadata (dates, classification, category, distance)
- Results (stage winners, GC standings)
- Participants list
- Race profile data (vertical meters, profile score)

**Usage:**
```java
RaceScraper raceScraper = new RaceScraper();
Race tdf = raceScraper.scrapeRaceData("tour-de-france", 2024, true, "gc");
Map<String, Race> allRaces = raceScraper.getAllRacesPerYear(2024, "worldtour", true);
```

## Important Notes

**No Input Sanitization**: Cyclist and race names are **not sanitized**. You must use the exact URL format from ProcyclingStats.com:

- Correct: `"tadej-pogacar"`, `"tour-de-france"`, `"milano-sanremo"`
- Wrong: `"Tadej Pogacar"`, `"Tour de France"`, `"Milan-San Remo"`

Check the ProcyclingStats.com URL structure for the correct naming format.

## Example

See `App.java` for a simple example of how to use the CyclistScraper to retrieve and iterate through a cyclist's races.

## Dependencies

- Jsoup (HTML parsing)
- JUnit 5 (testing)

## License

See LICENSE file for details.