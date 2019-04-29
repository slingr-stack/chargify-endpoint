package io.slingr.endpoints.chargify.utils;

import io.slingr.endpoints.exceptions.EndpointException;
import io.slingr.endpoints.exceptions.ErrorCode;
import io.slingr.endpoints.utils.Json;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>Converters and checkers used by the Chargify endpoint classes
 *
 * <p>Created by lefunes on 02/08/15.
 */
public class Convert {

    public static void checkJson(String label, Json json) throws EndpointException {
        if(json == null || json.isEmpty()){
            throw EndpointException.permanent(ErrorCode.ARGUMENT, String.format("%s is empty", label));
        }
    }

    public static int checkChargifyId(Json json) throws EndpointException {
        if(!json.contains("id") || StringUtils.isBlank(json.string("id"))){
            throw EndpointException.permanent(ErrorCode.ARGUMENT, String.format("Invalid chargify id [%s]", json.string("id")));
        }
        int chargifyId;
        try{
            chargifyId = Integer.parseInt(json.string("id"));
        }catch (Exception ex){
            chargifyId = -1;
        }
        if(chargifyId < 0){
            throw EndpointException.permanent(ErrorCode.ARGUMENT, String.format("Invalid chargify id [%s]", json.string("id")));
        }
        return chargifyId;
    }

    public static String checkId(Json customer) throws EndpointException {
        if(StringUtils.isBlank(customer.string("reference"))){
            throw EndpointException.permanent(ErrorCode.ARGUMENT, String.format("Invalid id [%s]", customer.string("reference")));
        }
        return customer.string("reference");
    }

    public static void checkInvalidFields(List<String> invalidFields) throws EndpointException {
        if(invalidFields.size() > 0){
            if(invalidFields.size() > 1) {
                throw EndpointException.permanent(ErrorCode.ARGUMENT, String.format("Invalid empty fields [%s]", invalidFields.toString().replaceAll("[\\[\\]]", "")));
            } else {
                throw EndpointException.permanent(ErrorCode.ARGUMENT, String.format("Invalid empty field [%s]", invalidFields.get(0)));
            }
        }
    }

    public static Json customerToSlingr(Json chargifyCustomer, String slingrId) {
        final Json response = customerToSlingr(chargifyCustomer);
        if(StringUtils.isBlank(response.string("id"))) {
            response.set("id", slingrId);
        }
        return response;
    }

    public static Json customerToSlingr(Json chargifyCustomer) {
        final Json response = Json.map();
        if(chargifyCustomer != null && !chargifyCustomer.isEmpty()) {
            response.setIfNotNull("id", chargifyCustomer.string("reference"));
            response.setIfNotNull("chargifyId", chargifyCustomer.string("id"));
            response.setIfNotNull("firstName", chargifyCustomer.string("first_name"));
            response.setIfNotNull("lastName", chargifyCustomer.string("last_name"));
            response.setIfNotNull("email", chargifyCustomer.string("email"));
            response.setIfNotNull("organization", chargifyCustomer.string("organization"));
            response.setIfNotNull("vat", chargifyCustomer.string("vat_number"));
            response.setIfNotNull("shippingAddress1", chargifyCustomer.string("address"));
            response.setIfNotNull("shippingAddress2", chargifyCustomer.string("address_2"));
            response.setIfNotNull("shippingCity", chargifyCustomer.string("city"));
            response.setIfNotNull("shippingState", chargifyCustomer.string("state"));
            response.setIfNotNull("shippingZipCode", chargifyCustomer.string("zip"));
            response.setIfNotNull("shippingCountry", chargifyCustomer.string("country"));
            response.setIfNotNull("phone", chargifyCustomer.string("phone"));
            response.setIfNotNull("createdAt", chargifyCustomer.string("created_at"));
            response.setIfNotNull("updatedAt", chargifyCustomer.string("updated_at"));

            response.setIfNotNull("removed", chargifyCustomer.bool("removed", false));
        }
        return response;
    }

