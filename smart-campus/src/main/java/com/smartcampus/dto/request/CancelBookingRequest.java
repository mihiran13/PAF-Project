package com.smartcampus.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CancelBookingRequest {

    private String cancellationReason;
}
