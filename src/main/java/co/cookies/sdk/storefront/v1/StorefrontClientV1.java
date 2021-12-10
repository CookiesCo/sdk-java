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


import co.cookies.sdk.CookiesSDK;
import co.cookies.sdk.SDKConfiguration;
import co.cookies.sdk.exceptions.RPCExecutionException;
import co.cookies.sdk.exceptions.ServiceSetupError;
import co.cookies.sdk.services.AsyncRPC;
import co.cookies.sdk.services.BaseService;
import co.cookies.sdk.services.BaseServiceInfo;
import co.cookies.sdk.storefront.Storefront;
import co.cookies.sdk.storefront.v1.err.UsernameIneligibleError;
import co.cookies.sdk.storefront.v1.err.UsernameInvalidError;
import co.cookies.sdk.storefront.v1.stub.MenuV1Stub;
import co.cookies.sdk.storefront.v1.stub.ProfileV1Stub;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import cookies.schema.store.*;
import cookies.schema.store.model.StoreUser;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.slf4j.ILoggerFactory;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;
import java.util.Optional;
import java.util.concurrent.Executors;

import static co.cookies.sdk.SDKUtil.protect;
import static com.google.common.util.concurrent.Futures.catching;


/**
 * Combined client facade implementing the Cookies Storefront API, version 1, which provides access to digital commerce
 * features for the Cookies Platform, in cooperation with close partners.
 *
 * <p>The <a href="https://go.cookies.co/apis">Storefront API</a> is a <b>private API service</b> enabling Cookies and
 * close partners to transact online with consumers and businesses. Access to this API requires an bearer token
 * (referred to as an <i>API key</i>) issued by Tech@Cookies, and for most methods, an authorized user access token.</p>
 */
@Immutable @ThreadSafe
@SuppressWarnings({"UnstableApiUsage", "unused"})
public final class StorefrontClientV1 implements Storefront {
    private final MenuClientV1 menuClient;
    private final ProfileClientV1 profileClient;

    // Private constructor. Please use static factory methods.
    private StorefrontClientV1(MenuClientV1 menuClient, ProfileClientV1 profileClient) {
        this.menuClient = menuClient;
        this.profileClient = profileClient;
    }

    /**
     * API client implementing access to the Storefront Menu Service, version 1; the menu service is responsible for
     * rendering final-goods data into abstract product-level menus in different contexts, and is typically consumed by
     * storefront UI implementations.
     *
     * <p>The <a href="https://go.cookies.co/apis">Storefront API</a>, and by extension this <b>Menu API</b>, is a
     * <b>private API service</b> enabling Cookies and close partners to transact online with consumers and businesses.
     * Access to this API requires an bearer token (referred to as an <i>API key</i>) issued by Tech@Cookies.</p>
     */
    @Immutable @ThreadSafe
    public final static class MenuClientV1 extends BaseService<MenuV1Client> implements Storefront.MenuClient {
        public static final String NAME = "menu";
        public static final String VERSION = "v1";

        /** Specification describing this service. */
        public final static MenuServiceInfo INFO = new MenuServiceInfo();

        /** Service info specification for the Storefront Menu API, version 1. */
        @Immutable @ThreadSafe
        public final static class MenuServiceInfo extends BaseServiceInfo {
            // Name of this service.
            @Override
            public @Nonnull String serviceName() {
                return NAME;
            }

            // Version of this service.
            @Override
            public @Nonnull String serviceVersion() {
                return VERSION;
            }

            // Whether API keys are required by this service.
            @Override
            public boolean apiKeyRequired() {
                return true;
            }

            // Whether authorization is required by this service.
            @Override
            public boolean authorizationRequired() {
                return false;
            }
        }

        /**
         * Private constructor. Please use static factories.
         *
         * @param menuClient Menu client object to mount.
         * @param serviceLogger Logger for this service.
         */
        private MenuClientV1(@Nonnull MenuV1Client menuClient, @Nonnull ILoggerFactory serviceLogger) {
            super(INFO, menuClient, serviceLogger);
        }

