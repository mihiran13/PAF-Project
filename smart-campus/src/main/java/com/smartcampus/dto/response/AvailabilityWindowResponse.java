package com.smartcampus.dto.response;

import com.smartcampus.model.ResourceAvailabilityWindow;
import lombok.Builder;
import lombok.Data;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Data
@Builder
public class AvailabilityWindowResponse {
    private Long id;
    private DayOfWeek dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;

    public static AvailabilityWindowResponse fromEntity(ResourceAvailabilityWindow window) {
        if (window == null)
            return null;

        return AvailabilityWindowResponse.builder()
                .id(window.getId())
                .dayOfWeek(window.getDayOfWeek())
                .startTime(window.getStartTime())
                .endTime(window.getEndTime())
                .build();
    }
}
