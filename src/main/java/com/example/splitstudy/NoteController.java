package com.example.splitstudy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/notes")
@CrossOrigin(origins = "*")
public class NoteController {

    @Autowired
    private FirestoreService firestoreService;

    @PostMapping
    public Map<String, Object> saveNote(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            String username = request.get("username");
            String title = request.get("title");
            String content = request.get("content");
            
            firestoreService.saveNote(username, title, content);
            response.put("success", true);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }

    @GetMapping
    public List<Map<String, Object>> getNotes(@RequestParam String username) {
        try {
            return firestoreService.getNotes(username);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> deleteNote(@PathVariable String id) {
        firestoreService.deleteNote(id);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        return response;
    }
}
