package com.example.splitstudy;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SearchTopicRepository extends JpaRepository<SearchTopic, Long> {
    List<SearchTopic> findByUsernameOrderByTimestampDesc(String username);
}
