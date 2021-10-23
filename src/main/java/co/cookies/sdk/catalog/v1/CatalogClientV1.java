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
import co.cookies.sdk.SDKConfiguration;
import co.cookies.sdk.catalog.CatalogClient;
import co.cookies.sdk.catalog.v1.stub.CatalogV1Stub;
import co.cookies.sdk.exceptions.ServiceSetupError;
import co.cookies.sdk.services.AsyncRPC;
import co.cookies.sdk.services.BaseService;
import co.cookies.sdk.services.BaseServiceInfo;
import co.cookies.sdk.services.Client;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import cookies.schema.Brand;
import cookies.schema.Strain;
import cookies.schema.catalog.*;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

import static co.cookies.sdk.SDKUtil.protect;


/**
 * API client implementing access to the Cookies Catalog API, which provides canonical catalog content for products made
 * by or carried by Cookies.
 *
 * <p>The <a href="https://console.api.cookies.co">Catalog API</a> is a <b>private API service</b> enabling Cookies
 * partners to consume raw content. It does not provide inventory signals or other information. Content may be requested
 * in a specific localized language or format.</p>
 */
@Immutable @ThreadSafe
@Client(name = CatalogClientV1.NAME, version = CatalogClientV1.VERSION)
public final class CatalogClientV1 extends BaseService<CatalogV1Client> implements CatalogClient {
    public static final String NAME = "catalog";
    public static final String VERSION = "v1";

    /** Specification describing this service. */
    public final static CatalogServiceInfo INFO = new CatalogServiceInfo();

    /** Service info specification for the Catalog API. */
    @Immutable @ThreadSafe
    public final static class CatalogServiceInfo extends BaseServiceInfo {
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
     * @param catalogClient Catalog client object to mount.
     * @param serviceLogger Logger for this service.
     */
    private CatalogClientV1(@Nonnull CatalogV1Client catalogClient,
                            @Nonnull ILoggerFactory serviceLogger) {
        super(INFO, catalogClient, serviceLogger);
    }

    // -- Client Acquisition -- //

    /**
     * Configure an instance of the Catalog Client facade using the provided SDK configuration suite, including any
     * specified custom endpoint, executor provider, header provider, credential provider, and transport channel
     * provider.
     *
     * <p>When using this interface instead of the builder methods exposed via {@link CookiesSDK#builder()}, the
     * developer is required to manage all service state manually. Please see the docs for {@link CookiesSDK} for more
     * information about how to acquire a Catalog API client easily.</p>
     *
     * @param configuration Configuration to use when creating the new facade.
     * @return Instance of the Catalog API client facade.
     */
    public static @Nonnull CatalogClientV1 configure(@Nonnull SDKConfiguration configuration) {
        return protect(ServiceSetupError::new, () -> new CatalogClientV1(
                CatalogV1Client.create(CatalogV1Settings.newBuilder()
                    .setEndpoint(configuration.endpoint())
                    .setExecutorProvider(configuration.executorProvider())
                    .setHeaderProvider(configuration.headerProvider())
                    .setCredentialsProvider(configuration.credentialsProvider())
                    .setTransportChannelProvider(configuration.transportChannelProvider())
                    .build()),
                configuration.loggerFactory()
        ));
    }

    /**
     * Create an instance of the Catalog Client backed entirely with defaults, including a default executor (started
     * with 3 threads at the time of writing), a default header provider applying any API key settings, an instantiating
     * gRPC transport, and a default empty credential provider.
     *
     * @return Catalog client facade.
     */
    public static @Nonnull CatalogClientV1 defaults() {
        return protect(ServiceSetupError::new, () -> new CatalogClientV1(
            CatalogV1Client.create(),
            LoggerFactory.getILoggerFactory()
        ));
    }

    /**
     * Create an instance of the Catalog Client backed directly with the provided stub (this is generally useful for
     * testing and in-process or inter-process dispatch).
     *
     * @param stub Service stub to use for the newly-minted client facade.
     * @return Catalog client facade.
     */
    public static @Nonnull CatalogClientV1 forStub(@Nonnull CatalogV1Stub stub) {
        return protect(ServiceSetupError::new, () -> new CatalogClientV1(
            CatalogV1Client.create(stub),
            LoggerFactory.getILoggerFactory()
        ));
    }

    // -- Compliance: Base Service -- //

    /** @inheritDoc */
    @Override
    public @Nonnull ListeningScheduledExecutorService executorService() {
        if (service().getSettings() != null) {
            return MoreExecutors.listeningDecorator(service().getSettings().getExecutorProvider().getExecutor());
        }

        // no custom executor: use a single-thread scheduled executor.
        return MoreExecutors.listeningDecorator(
            Executors.newSingleThreadScheduledExecutor()
        );
    }

    /** @inheritDoc */
    @Override
    public @Nonnull Logger logger() {
        return logging;
    }

    // -- API Interface: Catalog -- //

    /** @inheritDoc */
    @Override
    public @Nonnull ListenableFuture<Collection<Brand>> brands(@Nonnull AsyncRPC<BrandsRequest> rpc) {
        return execute(
            rpc,
            CatalogV1Grpc.getBrandsMethod(),
            service().brandsCallable()::futureCall,
            (response) -> ImmutableList.copyOf(response.getBrandList())
        );
    }

    /** @inheritDoc */
    @Override
    public @Nonnull ListenableFuture<Collection<Strain>> strains(@Nonnull AsyncRPC<StrainsRequest> rpc) {
        return execute(
            rpc,
            CatalogV1Grpc.getStrainsMethod(),
            service().strainsCallable()::futureCall,
            (response) -> ImmutableList.copyOf(response.getStrainList())
        );
    }

    /** @inheritDoc */
    @Override
    public @Nonnull ListenableFuture<Optional<FinalProduct>> product(@Nonnull AsyncRPC<ProductRequest> rpc) {
        return execute(
            rpc,
            CatalogV1Grpc.getProductMethod(),
            service().productCallable()::futureCall,
            (response) -> response.hasProduct() ? Optional.empty() : Optional.of(response.getProduct())
        );
    }

    /** @inheritDoc */
    @Override
    public @Nonnull ListenableFuture<Stream<CatalogProduct>> sync(@Nonnull AsyncRPC<MultiProductRequest> rpc) {
        return stream(
            rpc,
            CatalogV1Grpc.getSyncMethod(),
            service().syncCallable()::call,
            (response) -> response.getProductList().stream()
        );
    }
}
