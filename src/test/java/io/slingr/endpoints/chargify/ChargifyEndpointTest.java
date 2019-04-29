package io.slingr.endpoints.chargify;

import io.slingr.endpoints.services.exchange.Parameter;
import io.slingr.endpoints.services.rest.RestMethod;
import io.slingr.endpoints.utils.FilesUtils;
import io.slingr.endpoints.utils.Json;
import io.slingr.endpoints.utils.Strings;
import io.slingr.endpoints.utils.tests.EndpointTests;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;

/**
 * <p>Test over the function/events of the ChargifyEndpoint class
 *
 * <p>Created by lefunes on 01/08/15.
 */
public class ChargifyEndpointTest {

    private static final Logger logger = LoggerFactory.getLogger(ChargifyEndpointTest.class);

    private static EndpointTests test;

    @BeforeClass
    public static void init() throws Exception {
        test = EndpointTests.start(new io.slingr.endpoints.chargify.Runner(), "test.properties");
    }

    private String randomId(){
        return Strings.randomUUIDString();
    }

    @Test
    public void testCustomerFunctions() throws Exception {
        Json response;

        final String id = randomId();
        final int chargifyId = createCustomer(id);

        response = test.executeFunction( "findCustomerById", Json.map().set("id", id));
        checkConsumer(id, chargifyId, "email@slingr.io", response);

        response = test.executeFunction( "updateCustomer", response.set("email", "email+2@slingr.io"));
        checkConsumer(id, chargifyId, "email+2@slingr.io", response);

        response = test.executeFunction( "findCustomerByChargifyId", Json.map().set("chargifyId", chargifyId));
        checkConsumer(id, chargifyId, "email+2@slingr.io", response);

        response = test.executeFunction( "removeCustomer", Json.map().set("chargifyId", chargifyId));
        assertNotNull(response);
        assertEquals((Integer) chargifyId, response.integer("chargifyId"));
        assertTrue(response.bool("removed"));

        // queries over the deleted customer
        response = test.executeFunction( "removeCustomer", Json.map().set("chargifyId", chargifyId), true);
        assertNotNull(response);
        assertTrue(response.is(Parameter.EXCEPTION_FLAG));
        assertEquals("Invalid response code [404]", response.string(Parameter.EXCEPTION_MESSAGE));

        response = test.executeFunction( "findCustomerById", Json.map().set("id", id), true);
        assertNotNull(response);
        assertTrue(response.is(Parameter.EXCEPTION_FLAG));
        assertEquals("Invalid response code [404]", response.string(Parameter.EXCEPTION_MESSAGE));

        response = test.executeFunction( "findCustomerByChargifyId", Json.map().set("chargifyId", chargifyId), true);
        assertNotNull(response);
        assertTrue(response.is(Parameter.EXCEPTION_FLAG));
        assertEquals("Invalid response code [404]", response.string(Parameter.EXCEPTION_MESSAGE));

        response = test.executeFunction( "updateCustomer", Json.map().set("chargifyId", chargifyId).set("email", "email+2@slingr.io"), true);
        assertNotNull(response);
        assertTrue(response.is(Parameter.EXCEPTION_FLAG));
        assertEquals("Invalid response code [404]", response.string(Parameter.EXCEPTION_MESSAGE));

        logger.info("-- END");
    }

    @Test
    public void testCustomerEvents() throws Exception {
        Json response;

        final String id = randomId();
        final int chargifyId = createCustomer(id);

        response = test.executeFunction( "findCustomerById", Json.map().set("id", id));
        checkConsumer(id, chargifyId, "email@slingr.io", response);

        response = test.executeFunction( "updateCustomer", response.set("email", "email+2@slingr.io"));
        checkConsumer(id, chargifyId, "email+2@slingr.io", response);

        response = test.executeFunction( "findCustomerByChargifyId", Json.map().set("chargifyId", chargifyId));
        checkConsumer(id, chargifyId, "email+2@slingr.io", response);

        response = test.executeFunction( "removeCustomer", Json.map().set("chargifyId", chargifyId));
        assertNotNull(response);
        assertEquals((Integer) chargifyId, response.integer("chargifyId"));
        assertTrue(response.bool("removed"));

        logger.info("-- END");
    }

