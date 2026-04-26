package com.smartcampus.repository;

import com.smartcampus.model.ResourceAvailabilityWindow;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.DayOfWeek;
import java.util.List;

public interface ResourceAvailabilityWindowRepository extends JpaRepository<ResourceAvailabilityWindow, Long> {

    List<ResourceAvailabilityWindow> findByResourceId(Long resourceId);

    List<ResourceAvailabilityWindow> findByResourceIdAndDayOfWeek(Long resourceId, DayOfWeek dayOfWeek);

    void deleteByResourceId(Long resourceId);
}
