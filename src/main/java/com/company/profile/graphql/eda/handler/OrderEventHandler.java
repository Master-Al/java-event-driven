package ccom.company.profile.graphql.eda.handler;

import ccom.company.profile.graphql.eda.model.OrderCreatedEvent;
import ccom.company.profile.graphql.eda.store.OrderStore;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.util.logging.Logger;

@ApplicationScoped
@Named("orderEventHandler")
public class OrderEventHandler {
    private static final Logger LOG = Logger.getLogger(OrderEventHandler.class.getName());
    private final OrderStore store;

    @Inject
    public OrderEventHandler(OrderStore store) {
        this.store = store;
    }

    public void onOrderCreated(OrderCreatedEvent event) {
        LOG.info(() -> "HANDLER - onOrderCreated invoked for orderId=" + event.orderId());
        store.apply(event);
    }
}
