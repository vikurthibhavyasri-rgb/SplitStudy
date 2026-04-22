package com.example.splitstudy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.HttpEntity;
import org.json.JSONObject;
import org.json.JSONArray;
import java.util.*;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatController {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private FirestoreService firestoreService;

    @Value("${gemini.api.key}")
    private String apiKey;

    private String getGeminiUrl() {
        return "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + apiKey;
    }

    @PostMapping
    public Map<String, Object> chat(@RequestBody Map<String, String> request) {
        String userMessage = request.get("message");
        String username = request.get("username");
        
        // 1. Save user message to Firestore
        firestoreService.saveChatMessage(username, "user", userMessage);

        // 2. Call Gemini API
        String botResponse = "I'm sorry, I couldn't connect to the AI right now.";
        try {
            JSONObject body = new JSONObject();
            JSONArray contents = new JSONArray();
            JSONObject content = new JSONObject();
            JSONArray parts = new JSONArray();
            JSONObject part = new JSONObject();
            
            part.put("text", "You are a helpful study assistant for SpiltStudy. Answer the following: " + userMessage);
            parts.put(part);
            content.put("parts", parts);
            contents.put(content);
            body.put("contents", contents);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(body.toString(), headers);

            String response = restTemplate.postForObject(getGeminiUrl(), entity, String.class);
            JSONObject jsonResponse = new JSONObject(response);
            
            if (jsonResponse.has("candidates")) {
                botResponse = jsonResponse.getJSONArray("candidates")
                        .getJSONObject(0)
                        .getJSONObject("content")
                        .getJSONArray("parts")
                        .getJSONObject(0)
                        .getString("text");
            }
        } catch (Exception e) {
            botResponse = "AI Error: " + e.getMessage();
        }

        // 3. Save bot response to Firestore
        firestoreService.saveChatMessage(username, "bot", botResponse);

        Map<String, Object> result = new HashMap<>();
        result.put("response", botResponse);
        return result;
    }

    @GetMapping("/history")
    public List<Map<String, Object>> getHistory(@RequestParam String username) {
        try {
            return firestoreService.getChatHistory(username);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}
