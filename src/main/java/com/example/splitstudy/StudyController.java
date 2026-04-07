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
import java.util.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") 
public class StudyController {

    @Autowired
    private RestTemplate restTemplate;
    
    private final String API_KEY = "API_KEY";
    private final String CX_ID = "API KEY";
    private final String VIDEO_CX_ID = "API KEY"; 

    @GetMapping("/summarize")
    public Map<String, Object> getStudyMaterials(@RequestParam String topic) {
        // 1. Fetch Academic Notes
        String academicUrl = "https://www.googleapis.com/customsearch/v1?key=" + API_KEY + "&cx=" + CX_ID + "&q=" + topic + " site:w3schools.com OR site:geeksforgeeks.org";     
        String academicResponse = restTemplate.getForObject(academicUrl, String.class);
        String formattedNotes = formatNotes(academicResponse, topic);

        // 2. Fetch Multiple YouTube Videos
        String videoUrl = "https://www.googleapis.com/customsearch/v1?key=" + API_KEY + "&cx=" + VIDEO_CX_ID + "&q=" + topic + " tutorial";
        String videoResponse = restTemplate.getForObject(videoUrl, String.class);
        
        List<Map<String, String>> videoList = new ArrayList<>();
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
                    videoData.put("videoId", videoId); // Fix: use put()
                    videoData.put("title", title);     // Fix: use put()
                    videoList.add(videoData);          
                }
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("notes", formattedNotes);
        response.put("videos", videoList); 
        return response;
    }

    private String extractVideoId(String link) {
        if (link.contains("v=")) {
            String tempId = link.split("v=")[1];
            return tempId.contains("&") ? tempId.split("&")[0] : tempId;
        }
        return "video_not_found";
    }

    private String formatNotes(String jsonResponse, String topic) {
        JSONObject json = new JSONObject(jsonResponse);
        if (!json.has("items")) return "<p>No results found.</p>";
        
        JSONArray items = json.getJSONArray("items");
        StringBuilder html = new StringBuilder();

        try {
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

            html.append("<div style='font-family: Arial, sans-serif; padding: 15px; line-height: 1.6;'>");
            html.append("<h2 style='color: #2c3e50; border-bottom: 2px solid #3498db;'>Understanding: ").append(topic).append("</h2>");
            html.append("<p style='font-size: 0.8em; color: #7f8c8d;'>Easy explanation via ").append(sourceName).append("</p>");
            html.append("<h3 style='color: #2980b9;'>What is it?</h3>");
            html.append("<p style='text-align: justify; background: #fdfefe; padding: 10px; border-left: 4px solid #3498db;'>").append(intro).append("</p>");
            html.append("<h3 style='color: #2980b9;'>How it works</h3>");
            html.append("<p style='text-align: justify;'>").append(details).append("</p>");
            html.append("<div style='margin-top: 20px; padding: 10px; background: #e8f4fd; border-radius: 5px;'><b>Quick Tip:</b> Practice with examples!</div>");

        } catch (Exception e) {
            html.append("<p>Could not fetch full explanation.</p>");
        }

        html.append("<hr><h4>Further Easy Reading:</h4><ul>");
        for (int i = 0; i < items.length(); i++) {
            html.append("<li><a href='").append(items.getJSONObject(i).getString("link")).append("' target='_blank' style='color: #3498db;'>").append(items.getJSONObject(i).getString("title")).append("</a></li>");
        }
        html.append("</ul></div>");

        return html.toString();
    }
}
