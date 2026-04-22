package com.example.splitstudy;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutionException;

@Service
public class FirestoreService {

    @Autowired
    private Firestore firestore;

    // --- USER AUTH ---
    public boolean registerUser(String username, String password) throws ExecutionException, InterruptedException {
        String lowerUsername = username.toLowerCase();
        DocumentReference docRef = firestore.collection("users").document(lowerUsername);
        ApiFuture<DocumentSnapshot> future = docRef.get();
        DocumentSnapshot document = future.get();

        if (document.exists()) return false; // Already exists

        Map<String, Object> data = new HashMap<>();
        data.put("username", lowerUsername);
        data.put("password", password); // In a real app, hash this!
        docRef.set(data);
        return true;
    }

    public boolean loginUser(String username, String password) throws ExecutionException, InterruptedException {
        String lowerUsername = username.toLowerCase();
        DocumentReference docRef = firestore.collection("users").document(lowerUsername);
        DocumentSnapshot document = docRef.get().get();

        if (!document.exists()) return false;
        return password.equals(document.getString("password"));
    }

    // --- NOTES ---
    public void saveNote(String username, String title, String content) throws ExecutionException, InterruptedException {
        String lowerUsername = username.toLowerCase();
        CollectionReference notes = firestore.collection("notes");
        
        // Enforce 10-note limit
        Query query = notes.whereEqualTo("username", lowerUsername);
        QuerySnapshot snapshot = query.get().get();
        if (snapshot.size() >= 10) {
            throw new RuntimeException("Space Exceeded: You can only store up to 10 notes. Please delete an old note to make space.");
        }

        Map<String, Object> data = new HashMap<>();
        data.put("username", lowerUsername);
        data.put("title", title);
        data.put("content", content);
        data.put("createdAt", FieldValue.serverTimestamp());
        notes.add(data);
    }

    public void deleteNote(String noteId) {
        firestore.collection("notes").document(noteId).delete();
    }

    public List<Map<String, Object>> getNotes(String username) throws ExecutionException, InterruptedException {
        String lowerUsername = username.toLowerCase();
        
        // Search for exact match (case-sensitive)
        Query query1 = firestore.collection("notes").whereEqualTo("username", username);
        QuerySnapshot q1 = query1.get().get();
        
        List<Map<String, Object>> notes = new ArrayList<>();
        Set<String> seenIds = new HashSet<>();

        for (QueryDocumentSnapshot doc : q1) {
            Map<String, Object> data = doc.getData();
            data.put("id", doc.getId());
            notes.add(data);
            seenIds.add(doc.getId());
        }

        // Also search for lowercase version if different
        if (!username.equals(lowerUsername)) {
            Query query2 = firestore.collection("notes").whereEqualTo("username", lowerUsername);
            QuerySnapshot q2 = query2.get().get();
            for (QueryDocumentSnapshot doc : q2) {
                if (!seenIds.contains(doc.getId())) {
                    Map<String, Object> data = doc.getData();
                    data.put("id", doc.getId());
                    notes.add(data);
                }
            }
        }
        return notes;
    }

    // --- SEARCH HISTORY ---
    public void saveSearch(String username, String topic) {
        String lowerUsername = username.toLowerCase();
        CollectionReference history = firestore.collection("history");
        Map<String, Object> data = new HashMap<>();
        data.put("username", lowerUsername);
        data.put("topic", topic);
        data.put("timestamp", FieldValue.serverTimestamp());
        history.add(data);
    }

    public List<Map<String, Object>> getHistory(String username) throws ExecutionException, InterruptedException {
        String lowerUsername = username.toLowerCase();
        Query query = firestore.collection("history")
                .whereEqualTo("username", lowerUsername)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(10);
        
        QuerySnapshot querySnapshot = query.get().get();
        List<Map<String, Object>> history = new ArrayList<>();
        for (QueryDocumentSnapshot doc : querySnapshot) {
            history.add(doc.getData());
        }
        return history;
    }

    // --- CHAT HISTORY ---
    public void saveChatMessage(String username, String sender, String content) {
        String lowerUsername = username.toLowerCase();
        CollectionReference chats = firestore.collection("chats");
        Map<String, Object> data = new HashMap<>();
        data.put("username", lowerUsername);
        data.put("sender", sender);
        data.put("content", content);
        data.put("timestamp", FieldValue.serverTimestamp());
        chats.add(data);
    }

    public List<Map<String, Object>> getChatHistory(String username) throws ExecutionException, InterruptedException {
        String lowerUsername = username.toLowerCase();
        Query query = firestore.collection("chats")
                .whereEqualTo("username", lowerUsername)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .limit(50);
        
        QuerySnapshot querySnapshot = query.get().get();
        List<Map<String, Object>> chats = new ArrayList<>();
        for (QueryDocumentSnapshot doc : querySnapshot) {
            chats.add(doc.getData());
        }
        return chats;
    }
}
