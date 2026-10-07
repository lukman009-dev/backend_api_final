package com.shehia_management.api.announcement;

import com.shehia_management.api.issue.IssuePriority;
import com.shehia_management.api.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AnnouncementServiceImpl implements AnnouncementService {

    private final AnnouncementRepository announcementRepository;

    @Override
    public AnnouncementResponse createAnnouncement(AnnouncementRequest request) {
        Announcement announcement = new Announcement();
        applyAnnouncementRequest(announcement, request);
        if (announcement.getStatus() == null) announcement.setStatus(AnnouncementStatus.DRAFT);
        if (announcement.getStatus() == AnnouncementStatus.PUBLISHED) announcement.setPublishedAt(LocalDateTime.now());
        return toAnnouncementResponse(announcementRepository.save(announcement));
    }

    @Override
    public AnnouncementResponse updateAnnouncement(Long id, AnnouncementRequest request) {
        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Announcement not found with id: " + id));
        applyAnnouncementRequest(announcement, request);
        if (announcement.getStatus() == AnnouncementStatus.PUBLISHED && announcement.getPublishedAt() == null) {
            announcement.setPublishedAt(LocalDateTime.now());
        }
        return toAnnouncementResponse(announcementRepository.save(announcement));
    }

    @Override
    public AnnouncementResponse publishAnnouncement(Long id) {
        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Announcement not found with id: " + id));
        announcement.setStatus(AnnouncementStatus.PUBLISHED);
        announcement.setPublishedAt(LocalDateTime.now());
        return toAnnouncementResponse(announcementRepository.save(announcement));
    }

    @Override
    public AnnouncementResponse unpublishAnnouncement(Long id) {
        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Announcement not found with id: " + id));
        announcement.setStatus(AnnouncementStatus.DRAFT);
        announcement.setPublishedAt(null);
        return toAnnouncementResponse(announcementRepository.save(announcement));
    }

    @Override
    public void deleteAnnouncement(Long id) {
        if (!announcementRepository.existsById(id)) {
            throw new ResourceNotFoundException("Announcement not found with id: " + id);
        }
        announcementRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public AnnouncementResponse getAnnouncement(Long id) {
        return toAnnouncementResponse(announcementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Announcement not found with id: " + id)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AnnouncementResponse> getAllAnnouncements(AnnouncementStatus status) {
        List<Announcement> list = status == null ? announcementRepository.findAll() : announcementRepository.findByStatus(status);
        return list.stream().map(this::toAnnouncementResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AnnouncementResponse> getPublishedAnnouncements() {
        return announcementRepository.findByStatus(AnnouncementStatus.PUBLISHED).stream().map(this::toAnnouncementResponse).toList();
    }

    private void applyAnnouncementRequest(Announcement target, AnnouncementRequest request) {
        if (request.getTitle() == null || request.getTitle().isBlank()
                || request.getContent() == null || request.getContent().isBlank()) {
            throw new IllegalArgumentException("Announcement title and content are required");
        }
        target.setTitle(request.getTitle().trim());
        target.setContent(request.getContent().trim());
        target.setType(request.getType() == null ? AnnouncementType.GENERAL : request.getType());
        target.setPriority(request.getPriority() == null ? IssuePriority.MEDIUM : request.getPriority());
        if (request.getStatus() != null) target.setStatus(request.getStatus());
        target.setTargetShehia(request.getTargetShehia());
        target.setImageUrl(request.getImageUrl());
        target.setExpiryDate(request.getExpiryDate());
    }

    private AnnouncementResponse toAnnouncementResponse(Announcement a) {
        return AnnouncementResponse.builder().id(a.getId()).title(a.getTitle()).content(a.getContent())
                .type(a.getType()).priority(a.getPriority()).status(a.getStatus()).targetShehia(a.getTargetShehia())
                .imageUrl(a.getImageUrl()).expiryDate(a.getExpiryDate()).publishedAt(a.getPublishedAt()).build();
    }
}