    @Test
    public void testInvalidCustomerFunctions() throws Exception {
        Json response;
        String function;

        // createCustomer
        function = "createCustomer";

        response = test.executeFunction( function, true);
        assertNotNull(response);
        assertTrue(response.is(Parameter.EXCEPTION_FLAG));
        assertEquals("Customer is empty", response.string(Parameter.EXCEPTION_MESSAGE));

        response = test.executeFunction( function, Json.map(), true);
        assertNotNull(response);
        assertTrue(response.is(Parameter.EXCEPTION_FLAG));
        assertEquals("Customer is empty", response.string(Parameter.EXCEPTION_MESSAGE));

        response = test.executeFunction( function, Json.map().set("firstName", "First name").set("email", "email@slingr.io"), true);
        assertNotNull(response);
        assertTrue(response.is(Parameter.EXCEPTION_FLAG));
        assertEquals("Invalid empty field [lastName]", response.string(Parameter.EXCEPTION_MESSAGE));

        response = test.executeFunction( function, Json.map().set("lastName", "Last name"), true);
        assertNotNull(response);
        assertTrue(response.is(Parameter.EXCEPTION_FLAG));
        assertEquals("Invalid empty fields [firstName, email]", response.string(Parameter.EXCEPTION_MESSAGE));

        // updateCustomer
        function = "updateCustomer";

        response = test.executeFunction( function, true);
        assertNotNull(response);
        assertTrue(response.is(Parameter.EXCEPTION_FLAG));
        assertEquals("Customer is empty", response.string(Parameter.EXCEPTION_MESSAGE));

        response = test.executeFunction( function, Json.map(), true);
        assertNotNull(response);
        assertTrue(response.is(Parameter.EXCEPTION_FLAG));
        assertEquals("Customer is empty", response.string(Parameter.EXCEPTION_MESSAGE));

        response = test.executeFunction( function, Json.map().set("firstName", "First name"), true);
        assertNotNull(response);
        assertTrue(response.is(Parameter.EXCEPTION_FLAG));
        assertEquals("Invalid chargify id [null]", response.string(Parameter.EXCEPTION_MESSAGE));

        response = test.executeFunction( function, Json.map().set("firstName", "First name").set("chargifyId", ""), true);
        assertNotNull(response);
        assertTrue(response.is(Parameter.EXCEPTION_FLAG));
        assertEquals("Invalid chargify id [null]", response.string(Parameter.EXCEPTION_MESSAGE));

        response = test.executeFunction( function, Json.map().set("chargifyId", "First name"), true);
        assertNotNull(response);
        assertTrue(response.is(Parameter.EXCEPTION_FLAG));
        assertEquals("Invalid chargify id [First name]", response.string(Parameter.EXCEPTION_MESSAGE));

        response = test.executeFunction( function, Json.map().set("chargifyId", -100), true);
        assertNotNull(response);
        assertTrue(response.is(Parameter.EXCEPTION_FLAG));
        assertEquals("Invalid chargify id [-100]", response.string(Parameter.EXCEPTION_MESSAGE));

        // findCustomerById
        function = "findCustomerById";

        response = test.executeFunction( function, true);
        assertNotNull(response);
        assertTrue(response.is(Parameter.EXCEPTION_FLAG));
        assertEquals("Customer is empty", response.string(Parameter.EXCEPTION_MESSAGE));

        response = test.executeFunction( function, Json.map(), true);
        assertNotNull(response);
        assertTrue(response.is(Parameter.EXCEPTION_FLAG));
        assertEquals("Customer is empty", response.string(Parameter.EXCEPTION_MESSAGE));

        response = test.executeFunction( function, Json.map().set("firstName", "First name"), true);
        assertNotNull(response);
        assertTrue(response.is(Parameter.EXCEPTION_FLAG));
        assertEquals("Invalid id [null]", response.string(Parameter.EXCEPTION_MESSAGE));

        response = test.executeFunction( function, Json.map().set("firstName", "First name").set("id", ""), true);
        assertNotNull(response);
        assertTrue(response.is(Parameter.EXCEPTION_FLAG));
        assertEquals("Invalid id [null]", response.string(Parameter.EXCEPTION_MESSAGE));

        // findCustomerByChargifyId
        function = "findCustomerByChargifyId";

        response = test.executeFunction( function, true);
        assertNotNull(response);
        assertTrue(response.is(Parameter.EXCEPTION_FLAG));
        assertEquals("Customer is empty", response.string(Parameter.EXCEPTION_MESSAGE));

        response = test.executeFunction( function, Json.map(), true);
        assertNotNull(response);
        assertTrue(response.is(Parameter.EXCEPTION_FLAG));
        assertEquals("Customer is empty", response.string(Parameter.EXCEPTION_MESSAGE));

        response = test.executeFunction( function, Json.map().set("firstName", "First name"), true);
        assertNotNull(response);
        assertTrue(response.is(Parameter.EXCEPTION_FLAG));
        assertEquals("Invalid chargify id [null]", response.string(Parameter.EXCEPTION_MESSAGE));

        response = test.executeFunction( function, Json.map().set("firstName", "First name").set("chargifyId", ""), true);
        assertNotNull(response);
        assertTrue(response.is(Parameter.EXCEPTION_FLAG));
        assertEquals("Invalid chargify id [null]", response.string(Parameter.EXCEPTION_MESSAGE));

        response = test.executeFunction( function, Json.map().set("chargifyId", "First name"), true);
        assertNotNull(response);
        assertTrue(response.is(Parameter.EXCEPTION_FLAG));
        assertEquals("Invalid chargify id [First name]", response.string(Parameter.EXCEPTION_MESSAGE));

        response = test.executeFunction( function, Json.map().set("chargifyId", -100), true);
        assertNotNull(response);
        assertTrue(response.is(Parameter.EXCEPTION_FLAG));
        assertEquals("Invalid chargify id [-100]", response.string(Parameter.EXCEPTION_MESSAGE));

        // removeCustomer
        function = "removeCustomer";

        response = test.executeFunction( function, true);
        assertNotNull(response);
        assertTrue(response.is(Parameter.EXCEPTION_FLAG));
        assertEquals("Customer is empty", response.string(Parameter.EXCEPTION_MESSAGE));

        response = test.executeFunction( function, Json.map(), true);
        assertNotNull(response);
        assertTrue(response.is(Parameter.EXCEPTION_FLAG));
        assertEquals("Customer is empty", response.string(Parameter.EXCEPTION_MESSAGE));

        response = test.executeFunction( function, Json.map().set("firstName", "First name"), true);
        assertNotNull(response);
        assertTrue(response.is(Parameter.EXCEPTION_FLAG));
        assertEquals("Invalid chargify id [null]", response.string(Parameter.EXCEPTION_MESSAGE));

        response = test.executeFunction( function, Json.map().set("firstName", "First name").set("chargifyId", ""), true);
        assertNotNull(response);
        assertTrue(response.is(Parameter.EXCEPTION_FLAG));
        assertEquals("Invalid chargify id [null]", response.string(Parameter.EXCEPTION_MESSAGE));

        response = test.executeFunction( function, Json.map().set("chargifyId", "First name"), true);
        assertNotNull(response);
        assertTrue(response.is(Parameter.EXCEPTION_FLAG));
        assertEquals("Invalid chargify id [First name]", response.string(Parameter.EXCEPTION_MESSAGE));

        response = test.executeFunction( function, Json.map().set("chargifyId", -100), true);
        assertNotNull(response);
        assertTrue(response.is(Parameter.EXCEPTION_FLAG));
        assertEquals("Invalid chargify id [-100]", response.string(Parameter.EXCEPTION_MESSAGE));

        logger.info("-- END");
    }