    public static Json customerToChargify(Json slingrCustomer) {
        final Json response = Json.map();
        if(slingrCustomer != null && !slingrCustomer.isEmpty()) {
            response.setIfNotNull("reference", slingrCustomer.string("id"));
            response.setIfNotNull("id", slingrCustomer.string("chargifyId"));
            response.setIfNotNull("first_name", slingrCustomer.string("firstName"));
            response.setIfNotNull("last_name", slingrCustomer.string("lastName"));
            response.setIfNotNull("email", slingrCustomer.string("email"));
            response.setIfNotNull("organization", slingrCustomer.string("organization"));
            response.setIfNotNull("vat_number", slingrCustomer.string("vat"));
            response.setIfNotNull("address", slingrCustomer.string("shippingAddress1"));
            response.setIfNotNull("address_2", slingrCustomer.string("shippingAddress2"));
            response.setIfNotNull("city", slingrCustomer.string("shippingCity"));
            response.setIfNotNull("state", slingrCustomer.string("shippingState"));
            response.setIfNotNull("zip", slingrCustomer.string("shippingZipCode"));
            response.setIfNotNull("country", slingrCustomer.string("shippingCountry"));
            response.setIfNotNull("phone", slingrCustomer.string("phone"));
        }
        return response;
    }

    public static List<String> checkCustomerNoEmptyParameters(Json chargifyCustomer) {
        final List<String> invalidFields = new ArrayList<>();
        if(chargifyCustomer == null || chargifyCustomer.isEmpty()){
            invalidFields.add("firstName");
            invalidFields.add("lastName");
            invalidFields.add("email");
        } else {
            if (StringUtils.isBlank(chargifyCustomer.string("first_name"))) {
                invalidFields.add("firstName");
            }
            if (StringUtils.isBlank(chargifyCustomer.string("last_name"))) {
                invalidFields.add("lastName");
            }
            if (StringUtils.isBlank(chargifyCustomer.string("email"))) {
                invalidFields.add("email");
            }
        }
        return invalidFields;
    }

    public static Json paymentProfileToSlingr(Json chargifyPaymentProfile, String slingrId) {
        final Json response = paymentProfileToSlingr(chargifyPaymentProfile);
        if(StringUtils.isBlank(response.string("id"))) {
            response.set("id", slingrId);
        }
        return response;
    }

    public static Json paymentProfileToSlingr(Json chargifyPaymentProfile) {
        final Json response = Json.map();
        if(chargifyPaymentProfile != null && !chargifyPaymentProfile.isEmpty()) {
            response.setIfNotNull("chargifyId", chargifyPaymentProfile.string("id"));
            response.setIfNotNull("customerId", chargifyPaymentProfile.string("customer_id"));
            response.setIfNotNull("paymentType", chargifyPaymentProfile.string("payment_type"));
            response.setIfNotNull("firstName", chargifyPaymentProfile.string("first_name"));
            response.setIfNotNull("lastName", chargifyPaymentProfile.string("last_name"));
            response.setIfNotNull("creditCardNumber", chargifyPaymentProfile.string("full_number"));
            if(StringUtils.isNotBlank(chargifyPaymentProfile.string("masked_card_number"))) {
                response.setIfNotNull("originalCreditCardNumber", response.string("creditCardNumber"));
                response.setIfNotNull("creditCardNumber", chargifyPaymentProfile.string("masked_card_number"));
            }
            response.setIfNotNull("creditCardType", chargifyPaymentProfile.string("card_type"));
            response.setIfNotNull("expirationMonth", chargifyPaymentProfile.string("expiration_month"));
            response.setIfNotNull("expirationYear", chargifyPaymentProfile.string("expiration_year"));
            response.setIfNotNull("cvv", chargifyPaymentProfile.string("cvv"));
            response.setIfNotNull("billingAddress1", chargifyPaymentProfile.string("billing_address"));
            response.setIfNotNull("billingAddress2", chargifyPaymentProfile.string("billing_address_2"));
            response.setIfNotNull("billingCity", chargifyPaymentProfile.string("billing_city"));
            response.setIfNotNull("billingState", chargifyPaymentProfile.string("billing_state"));
            response.setIfNotNull("billingZipCode", chargifyPaymentProfile.string("billing_zip"));
            response.setIfNotNull("billingCountry", chargifyPaymentProfile.string("billing_country"));
            response.setIfNotNull("bankName", chargifyPaymentProfile.string("bank_name"));
            response.setIfNotNull("bankRoutingNumber", chargifyPaymentProfile.string("bank_routing_number"));
            response.setIfNotNull("bankAccountNumber", chargifyPaymentProfile.string("bank_account_number"));
            response.setIfNotNull("bankAccountType", chargifyPaymentProfile.string("bank_account_type"));
            response.setIfNotNull("bankAccountHolderType", chargifyPaymentProfile.string("bank_account_holder_type"));

            response.setIfNotNull("removed", chargifyPaymentProfile.bool("removed", false));

            // fields added for compatibility with old versions of the endpoint
            response.setIfNotNull("firstNameOnCard", chargifyPaymentProfile.string("first_name"));
            response.setIfNotNull("lastNameOnCard", chargifyPaymentProfile.string("last_name"));
            response.setIfNotNull("code", chargifyPaymentProfile.string("cvv"));
        }
        return response;
    }

