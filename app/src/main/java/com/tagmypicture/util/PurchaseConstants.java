package com.tagmypicture.util;

/**
 * Created by prasharma on 4/25/2017.
 */

public class PurchaseConstants {

    // (arbitrary) request code for the purchase flow
    public static final int RC_REQUEST = 10001;

    public static String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAxTafH3+ez4jU4w6P6NdffljCt31h/CAQumGuwzemC4MJ0gFNvcoke6LHUWnEz/i5PIiQ16jENEwMw4cKcKUvym4YWApF951YRcvl/HkdKgj3ug4pVEl1OHgrR0eMna7jZwblxwyLG7z+4VjhBln+blhY40jlsLKI+ZZZ2CmPrK92dZpPbXp4nf5rpVQGkIR0HA62ZAdo5PzwOQtAnuvB48T5lQTs+J/ObsE/sUHjPH+86mq5z5lm0l1ATHLSJiR9OoEouPh2IA4Ahumr4brJNTVlrBcjwoBu73uDYFjXGNLPPiv3dd7NjMrQY1VlMnWEI+hjYoJ+57v/N1jRcubePQIDAQAB";

    public static boolean isCheatsPurchased = false;

    public static final String PURCHASE_TEST_SKU = "android.test.purchased";
    public static final String UNAVAILABLE_TEST_SKU = "android.test.item_unavailable";
    public static final String REFUNDED_TEST_SKU = "android.test.refunded";
    public static final String CANCELED_TEST_SKU = "android.test.canceled";

    //Live test key
    public static final String PURCHASE_ITEM_SKU = "com.dating.datingappcheat.pack";


    //In app response codes
    static final String BILLING_RESPONSE_RESULT_OK = "0";
    static final String BILLING_RESPONSE_RESULT_USER_CANCELED = "1";
    static final String BILLING_RESPONSE_RESULT_SERVICE_UNAVAILABLE = "2";
    static final String BILLING_RESPONSE_RESULT_BILLING_UNAVAILABLE = "3";
    static final String BILLING_RESPONSE_RESULT_ITEM_UNAVAILABLE = "4";
    static final String BILLING_RESPONSE_RESULT_DEVELOPER_ERROR = "5";
    static final String BILLING_RESPONSE_RESULT_ERROR = "6";
    static final String BILLING_RESPONSE_RESULT_ITEM_ALREADY_OWNED = "7";
    static final String BILLING_RESPONSE_RESULT_ITEM_NOT_OWNED = "8";

}
