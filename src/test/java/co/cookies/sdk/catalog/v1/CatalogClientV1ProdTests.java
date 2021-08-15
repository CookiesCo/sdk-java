/*
 * Copyright © 2021, Cookies Creative Consulting & Promotions, Inc. All rights reserved.
 *
 * This project and all associated source or object computer code, except where otherwise noted, are licensed for
 * private use by Cookies Creative Consulting & Promotions, Inc., a California Corporation (heretofore referred to as
 * "Cookies"), and affiliates, partners, vendors, or contractors, as authorized in writing by Cookies. Use of this
 * computer code in object or source form requires and implies consent and agreement to that license in principle and
 * practice. Source or object code not listing this header, or unless specified otherwise, remain the property of
 * Cookies and its suppliers, if any. The intellectual and technical concepts contained herein are proprietary to
 * Cookies and its suppliers and may be covered by U.S. and Foreign Patents, or patents in process, and are protected
 * by trade secret and copyright law. Dissemination of this information, or reproduction of this material, in any form,
 * is strictly forbidden except in adherence with assigned license requirements.
 */
package co.cookies.sdk.catalog.v1;


import co.cookies.sdk.CookiesSDK;
import cookies.schema.catalog.BrandsRequest;
import cookies.schema.catalog.CatalogQueryOptions;
import cookies.schema.catalog.MultiProductRequest;
import cookies.schema.catalog.StrainsRequest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import static co.cookies.sdk.CookiesSDK.sync;
import static co.cookies.sdk.CookiesSDK.async;
import static org.junit.jupiter.api.Assertions.*;


/**
 * Tests the production API. Useful for one-off checks.
 */
@Disabled
public class CatalogClientV1ProdTests {
    @Test void testGetBrands() {
        var sdk = CookiesSDK.builder()
                .setApiKey(Optional.ofNullable(System.getenv("C6S_API_KEY_TESTING")))
                .setEndpoint(Optional.of("catalog.api.cookies.co:443"))
                .build();

        var brands = sdk.catalog().brands(sync(BrandsRequest.getDefaultInstance()));
        assertNotNull(brands, "brands response should not be null");
        assertFalse(brands.isEmpty(), "brands should not be empty");
    }

    @Test void testGetStrains() {
        var sdk = CookiesSDK.builder()
                .setApiKey(Optional.ofNullable(System.getenv("C6S_API_KEY_TESTING")))
                .setEndpoint(Optional.of("catalog.api.cookies.co:443"))
                .build();

        var strains = sdk.catalog().strains(sync(StrainsRequest.getDefaultInstance()));
        assertNotNull(strains, "strains response should not be null");
        assertFalse(strains.isEmpty(), "strains should not be empty");
    }

    @Test void testProductSync() throws InterruptedException, TimeoutException, ExecutionException {
        var sdk = CookiesSDK.builder()
                .setApiKey(Optional.ofNullable(System.getenv("C6S_API_KEY_TESTING")))
                .setEndpoint(Optional.of("catalog.api.cookies.co:443"))
                .build();

        var op = sdk.catalog().sync(async(MultiProductRequest.newBuilder()
            .setLocale("en-US")
            .setNonce(12345)
            .setOptions(CatalogQueryOptions.newBuilder()
                .setContent(CatalogQueryOptions.ContentMode.KEYS_ONLY)
                .build())
            .addCtin("C033274")
            .addCtin("C033280")
            .addCtin("C033281")
            .addCtin("C036809")
            .addCtin("C999999")  // known not to exist
            .build()));

        var productStream = op.get(30, TimeUnit.SECONDS);
        var products = productStream.collect(Collectors.toUnmodifiableList());

        assertNotNull(products, "products response should not be null");
        assertFalse(products.isEmpty(), "products should not be empty");

        // we should only find 4
        assertEquals(4, products.size(), "should only find 4 of 5 matching products");
    }
}
