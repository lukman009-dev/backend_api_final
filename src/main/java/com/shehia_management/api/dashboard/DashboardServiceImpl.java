package com.shehia_management.api.dashboard;

import com.shehia_management.api.announcement.AnnouncementRepository;
import com.shehia_management.api.announcement.AnnouncementStatus;
import com.shehia_management.api.identity.Role;
import com.shehia_management.api.identity.UserRepository;
import com.shehia_management.api.identity.UserStatus;
import com.shehia_management.api.identity.ZoneUtil;
import com.shehia_management.api.issue.IssuePriority;
import com.shehia_management.api.issue.IssueReport;
import com.shehia_management.api.issue.IssueReportRepository;
import com.shehia_management.api.issue.IssueStatus;
import com.shehia_management.api.letter.LetterApplicationRepository;
import com.shehia_management.api.letter.LetterStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final UserRepository userRepository;
    private final LetterApplicationRepository letterRepository;
    private final IssueReportRepository issueRepository;
    private final AnnouncementRepository announcementRepository;

    @Override
    public DashboardResponse getDashboard() {
        return DashboardResponse.builder()
                .totalResidents(userRepository.countByRole(Role.ROLE_RESIDENT))
                .pendingResidents(userRepository.countByRoleAndStatus(Role.ROLE_RESIDENT, UserStatus.PENDING))
                .activeResidents(userRepository.countByRoleAndStatus(Role.ROLE_RESIDENT, UserStatus.ACTIVE))
                .suspendedResidents(userRepository.countByRoleAndStatus(Role.ROLE_RESIDENT, UserStatus.SUSPENDED))
                .totalLetters(letterRepository.count())
                .pendingLetters(letterRepository.countByStatus(LetterStatus.PENDING))
                .totalIssues(issueRepository.count())
                .pendingIssues(issueRepository.countByStatus(IssueStatus.PENDING))
                .urgentIssues(issueRepository.countByPriority(IssuePriority.URGENT))
                .totalAnnouncements(announcementRepository.count())
                .publishedAnnouncements(announcementRepository.countByStatus(AnnouncementStatus.PUBLISHED))
                .build();
    }

    @Override
    public List<ActivityPoint> getActivity(int days) {
        int safeDays = Math.max(1, Math.min(days, 31));
        List<ActivityPoint> result = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int i = safeDays - 1; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            LocalDateTime from = date.atStartOfDay();
            LocalDateTime to = date.plusDays(1).atStartOfDay();
            result.add(new ActivityPoint(
                    date,
                    userRepository.countByRoleAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(Role.ROLE_RESIDENT, from, to),
                    letterRepository.countBySubmittedAtGreaterThanEqualAndSubmittedAtLessThan(from, to),
                    issueRepository.countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(from, to),
                    announcementRepository.countByPublishedAtGreaterThanEqualAndPublishedAtLessThan(from, to)
            ));
        }
        return result;
    }

    // ============================================================
    // ZONE-SCOPED DASHBOARD (staff)
    // ============================================================

    @Override
    public DashboardResponse getStaffDashboard(String zone) {
        String prefix = ZoneUtil.zonePrefix(zone);
        long total = userRepository.findByRoleAndHouseNumberStartingWithIgnoreCase(Role.ROLE_RESIDENT, prefix).size();
        long pending = userRepository.findByRoleAndStatusAndHouseNumberStartingWithIgnoreCase(Role.ROLE_RESIDENT, UserStatus.PENDING, prefix).size();
        long active = userRepository.findByRoleAndStatusAndHouseNumberStartingWithIgnoreCase(Role.ROLE_RESIDENT, UserStatus.ACTIVE, prefix).size();
        long suspended = userRepository.findByRoleAndStatusAndHouseNumberStartingWithIgnoreCase(Role.ROLE_RESIDENT, UserStatus.SUSPENDED, prefix).size();

        List<IssueReport> zoneIssues = issueRepository.findAll().stream()
                .filter(i -> zone.equalsIgnoreCase(ZoneUtil.extractZone(i.getResident().getHouseNumber())))
                .toList();
        long totalIssues = zoneIssues.size();
        long pendingIssues = zoneIssues.stream().filter(i -> i.getStatus() == IssueStatus.PENDING).count();
        long urgentIssues = zoneIssues.stream().filter(i -> i.getPriority() == IssuePriority.URGENT).count();

        return DashboardResponse.builder()
                .totalResidents(total).pendingResidents(pending).activeResidents(active).suspendedResidents(suspended)
                .totalLetters(0).pendingLetters(0)
                .totalIssues(totalIssues).pendingIssues(pendingIssues).urgentIssues(urgentIssues)
                .totalAnnouncements(0).publishedAnnouncements(0)
                .build();
    }
}