    private void checkConsumer(String id, Integer chargifyId, String email, Json response) {
        assertNotNull(response);
        if(response.contains(Parameter.EXCEPTION_FLAG) && response.is(Parameter.EXCEPTION_FLAG)){
            assertEquals("An error happens", "", response.toString());
        }
        assertEquals(id, response.string("id"));
        if(chargifyId == null || chargifyId < 0) {
            assertNotNull(response.integer("chargifyId"));
        } else {
            assertEquals(chargifyId, response.integer("chargifyId"));
        }
        assertEquals(email, response.string("email"));
        assertEquals("First name", response.string("firstName"));
        assertEquals("Last name", response.string("lastName"));
        assertNotNull(response.string("createdAt"));
        assertNotNull(response.string("updatedAt"));
    }

    @Test
    public void testPaymentProfileFunctions() throws Exception {
        Json response;

        // create customer
        final String customerId = randomId();
        final int customerChargifyId = createCustomer(customerId);

        // payment profile
        final String id = randomId();

        response = test.executeFunction( "createPaymentProfile",
                Json.map().set("id", id)
                        .set("customerId", customerChargifyId)
                        .set("creditCardNumber", "2222")
                        .set("expirationMonth", "02")
                        .set("expirationYear", "2017"), true);
        assertEquals("Credit card: cannot be expired.", response.json(Parameter.EXCEPTION_ADDITIONAL_INFO).json("body").objects("errors").get(0));

        response = test.executeFunction( "createPaymentProfile",
                Json.map().set("id", id)
                        .set("customerId", customerChargifyId)
                        .set("creditCardNumber", "4111111111111111")
                        .set("expirationMonth", "02")
                        .set("expirationYear", "2020"));
        checkPaymentProfile("visa", "XXXX-XXXX-XXXX-1111", null, response);

        final int chargifyId = response.integer("chargifyId");

        response = test.executeFunction( "updatePaymentProfile",
                Json.map().set("id", id)
                        .set("chargifyId", chargifyId)
                        .set("customerId", customerChargifyId)
                        .set("creditCardNumber", "5555555555554444")
                        .set("expirationMonth", "05")
                        .set("expirationYear", "2025"));
        checkPaymentProfile("master", "XXXX-XXXX-XXXX-4444", chargifyId, response);

        // remove customer
        response = test.executeFunction( "removeCustomer", Json.map().set("chargifyId", customerChargifyId));
        assertNotNull(response);
        if(response.contains(Parameter.EXCEPTION_FLAG) && response.is(Parameter.EXCEPTION_FLAG)){
            assertEquals("An error happens", "", response.toString());
        }
        assertEquals((Integer) customerChargifyId, response.integer("chargifyId"));
        assertTrue(response.bool("removed"));

        logger.info("-- END");
    }

