package com.shehia_management.api.issue;

/**
 * SHARED VOCABULARY: this enum is also used by the announcement capability
 * (Announcement.priority). It used to be duplicated as a separate
 * "PriorityLevel" enum there and was consolidated onto this one. Rather
 * than re-split it or invent a neutral home for a 4-value enum, the
 * announcement capability just imports this type directly - an explicit,
 * intentional, minor coupling on a piece of shared vocabulary, not a case
 * of hidden shared logic.
 */
public enum IssuePriority { LOW, MEDIUM, HIGH, URGENT }
