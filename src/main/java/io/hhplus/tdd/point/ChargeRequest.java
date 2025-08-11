package io.hhplus.tdd.point;

import jakarta.validation.constraints.Min;
import lombok.Getter;

@Getter
public class ChargeRequest {
    @Min(value = 0)
    private long amount;
}