    @Test
    public void testInvalidPaymentProfileFunctions() throws Exception {
        Json response;
        String function;

        // createPaymentProfile
        function = "createPaymentProfile";

        response = test.executeFunction( function, true);
        assertNotNull(response);
        assertTrue(response.is(Parameter.EXCEPTION_FLAG));
        assertEquals("Payment profile is empty", response.string(Parameter.EXCEPTION_MESSAGE));

        response = test.executeFunction( function, Json.map(), true);
        assertNotNull(response);
        assertTrue(response.is(Parameter.EXCEPTION_FLAG));
        assertEquals("Payment profile is empty", response.string(Parameter.EXCEPTION_MESSAGE));

        response = test.executeFunction( function, Json.map().set("paymentType", "credit_card"), true);
        assertNotNull(response);
        assertTrue(response.is(Parameter.EXCEPTION_FLAG));
        assertEquals("Invalid customer id [null]", response.string(Parameter.EXCEPTION_MESSAGE));

        response = test.executeFunction( function, Json.map().set("paymentType", "credit_card").set("customerId", ""), true);
        assertNotNull(response);
        assertTrue(response.is(Parameter.EXCEPTION_FLAG));
        assertEquals("Invalid customer id [null]", response.string(Parameter.EXCEPTION_MESSAGE));

        response = test.executeFunction( function, Json.map().set("customerId", "First name"), true);
        assertNotNull(response);
        assertTrue(response.is(Parameter.EXCEPTION_FLAG));
        assertEquals("Invalid customer id [First name]", response.string(Parameter.EXCEPTION_MESSAGE));

        response = test.executeFunction( function, Json.map().set("customerId", -100), true);
        assertNotNull(response);
        assertTrue(response.is(Parameter.EXCEPTION_FLAG));
        assertEquals("Invalid customer id [-100]", response.string(Parameter.EXCEPTION_MESSAGE));

        response = test.executeFunction( function, Json.map().set("customerId", 1).set("paymentType", "credit_cards"), true);
        assertNotNull(response);
        assertTrue(response.is(Parameter.EXCEPTION_FLAG));
        assertEquals("Invalid paymentType [credit_cards]. Valid values: empty (credit card), 'credit_card' and 'bank_account'", response.string(Parameter.EXCEPTION_MESSAGE));

        response = test.executeFunction( function, Json.map().set("customerId", 1).set("paymentType", "bank"), true);
        assertNotNull(response);
        assertTrue(response.is(Parameter.EXCEPTION_FLAG));
        assertEquals("Invalid paymentType [bank]. Valid values: empty (credit card), 'credit_card' and 'bank_account'", response.string(Parameter.EXCEPTION_MESSAGE));

        response = test.executeFunction( function, Json.map().set("customerId", 1), true);
        assertNotNull(response);
        assertTrue(response.is(Parameter.EXCEPTION_FLAG));
        assertEquals("Invalid empty fields [creditCardNumber, expirationMonth, expirationYear]", response.string(Parameter.EXCEPTION_MESSAGE));

        response = test.executeFunction( function, Json.map().set("customerId", 1).set("paymentType", ""), true);
        assertNotNull(response);
        assertTrue(response.is(Parameter.EXCEPTION_FLAG));
        assertEquals("Invalid empty fields [creditCardNumber, expirationMonth, expirationYear]", response.string(Parameter.EXCEPTION_MESSAGE));

        response = test.executeFunction( function, Json.map().set("customerId", 1).set("paymentType", "CREDIT_card"), true);
        assertNotNull(response);
        assertTrue(response.is(Parameter.EXCEPTION_FLAG));
        assertEquals("Invalid empty fields [creditCardNumber, expirationMonth, expirationYear]", response.string(Parameter.EXCEPTION_MESSAGE));

        response = test.executeFunction( function, Json.map().set("customerId", 1).set("paymentType", "CREDIT_card").set("expirationMonth", "02"), true);
        assertNotNull(response);
        assertTrue(response.is(Parameter.EXCEPTION_FLAG));
        assertEquals("Invalid empty fields [creditCardNumber, expirationYear]", response.string(Parameter.EXCEPTION_MESSAGE));

        response = test.executeFunction( function, Json.map().set("customerId", 1).set("creditCardNumber", "4111111111111111").set("expirationMonth", "02").set("expirationYear", "02"), true); // invalid customer id
        assertNotNull(response);
        assertTrue(response.is(Parameter.EXCEPTION_FLAG));
        assertEquals("Invalid response code [404]", response.string(Parameter.EXCEPTION_MESSAGE));

        response = test.executeFunction( function, Json.map().set("customerId", 1).set("paymentType", "bank_account"), true);
        assertNotNull(response);
        assertTrue(response.is(Parameter.EXCEPTION_FLAG));
        assertEquals("Invalid empty fields [bankName, bankRoutingNumber, bankAccountNumber]", response.string(Parameter.EXCEPTION_MESSAGE));

        response = test.executeFunction( function, Json.map().set("customerId", 1).set("paymentType", "bank_account").set("bankRoutingNumber", "ABC"), true);
        assertNotNull(response);
        assertTrue(response.is(Parameter.EXCEPTION_FLAG));
        assertEquals("Invalid empty fields [bankName, bankAccountNumber]", response.string(Parameter.EXCEPTION_MESSAGE));

        logger.info("-- END");
    }

