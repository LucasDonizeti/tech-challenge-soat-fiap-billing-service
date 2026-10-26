package com.techchallenge.billing.domain.events;

import java.time.LocalDateTime;

/**
 * Marker interface for domain events
 * All events in the billing service implement this interface
 */
public interface DomainEvent {
    LocalDateTime getTimestamp();
}
