package com.shehia_management.api.resident;

import lombok.Builder;
import lombok.Data;

/**
 * Anonymous, counts-only numbers shown on the public landing page's
 * statistics card. Contains no personal data. Only ACTIVE (verified)
 * residents are counted.
 */
@Data
@Builder
public class PublicStatsResponse {
    private long totalResidents;
    private long totalHouses;
    private long maleResidents;
    private long femaleResidents;
}
