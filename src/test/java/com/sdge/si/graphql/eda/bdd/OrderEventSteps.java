package ccom.company.profile.graphql.eda.bdd;

import ccom.company.profile.graphql.eda.handler.OrderEventHandler;
import ccom.company.profile.graphql.eda.model.OrderCreatedEvent;
import ccom.company.profile.graphql.eda.model.OrderView;
import ccom.company.profile.graphql.eda.routes.OrderEventRoute;
import ccom.company.profile.graphql.eda.store.OrderStore;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.support.SimpleRegistry;
import org.junit.jupiter.api.Assertions;

import java.math.BigDecimal;
import java.time.Instant;

public class OrderEventSteps {
    private CamelContext camelContext;
    private ProducerTemplate producerTemplate;
    private OrderStore orderStore;
    private OrderCreatedEvent orderEvent;

    @Before
    public void setup() throws Exception {
        orderStore = new OrderStore();
        OrderEventHandler handler = new OrderEventHandler(orderStore);

        SimpleRegistry registry = new SimpleRegistry();
        registry.bind("orderEventHandler", handler);

        camelContext = new DefaultCamelContext(registry);
        camelContext.addRoutes(new OrderEventRoute());
        camelContext.start();
        producerTemplate = camelContext.createProducerTemplate();
    }

    @After
    public void tearDown() throws Exception {
        if (producerTemplate != null) {
            producerTemplate.stop();
        }
        if (camelContext != null) {
            camelContext.stop();
        }
    }

    @Given("an order {string} for customer {string} amount {double}")
    public void an_order_for_customer_amount(String orderId, String customerId, double amount) {
        orderEvent = new OrderCreatedEvent(orderId, customerId, BigDecimal.valueOf(amount), Instant.now());
    }

    @When("the order is submitted")
    public void the_order_is_submitted() {
        producerTemplate.sendBody("direct:orders", orderEvent);
    }

    @Then("the order status is {string}")
    public void the_order_status_is(String expectedStatus) {
        OrderView view = waitForOrderView(orderEvent.orderId());
        Assertions.assertNotNull(view, "Order view should be created by the event handler");
        Assertions.assertEquals(expectedStatus, view.status());
    }

    private OrderView waitForOrderView(String orderId) {
        long deadline = System.currentTimeMillis() + 2000;
        while (System.currentTimeMillis() < deadline) {
            OrderView view = orderStore.findById(orderId).orElse(null);
            if (view != null) {
                return view;
            }
            try {
                Thread.sleep(25);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }
        }
        return null;
    }
}
