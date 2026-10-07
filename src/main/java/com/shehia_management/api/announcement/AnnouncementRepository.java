package com.shehia_management.api.announcement;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {
    List<Announcement> findByStatus(AnnouncementStatus status);
    List<Announcement> findByTargetShehiaOrTargetShehiaIsNull(String targetShehia);
    long countByStatus(AnnouncementStatus status);
    long countByPublishedAtGreaterThanEqualAndPublishedAtLessThan(LocalDateTime from, LocalDateTime to);
}
