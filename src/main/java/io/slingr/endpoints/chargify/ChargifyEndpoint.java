package io.slingr.endpoints.chargify;

import io.slingr.endpoints.HttpEndpoint;
import io.slingr.endpoints.chargify.utils.Convert;
import io.slingr.endpoints.exceptions.EndpointException;
import io.slingr.endpoints.exceptions.ErrorCode;
import io.slingr.endpoints.framework.annotations.EndpointFunction;
import io.slingr.endpoints.framework.annotations.EndpointProperty;
import io.slingr.endpoints.framework.annotations.EndpointWebService;
import io.slingr.endpoints.framework.annotations.SlingrEndpoint;
import io.slingr.endpoints.services.rest.HttpRequest;
import io.slingr.endpoints.services.rest.RestMethod;
import io.slingr.endpoints.utils.Json;
import io.slingr.endpoints.utils.Strings;
import io.slingr.endpoints.ws.exchange.WebServiceRequest;
import io.slingr.endpoints.ws.exchange.WebServiceResponse;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * <p>Chargify endpoint
 *
 * <p>Created by lefunes on 07/30/15.
 */
@SlingrEndpoint(name = "chargify")
public class ChargifyEndpoint extends HttpEndpoint {
    private static final Logger logger = LoggerFactory.getLogger(ChargifyEndpoint.class);

    private static final String FORMAT = ".json";
    private static final String UNIQUENESS_TOKEN = "uniqueness_token";

    private static final String STATS = "stats";
    private static final String CUSTOMERS = "customers";
    private static final String PAYMENT_PROFILES = "payment_profiles";
    private static final String SUBSCRIPTIONS = "subscriptions";

    private static final String WRAPPER_CUSTOMER = "customer";
    private static final String WRAPPER_PAYMENT_PROFILE = "payment_profile";
    private static final String WRAPPER_SUBSCRIPTION = "subscription";

    private static final String CUSTOMER_LABEL = "Customer";
    private static final String PAYMENT_PROFILE_LABEL = "Payment profile";
    private static final String SUBSCRIPTION_LABEL = "Subscription";

    @EndpointProperty
    private String apiKey;

    @EndpointProperty
    private String subdomain;

    @EndpointProperty
    private String siteSharedKey;

    @Override
    public String getApiUri() {
        if(StringUtils.isBlank(subdomain)){
            throw EndpointException.permanent(ErrorCode.ARGUMENT, "Invalid empty subdomain.");
        }
        return String.format("https://%s.chargify.com", subdomain);
    }

