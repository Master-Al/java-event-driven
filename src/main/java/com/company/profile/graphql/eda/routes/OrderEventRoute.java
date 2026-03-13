package ccom.company.profile.graphql.eda.routes;

import org.apache.camel.builder.RouteBuilder;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class OrderEventRoute extends RouteBuilder {
    @Override
    public void configure() {
        from("direct:orders")
                .routeId("order-command")
                .validate(exchange -> exchange.getIn()
                        .getBody(ccom.company.profile.graphql.eda.model.OrderCreatedEvent.class)
                        .orderId() != null)
                .wireTap("seda:order-audit")
                .to("seda:order-events");

        from("seda:order-events")
                .routeId("order-event-consumer")
                .bean("orderEventHandler", "onOrderCreated");

        from("seda:order-audit")
                .routeId("order-audit")
                .log("AUDIT - ${body}");
    }
}
