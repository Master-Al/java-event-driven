package ccom.company.profile.graphql.eda.handler;

import ccom.company.profile.graphql.eda.model.OrderCreatedEvent;
import ccom.company.profile.graphql.eda.store.OrderStore;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@ApplicationScoped
@Named("orderEventHandler")
public class OrderEventHandler {
    private final OrderStore store;

    @Inject
    public OrderEventHandler(OrderStore store) {
        this.store = store;
    }

    public void onOrderCreated(OrderCreatedEvent event) {
        store.apply(event);
    }
}
