package com.github.gblokker.scraper;

import java.util.Map;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class FindElement {
    public void addIfFound(Document doc, Map<String, String> meta, String key) {
        // Try multiple patterns:
        
        // Pattern 1: <span>Label</span> <span>Value</span>
        Elements adjacent = doc.select(String.format("span:containsOwn(%s) + span", key));
        if (!adjacent.isEmpty()) {
            meta.put(key, adjacent.first().text().trim());
            return;
        }
        
        // Pattern 2: <div class="infolist"><div>Label</div><div>Value</div></div>
        Element parent = doc.selectFirst(String.format("div:containsOwn(%s)", key));
        if (parent != null) {
            Element next = parent.nextElementSibling();
            if (next != null) {
                meta.put(key, next.text().trim());
                return;
            }
        }
        
        // Pattern 3: Label and value in same element, extract after colon
        Element combined = doc.selectFirst(String.format(":containsOwn(%s:)", key));
        if (combined != null) {
            String text = combined.text();
            int colonIdx = text.indexOf(':');
            if (colonIdx > 0 && colonIdx < text.length() - 1) {
                meta.put(key, text.substring(colonIdx + 1).trim());
                return;
            }
        }
    }
}