        // -- Static Factories -- //

        /**
         * Configure an instance of the Menu Client facade using the provided SDK configuration suite, including any
         * specified custom endpoint, executor provider, header provider, credential provider, and transport channel
         * provider.
         *
         * <p>When using this interface instead of the builder methods exposed via {@link CookiesSDK#builder()}, the
         * developer is required to manage all service state manually. Please see the docs for {@link CookiesSDK} for
         * more information about how to acquire a Menu API client easily.</p>
         *
         * @param configuration Configuration to use when creating the new facade.
         * @return Instance of the Menu API client facade.
         */
        public static @Nonnull MenuClientV1 configure(@Nonnull SDKConfiguration configuration) {
            return protect(ServiceSetupError::new, () -> new MenuClientV1(
                MenuV1Client.create(MenuV1Settings.newBuilder()
                    .setEndpoint(configuration.endpoint())
                    .setBackgroundExecutorProvider(configuration.executorProvider())
                    .setHeaderProvider(configuration.headerProvider())
                    .setCredentialsProvider(configuration.credentialsProvider())
                    .setTransportChannelProvider(configuration.transportChannelProvider())
                    .build()),
                configuration.loggerFactory()
            ));
        }

        /**
         * Create an instance of the Menu Client backed entirely with defaults, including a default executor (started
         * with 3 threads at the time of writing), a default header provider applying any API key and authorization
         * settings, an instantiating gRPC transport, and a default empty credential provider.
         *
         * @return Menu client facade.
         */
        public static @Nonnull MenuClientV1 defaults() {
            return protect(ServiceSetupError::new, () -> new MenuClientV1(
                MenuV1Client.create(),
                LoggerFactory.getILoggerFactory()
            ));
        }

        /**
         * Create an instance of the Menu Client backed directly with the provided stub (this is generally useful for
         * testing and in-process or inter-process dispatch).
         *
         * @param stub Service stub to use for the newly-minted client facade.
         * @return Menu client facade.
         */
        public static @Nonnull MenuClientV1 forStub(@Nonnull MenuV1Stub stub) {
            return protect(ServiceSetupError::new, () -> new MenuClientV1(
                MenuV1Client.create(stub),
                LoggerFactory.getILoggerFactory()
            ));
        }

        // -- Compliance: Base Service -- //

        /** @inheritDoc */
        @Override
        public @Nonnull ListeningScheduledExecutorService executorService() {
            if (service().getSettings() != null) {
                return MoreExecutors.listeningDecorator(
                    service().getSettings().getBackgroundExecutorProvider().getExecutor()
                );
            }

            // no custom executor: use a single-thread scheduled executor.
            return MoreExecutors.listeningDecorator(
                Executors.newSingleThreadScheduledExecutor()
            );
        }

        // -- Implementation: Menu V1 -- //

        /** @inheritDoc */
        @Override
        public @Nonnull ListenableFuture<MenuResponse> menu(@Nonnull AsyncRPC<MenuRequest> rpc) {
            return execute(
                rpc,
                MenuV1Grpc.getMenuMethod(),
                service().menuCallable()::futureCall
            );
        }

        /** @inheritDoc */
        @Override
        public @Nonnull ListenableFuture<ProductGroupResponse> product(@Nonnull AsyncRPC<ProductGroupRequest> rpc) {
            return execute(
                rpc,
                MenuV1Grpc.getProductFetchMethod(),
                service().productFetchCallable()::futureCall
            );
        }
    }

