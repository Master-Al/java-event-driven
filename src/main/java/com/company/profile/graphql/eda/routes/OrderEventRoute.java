package ccom.company.profile.graphql.eda.routes;

import org.apache.camel.builder.RouteBuilder;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class OrderEventRoute extends RouteBuilder {
    @Override
    public void configure() {
        from("direct:orders")
                .routeId("order-command")
                .log("CMD - received order event ${body}")
                .validate(exchange -> exchange.getIn()
                        .getBody(ccom.company.profile.graphql.eda.model.OrderCreatedEvent.class)
                        .orderId() != null)
                .log("CMD - validated order ${body.orderId}")
                .wireTap("seda:order-audit")
                .to("seda:order-events");

        from("seda:order-events")
                .routeId("order-event-consumer")
                .log("EVENT - consuming order event ${body}")
                .bean("orderEventHandler", "onOrderCreated");

        from("seda:order-audit")
                .routeId("order-audit")
                .log("AUDIT - ${body}");
    }
}
