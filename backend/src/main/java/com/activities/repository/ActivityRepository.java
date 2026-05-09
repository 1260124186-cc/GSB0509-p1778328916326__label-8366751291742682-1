package com.activities.repository;

import com.activities.entity.Activity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ActivityRepository extends JpaRepository<Activity, Long> {
    Optional<Activity> findByCheckinToken(String checkinToken);
}