    public static Json paymentProfileToChargify(Json slingrPaymentProfile) {
        final Json response = Json.map();
        if(slingrPaymentProfile != null && !slingrPaymentProfile.isEmpty()) {
            response.setIfNotNull("id", slingrPaymentProfile.string("chargifyId"));
            response.setIfNotNull("customer_id", slingrPaymentProfile.string("customerId"));
            response.setIfNotNull("payment_type", slingrPaymentProfile.string("paymentType"));
            response.setIfNotNull("first_name", slingrPaymentProfile.string("firstName"));
            response.setIfNotNull("last_name", slingrPaymentProfile.string("lastName"));
            response.setIfNotNull("full_number", slingrPaymentProfile.string("creditCardNumber"));
            response.setIfNotNull("expiration_month", slingrPaymentProfile.string("expirationMonth"));
            response.setIfNotNull("expiration_year", slingrPaymentProfile.string("expirationYear"));
            response.setIfNotNull("cvv", slingrPaymentProfile.string("cvv"));
            response.setIfNotNull("billing_address", slingrPaymentProfile.string("billingAddress1"));
            response.setIfNotNull("billing_address_2", slingrPaymentProfile.string("billingAddress2"));
            response.setIfNotNull("billing_city", slingrPaymentProfile.string("billingCity"));
            response.setIfNotNull("billing_state", slingrPaymentProfile.string("billingState"));
            response.setIfNotNull("billing_zip", slingrPaymentProfile.string("billingZipCode"));
            response.setIfNotNull("billing_country", slingrPaymentProfile.string("billingCountry"));
            response.setIfNotNull("bank_name", slingrPaymentProfile.string("bankName"));
            response.setIfNotNull("bank_routing_number", slingrPaymentProfile.string("bankRoutingNumber"));
            response.setIfNotNull("bank_account_number", slingrPaymentProfile.string("bankAccountNumber"));
            response.setIfNotNull("bank_account_type", slingrPaymentProfile.string("bankAccountType"));
            response.setIfNotNull("bank_account_holder_type", slingrPaymentProfile.string("bankAccountHolderType"));

            // fields added for compatibility with old versions of the endpoint
            if(StringUtils.isNotBlank(response.string("first_name"))) {
                response.setIfNotNull("first_name", slingrPaymentProfile.string("firstNameOnCard"));
            }
            if(StringUtils.isNotBlank(response.string("last_name"))) {
                response.setIfNotNull("last_name", slingrPaymentProfile.string("lastNameOnCard"));
            }
            if(StringUtils.isNotBlank(response.string("cvv"))) {
                response.setIfNotNull("cvv", slingrPaymentProfile.string("code"));
            }
        }
        return response;
    }

