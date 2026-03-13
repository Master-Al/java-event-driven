package ccom.company.profile.graphql.eda.model;

import java.math.BigDecimal;
import java.time.Instant;

public record OrderCreatedEvent(
        String orderId,
        String customerId,
        BigDecimal amount,
        Instant createdAt
) {
}
