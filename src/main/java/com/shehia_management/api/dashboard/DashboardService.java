package com.shehia_management.api.dashboard;

import java.util.List;

/**
 * Read-only reporting/aggregation capability. It legitimately depends on
 * Resident (via identity.UserRepository), Issue, Letter and Announcement
 * repositories directly - summarizing other capabilities' data is this
 * capability's entire job, unlike e.g. Staff reaching into Resident's data
 * to mutate it (which would be a real violation). Serves both the admin
 * dashboard and the staff zone-scoped dashboard, since "who's asking" is
 * an audience concern, not a different business rule.
 */
public interface DashboardService {
    DashboardResponse getDashboard();
    List<ActivityPoint> getActivity(int days);
    DashboardResponse getStaffDashboard(String zone);
}
