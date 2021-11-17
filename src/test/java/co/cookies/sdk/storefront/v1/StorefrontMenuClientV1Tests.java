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
package co.cookies.sdk.storefront.v1;

import co.cookies.sdk.ProtoLoader;
import co.cookies.sdk.services.SyncRPC;
import co.cookies.sdk.storefront.Storefront;
import co.cookies.sdk.storefront.v1.stub.MenuV1StubSettings;
import cookies.schema.StoreKey;
import cookies.schema.store.*;
import cookies.schema.store.model.UserLocation;
import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.Optional;
import java.util.function.Consumer;

import static co.cookies.sdk.CookiesSDK.sync;
import static co.cookies.sdk.CookiesSDK.async;
import static co.cookies.sdk.ServiceTestUtil.*;
import static com.google.common.truth.extensions.proto.ProtoTruth.assertThat;
import static org.junit.jupiter.api.Assertions.*;


/** Tests for the {@link StorefrontClientV1.MenuClientV1} implementation. */
public final class StorefrontMenuClientV1Tests {
    private MenuV1Grpc.MenuV1ImplBase acquireService() {
        return MockStorefrontMenuServiceImpl.acquire();
    }

    private void acquireMockedClient(Consumer<Storefront.MenuClient> clientTest) {
        setupMockedClient(
            clientTest,
            this::acquireService,
            (server, channelProvider) -> MenuV1StubSettings.newBuilder()
                    .setTransportChannelProvider(channelProvider)
                    .build()
                    .createStub(),
            StorefrontClientV1.MenuClientV1::forStub
        );
    }

    @Test public void testAcquireMockService() {
        assertNotNull(
            acquireService(),
            "should be able to acquire local mock menu service"
        );
    }

    @Test public void testAcquireDefaultClient() {
        assertNotNull(
            StorefrontClientV1.MenuClientV1.defaults(),
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
                "menu",
                "menu service should have `menu` as name"
            );
            assertEquals(
                client.getServiceVersion(),
                "v1",
                "menu service version should be `v1`"
            );
            assertTrue(
                client.getServiceInfo().apiKeyRequired(),
                "API keys should be required w/menu API"
            );
            assertFalse(
                client.getServiceInfo().authorizationRequired(),
                "auth keys should be optional w/menu API"
            );
        });
    }

    @Test void testDefaultMenuBlocking() {
        acquireMockedClient((client) -> {
            var basicMenu = client.menu(sync(MenuRequest.getDefaultInstance()));
            assertNotNull(basicMenu, "mock menu response should not be null");
            assertThat(basicMenu).ignoringRepeatedFieldOrder().isEqualTo(ProtoLoader.loadTextFile(
                MenuResponse.newBuilder(),
                "/store_menu_default.prototxt"
            ));
        });
    }

    @Test void testDefaultMenuNonBlocking() {
        acquireMockedClient((client) -> {
            var basicMenu = resolve(
                client.menu()
            );
            assertNotNull(basicMenu, "mock menu response should not be null");
            assertThat(basicMenu).ignoringRepeatedFieldOrder().isEqualTo(ProtoLoader.loadTextFile(
                MenuResponse.newBuilder(),
                "/store_menu_default.prototxt"
            ));
        });
    }

    @Test void testLocaleMenuNonBlocking() {
        acquireMockedClient((client) -> {
            var basicMenu = resolve(
                client.menu(Optional.of(Locale.FRANCE))
            );
            assertNotNull(basicMenu, "mock menu response should not be null");
            assertThat(basicMenu).ignoringRepeatedFieldOrder().isEqualTo(ProtoLoader.loadTextFile(
                MenuResponse.newBuilder(),
                "/store_menu_default.prototxt"
            ));
        });
    }

    @Test void testDefaultMenuSpecNonblocking() {
        acquireMockedClient((client) -> {
            var spec = MenuRequestSpec.defaults();
            var basicMenu = resolve(client.menu(spec));
            assertNotNull(basicMenu, "mock menu response should not be null");
            assertThat(basicMenu).ignoringRepeatedFieldOrder().isEqualTo(ProtoLoader.loadTextFile(
                MenuResponse.newBuilder(),
                "/store_menu_default.prototxt"
            ));
        });
    }

    @Test void testUserLocationMenuSpecNonblocking() {
        acquireMockedClient((client) -> {
            var spec = MenuRequestSpec.forUserLocation(
                UserLocation.newBuilder()
                    .setMarket("testing")
                    .build()
            );
            var locationMenu = resolve(client.menu(spec));
            assertNotNull(locationMenu, "mock menu response should not be null");
            assertThat(locationMenu).ignoringRepeatedFieldOrder().isEqualTo(ProtoLoader.loadTextFile(
                    MenuResponse.newBuilder(),
                    "/store_menu_default.prototxt"
            ));
        });
    }

    @Test void testKeysOnlyMenuSpecNonblocking() {
        acquireMockedClient((client) -> {
            var spec = MenuRequestSpec.defaults().setKeysOnly(true);
            var keysMenu = resolve(client.menu(spec));
            assertNotNull(keysMenu, "mock menu response should not be null");
            assertThat(keysMenu).ignoringRepeatedFieldOrder().isEqualTo(ProtoLoader.loadTextFile(
                MenuResponse.newBuilder(),
                "/store_menu_keysonly.prototxt"
            ));
        });
    }

    @Test void testLocaleStoreMenuNonBlocking() {
        acquireMockedClient((client) -> {
            var storeMenu = resolve(
                client.menu(Optional.of(Locale.FRANCE), StoreKey.newBuilder()
                    .setCode("BOH")
                    .build())
            );
            assertNotNull(storeMenu, "mock menu response should not be null");
            assertThat(storeMenu).ignoringRepeatedFieldOrder().isEqualTo(ProtoLoader.loadTextFile(
                MenuResponse.newBuilder(),
                "/store_menu_default.prototxt"
            ));
        });
    }

    @Test void testMenuSearchBlocking() {
        acquireMockedClient((client) -> {
            var storeSearch = client.search(SyncRPC.of(MenuSearchRequest.newBuilder()
                .setTerm("some search term")
                .build()));
            assertNotNull(
                storeSearch,
                "should get valid result for sync store search operation"
            );
        });
    }

    @Test void testMenuSearchNonBlocking() {
        acquireMockedClient((client) -> {
            var storeSearch = resolve(
                client.search(async(MenuSearchRequest.newBuilder()
                    .setTerm("some search term")
                    .build()))
            );
            assertNotNull(
                storeSearch,
                "should get valid result for async store search operation"
            );
        });
    }
}