    /**
     * API client implementing access to the Storefront Profile Service, version 1; the profile service is responsible
     * for fetching and updating user profiles, usernames, avatars, and so forth and is typically consumed by storefront
     * UI implementations.
     *
     * <p>The <a href="https://go.cookies.co/apis">Storefront API</a>, and by extension this <b>Profile API</b>, is a
     * <b>private API service</b> enabling Cookies and close partners to transact online with consumers and businesses.
     * Access to this API requires an bearer token (referred to as an <i>API key</i>) issued by Tech@Cookies, and all
     * methods require an active and unexpired user access token.</p>
     */
    @Immutable @ThreadSafe
    public final static class ProfileClientV1 extends BaseService<ProfileV1Client> implements Storefront.ProfileClient {
        public static final String NAME = "profile";
        public static final String VERSION = "v1";

        /** Specification describing this service. */
        public final static ProfileServiceInfo INFO = new ProfileServiceInfo();

        /** Service info specification for the Profile API, version 1. */
        @Immutable @ThreadSafe
        public final static class ProfileServiceInfo extends BaseServiceInfo {
            // Name of this service.
            @Override
            public @Nonnull String serviceName() {
                return NAME;
            }

            // Version of this service.
            @Override
            public @Nonnull String serviceVersion() {
                return VERSION;
            }

            // Whether API keys are required by this service.
            @Override
            public boolean apiKeyRequired() {
                return true;
            }

            // Whether authorization is required by this service.
            @Override
            public boolean authorizationRequired() {
                return true;
            }
        }

        /**
         * Private constructor. Please use static factories.
         *
         * @param profileClient Profile client object to mount.
         * @param serviceLogger Logger for this service.
         */
        public ProfileClientV1(@Nonnull ProfileV1Client profileClient, @Nonnull ILoggerFactory serviceLogger) {
            super(INFO, profileClient, serviceLogger);
        }

        // -- Static Factories -- //

        /**
         * Configure an instance of the Profile Client facade using the provided SDK configuration suite, including any
         * specified custom endpoint, executor provider, header provider, credential provider, and transport channel
         * provider.
         *
         * <p>When using this interface instead of the builder methods exposed via {@link CookiesSDK#builder()}, the
         * developer is required to manage all service state manually. Please see the docs for {@link CookiesSDK} for
         * more information about how to acquire a Profile API client easily.</p>
         *
         * @param configuration Configuration to use when creating the new facade.
         * @return Instance of the Profile API client facade.
         */
        public static @Nonnull ProfileClientV1 configure(@Nonnull SDKConfiguration configuration) {
            return protect(ServiceSetupError::new, () -> new ProfileClientV1(
                    ProfileV1Client.create(ProfileV1Settings.newBuilder()
                            .setEndpoint(configuration.endpoint())
                            .setBackgroundExecutorProvider(configuration.executorProvider())
                            .setHeaderProvider(configuration.headerProvider())
                            .setCredentialsProvider(configuration.credentialsProvider())
                            .setTransportChannelProvider(configuration.transportChannelProvider())
                            .build()),
                    configuration.loggerFactory()
            ));
        }

        /**
         * Create an instance of the Profile Client backed entirely with defaults, including a default executor (started
         * with 3 threads at the time of writing), a default header provider applying any API key and authorization
         * settings, an instantiating gRPC transport, and a default empty credential provider.
         *
         * @return Profile client facade.
         */
        public static @Nonnull ProfileClientV1 defaults() {
            return protect(ServiceSetupError::new, () -> new ProfileClientV1(
                    ProfileV1Client.create(),
                    LoggerFactory.getILoggerFactory()
            ));
        }

        /**
         * Create an instance of the Profile Client backed directly with the provided stub (this is generally useful for
         * testing and in-process or inter-process dispatch).
         *
         * @param stub Service stub to use for the newly-minted client facade.
         * @return Profile client facade.
         */
        public static @Nonnull ProfileClientV1 forStub(@Nonnull ProfileV1Stub stub) {
            return protect(ServiceSetupError::new, () -> new ProfileClientV1(
                    ProfileV1Client.create(stub),
                    LoggerFactory.getILoggerFactory()
            ));
        }

        // -- Compliance: Base Service -- //

