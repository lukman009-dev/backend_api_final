package com.shehia_management.api.staff;

import com.shehia_management.api.identity.Role;
import com.shehia_management.api.identity.User;
import com.shehia_management.api.identity.UserLookupService;
import com.shehia_management.api.identity.UserMapper;
import com.shehia_management.api.identity.UserRepository;
import com.shehia_management.api.identity.UserResponse;
import com.shehia_management.api.identity.UserStatus;
import com.shehia_management.api.identity.ZoneUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class StaffServiceImpl implements StaffService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserLookupService userLookupService;

    @Override
    public UserResponse registerStaff(StaffRegisterRequest request) {
        if (userRepository.findByZanId(request.getUsername()).isPresent()) {
            throw new IllegalArgumentException("A user with this username already exists");
        }
        User staff = User.builder()
                .zanId(request.getUsername().trim())
                .fullName(request.getUsername().trim())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.ROLE_STAFF)
                .status(UserStatus.PENDING)
                .mustChangePassword(false)
                .build();
        return UserMapper.toResponse(userRepository.save(staff));
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentStaff(String zanId) {
        User user = userLookupService.findByZanId(zanId);
        userLookupService.validateStaff(user);
        return UserMapper.toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getStaffById(Long id) {
        return UserMapper.toResponse(userLookupService.findStaffById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllStaff(UserStatus status) {
        List<User> users = status == null
                ? userRepository.findByRole(Role.ROLE_STAFF)
                : userRepository.findByRoleAndStatus(Role.ROLE_STAFF, status);
        return users.stream().map(UserMapper::toResponse).toList();
    }

    @Override
    public UserResponse updateStaffStatus(Long id, UserStatus status) {
        User staff = userLookupService.findStaffById(id);
        if (status == UserStatus.ACTIVE
                && (staff.getAssignedZone() == null || staff.getAssignedZone().isBlank())) {
            throw new IllegalStateException("Assign a zone to this staff member before approving their account");
        }
        staff.setStatus(status);
        return UserMapper.toResponse(userRepository.save(staff));
    }

    @Override
    public UserResponse assignStaffZone(Long id, String zone) {
        if (!ZoneUtil.isValidZoneLetter(zone)) {
            throw new IllegalArgumentException("Zone must be a single letter, e.g. A, B, C");
        }
        User staff = userLookupService.findStaffById(id);
        staff.setAssignedZone(zone.trim().toUpperCase());
        return UserMapper.toResponse(userRepository.save(staff));
    }

    @Override
    @Transactional(readOnly = true)
    public String requireAssignedZone(String zanId) {
        UserResponse staff = getCurrentStaff(zanId);
        String zone = staff.getAssignedZone();
        if (zone == null || zone.isBlank()) {
            throw new IllegalStateException("Your staff account has no zone assigned yet. Contact an administrator.");
        }
        return zone;
    }
}