    public static void checkPaymentProfileParameters(Json chargifyPaymentProfile) {
        // customer id
        if(!chargifyPaymentProfile.contains("customer_id") || StringUtils.isBlank(chargifyPaymentProfile.string("customer_id"))){
            throw EndpointException.permanent(ErrorCode.ARGUMENT, String.format("Invalid customer id [%s]", chargifyPaymentProfile.string("customer_id")));
        }
        int chargifyId;
        try{
            chargifyId = Integer.parseInt(chargifyPaymentProfile.string("customer_id"));
        }catch (Exception ex){
            chargifyId = -1;
        }
        if(chargifyId < 0){
            throw EndpointException.permanent(ErrorCode.ARGUMENT, String.format("Invalid customer id [%s]", chargifyPaymentProfile.string("customer_id")));
        }

        // payment type
        String paymentType = chargifyPaymentProfile.string("payment_type");
        if(StringUtils.isNotBlank(paymentType)){
            paymentType = paymentType.toLowerCase();
            if(!paymentType.equals("credit_card") && !paymentType.equals("bank_account")){
                throw EndpointException.permanent(ErrorCode.ARGUMENT, String.format("Invalid paymentType [%s]. Valid values: empty (credit card), 'credit_card' and 'bank_account'", chargifyPaymentProfile.string("payment_type")));
            }
        } else {
            paymentType = "credit_card";
        }
        chargifyPaymentProfile.set("payment_type", paymentType);
    }

    public static List<String> checkPaymentProfileNoEmptyParameters(Json chargifyPaymentProfile) {
        final List<String> invalidFields = new ArrayList<>();
        if(chargifyPaymentProfile == null || chargifyPaymentProfile.isEmpty()){
            invalidFields.add("creditCardNumber");
            invalidFields.add("expirationMonth");
            invalidFields.add("expirationYear");
        } else {
            final String paymentType = chargifyPaymentProfile.string("payment_type");
            if(StringUtils.isNotBlank(paymentType) && paymentType.equals("bank_account")){
                if (StringUtils.isBlank(chargifyPaymentProfile.string("bank_name"))) {
                    invalidFields.add("bankName");
                }
                if (StringUtils.isBlank(chargifyPaymentProfile.string("bank_routing_number"))) {
                    invalidFields.add("bankRoutingNumber");
                }
                if (StringUtils.isBlank(chargifyPaymentProfile.string("bank_account_number"))) {
                    invalidFields.add("bankAccountNumber");
                }
            } else {
                if (StringUtils.isBlank(chargifyPaymentProfile.string("full_number"))) {
                    invalidFields.add("creditCardNumber");
                }
                if (StringUtils.isBlank(chargifyPaymentProfile.string("expiration_month"))) {
                    invalidFields.add("expirationMonth");
                }
                if (StringUtils.isBlank(chargifyPaymentProfile.string("expiration_year"))) {
                    invalidFields.add("expirationYear");
                }
            }
        }
        return invalidFields;
    }

    public static Json subscriptionToSlingr(Json chargifyCustomer, String slingrId) {
        final Json response = subscriptionToSlingr(chargifyCustomer);
        if(StringUtils.isBlank(response.string("id"))) {
            response.set("id", slingrId);
        }
        return response;
    }

