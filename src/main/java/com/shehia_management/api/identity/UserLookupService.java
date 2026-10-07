package com.shehia_management.api.identity;

import com.shehia_management.api.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * EXTRACTED SHARED LOGIC: these four methods were private helpers on the old
 * god-service (findUserByZanId, validateResident, validateStaff,
 * findStaffById) and were called from the resident, staff, issue AND letter
 * sections of that class. That fan-out is exactly the signal that this
 * logic belongs to the shared "identity" kernel rather than to any single
 * business capability - moving it into e.g. ResidentServiceImpl would have
 * forced the issue/letter capabilities to depend on the resident capability
 * just to resolve "who is the authenticated account", which is not a
 * resident-specific concern.
 */
@Service
@RequiredArgsConstructor
public class UserLookupService {

    private final UserRepository userRepository;

    public User findByZanId(String zanId) {
        return userRepository.findByZanId(zanId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ZanID: " + zanId));
    }

    /** Looks up the account and asserts it is an active resident account. */
    public User requireActiveResident(String zanId) {
        User user = findByZanId(zanId);
        validateResident(user);
        return user;
    }

    public void validateResident(User user) {
        if (user.getRole() != Role.ROLE_RESIDENT) {
            throw new IllegalArgumentException("The authenticated account is not a resident account");
        }
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new IllegalStateException("Resident account is not active");
        }
    }

    public void validateStaff(User user) {
        if (user.getRole() != Role.ROLE_STAFF) {
            throw new IllegalArgumentException("The authenticated account is not a staff account");
        }
    }

    public User findStaffById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found with id: " + id));
        if (user.getRole() != Role.ROLE_STAFF) {
            throw new ResourceNotFoundException("Staff not found with id: " + id);
        }
        return user;
    }
}
