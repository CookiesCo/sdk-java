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
import co.cookies.sdk.catalog.CatalogClient;
import co.cookies.sdk.catalog.v1.stub.CatalogV1StubSettings;
import com.google.api.gax.grpc.GrpcTransportChannel;
import com.google.api.gax.rpc.FixedTransportChannelProvider;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import cookies.schema.catalog.*;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static co.cookies.sdk.CookiesSDK.sync;
import static co.cookies.sdk.CookiesSDK.async;
import static co.cookies.sdk.ServiceTestUtil.*;
import static org.junit.jupiter.api.Assertions.*;


/** Tests for the main {@link CatalogClientV1} implementation. */
public final class CatalogClientV1Tests {
    private CatalogV1Grpc.CatalogV1ImplBase acquireService() {
        return MockCatalogServiceImpl.acquire();
    }

    private void acquireMockedClient(Consumer<CatalogClient> clientTest) {
        standup(
            acquireService(),
            (server, channel) -> CatalogV1StubSettings.newBuilder()
                    .setTransportChannelProvider(FixedTransportChannelProvider.create(
                        GrpcTransportChannel.create(channel)))
                    .build()
                    .createStub(), (stub) -> {
            // using the provided stub, factory ourselves a client instance, and hand it to the test.
            clientTest.accept(CatalogClientV1.forStub(stub));
        });
    }

    private <T> T resolve(ListenableFuture<T> future) {
        try {
            return future.get(2, TimeUnit.MINUTES);
        } catch (InterruptedException | TimeoutException | ExecutionException rxe) {
            throw new RuntimeException(rxe);
        }
    }

    @Test void testAcquireMockService() {
        assertNotNull(
            acquireService(),
            "should be able to acquire local mock service"
        );
    }

    @Test void testAcquireDefaultClient() {
        assertNotNull(
            CatalogClientV1.defaults(),
            "should be able to acquire entirely default client"
        );
    }

    @Test void testAcquireMockClient() {
        acquireMockedClient((client) -> {
            assertNotNull(
                client,
                "should not get `null` for mocked client"
            );
            assertNotNull(
                client.getServiceInfo(),
                "should have access to valid service info"
            );
            assertEquals(
                client.getServiceName(),
                "catalog",
                "catalog service should `catalog` name"
            );
            assertEquals(
                client.getServiceVersion(),
                "v1",
                "catalog service version should be `v1`"
            );
            assertTrue(
                client.getServiceInfo().apiKeyRequired(),
                "API keys should be required w/catalog API"
            );
            assertFalse(
                client.getServiceInfo().authorizationRequired(),
                "auth keys should be optional w/catalog API"
            );
        });
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

    @Test void testFetchBrandsBlocking() {
        acquireMockedClient((client) -> {
            var brands = client.brands(sync(BrandsRequest.getDefaultInstance()));
            assertNotNull(brands, "mock brands response should not be null");
            assertFalse(brands.isEmpty(), "mock brands should not be empty");
        });
    }

    @Test void testFetchStrainsBlocking() {
        acquireMockedClient((client) -> {
            var strains = client.strains(sync(StrainsRequest.getDefaultInstance()));
            assertNotNull(strains, "mock strains response should not be null");
            assertFalse(strains.isEmpty(), "mock strains should not be empty");
        });
    }

    @Test void testFetchProductBlocking() {
        acquireMockedClient((client) -> {
            var product = client.product(sync(ProductRequest.getDefaultInstance()));
            assertNotNull(product, "mock product response should not be null");
            assertFalse(product.isEmpty(), "mock product should not be empty");
        });
    }

    @Test void testFetchBrandsNonBlocking() {
        acquireMockedClient((client) -> {
            var brands = client.brands(async(BrandsRequest.getDefaultInstance()));
            assertNotNull(brands, "mock brands future response should not be null");
            var brandsList = resolve(brands);
            assertFalse(brandsList.isEmpty(), "mock brands should not be empty");
        });
    }

    @Test void testFetchStrainsNonBlocking() {
        acquireMockedClient((client) -> {
            var strains = client.strains(async(StrainsRequest.getDefaultInstance()));
            assertNotNull(strains, "mock strains future response should not be null");
            var strainsList = resolve(strains);
            assertFalse(strainsList.isEmpty(), "mock strains should not be empty");
        });
    }

    @Test void testFetchProductNonBlocking() {
        acquireMockedClient((client) -> {
            var product = client.product(async(ProductRequest.getDefaultInstance()));
            assertNotNull(product, "mock product future response should not be null");
            var productObj = resolve(product);
            assertFalse(productObj.isEmpty(), "mock product should not be empty");
        });
    }

    @Test void testSyncProductsNonBlocking() {
        acquireMockedClient((client) -> {
            var product = client.sync(
                    async(MultiProductRequest.getDefaultInstance()));
            assertNotNull(product, "mock product response should not be null");
            var stream = resolve(product);
            var items = stream.collect(Collectors.toUnmodifiableList());
            assertFalse(items.isEmpty(), "mock product sync response should not be empty");
        });
    }
}
