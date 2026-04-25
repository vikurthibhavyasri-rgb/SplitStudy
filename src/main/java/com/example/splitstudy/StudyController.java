package com.example.splitstudy;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.json.JSONObject;
import org.json.JSONArray;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import java.util.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") 
public class StudyController {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private FirestoreService firestoreService;
    
    @Value("${google.search.api.key}")
    private String searchApiKey;

    private final String CX_ID = "86274116fd8654208";
    private final String VIDEO_CX_ID = "c08ceb4557a2b4032"; 

    @GetMapping("/summarize")
    public Map<String, Object> getStudyMaterials(@RequestParam String topic, @RequestParam String username) {
        Map<String, Object> response = new HashMap<>();
        List<Map<String, String>> videoList = new ArrayList<>();
        String formattedNotes = "<p>No data found.</p>";

        // Save to Firestore history
        firestoreService.saveSearch(username, topic);

        try {
            // 1. Fetch Academic Notes
            String academicUrl = "https://www.googleapis.com/customsearch/v1?key=" + searchApiKey + "&cx=" + CX_ID + "&q=" + topic;     
            String academicResponse = restTemplate.getForObject(academicUrl, String.class);
            formattedNotes = formatNotes(academicResponse, topic);

            // 2. Fetch Multiple YouTube Videos
            String videoUrl = "https://www.googleapis.com/customsearch/v1?key=" + searchApiKey + "&cx=" + VIDEO_CX_ID + "&q=" + topic + " tutorial";
            String videoResponse = restTemplate.getForObject(videoUrl, String.class);
            
            if (videoResponse != null) {
                JSONObject videoJson = new JSONObject(videoResponse);
                if (videoJson.has("items")) {
                    JSONArray items = videoJson.getJSONArray("items");
                    for (int i = 0; i < Math.min(items.length(), 5); i++) {
                        JSONObject item = items.getJSONObject(i);
                        String link = item.getString("link");
                        String title = item.getString("title");
                        String videoId = extractVideoId(link);
                        
                        if (!videoId.equals("video_not_found")) {
                            Map<String, String> videoData = new HashMap<>();
                            videoData.put("videoId", videoId);
                            videoData.put("title", title);
                            videoList.add(videoData);          
                        }
                    }
                }
            }
        } catch (Exception e) {
            formattedNotes = "<h2>Search Error</h2><p>Backend issue reaching Google Services. Topic saved to history.</p>";
        }

        response.put("notes", formattedNotes);
        response.put("videos", videoList); 
        return response;
    }

    @GetMapping("/history")
    public List<Map<String, Object>> getSearchHistory(@RequestParam String username) {
        try {
            return firestoreService.getHistory(username);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private String extractVideoId(String link) {
        if (link.contains("v=")) {
            String tempId = link.split("v=")[1];
            return tempId.contains("&") ? tempId.split("&")[0] : tempId;
        }
        return "video_not_found";
    }

    private String formatNotes(String jsonResponse, String topic) {
        if (jsonResponse == null || jsonResponse.isEmpty()) return "<p>No response from search API.</p>";
        try {
            JSONObject json = new JSONObject(jsonResponse);
            if (!json.has("items")) return "<p>No academic results found for this topic.</p>";
            
            JSONArray items = json.getJSONArray("items");
            StringBuilder html = new StringBuilder();

            JSONObject topSource = items.getJSONObject(0);
            String sourceUrl = topSource.getString("link");
            String sourceName = topSource.getString("displayLink");
            
            Document doc = Jsoup.connect(sourceUrl).timeout(5000).get();
            Elements paragraphs = doc.select("p");
            
            StringBuilder intro = new StringBuilder();
            StringBuilder details = new StringBuilder();

            int pCount = 0;
            for (Element p : paragraphs) {
                String text = p.text();
                if (text.length() > 50) {
                    if (pCount < 2) intro.append(text).append(" ");
                    else if (pCount < 8) details.append(text).append(" ");
                    pCount++;
                }
            }

            html.append("<div style='padding: 10px;'>");
            html.append("<h1 style='font-weight: 800; font-size: 2.5rem; margin-bottom: 0.5rem;'>").append(topic).append("</h1>");
            html.append("<p style='font-size: 0.8rem; color: #7f8c8d; margin-bottom: 2rem;'>Sourced via ").append(sourceName).append("</p>");
            html.append("<h3 style='margin-bottom: 1rem; color: #3498db;'>Introduction</h3>");
            html.append("<p style='margin-bottom: 2rem; opacity: 0.9;'>").append(intro).append("</p>");
            html.append("<h3 style='margin-bottom: 1rem; color: #3498db;'>Core Concepts</h3>");
            html.append("<p style='margin-bottom: 2rem; opacity: 0.9;'>").append(details).append("</p>");

            html.append("<h4 style='margin-top: 3rem;'>Further Exploration</h4><ul style='margin-top: 1rem; list-style: none;'>");
            for (int i = 0; i < Math.min(items.length(), 4); i++) {
                html.append("<li style='margin-bottom: 0.5rem;'><a href='").append(items.getJSONObject(i).getString("link")).append("' target='_blank' style='color: #3498db; text-decoration: none;'>→ ").append(items.getJSONObject(i).getString("title")).append("</a></li>");
            }
            html.append("</ul></div>");

            return html.toString();
        } catch (Exception e) {
            return "<h2>Summary Unavailable</h2><p>Results found but summary extraction failed.</p>";
        }
    }
}
