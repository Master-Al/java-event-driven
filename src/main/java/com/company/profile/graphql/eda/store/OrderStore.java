package ccom.company.profile.graphql.eda.store;

import ccom.company.profile.graphql.eda.model.OrderCreatedEvent;
import ccom.company.profile.graphql.eda.model.OrderView;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class OrderStore {
    private final ConcurrentHashMap<String, OrderView> orders = new ConcurrentHashMap<>();

    public void apply(OrderCreatedEvent event) {
        String updated = (event.createdAt() != null ? event.createdAt() : java.time.Instant.now()).toString();
        orders.put(event.orderId(), new OrderView(event.orderId(), "CREATED", updated));
    }

    public Optional<OrderView> findById(String orderId) {
        return Optional.ofNullable(orders.get(orderId));
    }

    public void clear() {
        orders.clear();
    }
}