        /** @inheritDoc */
        @Override
        public @Nonnull ListeningScheduledExecutorService executorService() {
            if (service().getSettings() != null) {
                return MoreExecutors.listeningDecorator(
                    service().getSettings().getBackgroundExecutorProvider().getExecutor()
                );
            }

            // no custom executor: use a single-thread scheduled executor.
            return MoreExecutors.listeningDecorator(
                Executors.newSingleThreadScheduledExecutor()
            );
        }

        // -- Implementation: Profile V1 -- //

        /** @inheritDoc */
        @Override
        public @Nonnull ListenableFuture<Boolean> usernameCheck(@Nonnull AsyncRPC<UsernameCheckRequest> rpc) {
            return catching(execute(
                rpc,
                ProfileV1Grpc.getProfileUsernameCheckMethod(),
                service().profileUsernameCheckCallable()::futureCall,
                (response) -> true
            ), RPCExecutionException.class, (exc) -> {
                var cause = exc != null ? exc.getCause() : null;
                while (cause != null && !(cause instanceof StatusRuntimeException)) {
                    cause = cause.getCause();
                }
                if (cause != null) {
                    var responseCode = ((StatusRuntimeException) cause).getStatus().getCode();

                    switch(responseCode) {
                        // if a `FAILED_PRECONDITION` status is returned, the user has not yet activated their account,
                        // which is a pre-requisite for picking a username.
                        case FAILED_PRECONDITION: throw UsernameIneligibleError.create();

                        // if an `INVALID_ARGUMENT` status is returned, the username is not taken but is also not
                        // available for policy reasons (potentially because it includes hate speech or other banned
                        // terms or invalid username options).
                        case INVALID_ARGUMENT: throw UsernameInvalidError.create();

                        // if an `ALREADY_EXISTS` status is returned, the username is taken by another user.
                        case ALREADY_EXISTS: return false;

                        // otherwise, it's just a regular old error, not an indication from the server relating to our
                        // username check request, so we just re-throw the exception.
                        default: break;
                    }
                }
                // rethrow
                throw (exc != null ? exc : Status.INTERNAL.asRuntimeException());
            }, executorService());
        }

        /** @inheritDoc */
        @Override
        public @Nonnull ListenableFuture<Optional<ProfileResponse>> fetch(@Nonnull AsyncRPC<ProfileRequest> rpc) {
            return execute(
                rpc,
                ProfileV1Grpc.getProfileMethod(),
                service().profileCallable()::futureCall,
                Optional::of
            );
        }

        /** @inheritDoc */
        @Override
        public @Nonnull ListenableFuture<StoreUser> update(@Nonnull AsyncRPC<ProfileUpdateRequest> rpc) {
            return execute(
                rpc,
                ProfileV1Grpc.getProfileUpdateMethod(),
                service().profileUpdateCallable()::futureCall
            );
        }
    }

    // -- Static Factories -- //

    /**
     * Acquire an instance of the main Storefront service facade, using the provided clients as constituent services
     * to be handed back to the developer when requested.
     *
     * @param menuClient Menu client instance to use.
     * @param profileClient Profile client instance to use.
     * @return Instance of the Storefront client facade, configured with the provided clients.
     */
    public static @Nonnull StorefrontClientV1 withServices(@Nonnull MenuClientV1 menuClient,
                                                           @Nonnull ProfileClientV1 profileClient) {
        return new StorefrontClientV1(
            menuClient,
            profileClient
        );
    }

    // -- API Client Access -- //

    /** @inheritDoc */
    @Override
    public @Nonnull MenuClient menu(@Nonnull Optional<SDKConfiguration> configuration) {
        return configuration.map(MenuClientV1::configure).orElse(menuClient);
    }

    /** @inheritDoc */
    @Override
    public @Nonnull ProfileClient profile(@Nonnull Optional<SDKConfiguration> configuration) {
        return configuration.map(ProfileClientV1::configure).orElse(profileClient);
    }
}
