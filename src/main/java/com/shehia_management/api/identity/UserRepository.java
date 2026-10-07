package com.shehia_management.api.identity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByZanId(String zanId);
    Optional<User> findByEmail(String email);
    List<User> findByStatus(UserStatus status);
    List<User> findByShehia(String shehia);
    List<User> findByRole(Role role);
    List<User> findByRoleAndStatus(Role role, UserStatus status);
    long countByRole(Role role);
    long countByRoleAndStatus(Role role, UserStatus status);
    long countByRoleAndStatusAndGender(Role role, UserStatus status, Gender gender);

    // Number of distinct houses (house numbers) that have at least one resident with this status.
    @Query("select count(distinct upper(u.houseNumber)) from User u "
            + "where u.role = :role and u.status = :status "
            + "and u.houseNumber is not null and u.houseNumber <> ''")
    long countDistinctHousesByRoleAndStatus(@Param("role") Role role, @Param("status") UserStatus status);
    long countByRoleAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(Role role, LocalDateTime from, LocalDateTime to);

    // Zone-scoped lookups: houseNumber is stored as "SH/UW/<ZONE>/<NNN>", so
    // filtering by the "SH/UW/<ZONE>/" prefix effectively filters by zone.
    List<User> findByRoleAndHouseNumberStartingWithIgnoreCase(Role role, String houseNumberPrefix);
    List<User> findByRoleAndStatusAndHouseNumberStartingWithIgnoreCase(Role role, UserStatus status, String houseNumberPrefix);
}