    @Test
    public void testPaymentProfileEvents() throws Exception {
        Json response;

        // create customer
        final String customerId = randomId();
        final int customerChargifyId = createCustomer(customerId);

        // payment profile
        final String id = randomId();
        final int chargifyId = createPaymentProfile(customerChargifyId, id);

        response = test.executeFunction( "updatePaymentProfile",
                Json.map().set("id", id)
                        .set("chargifyId", chargifyId)
                        .set("customerId", customerChargifyId)
                        .set("creditCardNumber", "5555555555554444")
                        .set("expirationMonth", "05")
                        .set("expirationYear", "2020"));
        checkPaymentProfile("master", "XXXX-XXXX-XXXX-4444", chargifyId, response);

        // remove customer
        response = test.executeFunction( "removeCustomer", Json.map().set("chargifyId", customerChargifyId));
        assertNotNull(response);
        if(response.contains(Parameter.EXCEPTION_FLAG) && response.is(Parameter.EXCEPTION_FLAG)){
            assertEquals("An error happens", "", response.toString());
        }
        assertEquals((Integer) customerChargifyId, response.integer("chargifyId"));
        assertTrue(response.bool("removed"));

        logger.info("-- END");
    }

    private void checkPaymentProfile(String creditCardType, String creditCard, Integer chargifyId, Json response) {
        assertNotNull(response);
        if(response.contains(Parameter.EXCEPTION_FLAG) && response.is(Parameter.EXCEPTION_FLAG)){
            assertEquals("An error happens", "", response.toString());
        }
        if(chargifyId == null || chargifyId < 0) {
            assertNotNull(response.integer("chargifyId"));
        } else {
            assertEquals(chargifyId, response.integer("chargifyId"));
        }
        assertEquals(creditCardType, response.string("creditCardType"));
        assertEquals(creditCard, response.string("creditCardNumber"));
    }

