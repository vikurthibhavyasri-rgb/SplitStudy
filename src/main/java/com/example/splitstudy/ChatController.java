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

    @Value("${groq.api.key}")
    private String apiKey;

    private String getGroqUrl() {
        return "https://api.groq.com/openai/v1/chat/completions";
    }

    @PostMapping
    public Map<String, Object> chat(@RequestBody Map<String, String> request) {
        String userMessage = request.get("message");
        String username = request.get("username");
        
        // 1. Save user message to Firestore
        firestoreService.saveChatMessage(username, "user", userMessage);

        // 2. Call Groq API (OpenAI Compatible)
        String botResponse = "I'm sorry, I couldn't connect to the AI right now.";
        try {
            JSONObject body = new JSONObject();
            body.put("model", "llama3-8b-8192");
            
            JSONArray messages = new JSONArray();
            JSONObject systemMsg = new JSONObject();
            systemMsg.put("role", "system");
            systemMsg.put("content", "You are a helpful study assistant for SpiltStudy.");
            messages.put(systemMsg);
            
            JSONObject userMsg = new JSONObject();
            userMsg.put("role", "user");
            userMsg.put("content", userMessage);
            messages.put(userMsg);
            
            body.put("messages", messages);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);
            
            HttpEntity<String> entity = new HttpEntity<>(body.toString(), headers);

            String response = restTemplate.postForObject(getGroqUrl(), entity, String.class);
            JSONObject jsonResponse = new JSONObject(response);
            
            if (jsonResponse.has("choices")) {
                botResponse = jsonResponse.getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content");
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
