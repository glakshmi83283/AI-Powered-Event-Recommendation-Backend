
package com.example.backend.service;

import com.example.backend.entity.Event;
import com.example.backend.repository.EventRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class EventService {

    private final EventRepository eventRepository;

    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public Event saveEvent(Event event) {
        if (event == null) {
            throw new IllegalArgumentException("Event cannot be empty");
        }

        List<Event> events = eventRepository.findAll();

        for (Event existing : events) {
            if (same(existing.getName(), event.getName())
                    && same(existing.getCategory(), event.getCategory())
                    && same(existing.getLocation(), event.getLocation())
                    && Objects.equals(existing.getDate(), event.getDate())) {

                throw new IllegalArgumentException(
                        "This event already exists!");
            }
        }

        return eventRepository.save(event);
    }

    private boolean same(String first, String second) {
        return first != null
                && second != null
                && first.trim().equalsIgnoreCase(second.trim());
    }

    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }
}
