package ccom.company.profile.graphql.eda.store;

import ccom.company.profile.graphql.eda.model.OrderCreatedEvent;
import ccom.company.profile.graphql.eda.model.OrderView;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

@ApplicationScoped
public class OrderStore {
    private static final Logger LOG = Logger.getLogger(OrderStore.class.getName());
    private final ConcurrentHashMap<String, OrderView> orders = new ConcurrentHashMap<>();

    public void apply(OrderCreatedEvent event) {
        LOG.info(() -> "STORE - apply event for orderId=" + event.orderId());
        String updated = (event.createdAt() != null ? event.createdAt() : java.time.Instant.now()).toString();
        orders.put(event.orderId(), new OrderView(event.orderId(), "CREATED", updated));
    }

    public Optional<OrderView> findById(String orderId) {
        LOG.fine(() -> "STORE - findById orderId=" + orderId);
        return Optional.ofNullable(orders.get(orderId));
    }

    public void clear() {
        LOG.info("STORE - clear");
        orders.clear();
    }
}
