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


import co.cookies.sdk.catalog.CatalogClient;
import co.cookies.sdk.services.AsyncRPC;
import co.cookies.sdk.services.SyncRPC;
import co.cookies.sdk.storefront.Storefront;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.JwtClaims;
import com.google.auth.oauth2.JwtCredentials;
import com.google.common.util.concurrent.MoreExecutors;
import cookies.schema.catalog.BrandsRequest;
import io.grpc.ManagedChannelBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.ThrowingSupplier;

import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.google.common.truth.extensions.proto.ProtoTruth.assertThat;
import static org.junit.jupiter.api.Assertions.*;


/** Generic facade class tests. */
public final class CookiesSDKTests {
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

    @Test void testAcquireCached() {
        var sdk = CookiesSDK.builder().build();
        assertDoesNotThrow(() -> {
            var cat1 = sdk.catalog();
            var cat2 = sdk.catalog();
            assertSame(
                cat1,
                cat2,
                "accessing a cached service should return the same object"
            );
        });
        assertDoesNotThrow(() -> {
            var store1 = sdk.storefront().menu();
            var store2 = sdk.storefront().menu();
            assertSame(
                store1,
                store2,
                "accessing a cached service (storefront) should return the same object"
            );
        });
        assertDoesNotThrow(
            sdk::close,
            "SDK objects should be safely closeable"
        );
    }

    @Test void testCacheBoundToSDKInstance() {
        var sdk = CookiesSDK.builder().build();
        var sdk2 = CookiesSDK.builder().build();
        assertDoesNotThrow(() -> {
            var cat1 = sdk.catalog();
            var cat2 = sdk.catalog();
            var cat3 = sdk2.catalog();
            assertSame(
                cat1,
                cat2,
                "accessing a cached service should return the same object"
            );
            assertNotSame(
                cat1,
                cat3,
                "accessing a service in a different SDK should not return the same object"
            );
        });
        assertDoesNotThrow(() -> {
            var store1 = sdk.storefront().profile();
            var store2 = sdk.storefront().profile();
            var store3 = sdk2.storefront().profile();
            assertSame(
                store1,
                store2,
                "accessing a cached service (storefront) should return the same object"
            );
            assertNotSame(
                store1,
                store3,
                "accessing a service in a different SDK should not return the same object"
            );
        });
        assertDoesNotThrow(
            sdk::close,
            "SDK objects should be safely closeable"
        );
        assertDoesNotThrow(
            sdk2::close,
            "SDK objects should be safely closeable"
        );
    }

    @Test void testDefaultCredentials() {
        var sdk = CookiesSDK.builder()
            .build();

        assertNotNull(
            sdk,
            "should not get null SDK"
        );
        assertFalse(
            sdk.getCredentials().isPresent(),
            "should not have explicit credentials when not provided to builder"
        );
        assertFalse(
            sdk.getCredentialsProvider().isPresent(),
            "should not have an explicit credentials provider when not provided to builder"
        );
        assertNotNull(
            sdk.credentialsProvider(),
            "credential provider should always return a value"
        );
        assertDoesNotThrow(
            sdk::close,
            "SDK objects should be safely closeable"
        );
    }

    @Test void testCustomCredentials() throws NoSuchAlgorithmException, IOException {
        var kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair kp = kpg.generateKeyPair();

        var specific = JwtCredentials.newBuilder()
            .setPrivateKey(kp.getPrivate())
            .setLifeSpanSeconds(3600L)
            .setJwtClaims(JwtClaims.newBuilder()
                .setIssuer("abc123")
                .setAudience("abc123")
                .setSubject("abc123")
                .build())
            .build();

        var sdk = CookiesSDK.builder()
            .setCredentials(Optional.of(specific))
            .build();

        assertNotNull(sdk, "should not get null SDK");
        var creds = sdk.getCredentials();
        assertTrue(
            creds.isPresent(),
            "creds should be present on SDK when provided to builder"
        );
        assertSame(
            specific,
            creds.get(),
            "creds on SDK should be exact same as those provided to builder"
        );
        assertSame(
            specific,
            sdk.credentialsProvider().getCredentials(),
            "providing explicit credentials should wrap a credential provider"
        );

        var provider = FixedCredentialsProvider.create(specific);

        var sdk2 = CookiesSDK.builder()
            .setCredentialsProvider(Optional.of(provider))
            .build();

        assertNotNull(sdk2, "should not get null SDK");
        var creds2 = sdk2.credentialsProvider();
        assertNotNull(
            creds2,
            "creds provider should never be null"
        );
        assertSame(
            provider,
            creds2,
            "creds provider on SDK should be exact same as the one provided to builder"
        );
        assertDoesNotThrow(
            sdk::close,
            "SDK objects should be safely closeable"
        );
        assertDoesNotThrow(
            sdk2::close,
            "SDK objects should be safely closeable"
        );
    }

