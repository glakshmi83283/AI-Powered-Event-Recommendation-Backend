
package com.example.backend.service;

import com.example.backend.entity.Event;
import com.example.backend.entity.User;
import com.example.backend.repository.EventRepository;
import com.example.backend.repository.UserRepository;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RecommendationService {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final WebClient webClient;

    public RecommendationService(
            UserRepository userRepository,
            EventRepository eventRepository) {

        this.userRepository = userRepository;
        this.eventRepository = eventRepository;

        this.webClient = WebClient.builder()
                .baseUrl("http://127.0.0.1:5000")
                .build();
    }

    public List<RecommendationResult> getRecommendations(Long userId) {

        User user = userRepository.findById(userId).orElse(null);

        if (user == null
                || user.getInterests() == null
                || user.getInterests().trim().isEmpty()) {
            return new ArrayList<>();
        }

        List<Event> events = eventRepository.findAll();

        if (events.isEmpty()) {
            return new ArrayList<>();
        }

        List<String> interests = new ArrayList<>();

        for (String interest : user.getInterests().split(",")) {
            String cleanedInterest = interest.trim();

            if (!cleanedInterest.isEmpty()) {
                interests.add(cleanedInterest);
            }
        }

        if (interests.isEmpty()) {
            return new ArrayList<>();
        }

        Map<String, Object> request = new HashMap<>();
        request.put("interests", interests);
        request.put("events", events);

        try {
            List<Map<String, Object>> mlResults = webClient.post()
                    .uri("/recommend")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(
                            new ParameterizedTypeReference<
                                    List<Map<String, Object>>>() {
                            })
                    .block();

            if (mlResults == null || mlResults.isEmpty()) {
                return new ArrayList<>();
            }

            List<RecommendationResult> results = new ArrayList<>();

            for (Map<String, Object> mlResult : mlResults) {

                if (mlResult == null) {
                    continue;
                }

                Object eventObject = mlResult.get("event");

                if (!(eventObject instanceof Map<?, ?> rawEventData)) {
                    continue;
                }

                Map<String, Object> eventData = new HashMap<>();

                for (Map.Entry<?, ?> entry : rawEventData.entrySet()) {
                    if (entry.getKey() instanceof String key) {
                        eventData.put(key, entry.getValue());
                    }
                }

                Event event = convertToEvent(eventData);

                Object scoreObject = mlResult.get("score");
                double score = 0.0;

                if (scoreObject instanceof Number number) {
                    score = number.doubleValue();
                } else if (scoreObject != null) {
                    try {
                        score = Double.parseDouble(scoreObject.toString());
                    } catch (NumberFormatException ignored) {
                        score = 0.0;
                    }
                }

                // Only show events with a positive similarity score.
                if (score <= 0.0) {
                    continue;
                }

                String reason = "Recommended based on similarity to your interests.";

                results.add(new RecommendationResult(event, score, reason));
            }

            return results;

        } catch (Exception exception) {
            System.err.println(
                    "Could not retrieve recommendations from the Python ML server: "
                            + exception.getMessage());

            return new ArrayList<>();
        }
    }

    private Event convertToEvent(Map<String, Object> data) {

        Event event = new Event();

        Object id = data.get("id");

        if (id instanceof Number number) {
            event.setId(number.longValue());
        } else if (id != null) {
            try {
                event.setId(Long.parseLong(id.toString()));
            } catch (NumberFormatException ignored) {
                // Keep the default ID if the value is invalid.
            }
        }

        event.setName(getString(data, "name"));
        event.setDescription(getString(data, "description"));
        event.setCategory(getString(data, "category"));
        event.setLocation(getString(data, "location"));
        event.setDate(getString(data, "date"));

        return event;
    }

    private String getString(Map<String, Object> data, String key) {
        Object value = data.get(key);
        return value == null ? "" : value.toString();
    }

    public static class RecommendationResult {

        private Event event;
        private double score;
        private String reason;

        public RecommendationResult(Event event, double score, String reason) {
            this.event = event;
            this.score = score;
            this.reason = reason;
        }

        public Event getEvent() {
            return event;
        }

        public void setEvent(Event event) {
            this.event = event;
        }

        public double getScore() {
            return score;
        }

        public void setScore(double score) {
            this.score = score;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }
    }
}
