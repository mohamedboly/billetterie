package org.montrealjug.billetterie.ui;


import jakarta.validation.Valid;
import org.montrealjug.billetterie.entity.Activity;
import org.montrealjug.billetterie.entity.Event;
import org.montrealjug.billetterie.repository.ActivityRepository;
import org.montrealjug.billetterie.repository.EventRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.montrealjug.billetterie.ui.Utils.toIndexActivities;

@Controller
@RequestMapping("/admin/events")
public class EventsController {

    private final EventRepository eventRepository;
    private final ActivityRepository activityRepository;

    public EventsController(EventRepository eventRepository, ActivityRepository activityRepository) {
        this.eventRepository = eventRepository;
        this.activityRepository = activityRepository;
    }

    @GetMapping("")
    public String events(Model model) {
        List<PresentationEvent> presentationEvents = new ArrayList<>();
        Iterable<Event> events = this.eventRepository.findAll();
        events.forEach(event -> {
            PresentationEvent presentationEvent = new PresentationEvent(event.getId(), event.getTitle(), event.getDescription(), event.getDate(), toIndexActivities(event.getActivities()), event.isActive());
            presentationEvents.add(presentationEvent);
        });
        model.addAttribute("events", presentationEvents);
        return "events-list";
    }

    @PostMapping
    public ResponseEntity<Void> createEvents(@Valid PresentationEvent event, Model model) {
        Event entity = new Event();
        entity.setDescription(event.description());
        entity.setTitle(event.title());
        entity.setDate(event.date());
        eventRepository.save(entity);

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create("/admin/events"))
                .build();

    }

    @GetMapping("{id}")
    public String event(Model model, @PathVariable long id) {
        Optional<Event> optionalEvent = this.eventRepository.findById(id);
        PresentationEvent presentationEvent;
        if (optionalEvent.isPresent()) {
            Event event = optionalEvent.get();
            presentationEvent = new PresentationEvent(event.getId(), event.getTitle(), event.getDescription(), event.getDate(), Collections.emptyList(), event.isActive());
        } else {
            throw new RuntimeException("Event with id " + id + " not found");
        }
        model.addAttribute("event", presentationEvent);
        return "events-create-update";
    }

    @PostMapping("{id}")
    public ResponseEntity<Void> updateEvent(@Valid PresentationEvent presentationEvent, Model model, @PathVariable long id) {
        Optional<Event> optionalEvent = this.eventRepository.findById(id);

        if (optionalEvent.isPresent()) {
            Event event = optionalEvent.get();
            event.setTitle(presentationEvent.title());
            event.setDescription(presentationEvent.description());
            event.setDate(presentationEvent.date());
            event.setActive(presentationEvent.active() !=null ? presentationEvent.active() : false);
            eventRepository.save(event);
        } else {
            throw new RuntimeException("Event with id " + id + " not found");
        }

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create("/admin/events"))
                .build();

    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> delete(Model model, @PathVariable long id) {
        this.eventRepository.deleteById(id);

        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .build();

    }


    @GetMapping("createEvent")
    public String createEvent(Model model) {
        return "events-create-update";
    }


    @GetMapping("{id}/createActivity")
    public String createActivity(Model model, @PathVariable final Long id) {
        model.addAttribute("eventId", id);
        return "activities-create-update";
    }


    @GetMapping("{eventId}/activities/{activityId}")
    public String activity(Model model, @PathVariable long eventId, @PathVariable Long activityId) {
        Optional<Activity> optionalActivity = this.activityRepository.findById(activityId);
        PresentationActivity presentationActivity;
        if (optionalActivity.isPresent()) {
            Activity activity = optionalActivity.get();
            presentationActivity = new PresentationActivity(activity.getId(), activity.getTitle(), activity.getDescription(), activity.getMaxParticipants(), activity.getMaxWaitingQueue(), activity.getStartTime().toLocalTime());
        } else {
            throw new RuntimeException("Activity with id " + activityId + " not found");
        }
        model.addAttribute("activity", presentationActivity);
        model.addAttribute("eventId", eventId);
        return "activities-create-update";
    }

    @PostMapping("{eventId}/activities/{activityId}")
    public ResponseEntity<Void> updateActivity(@Valid PresentationActivity presentationActivity, Model model, @PathVariable long eventId, @PathVariable long activityId) {
        Optional<Activity> optionalActivity = activityRepository.findById(activityId);

        if (optionalActivity.isPresent()) {
            Activity activity = optionalActivity.get();
            activity.setTitle(presentationActivity.title());
            activity.setDescription(presentationActivity.description());
            activity.setMaxParticipants(presentationActivity.maxParticipants());
            activity.setMaxWaitingQueue(presentationActivity.maxWaitingQueue());

            eventRepository.findById(eventId).ifPresent(event -> {
                LocalDate date = event.getDate();
                LocalDateTime localDateTime = date.atTime(presentationActivity.time());
                activity.setStartTime(localDateTime);
            });

            activityRepository.save(activity);
        } else {
            throw new RuntimeException("Activity with id " + activityId + " not found");
        }

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create("/admin/events"))
                .build();

    }

    @PostMapping("{id}/activities")
    public ResponseEntity<Void> saveActivity(@Valid PresentationActivity activity, Model model, @PathVariable final Long id) {

        Optional<Event> byId = eventRepository.findById(id);

        byId.ifPresent(event -> {
            Activity entity = new Activity();
            entity.setDescription(activity.description());
            entity.setTitle(activity.title());
            entity.setMaxParticipants(activity.maxParticipants());
            entity.setMaxWaitingQueue(activity.maxWaitingQueue());

            LocalDate date = event.getDate();
            LocalDateTime localDateTime = date.atTime(activity.time());

            entity.setStartTime(localDateTime);
            event.getActivities().add(entity);
            activityRepository.save(entity);
            eventRepository.save(event);

        });
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create("/admin/events"))
                .build();

    }

    @DeleteMapping("{eventId}/activities/{activityId}")
    public ResponseEntity<Void> deleteActivity(Model model, @PathVariable long eventId, @PathVariable long activityId) {
        this.eventRepository.findById(eventId).ifPresent(event -> {
            Optional<Activity> optionalActivity = activityRepository.findById(activityId);
            optionalActivity.ifPresent(activity -> {
                event.getActivities().remove(activity);
                eventRepository.save(event);
                activityRepository.delete(activity);
            });
        });
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .build();

    }

}