    @Test void testCustomChannelConfigurator() {
        var touched = new AtomicBoolean(false);
        var sdk = CookiesSDK.builder()
            .setChannelConfigurator(Optional.of((channelBuilder) -> {
                touched.compareAndSet(false, true);
                return channelBuilder;
            }))
            .build();

        assertNotNull(
            sdk,
            "SDK should never be null from builder"
        );
        sdk.channelConfigurator().apply(
            ManagedChannelBuilder.forTarget("sample")
        );
        assertTrue(
            touched.get(),
            "channel configurator should be invoked when provided"
        );
        assertDoesNotThrow(
            sdk::close,
            "SDK objects should be safely closeable"
        );
    }

    @Test void testDefaultChannelConfigurator() {
        var sdk = CookiesSDK.builder().build();
        assertFalse(
            sdk.getChannelConfigurator().isPresent(),
            "should not have a default channel configurator"
        );
        assertDoesNotThrow(
            sdk::close,
            "SDK objects should be safely closeable"
        );
    }

    @Test void testPrivateAccessMode() {
        var sdk = CookiesSDK.builder()
            .setPrivateAccess(Optional.of(true))
            .setEndpoint(Optional.of("testing123"))
            .build();

        assertNotNull(
            sdk,
            "SDK should never be null from builder"
        );
        assertTrue(
            sdk.getPrivateAccess().isPresent(),
            "private access setting should be present when enabled"
        );
        assertTrue(
            sdk.getPrivateAccess().get(),
            "private access setting should be active when enabled"
        );
        assertTrue(
            sdk.privateAccess(),
            "private access setting should be active when enabled"
        );
        assertEquals(
            "testing123",
            sdk.endpoint(),
            "custom SDK endpoint should be used in private access mode"
        );
        assertDoesNotThrow(
            sdk::close,
            "SDK objects should be safely closeable"
        );

        // when private access is enabled, the transport channel should reflect the adjusted settings.
        var userConfigurator = sdk.getChannelConfigurator();
        assertFalse(
            userConfigurator.isPresent(),
            "should not have a default configurator even when private mode is enabled"
        );

        // resolve the main configurator
        var configurator = sdk.channelConfigurator();
        assertNotNull(
            configurator,
            "should always have a resolved channel configurator"
        );
        var decorated = sdk.channelConfigurator().apply(
            ManagedChannelBuilder.forTarget("sample")
        );
        var channel = decorated.build();
        assertEquals(
            "testing123",
            channel.authority(),
            "managed channel authority should be overridden to endpoint value in private access mode"
        );

        var sdk2 = CookiesSDK.builder()
            .setPrivateAccess(Optional.of(false))
            .build();
        assertNotNull(
            sdk2,
            "SDK should never be null from builder"
        );
        assertTrue(
            sdk2.getPrivateAccess().isPresent(),
            "private access setting should be present when disabled"
        );
        assertFalse(
            sdk2.getPrivateAccess().get(),
            "private access setting should be inactive when disabled"
        );
        assertFalse(
            sdk2.privateAccess(),
            "private access setting should be inactive when disabled"
        );
        assertDoesNotThrow(
            sdk2::close,
            "SDK objects should be safely closeable"
        );

        var sdk3 = CookiesSDK.builder()
                .build();
        assertNotNull(
            sdk3,
            "SDK should never be null from builder"
        );
        assertFalse(
            sdk3.getPrivateAccess().isPresent(),
            "private access setting should be missing when defaulted"
        );
        assertFalse(
            sdk3.privateAccess(),
            "private access setting should be disabled when defaulted"
        );
        assertDoesNotThrow(
            sdk3::close,
            "SDK objects should be safely closeable"
        );
    }

    @Test void testConfigurableClient() {
        var sdk = CookiesSDK
            .builder()
            .setEndpoint(Optional.of("sample.local:1234"))
            .setExecutorService(Optional.of(MoreExecutors.listeningDecorator(
                    Executors.newSingleThreadScheduledExecutor())))
            .build();

        assertNotNull(
            sdk,
            "should be able to acquire SDK via configurable entrypoint"
        );
        assertEquals(
            "sample.local:1234",
            sdk.endpoint(),
            "overridden endpoint should apply"
        );
        assertEquals(
            "sample.local:1234",
            sdk.catalog().service().getSettings().getEndpoint(),
            "overridden endpoint should apply for catalog service"
        );
        assertEquals(
            "sample.local:1234",
            sdk.getSettings().endpoint(),
            "overridden SDK-level endpoint should apply for catalog service"
        );
        assertNotNull(
            sdk.getSettings(),
            "should be able to get SDK-level settings from the client"
        );
        assertDoesNotThrow(
            sdk::close,
            "SDK objects should be safely closeable"
        );
    }

    @Test void testCannotAcquireClosed() {
        var sdk = CookiesSDK.builder().build();
        assertDoesNotThrow((ThrowingSupplier<CatalogClient>) sdk::catalog);
        assertDoesNotThrow((ThrowingSupplier<Storefront>) sdk::storefront);
        assertDoesNotThrow(sdk::close);
        assertThrows(IllegalStateException.class, sdk::catalog);
        assertThrows(IllegalStateException.class, sdk::storefront);
    }
}