    @Test
    public void testSubscriptionFunctions() throws Exception {
        Json response;

        // create customer
        final String customerId = randomId();
        final int customerChargifyId = createCustomer(customerId);

        // create payment profile
        final String paymentProfileId = randomId();
        final int paymentProfileChargifyId = createPaymentProfile(customerChargifyId, paymentProfileId);

        // create subscription
        final String id = randomId();
        final int chargifyId = createSubscription(customerChargifyId, paymentProfileChargifyId, id);

        // update subscription
        response = test.executeFunction( "updateSubscription", Json.map().set("chargifyId", chargifyId).set("id", id).set("customerId", customerChargifyId).set("paymentProfileId", paymentProfileChargifyId).set("productHandle", "product-2"));
        checkSubscription("product-2", chargifyId, response);

        // cancel subscription
        cancelSubscription(chargifyId);

        logger.info("-- END");
    }

    @Test
    public void testInvalidSubscriptionFunctions() throws Exception {
        Json response;
        String function;

        // createPaymentProfile
        function = "createSubscription";

        response = test.executeFunction( function, true);
        assertNotNull(response);
        assertTrue(response.is(Parameter.EXCEPTION_FLAG));
        assertEquals("Subscription is empty", response.string(Parameter.EXCEPTION_MESSAGE));

        response = test.executeFunction( function, Json.map(), true);
        assertNotNull(response);
        assertTrue(response.is(Parameter.EXCEPTION_FLAG));
        assertEquals("Subscription is empty", response.string(Parameter.EXCEPTION_MESSAGE));

        response = test.executeFunction( function, Json.map().set("state", "active"), true);
        assertNotNull(response);
        assertTrue(response.is(Parameter.EXCEPTION_FLAG));
        assertEquals("Invalid customer id [null]", response.string(Parameter.EXCEPTION_MESSAGE));

        response = test.executeFunction( function, Json.map().set("state", "active").set("customerId", ""), true);
        assertNotNull(response);
        assertTrue(response.is(Parameter.EXCEPTION_FLAG));
        assertEquals("Invalid customer id [null]", response.string(Parameter.EXCEPTION_MESSAGE));

        response = test.executeFunction( function, Json.map().set("customerId", "First name"), true);
        assertNotNull(response);
        assertTrue(response.is(Parameter.EXCEPTION_FLAG));
        assertEquals("Invalid customer id [First name]", response.string(Parameter.EXCEPTION_MESSAGE));

        response = test.executeFunction( function, Json.map().set("customerId", "-100"), true);
        assertNotNull(response);
        assertTrue(response.is(Parameter.EXCEPTION_FLAG));
        assertEquals("Invalid customer id [-100]", response.string(Parameter.EXCEPTION_MESSAGE));

        response = test.executeFunction( function, Json.map().set("customerId", 1), true);
        assertNotNull(response);
        assertTrue(response.is(Parameter.EXCEPTION_FLAG));
        assertEquals("Invalid payment profile id [null]", response.string(Parameter.EXCEPTION_MESSAGE));

        response = test.executeFunction( function, Json.map().set("customerId", 1).set("paymentProfileId", ""), true);
        assertNotNull(response);
        assertTrue(response.is(Parameter.EXCEPTION_FLAG));
        assertEquals("Invalid payment profile id [null]", response.string(Parameter.EXCEPTION_MESSAGE));

        response = test.executeFunction( function, Json.map().set("customerId", 1).set("paymentProfileId", "First name"), true);
        assertNotNull(response);
        assertTrue(response.is(Parameter.EXCEPTION_FLAG));
        assertEquals("Invalid payment profile id [First name]", response.string(Parameter.EXCEPTION_MESSAGE));

        response = test.executeFunction( function, Json.map().set("customerId", 1).set("paymentProfileId", "-100"), true);
        assertNotNull(response);
        assertTrue(response.is(Parameter.EXCEPTION_FLAG));
        assertEquals("Invalid payment profile id [-100]", response.string(Parameter.EXCEPTION_MESSAGE));

        response = test.executeFunction( function, Json.map().set("customerId", 1).set("paymentProfileId", 1), true);
        assertNotNull(response);
        assertTrue(response.is(Parameter.EXCEPTION_FLAG));
        assertEquals("Invalid product handle [null]", response.string(Parameter.EXCEPTION_MESSAGE));

        response = test.executeFunction( function, Json.map().set("customerId", 1).set("paymentProfileId", 1).set("productHandle", ""), true);
        assertNotNull(response);
        assertTrue(response.is(Parameter.EXCEPTION_FLAG));
        assertEquals("Invalid product handle [null]", response.string(Parameter.EXCEPTION_MESSAGE));

        logger.info("-- END");
    }