    public static Json subscriptionToSlingr(Json chargifySubscription) {
        final Json response = Json.map();
        if(chargifySubscription != null && !chargifySubscription.isEmpty()) {
            response.setIfNotNull("chargifyId", chargifySubscription.string("id"));
            response.setIfNotNull("state", chargifySubscription.string("state"));
            response.setIfNotNull("previousState", chargifySubscription.string("previous_state"));

            response.setIfNotNull("customerId", chargifySubscription.string("customer_id"));
            final Json customer = customerToSlingr(chargifySubscription.json("customer"));
            if(!customer.isEmpty()){
                response.setIfNotNull("customer", customer);
                if(StringUtils.isBlank(response.string("customerId")) && StringUtils.isNotBlank(customer.string("chargifyId"))){
                    response.setIfNotNull("customerId", customer.string("chargifyId"));
                }
            }

            response.setIfNotNull("paymentProfileId", chargifySubscription.string("payment_profile_id"));
            final String paymentType = chargifySubscription.string("payment_type");
            if(StringUtils.isBlank(paymentType) || paymentType.equals("credit_card")){
                Json creditCard = paymentProfileToSlingr(chargifySubscription.json("credit_card"));

                if(!creditCard.isEmpty()){
                    response.setIfNotNull("paymentProfile", creditCard);
                    if(StringUtils.isBlank(response.string("paymentProfileId")) && StringUtils.isNotBlank(creditCard.string("chargifyId"))){
                        response.setIfNotNull("paymentProfileId", creditCard.string("chargifyId"));
                    }
                }
            } else if(paymentType.equals("bank_account")){
                Json bank_account = paymentProfileToSlingr(chargifySubscription.json("bank_account"));

                if(!bank_account.isEmpty()){
                    response.setIfNotNull("paymentProfile", bank_account);
                    if(StringUtils.isBlank(response.string("paymentProfileId")) && StringUtils.isNotBlank(bank_account.string("chargifyId"))){
                        response.setIfNotNull("paymentProfileId", bank_account.string("chargifyId"));
                    }
                }
            }

            response.setIfNotNull("productHandle", chargifySubscription.string("product_handle"));
            final Json product = productToSlingr(chargifySubscription.json("product"));
            if(!product.isEmpty()){
                response.setIfNotNull("product", product);
                if(StringUtils.isBlank(response.string("productHandle")) && StringUtils.isNotBlank(product.string("handle"))){
                    response.setIfNotNull("productHandle", product.string("handle"));
                }
            }

            response.setIfNotNull("createdAt", chargifySubscription.string("created_at"));
            response.setIfNotNull("updatedAt", chargifySubscription.string("updated_at"));
            response.setIfNotNull("archivedAt", chargifySubscription.string("archived_at"));
            response.setIfNotNull("currentPeriodStartedAt", chargifySubscription.string("current_period_started_at"));
            response.setIfNotNull("currentPeriodEndsAt", chargifySubscription.string("current_period_ends_at"));
            response.setIfNotNull("nextAssessmentAt", chargifySubscription.string("next_assessment_at"));
            response.setIfNotNull("delayedCancelAt", chargifySubscription.string("delayed_cancel_at"));
            response.setIfNotNull("expiresAt", chargifySubscription.string("expires_at"));
            response.setIfNotNull("canceledAt", chargifySubscription.string("canceled_at"));
            response.setIfNotNull("cancellationMessage", chargifySubscription.string("cancellation_message"));
            response.setIfNotNull("cancelAtEndOfPeriod", chargifySubscription.string("cancel_at_end_of_period"));
            response.setIfNotNull("balanceInCents", chargifySubscription.string("balance_in_cents"));
            response.setIfNotNull("couponCode", chargifySubscription.string("coupon_code"));
            response.setIfNotNull("paymentCollectionMethod", chargifySubscription.string("payment_collection_method"));
            response.setIfNotNull("productPriceInCents", chargifySubscription.string("product_price_in_cents"));
            response.setIfNotNull("productVersionNumber", chargifySubscription.string("product_version_number"));
            response.setIfNotNull("signupPaymentId", chargifySubscription.string("signup_payment_id"));
            response.setIfNotNull("signupRevenue", chargifySubscription.string("signup_revenue"));
            response.setIfNotNull("totalRevenueInCents", chargifySubscription.string("total_revenue_in_cents"));
            response.setIfNotNull("trialStartedAt", chargifySubscription.string("trial_started_at"));
            response.setIfNotNull("trialEndedAt", chargifySubscription.string("trial_ended_at"));

            response.setIfNotNull("canceled", chargifySubscription.bool("canceled", false));
        }
        return response;
    }

    public static Json subscriptionToChargify(Json slingrSubscription) {
        final Json response = Json.map();
        if(slingrSubscription != null && !slingrSubscription.isEmpty()) {
            response.setIfNotNull("id", slingrSubscription.string("chargifyId"));
            response.setIfNotNull("state", slingrSubscription.string("state"));
            response.setIfNotNull("previous_state", slingrSubscription.string("previousState"));
            response.setIfNotNull("customer_id", slingrSubscription.string("customerId"));
            response.setIfNotNull("payment_profile_id", slingrSubscription.string("paymentProfileId"));
            response.setIfNotNull("product_handle", slingrSubscription.string("productHandle"));
            response.setIfNotNull("cancellation_message", slingrSubscription.string("cancellationMessage"));
        }
        return response;
    }

