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
import co.cookies.sdk.storefront.Storefront;
import co.cookies.sdk.storefront.v1.err.UsernameIneligibleError;
import co.cookies.sdk.storefront.v1.err.UsernameInvalidError;
import co.cookies.sdk.storefront.v1.stub.ProfileV1StubSettings;
import cookies.schema.store.*;
import cookies.schema.store.model.StoreUser;
import cookies.schema.store.model.UserKey;
import org.junit.jupiter.api.Test;

import java.util.function.Consumer;

import static co.cookies.sdk.CookiesSDK.sync;
import static co.cookies.sdk.CookiesSDK.async;
import static co.cookies.sdk.ServiceTestUtil.*;
import static com.google.common.truth.extensions.proto.ProtoTruth.assertThat;
import static org.junit.jupiter.api.Assertions.*;


/** Tests for the {@link StorefrontClientV1.ProfileClientV1} implementation. */
public final class StorefrontProfileClientV1Tests {
    private ProfileV1Grpc.ProfileV1ImplBase acquireService() {
        return MockStorefrontProfileServiceImpl.acquire();
    }

    private void acquireMockedClient(Consumer<Storefront.ProfileClient> clientTest) {
        setupMockedClient(
            clientTest,
            this::acquireService,
            (server, channelProvider) -> ProfileV1StubSettings.newBuilder()
                    .setTransportChannelProvider(channelProvider)
                    .build()
                    .createStub(),
            StorefrontClientV1.ProfileClientV1::forStub
        );
    }

    @Test public void testAcquireMockService() {
        assertNotNull(
            acquireService(),
            "should be able to acquire local mock profile service"
        );
    }

