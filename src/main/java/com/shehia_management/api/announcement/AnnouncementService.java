package com.shehia_management.api.announcement;

import java.util.List;

public interface AnnouncementService {
    AnnouncementResponse createAnnouncement(AnnouncementRequest request);
    AnnouncementResponse updateAnnouncement(Long id, AnnouncementRequest request);
    AnnouncementResponse publishAnnouncement(Long id);
    AnnouncementResponse unpublishAnnouncement(Long id);
    void deleteAnnouncement(Long id);
    AnnouncementResponse getAnnouncement(Long id);
    List<AnnouncementResponse> getAllAnnouncements(AnnouncementStatus status);

    /** Public: only PUBLISHED announcements, for the public portal. */
    List<AnnouncementResponse> getPublishedAnnouncements();
}
