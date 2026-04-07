package com.example.splitstudy;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SplitstudyApplication {

    public static void main(String[] args) {
        SpringApplication.run(SplitstudyApplication.class, args);
    }

    // ADD THIS BEAN HERE
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}