    @Test public void testAcquireDefaultClient() {
        assertNotNull(
            StorefrontClientV1.ProfileClientV1.defaults(),
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
                "profile",
                "profile service should have `profile` as name"
            );
            assertEquals(
                client.getServiceVersion(),
                "v1",
                "profile service version should be `v1`"
            );
            assertTrue(
                client.getServiceInfo().apiKeyRequired(),
                "API keys should be required w/profile API"
            );
            assertTrue(
                client.getServiceInfo().authorizationRequired(),
                "auth keys should be required w/profile API"
            );
        });
    }

    @Test void testFetchProfileBlocking() {
        acquireMockedClient((client) -> {
            assertNotNull(
                client,
                "should not get `null` for mocked client"
            );
            var profile = client.fetch(sync(ProfileRequest.newBuilder()
                    .setProfileId("abc123")
                    .build()));
            assertTrue(
                profile.isPresent(),
                "profile should be present after fetch"
            );
            assertNotNull(
                profile,
                "fetched sync profile should not be `null`"
            );
            assertThat(
                profile.get()
            ).ignoringRepeatedFieldOrder().isEqualTo(ProtoLoader.loadTextFile(
                ProfileResponse.newBuilder(),
                "/store_profile_fetch.prototxt"
            ));
        });
    }

    @Test void testFetchProfileNonBlocking() {
        acquireMockedClient((client) -> {
            assertNotNull(
                    client,
                    "should not get `null` for mocked client"
            );
            var profile = resolve(
                client.fetch(async(ProfileRequest.newBuilder()
                    .setProfileId("abc123")
                    .build()))
            );
            assertTrue(
                profile.isPresent(),
                "profile should be present after fetch"
            );
            assertNotNull(
                profile,
                "fetched sync profile should not be `null`"
            );
            assertThat(
                profile.get()
            ).ignoringRepeatedFieldOrder().isEqualTo(ProtoLoader.loadTextFile(
                ProfileResponse.newBuilder(),
                "/store_profile_fetch.prototxt"
            ));
        });
    }

    @Test void testProfileUpdateBlocking() {
        acquireMockedClient((client) -> {
            assertNotNull(
                client,
                "should not get `null` for mocked client"
            );
            var profile = client.update(sync(ProfileUpdateRequest.newBuilder()
                .setProfile(StoreUser.newBuilder()
                    .setKey(UserKey.newBuilder().setProfileId("abc123")))
                .build()));
            assertNotNull(
                profile,
                "fetched sync profile should not be `null`"
            );
            assertThat(
                profile
            ).ignoringRepeatedFieldOrder().isEqualTo(ProtoLoader.loadTextFile(
                StoreUser.newBuilder(),
                "/store_profile_update.prototxt"
            ));
        });
    }

    @Test void testProfileUpdateNonBlocking() {
        acquireMockedClient((client) -> {
            assertNotNull(
                client,
                "should not get `null` for mocked client"
            );
            var profile = resolve(
                client.update(async(ProfileUpdateRequest.newBuilder()
                    .setProfile(StoreUser.newBuilder()
                        .setKey(UserKey.newBuilder().setProfileId("abc123")))
                    .build()))
            );
            assertNotNull(
                profile,
                "fetched sync profile should not be `null`"
            );
            assertThat(
                profile
            ).ignoringRepeatedFieldOrder().isEqualTo(ProtoLoader.loadTextFile(
                StoreUser.newBuilder(),
                "/store_profile_update.prototxt"
            ));
        });
    }

    @Test void testProfileUsernameCheckBlockingSuccess() {
        acquireMockedClient((client) -> {
            assertNotNull(
                client,
                "should not get `null` for mocked client"
            );
            var available = client.usernameCheck("test");
            assertTrue(
                available,
                "should indicate test username is available"
            );
        });
    }

    @Test void testProfileUsernameCheckNonBlockingSuccess() {
        acquireMockedClient((client) -> {
            assertNotNull(
                client,
                "should not get `null` for mocked client"
            );
            var available = resolve(
                client.usernameCheck(async(UsernameCheckRequest.newBuilder()
                    .setUsername("test")
                    .build()))
            );
            assertTrue(
                available,
                "should indicate test username is available"
            );
        });
    }

    @Test void testProfileUsernameCheckBlockingFailureTaken() {
        acquireMockedClient((client) -> {
            assertNotNull(
                client,
                "should not get `null` for mocked client"
            );
            var available = client.usernameCheck("failure:taken");
            assertFalse(
                available,
                "should indicate known-taken username is not available"
            );
        });
    }

    @Test void testProfileUsernameCheckNonBlockingFailureTaken() {
        acquireMockedClient((client) -> {
            assertNotNull(
                    client,
                    "should not get `null` for mocked client"
            );
            var available = resolve(
                client.usernameCheck(async(UsernameCheckRequest.newBuilder()
                        .setUsername("failure:taken")
                        .build()))
            );
            assertFalse(
                available,
                "should indicate known-taken username is not available"
            );
        });
    }

    @Test void testProfileUsernameCheckBlockingFailureBlocked() {
        acquireMockedClient((client) -> {
            assertNotNull(
                client,
                "should not get `null` for mocked client"
            );
            assertThrows(UsernameInvalidError.class, () -> {
                client.usernameCheck("failure:policy");
            });
        });
    }

    @Test void testProfileUsernameCheckNonBlockingFailureBlocked() {
        acquireMockedClient((client) -> {
            assertNotNull(
                    client,
                    "should not get `null` for mocked client"
            );
            assertThrows(UsernameInvalidError.class, () -> {
                resolve(
                    client.usernameCheck(async(UsernameCheckRequest.newBuilder()
                        .setUsername("failure:policy")
                        .build()))
                );
            });
        });
    }

    @Test void testProfileUsernameCheckBlockingFailureIneligible() {
        acquireMockedClient((client) -> {
            assertNotNull(
                client,
                "should not get `null` for mocked client"
            );
            assertThrows(UsernameIneligibleError.class, () -> {
                client.usernameCheck("failure:ineligible");
            });
        });
    }

    @Test void testProfileUsernameCheckNonBlockingFailureIneligible() {
        acquireMockedClient((client) -> {
            assertNotNull(
                client,
                "should not get `null` for mocked client"
            );
            assertThrows(UsernameIneligibleError.class, () -> {
                resolve(
                    client.usernameCheck(async(UsernameCheckRequest.newBuilder()
                        .setUsername("failure:ineligible")
                        .build()))
                );
            });
        });
    }
}
