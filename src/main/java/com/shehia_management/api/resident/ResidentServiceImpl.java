package com.shehia_management.api.resident;

import com.shehia_management.api.identity.Gender;
import com.shehia_management.api.identity.Role;
import com.shehia_management.api.identity.User;
import com.shehia_management.api.identity.UserMapper;
import com.shehia_management.api.identity.UserRepository;
import com.shehia_management.api.identity.UserResponse;
import com.shehia_management.api.identity.UserStatus;
import com.shehia_management.api.identity.ZoneUtil;
import com.shehia_management.api.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ResidentServiceImpl implements ResidentService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponse registerResident(RegisterRequest request) {
        if (userRepository.findByZanId(request.getZanId()).isPresent()) {
            throw new IllegalArgumentException("A resident with this ZanID already exists");
        }
        if (request.getEmail() != null && !request.getEmail().isBlank()
                && userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("A user with this email already exists");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }

        // Build the entity ourselves from the DTO instead of trusting a
        // client-supplied User entity. role / status / mustChangePassword are
        // always set server-side, never taken from client input.
        User user = User.builder()
                .zanId(request.getZanId())
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .houseNumber(ZoneUtil.normalize(request.getHouseNumber()))
                .street(request.getStreet())
                .shehia(request.getShehia())
                .district(request.getDistrict())
                .region(request.getRegion())
                .dateOfBirth(request.getDateOfBirth())
                .gender(request.getGender())
                .idDocumentUrl(request.getIdDocumentUrl())
                .proofOfResidenceUrl(request.getProofOfResidenceUrl())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.ROLE_RESIDENT)
                .status(UserStatus.PENDING)
                .mustChangePassword(false)
                .build();

        return UserMapper.toResponse(userRepository.save(user));
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getResident(Long id) {
        return UserMapper.toResponse(userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resident not found with id: " + id)));
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentResident(String zanId) {
        return UserMapper.toResponse(findResidentByZanId(zanId));
    }

    @Override
    public UserResponse updateResidentStatus(Long id, UserStatus status) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resident not found with id: " + id));
        user.setStatus(status);
        return UserMapper.toResponse(userRepository.save(user));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllResidents(UserStatus status) {
        List<User> users = status == null
                ? userRepository.findByRole(Role.ROLE_RESIDENT)
                : userRepository.findByRoleAndStatus(Role.ROLE_RESIDENT, status);
        return users.stream().map(UserMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PublicStatsResponse getPublicStats() {
        return PublicStatsResponse.builder()
                .totalResidents(userRepository.countByRoleAndStatus(Role.ROLE_RESIDENT, UserStatus.ACTIVE))
                .totalHouses(userRepository.countDistinctHousesByRoleAndStatus(Role.ROLE_RESIDENT, UserStatus.ACTIVE))
                .maleResidents(userRepository.countByRoleAndStatusAndGender(Role.ROLE_RESIDENT, UserStatus.ACTIVE, Gender.MALE))
                .femaleResidents(userRepository.countByRoleAndStatusAndGender(Role.ROLE_RESIDENT, UserStatus.ACTIVE, Gender.FEMALE))
                .build();
    }

    // ============================================================
    // ZONE-SCOPED RESIDENT MANAGEMENT (staff)
    // ============================================================

    @Override
    public UserResponse registerResidentInZone(RegisterRequest request, String zone) {
        String requestedZone = ZoneUtil.extractZone(request.getHouseNumber());
        if (requestedZone == null || !requestedZone.equalsIgnoreCase(zone)) {
            throw new IllegalArgumentException(
                    "House number must be in your assigned zone " + zone.toUpperCase()
                            + " (e.g. SH/UW/" + zone.toUpperCase() + "/123)");
        }
        return registerResident(request);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getResidentsInZone(String zone, UserStatus status) {
        String prefix = ZoneUtil.zonePrefix(zone);
        List<User> users = status == null
                ? userRepository.findByRoleAndHouseNumberStartingWithIgnoreCase(Role.ROLE_RESIDENT, prefix)
                : userRepository.findByRoleAndStatusAndHouseNumberStartingWithIgnoreCase(Role.ROLE_RESIDENT, status, prefix);
        return users.stream().map(UserMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getResidentInZone(Long id, String zone) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resident not found with id: " + id));
        ensureResidentInZone(user, zone);
        return UserMapper.toResponse(user);
    }

    @Override
    public UserResponse updateResidentStatusInZone(Long id, UserStatus status, String zone) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resident not found with id: " + id));
        ensureResidentInZone(user, zone);
        user.setStatus(status);
        return UserMapper.toResponse(userRepository.save(user));
    }

    private User findResidentByZanId(String zanId) {
        return userRepository.findByZanId(zanId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ZanID: " + zanId));
    }

    private void ensureResidentInZone(User user, String zone) {
        if (user.getRole() != Role.ROLE_RESIDENT) {
            throw new ResourceNotFoundException("Resident not found");
        }
        String residentZone = ZoneUtil.extractZone(user.getHouseNumber());
        if (residentZone == null || !residentZone.equalsIgnoreCase(zone)) {
            throw new AccessDeniedException("You are not allowed to manage residents outside your assigned zone");
        }
    }
}