    @Override
    public void endpointStarted() {
        // authentication
        if(StringUtils.isBlank(apiKey)){
            throw EndpointException.permanent(ErrorCode.ARGUMENT, "Invalid empty apiKey.");
        }
        httpService().setupBasicAuthentication(apiKey, "x");

        logger.info(String.format("Configured Chargify endpoint: apiKey [%s] - subdomain [%s] - site shared key [%s]", Strings.maskToken(apiKey), subdomain, Strings.maskToken(siteSharedKey)));
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Stats
    ///////////////////////////////////////////////////////////////////////////////////////////////

    @EndpointFunction(name = "getStats")
    public Json getStats(){
        final HttpRequest request = generateRequest(RestMethod.GET, STATS);
        return httpService().httpGet(request);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Customer
    ///////////////////////////////////////////////////////////////////////////////////////////////

    @EndpointFunction(name = "createCustomer")
    public Json createCustomer(Json body){

        final Json customer = Convert.customerToChargify(body);
        logger.info(String.format("Customer to create on Chargify [%s]", customer));

        Convert.checkJson(CUSTOMER_LABEL, customer);

        final List<String> invalidFields = Convert.checkCustomerNoEmptyParameters(customer);
        Convert.checkInvalidFields(invalidFields);

        final HttpRequest request = generateRequest(RestMethod.POST, CUSTOMERS);
        request.setBody(wrapRequest(WRAPPER_CUSTOMER, customer));

        final Json response = httpService().httpPost(request);
        final Json customerCreated = response.json(WRAPPER_CUSTOMER);
        if(customerCreated == null){
            throw EndpointException.permanent(ErrorCode.ARGUMENT, "HTTP 404 Not Found").returnCode(404);
        }

        final Json converted = Convert.customerToSlingr(customerCreated, body.string("id"));
        logger.info(String.format("Customer created [%s]", converted));
        return converted;
    }

    @EndpointFunction(name = "updateCustomer")
    public Json updateCustomer(Json body){

        final Json customer = Convert.customerToChargify(body);
        logger.info(String.format("Customer to update on Chargify [%s]", customer));

        Convert.checkJson(CUSTOMER_LABEL, customer);
        final int chargifyId = Convert.checkChargifyId(customer);

        final HttpRequest request = generateRequest(RestMethod.PUT, CUSTOMERS, chargifyId);
        request.setBody(wrapRequest(WRAPPER_CUSTOMER, customer));

        final Json response = httpService().httpPut(request);
        final Json customerUpdated = response.json(WRAPPER_CUSTOMER);
        if(customerUpdated == null){
            throw EndpointException.permanent(ErrorCode.ARGUMENT, "HTTP 404 Not Found").returnCode(404);
        }

        final Json converted = Convert.customerToSlingr(customerUpdated, body.string("id"));
        logger.info(String.format("Customer updated [%s]", converted));
        return converted;
    }

    @EndpointFunction(name = "findCustomerByChargifyId")
    public Json findCustomerByChargifyId(Json body){

        final Json customer = Convert.customerToChargify(body);
        logger.info(String.format("Find customer by chargify id on Chargify [%s]", customer));

        Convert.checkJson(CUSTOMER_LABEL, customer);
        final int chargifyId = Convert.checkChargifyId(customer);

        final HttpRequest request = generateRequest(RestMethod.GET, CUSTOMERS, chargifyId);

        final Json response = httpService().httpGet(request);
        final Json customerFound =  response.json(WRAPPER_CUSTOMER);
        if(customerFound == null){
            throw EndpointException.permanent(ErrorCode.ARGUMENT, "HTTP 404 Not Found").returnCode(404);
        }

        final Json converted = Convert.customerToSlingr(customerFound, body.string("id"));
        logger.info(String.format("Customer found [%s]", converted));
        return converted;
    }

    @EndpointFunction(name = "findCustomerById")
    public Json findCustomerById(Json body){

        final Json customer = Convert.customerToChargify(body);
        logger.info(String.format("Find customer by id on Chargify [%s]", customer));

        Convert.checkJson(CUSTOMER_LABEL, customer);
        final String id = Convert.checkId(customer);

        final HttpRequest request = generateRequest(RestMethod.GET, CUSTOMERS, id);
        final Json response = httpService().httpGet(request);
        final Json customerFound = response.json(WRAPPER_CUSTOMER);
        if(customerFound == null){
            throw EndpointException.permanent(ErrorCode.ARGUMENT, "HTTP 404 Not Found").returnCode(404);
        }

        final Json converted = Convert.customerToSlingr(customerFound, body.string("id"));
        logger.info(String.format("Customer found [%s]", converted));
        return converted;
    }

    @EndpointFunction(name = "removeCustomer")
    public Json removeCustomer(Json body){

        final Json customer = Convert.customerToChargify(body);
        logger.info(String.format("Remove customer from Chargify [%s]", customer));

        Convert.checkJson(CUSTOMER_LABEL, customer);
        final int chargifyId = Convert.checkChargifyId(customer);

        final HttpRequest request = generateRequest(RestMethod.DELETE, CUSTOMERS, chargifyId);
        try {
            httpService().httpDelete(request);
        } catch (EndpointException ex) {
            if ("HTTP 404 Not Found".equalsIgnoreCase(ex.getMessage())) {
                return Json.map().set("id", chargifyId).set("removed", false);
            }
            throw ex;
        }
        final Json customerDeleted = Json.map().set("id", chargifyId).set("removed", true);
        if(customerDeleted == null){
            throw EndpointException.permanent(ErrorCode.ARGUMENT, "HTTP 404 Not Found").returnCode(404);
        }

        final Json converted = Convert.customerToSlingr(customerDeleted, body.string("id"));
        logger.info(String.format("Customer deleted [%s]", converted));
        return converted;
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Payment profile
    ///////////////////////////////////////////////////////////////////////////////////////////////

    @EndpointFunction(name = "createPaymentProfile")
    public Json createPaymentProfile(Json body){

        final Json paymentProfile = Convert.paymentProfileToChargify(body);
        logger.info(String.format("Create payment profile on Chargify [%s]", paymentProfile));

        Convert.checkJson(PAYMENT_PROFILE_LABEL, paymentProfile);
        Convert.checkPaymentProfileParameters(paymentProfile);

        final List<String> invalidFields = Convert.checkPaymentProfileNoEmptyParameters(paymentProfile);
        Convert.checkInvalidFields(invalidFields);

        final HttpRequest request = generateRequest(RestMethod.POST, PAYMENT_PROFILES);
        request.setBody(wrapRequest(WRAPPER_PAYMENT_PROFILE, paymentProfile));

        final Json response = httpService().httpPost(request);
        if(!response.contains(WRAPPER_PAYMENT_PROFILE) && response.contains("errors")){
            String message = "Client error";
            final List<String> errors = response.strings("errors");
            if(errors != null && errors.size() > 0){
                message = errors.get(0);
            }
            throw EndpointException.permanent(ErrorCode.API, message, response);
        }
        final Json paymentProfileCreated = response.json(WRAPPER_PAYMENT_PROFILE);
        if(paymentProfileCreated == null){
            throw EndpointException.permanent(ErrorCode.ARGUMENT, "HTTP 404 Not Found").returnCode(404);
        }

        final Json converted = Convert.paymentProfileToSlingr(paymentProfileCreated, body.string("id"));
        logger.info(String.format("Payment profile created [%s]", converted));
        return converted;
    }

    @EndpointFunction(name = "updatePaymentProfile")
    public Json updatePaymentProfile(Json body){

        final Json paymentProfile = Convert.paymentProfileToChargify(body);
        logger.info(String.format("Payment profile to update on Chargify [%s]", paymentProfile));

        Convert.checkJson(PAYMENT_PROFILE_LABEL, paymentProfile);
        final int chargifyId = Convert.checkChargifyId(paymentProfile);

        final HttpRequest request = generateRequest(RestMethod.PUT, PAYMENT_PROFILES, chargifyId);
        request.setBody(wrapRequest(WRAPPER_PAYMENT_PROFILE, paymentProfile));

        final Json response = httpService().httpPut(request);
        final Json paymentProfileUpdated = response.json(WRAPPER_PAYMENT_PROFILE);
        if(paymentProfileUpdated == null){
            throw EndpointException.permanent(ErrorCode.ARGUMENT, "HTTP 404 Not Found").returnCode(404);
        }

        final Json converted = Convert.paymentProfileToSlingr(paymentProfileUpdated, body.string("id"));
        logger.info(String.format("Payment profile updated [%s]", converted));
        return converted;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Subscriptions
    ///////////////////////////////////////////////////////////////////////////////////////////////

    @EndpointFunction(name = "createSubscription")
    public Json createSubscription(Json body){

        final Json subscription = Convert.subscriptionToChargify(body);
        logger.info(String.format("Create subscription on Chargify [%s]", subscription));

        Convert.checkJson(SUBSCRIPTION_LABEL, subscription);
        Convert.checkSubscriptionParameters(subscription);

        final HttpRequest request = generateRequest(RestMethod.POST, SUBSCRIPTIONS);
        request.setBody(wrapRequest(WRAPPER_SUBSCRIPTION, subscription));

        final Json response = httpService().httpPost(request);
        final Json subscriptionCreated = response.json(WRAPPER_SUBSCRIPTION);
        if(subscriptionCreated == null){
            throw new IllegalStateException("HTTP 404 Not Found");
        }

        final Json converted = Convert.subscriptionToSlingr(subscriptionCreated, body.string("id"));
        logger.info(String.format("Subscription created [%s]", converted));
        return converted;
    }

    @EndpointFunction(name = "updateSubscription")
    public Json updateSubscription(Json body){

        final Json subscription = Convert.subscriptionToChargify(body);
        logger.info(String.format("Subscription to update on Chargify [%s]", subscription));

        Convert.checkJson(SUBSCRIPTION_LABEL, subscription);
        final int chargifyId = Convert.checkChargifyId(subscription);

        final HttpRequest request = generateRequest(RestMethod.PUT, SUBSCRIPTIONS, chargifyId);
        request.setBody(wrapRequest(WRAPPER_SUBSCRIPTION, subscription));

        final Json response = httpService().httpPut(request);
        final Json subscriptionUpdated = response.json(WRAPPER_SUBSCRIPTION);
        if(subscriptionUpdated == null){
            throw new IllegalStateException("HTTP 404 Not Found");
        }

        final Json converted = Convert.subscriptionToSlingr(subscriptionUpdated, body.string("id"));
        logger.info(String.format("Subscription updated [%s]", converted));
        return converted;
    }

    @EndpointFunction(name = "cancelSubscription")
    public Json cancelSubscription(Json body){

        final Json subscription = Convert.subscriptionToChargify(body);
        logger.info(String.format("Subscription to cancel on Chargify [%s]", subscription));

        Convert.checkJson(SUBSCRIPTION_LABEL, subscription);
        final int chargifyId = Convert.checkChargifyId(subscription);

        final HttpRequest request = generateRequest(RestMethod.DELETE, SUBSCRIPTIONS, chargifyId);
        try {
            httpService().httpDelete(request);
        } catch (EndpointException ee) {
            if ("HTTP 404 Not Found".equalsIgnoreCase(ee.getMessage())) {
                return Json.map().set("id", chargifyId).set("canceled", false);
            }
            throw ee;
        }
        final Json canceledSubscription = Json.map().set("id", chargifyId).set("canceled", true);
        if(canceledSubscription == null){
            throw new IllegalStateException("HTTP 404 Not Found");
        }

        final Json converted = Convert.subscriptionToSlingr(canceledSubscription, body.string("id"));
        logger.info(String.format("Canceled subscription [%s]", converted));
        return converted;
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Self service url
    ///////////////////////////////////////////////////////////////////////////////////////////////

    @EndpointFunction(name = "calculateSelfServiceUrl")
    public Json calculateSelfServiceUrl(Json body){
        if(StringUtils.isBlank(siteSharedKey)){
            throw new IllegalStateException("Empty site shared key");
        }
        if(body == null || StringUtils.isBlank(body.string("chargifyId"))){
            throw new IllegalStateException("Empty chargify ID");
        }
        final String chargifyId = body.string("chargifyId");
        final String token = DigestUtils.sha1Hex(String.format("update_payment--%s--%s", chargifyId, siteSharedKey)).substring(0, 10);

        return Json.map().set("body", String.format("https://%s.chargify.com/update_payment/%s/%s", subdomain, chargifyId, token));
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Webhooks
    ///////////////////////////////////////////////////////////////////////////////////////////////

    @EndpointWebService(methods = RestMethod.POST)
    protected WebServiceResponse webhooks(WebServiceRequest request) {
        return defaultWebhookProcessor(request);
    }


    @EndpointWebService(methods = {RestMethod.GET, RestMethod.HEAD})
    public void exposeWebhookUri(WebServiceRequest request){
        // do nothing
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Helpers
    ///////////////////////////////////////////////////////////////////////////////////////////////

    private Json request(){
        return Json.map().set(UNIQUENESS_TOKEN, Strings.randomUUID());
    }

    private Json wrapRequest(String wrapper, Json request){
        return request().set(wrapper, request);
    }

    private HttpRequest generateRequest(RestMethod restMethod, String type){
        return generateRequest(restMethod, type, -1, null);
    }

    private HttpRequest generateRequest(RestMethod restMethod, String type, int chargifyId){
        return generateRequest(restMethod, type, chargifyId, null);
    }

    private HttpRequest generateRequest(RestMethod restMethod, String type, String reference){
        return generateRequest(restMethod, type, -1, reference);
    }

    private HttpRequest generateRequest(RestMethod restMethod, String type, int chargifyId, String reference){
        HttpRequest request;
        if(chargifyId < 1) {
            if(StringUtils.isNotBlank(reference)){
                request = new HttpRequest(restMethod, String.format("%s/lookup%s", type, FORMAT));
                request.getParams().set("reference", reference);
            } else {
                request = new HttpRequest(restMethod, String.format("%s%s", type, FORMAT));
            }
        } else {
            request = new HttpRequest(restMethod, String.format("%s/%s%s", type, chargifyId, FORMAT));
        }
        return request;
    }
}
