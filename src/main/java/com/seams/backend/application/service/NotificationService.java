package com.seams.backend.application.service;

import com.seams.backend.core.model.Notification;
import com.seams.backend.core.model.User;
import com.seams.backend.core.repository.NotificationRepository;
import com.seams.backend.core.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository repository;
    private final UserRepository userRepository;

    public NotificationService(NotificationRepository repository, UserRepository userRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
    }

    public List<Notification> findAll() {
        return repository.findAll();
    }

    public Notification save(Notification notification) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        notification.setPublisherName(user.getDisplayName());
        notification.setPublishedAt(Instant.now());
        return repository.save(notification);
    }

    public Notification update(Integer id, Notification request) {
        Notification notification = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Announcement not found"));
        
        notification.setEventName(request.getEventName());
        notification.setVenue(request.getVenue());
        notification.setDate(request.getDate());
        notification.setStartTime(request.getStartTime());
        notification.setEndTime(request.getEndTime());
        notification.setMessage(request.getMessage());
        
        return repository.save(notification);
    }

    public void deleteById(Integer id) {
        repository.deleteById(id);
    }
}