    @Test
    public void testSubscriptionEvents() throws Exception {
        Json response;

        // create customer
        final String customerId = randomId();
        final int customerChargifyId = createCustomer(customerId);

        // create payment profile
        final String paymentProfileId = randomId();
        final int paymentProfileChargifyId = createPaymentProfile(customerChargifyId, paymentProfileId);

        // create subscription
        final String id = randomId();
        final int chargifyId = createSubscription(customerChargifyId, paymentProfileChargifyId, id);

        // update subscription
        response = test.executeFunction( "updateSubscription", Json.map().set("chargifyId", chargifyId).set("id", id).set("customerId", customerChargifyId).set("paymentProfileId", paymentProfileChargifyId).set("productHandle", "product-2"));
        checkSubscription("product-2", chargifyId, response);

        // cancel subscription
        cancelSubscription(chargifyId);

        logger.info("-- END");
    }

    private void checkSubscription(String productHandle, Integer chargifyId, Json response) {
        assertNotNull(response);
        if(response.contains(Parameter.EXCEPTION_FLAG) && response.is(Parameter.EXCEPTION_FLAG)){
            assertEquals("An error happens", "", response.toString());
        }
        if(chargifyId == null || chargifyId < 0) {
            assertNotNull(response.integer("chargifyId"));
        } else {
            assertEquals(chargifyId, response.integer("chargifyId"));
        }
        assertEquals(productHandle, response.string("productHandle"));
        assertEquals("active", response.string("state"));
    }

    @Test
    public void testWebhooks() throws Exception {
        Json response;
        Json subscription;
        Json customer;

        response = sendWebhookRequest("signup_success");
        assertEquals("ok", response.string("body"));
        /*
        TODO check endpoint events received by ES
        assertNotNull(response);
        assertEquals("signup_success", response.json("body").string("event"));
        subscription = response.json("body").json("payload").json("subscription");
        assertEquals("9780674", subscription.string("id"));
        assertEquals("9498938", subscription.json("customer").string("id"));
        assertEquals("email@slingr.io", subscription.json("customer").string("email"));
        assertEquals("6471303", subscription.json("credit_card").string("id"));
        assertEquals("visa", subscription.json("credit_card").string("card_type"));
        assertEquals("product-1", subscription.json("product").string("handle"));
        assertEquals("Product 1", subscription.json("product").string("name"));
        assertEquals("active", subscription.string("state"));
        assertEquals("active", subscription.string("previous_state"));
        */

        response = sendWebhookRequest("subscription_state_change");
        assertEquals("ok", response.string("body"));
        /*
        assertNotNull(response);
        assertEquals("subscription_state_change", response.json("body").string("event"));
        subscription = response.json("body").json("payload").json("subscription");
        assertEquals("9780655", subscription.string("id"));
        assertEquals("9498920", subscription.json("customer").string("id"));
        assertEquals("email@slingr.io", subscription.json("customer").string("email"));
        assertEquals("6471289", subscription.json("credit_card").string("id"));
        assertEquals("visa", subscription.json("credit_card").string("card_type"));
        assertEquals("product-2", subscription.json("product").string("handle"));
        assertEquals("Product 2", subscription.json("product").string("name"));
        assertEquals("canceled", subscription.string("state"));
        assertEquals("active", subscription.string("previous_state"));
        */

        response = sendWebhookRequest("customer_update");
        assertEquals("ok", response.string("body"));
        /*
        assertNotNull(response);
        assertEquals("customer_update", response.json("body").string("event"));
        customer = response.json("body").json("payload").json("customer");
        assertEquals("9498920", customer.string("id"));
        assertEquals("Test", customer.string("last_name"));
        assertEquals("US", customer.string("country"));
        assertEquals("CT", customer.string("state"));
        assertEquals("email@slingr.io", customer.string("email"));
        */

        logger.info("-- END");
    }

