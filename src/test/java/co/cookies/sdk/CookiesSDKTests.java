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
package co.cookies.sdk;


import co.cookies.sdk.services.AsyncRPC;
import co.cookies.sdk.services.SyncRPC;
import cookies.schema.catalog.BrandsRequest;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static com.google.common.truth.extensions.proto.ProtoTruth.assertThat;
import static org.junit.jupiter.api.Assertions.*;


/** Generic facade class tests. */
public class CookiesSDKTests {
    @Test void testGetStaticDefaults() {
        assertEquals(
            "api.cookies.co",
            ExtensibleCookiesSDK.getDefaultApiHostname(),
                "default hostname should be `api.cookies.co`"
        );
        assertEquals(
            443,
            ExtensibleCookiesSDK.getDefaultApiPort(),
            "default port should be 443"
        );
        assertEquals(
            "api.cookies.co:443",
            ExtensibleCookiesSDK.getDefaultApiEndpoint()
        );
    }

    @Test void testClientHeaders() {
        assertEquals(
            "x-api-key",
            ExtensibleCookiesSDK.Headers.API_KEY.toString()
        );
        assertEquals(
            "user-agent",
            ExtensibleCookiesSDK.Headers.USER_AGENT.toString()
        );
    }

    @Test void testFactoryMethods() {
        var builder = CookiesSDK.builder();
        assertNotNull(
            builder,
            "should not get `null` for new builder request for SDK"
        );
        var manager = builder.build();
        assertNotNull(
            manager,
            "should not get `null` for new SDK manager object"
        );
    }

    @Test void testSyncRPCContainer() {
        var syncBrands = SyncRPC.of(BrandsRequest.newBuilder().build());
        assertNotNull(
            syncBrands.request(),
            "should be able to get request from sync RPC wrapper"
        );
        assertNotNull(
            syncBrands.timeout(),
            "should be able to get timeout from sync RPC wrapper"
        );
        assertEquals(
            Optional.empty(),
            syncBrands.context(),
            "should have empty call context by default"
        );
        assertNotNull(
            syncBrands.unwrap(),
            "should be able to convert a sync RPC wrapper to an async wrapper"
        );
        var asyncBrands = syncBrands.unwrap();
        assertNotNull(
            asyncBrands.request(),
            "should be able to get request from async RPC wrapper"
        );
        assertThat(
            syncBrands.request()
        ).isEqualTo(asyncBrands.request());
        assertNotNull(
            asyncBrands.timeout(),
            "should be able to get timeout from async RPC wrapper"
        );
        assertEquals(
            syncBrands.timeout().value(),
            asyncBrands.timeout().value(),
            "value for timeout should match after unwrapping RPC"
        );
        assertEquals(
            syncBrands.timeout().unit(),
            asyncBrands.timeout().unit(),
            "unit for timeout should match after unwrapping RPC"
        );
        assertEquals(
            Optional.empty(),
            asyncBrands.context(),
            "should have empty call context by default"
        );
        var syncBrands2 = CookiesSDK.sync(BrandsRequest.newBuilder().build());
        assertNotNull(
            syncBrands2.request(),
            "should be able to get request from sync RPC wrapper"
        );
        assertNotNull(
            syncBrands2.timeout(),
            "should be able to get timeout from sync RPC wrapper"
        );
        assertEquals(
            Optional.empty(),
            syncBrands2.context(),
            "should have empty call context by default"
        );
        assertNotNull(
            syncBrands2.unwrap(),
            "should be able to convert a sync RPC wrapper to an async wrapper"
        );
    }

    @Test void testAsyncRPCContainer() {
        var asyncBrands = AsyncRPC.of(BrandsRequest.newBuilder().build());
        assertNotNull(
            asyncBrands.request(),
            "should be able to get request from async RPC wrapper"
        );
        assertNotNull(
            asyncBrands.timeout(),
            "should be able to get timeout from async RPC wrapper"
        );
        assertEquals(
            Optional.empty(),
            asyncBrands.context(),
            "should have empty call context by default"
        );
        var asyncBrands2 = CookiesSDK.async(BrandsRequest.newBuilder().build());
        assertNotNull(
            asyncBrands2.request(),
            "should be able to get request from async RPC wrapper"
        );
        assertNotNull(
            asyncBrands2.timeout(),
            "should be able to get timeout from async RPC wrapper"
        );
        assertEquals(
            Optional.empty(),
            asyncBrands2.context(),
            "should have empty call context by default"
        );
    }

    @Test void testCannotAcquireClosed() {
        var sdk = CookiesSDK.builder().build();
        assertDoesNotThrow(sdk::close);
        assertThrows(IllegalStateException.class, sdk::catalog);
    }
}
