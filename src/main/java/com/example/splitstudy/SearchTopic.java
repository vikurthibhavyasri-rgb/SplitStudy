package com.example.splitstudy;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "search_topics")
public class SearchTopic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String topic;
    private LocalDateTime timestamp;

    public SearchTopic() {
        this.timestamp = LocalDateTime.now();
    }

    public SearchTopic(String username, String topic) {
        this();
        this.username = username;
        this.topic = topic;
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