    public static void checkSubscriptionParameters(Json chargifySubscription) {
        // customer id
        if(!chargifySubscription.contains("customer_id") || StringUtils.isBlank(chargifySubscription.string("customer_id"))){
            throw EndpointException.permanent(ErrorCode.ARGUMENT, String.format("Invalid customer id [%s]", chargifySubscription.string("customer_id")));
        }
        int chargifyId;
        try{
            chargifyId = Integer.parseInt(chargifySubscription.string("customer_id"));
        }catch (Exception ex){
            chargifyId = -1;
        }
        if(chargifyId < 0){
            throw EndpointException.permanent(ErrorCode.ARGUMENT, String.format("Invalid customer id [%s]", chargifySubscription.string("customer_id")));
        }

        // payment profile id
        if(!chargifySubscription.contains("payment_profile_id") || StringUtils.isBlank(chargifySubscription.string("payment_profile_id"))){
            throw EndpointException.permanent(ErrorCode.ARGUMENT, String.format("Invalid payment profile id [%s]", chargifySubscription.string("payment_profile_id")));
        }
        try{
            chargifyId = Integer.parseInt(chargifySubscription.string("payment_profile_id"));
        }catch (Exception ex){
            chargifyId = -1;
        }
        if(chargifyId < 0){
            throw EndpointException.permanent(ErrorCode.ARGUMENT, String.format("Invalid payment profile id [%s]", chargifySubscription.string("payment_profile_id")));
        }

        // product handle
        if(!chargifySubscription.contains("product_handle") || StringUtils.isBlank(chargifySubscription.string("product_handle"))){
            throw EndpointException.permanent(ErrorCode.ARGUMENT, String.format("Invalid product handle [%s]", chargifySubscription.string("product_handle")));
        }
    }

    public static Json productToSlingr(Json chargifyProduct) {
        final Json response = Json.map();
        if(chargifyProduct != null && !chargifyProduct.isEmpty()) {
            response.setIfNotNull("chargifyId", chargifyProduct.string("id"));
            response.setIfNotNull("handle", chargifyProduct.string("handle"));
            response.setIfNotNull("name", chargifyProduct.string("name"));
            response.setIfNotNull("description", chargifyProduct.string("description"));
            final Json family = Json.map();
            final Json productFamily = chargifyProduct.json("product_family");
            if(!productFamily.isEmpty()) {
                family.setIfNotNull("handle", productFamily.string("handle"));
                family.setIfNotNull("name", productFamily.string("name"));
                family.setIfNotNull("description", productFamily.string("description"));
            }
            response.setIfNotNull("family", family);
            response.setIfNotNull("intervalUnit", chargifyProduct.string("interval_unit"));
            response.setIfNotNull("interval", chargifyProduct.string("interval"));
            response.setIfNotNull("initialChargeInCents", chargifyProduct.string("initial_charge_in_cents"));
            response.setIfNotNull("trialPriceInCents", chargifyProduct.string("trial_price_in_cents"));
            response.setIfNotNull("trialInterval", chargifyProduct.string("trial_interval"));
            response.setIfNotNull("trialIntervalUnit", chargifyProduct.string("trial_interval_unit"));
            response.setIfNotNull("expirationInterval", chargifyProduct.string("expiration_interval"));
            response.setIfNotNull("expirationIntervalUnit", chargifyProduct.string("expiration_interval_unit"));
            response.setIfNotNull("versionNumber", chargifyProduct.string("version_number"));
            response.setIfNotNull("createdAt", chargifyProduct.string("created_at"));
            response.setIfNotNull("updatedAt", chargifyProduct.string("updated_at"));
            response.setIfNotNull("archivedAt", chargifyProduct.string("archived_at"));

            response.setIfNotNull("removed", chargifyProduct.bool("removed", false));
        }
        return response;
    }

}
