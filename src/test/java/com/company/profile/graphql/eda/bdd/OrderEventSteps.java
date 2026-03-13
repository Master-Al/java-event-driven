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
import org.apache.camel.builder.NotifyBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.support.SimpleRegistry;
import org.junit.jupiter.api.Assertions;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class OrderEventSteps {
    private static final Logger LOG = Logger.getLogger(OrderEventSteps.class.getName());
    private CamelContext camelContext;
    private ProducerTemplate producerTemplate;
    private OrderStore orderStore;
    private OrderCreatedEvent orderEvent;
    private NotifyBuilder commandRouteDone;
    private NotifyBuilder consumerRouteDone;

    @Before
    public void setup() throws Exception {
        LOG.info("TEST - setup");
        orderStore = new OrderStore();
        OrderEventHandler handler = new OrderEventHandler(orderStore);

        SimpleRegistry registry = new SimpleRegistry();
        registry.bind("orderEventHandler", handler);

        camelContext = new DefaultCamelContext(registry);
        camelContext.addRoutes(new OrderEventRoute());
        camelContext.start();
        producerTemplate = camelContext.createProducerTemplate();
        commandRouteDone = new NotifyBuilder(camelContext)
                .fromRoute("order-command")
                .whenDone(1)
                .create();
        consumerRouteDone = new NotifyBuilder(camelContext)
                .fromRoute("order-event-consumer")
                .whenDone(1)
                .create();
    }

    @After
    public void tearDown() throws Exception {
        LOG.info("TEST - tearDown");
        if (producerTemplate != null) {
            producerTemplate.stop();
        }
        if (camelContext != null) {
            camelContext.stop();
        }
    }

    @Given("an order {string} for customer {string} amount {double}")
    public void an_order_for_customer_amount(String orderId, String customerId, double amount) {
        LOG.info(() -> "TEST - Given orderId=" + orderId + " customerId=" + customerId + " amount=" + amount);
        orderEvent = new OrderCreatedEvent(orderId, customerId, BigDecimal.valueOf(amount), Instant.now());
    }

    @When("the order is submitted")
    public void the_order_is_submitted() {
        LOG.info("TEST - When order submitted");
        producerTemplate.sendBody("direct:orders", orderEvent);
    }

    @Then("the order status is {string}")
    public void the_order_status_is(String expectedStatus) {
        LOG.info(() -> "TEST - Then expect status=" + expectedStatus);
        Assertions.assertTrue(commandRouteDone.matches(2, TimeUnit.SECONDS),
                "Command route should process the order event");
        Assertions.assertTrue(consumerRouteDone.matches(2, TimeUnit.SECONDS),
                "Consumer route should handle the order event");
        OrderView view = waitForOrderView(orderEvent.orderId());
        Assertions.assertNotNull(view, "Order view should be created by the event handler");
        Assertions.assertEquals(expectedStatus, view.status());
    }

    private OrderView waitForOrderView(String orderId) {
        LOG.info(() -> "TEST - waitForOrderView orderId=" + orderId);
        long deadline = System.currentTimeMillis() + 2000;
        while (System.currentTimeMillis() < deadline) {
            OrderView view = orderStore.findById(orderId).orElse(null);
            if (view != null) {
                LOG.info(() -> "TEST - waitForOrderView found orderId=" + orderId);
                return view;
            }
            try {
                Thread.sleep(25);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }
        }
        LOG.warning(() -> "TEST - waitForOrderView timed out orderId=" + orderId);
        return null;
    }
}
