package ccom.company.profile.graphql.eda.graphql;

import ccom.company.profile.graphql.eda.model.OrderCreatedEvent;
import ccom.company.profile.graphql.eda.model.OrderView;
import ccom.company.profile.graphql.eda.store.OrderStore;

import org.apache.camel.ProducerTemplate;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Mutation;
import org.eclipse.microprofile.graphql.Query;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.time.Instant;

@GraphQLApi
@ApplicationScoped
public class OrderGraphQLApi {
    private final ProducerTemplate producerTemplate;
    private final OrderStore orderStore;

    @Inject
    public OrderGraphQLApi(ProducerTemplate producerTemplate, OrderStore orderStore) {
        this.producerTemplate = producerTemplate;
        this.orderStore = orderStore;
    }

    @Mutation
    public OrderView createOrder(String orderId, String customerId, BigDecimal amount) {
        OrderCreatedEvent event = new OrderCreatedEvent(orderId, customerId, amount, Instant.now());
        producerTemplate.sendBody("direct:orders", event);
        return orderStore.findById(orderId)
                .orElse(new OrderView(orderId, "PENDING", event.createdAt().toString()));
    }

    @Query
    public OrderView order(String orderId) {
        return orderStore.findById(orderId).orElse(null);
    }
}
