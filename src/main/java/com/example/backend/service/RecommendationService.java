
package com.example.backend.service;

import com.example.backend.entity.Event;
import com.example.backend.entity.User;
import com.example.backend.repository.EventRepository;
import com.example.backend.repository.UserRepository;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class RecommendationService {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    public RecommendationService(
            UserRepository userRepository,
            EventRepository eventRepository) {
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
    }

    public List<RecommendationResult> getRecommendations(Long userId) {

        User user = userRepository.findById(userId).orElse(null);

        List<RecommendationResult> results = new ArrayList<>();

        if (user == null || user.getInterests() == null) {
            return results;
        }

        String interests = user.getInterests().toLowerCase(Locale.ROOT);

        for (Event event : eventRepository.findAll()) {

            String category = event.getCategory() == null
                    ? ""
                    : event.getCategory().toLowerCase(Locale.ROOT);

            String name = event.getName() == null
                    ? ""
                    : event.getName().toLowerCase(Locale.ROOT);

            String description = event.getDescription() == null
                    ? ""
                    : event.getDescription().toLowerCase(Locale.ROOT);

            double score = 0.0;

            for (String interest : interests.split(",")) {
                String keyword = interest.trim();

                if (keyword.isEmpty()) {
                    continue;
                }

                if (category.contains(keyword)) {
                    score += 1.0;
                } else if (name.contains(keyword)
                        || description.contains(keyword)) {
                    score += 0.5;
                }
            }

            if (score > 0.0) {
                results.add(new RecommendationResult(
                        event,
                        score,
                        "Matches your interests: " + user.getInterests()
                ));
            }
        }

        results.sort((a, b) ->
                Double.compare(b.getScore(), a.getScore()));

        return results;
    }

    public static class RecommendationResult {

        private Event event;
        private double score;
        private String reason;

        public RecommendationResult(
                Event event, double score, String reason) {
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