    @Test
    public void testSelfServiceUrl() throws Exception {
        Json response;

        // create customer
        final String customerId = randomId();
        final int customerChargifyId = createCustomer(customerId);

        // create payment profile
        final String paymentProfileId = randomId();
        final int paymentProfileChargifyId = createPaymentProfile(customerChargifyId, paymentProfileId);

        // create subscription
        final String id = randomId();
        final int chargifyId = createSubscription(customerChargifyId, paymentProfileChargifyId, id);

        // calculates self service url
        response = test.executeFunction("calculateSelfServiceUrl", Json.map().set("chargifyId", chargifyId));
        assertNotNull(response);
        logger.info(String.format("Self Services URL: [%s]", response.string("body")));
        assertNotSame("-", response.string("body"));

        // cancel subscription
        cancelSubscription(chargifyId);

        logger.info("-- END");
    }


    private void cancelSubscription(int chargifyId) throws Exception {
        Json response;
        response = test.executeFunction( "cancelSubscription", Json.map().set("chargifyId", chargifyId).set("cancellationMessage", "Cancelled by a test."));
        assertNotNull(response);
        if(response.contains(Parameter.EXCEPTION_FLAG) && response.is(Parameter.EXCEPTION_FLAG)){
            assertEquals("An error happens", "", response.toString());
        }
        assertEquals((Integer) chargifyId, response.integer("chargifyId"));
        assertTrue(response.bool("canceled"));
    }

    private int createSubscription(int customerId, int paymentProfileChargifyId, String id) throws Exception {
        Json response;
        response = test.executeFunction( "createSubscription", Json.map()
                .set("id", id)
                .set("customerId", customerId)
                .set("paymentProfileId", paymentProfileChargifyId)
                .set("productHandle", "product-1"));
        checkSubscription("product-1", null, response);

        return response.integer("chargifyId");
    }

    private int createCustomer(String customerId) throws Exception {
        Json response;
        response = test.executeFunction( "createCustomer", Json.map()
                .set("id", customerId)
                .set("lastName", "Last name")
                .set("firstName", "First name")
                .set("email", "email@slingr.io"));
        checkConsumer(customerId, null, "email@slingr.io", response);
        return response.integer("chargifyId");
    }

    private int createPaymentProfile(int customerChargifyId, String paymentProfileId) throws Exception {
        Json response;
        response = test.executeFunction( "createPaymentProfile", Json.map()
                .set("id", paymentProfileId)
                .set("customerId", customerChargifyId)
                .set("creditCardNumber", "4111111111111111")
                .set("expirationMonth", "02")
                .set("expirationYear", "2040"));
        checkPaymentProfile("visa", "XXXX-XXXX-XXXX-1111", null, response);
        return response.integer("chargifyId");
    }

    private Json sendWebhookRequest(String filename) throws Exception{
        final String content = FilesUtils.readInternalFile(String.format("webhooks/%s", filename));
        return test.executeWebServices(RestMethod.POST, "", content, Json.map().set(Parameter.CONTENT_TYPE, "application/x-www-form-urlencoded")).toJson(false);
    }
}