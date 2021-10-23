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
package co.cookies.sdk.catalog;


import co.cookies.sdk.CookiesSDK;
import co.cookies.sdk.SDKServiceProvider;
import co.cookies.sdk.catalog.v1.CatalogV1Client;
import co.cookies.sdk.services.AsyncRPC;
import co.cookies.sdk.services.ServiceClient;
import co.cookies.sdk.services.SyncRPC;
import com.google.common.util.concurrent.ListenableFuture;
import cookies.schema.Brand;
import cookies.schema.Strain;
import cookies.schema.catalog.*;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

import static co.cookies.sdk.SDKUtil.block;


/**
 * Defines the main API surface for the Java client facade implementing the Cookies Catalog API, which provides invoking
 * code access to canonical product data about items made by, or carried by, Cookies.
 *
 * <p>Instances of the Catalog API Client may be acquired via the {@link CookiesSDK#builder()} method, which can be
 * built into an SDK manager that provides a client via {@link SDKServiceProvider#catalog()}. Customizations can be
 * applied for this service's construction via the methods offered on the builder.</p>
 *
 * <p><b>Note:</b> The Cookies Catalog API is a <b>private API service</b> designed for use by Cookies Engineering,
 * partners, contractors, and vendors. To invoke methods on this API, one must provide a valid API key and set of
 * credentials with sufficient authorization scopes.</p>
 */
public interface CatalogClient extends ServiceClient<CatalogV1Client> {
    // -- API: Brands -- //

    /**
     * <b>Retrieve a list of brands</b> known as members of the Cookies network/umbrella, optionally filtering by any
     * present criteria on the provided request.
     *
     * <p>To provide no criteria at all, just pass {@link BrandsRequest#getDefaultInstance()}.</p>
     *
     * <p><b>Blocking:</b> This method blocks until a response is available from the server.</p>
     *
     * @param rpc RPC operation to retrieve a set of canonical Cookies brands.
     * @return Collection of resulting brand records.
     */
    default @Nonnull Collection<Brand> brands(@Nonnull SyncRPC<BrandsRequest> rpc) {
        return block(rpc, logger(), CatalogV1Grpc.getBrandsMethod(), this::brands);
    }

    /**
     * <b>Retrieve a list of brands</b> known as members of the Cookies network/umbrella, optionally filtering by any
     * present criteria on the provided request.
     *
     * <p>To provide no criteria at all, just pass {@link BrandsRequest#getDefaultInstance()}.</p>
     *
     * <p><b>Non-blocking:</b> This method immediately returns a container for a future value, rather than blocking
     * until the server produces a response.</p>
     *
     * @param rpc RPC operation to retrieve a set of canonical Cookies brands.
     * @return Future for a collection of resulting brand records.
     */
    @Nonnull ListenableFuture<Collection<Brand>> brands(@Nonnull AsyncRPC<BrandsRequest> rpc);

    // -- API: Strains -- //

    /**
     * <b>Retrieve a list of strains</b> produced or designed or sold by Cookies or network stores, optionally filtering
     * by present criteria on the provided request.
     *
     * <p>To provide no criteria at all, just pass {@link BrandsRequest#getDefaultInstance()}.</p>
     *
     * <p><b>Blocking:</b> This method blocks until a response is available from the server.</p>
     *
     * @param rpc RPC operation to retrieve a set of canonical Cookies strains.
     * @return Collection of resulting brand records.
     */
    default @Nonnull Collection<Strain> strains(@Nonnull SyncRPC<StrainsRequest> rpc) {
        return block(rpc, logger(), CatalogV1Grpc.getStrainsMethod(), this::strains);
    }

    /**
     * <b>Retrieve a list of strains</b> produced or designed or sold by Cookies or network stores, optionally filtering
     * by present criteria on the provided request.
     *
     * <p>To provide no criteria at all, just pass {@link BrandsRequest#getDefaultInstance()}.</p>
     *
     * <p><b>Non-blocking:</b> This method immediately returns a container for a future value, rather than blocking
     * until the server produces a response.</p>
     *
     * @param rpc RPC operation to retrieve a set of canonical Cookies strains.
     * @return Future for a collection of resulting strain records.
     */
    @Nonnull ListenableFuture<Collection<Strain>> strains(@Nonnull AsyncRPC<StrainsRequest> rpc);

    // -- API: Product Fetch -- //

    /**
     * <b>Retrieve a canonical product record</b> addressed by its unique CTIN, present on the provided request.
     *
     * <p><b>Blocking:</b> This method blocks until a response is available from the server.</p>
     *
     * @param rpc RPC operation to retrieve an individual canonical product record.
     * @return Resulting product record, or {@link Optional#empty()} if it could not be found.
     */
    default @Nonnull Optional<FinalProduct> product(@Nonnull SyncRPC<ProductRequest> rpc) {
        return block(rpc, logger(), CatalogV1Grpc.getProductMethod(), this::product);
    }

    /**
     * <b>Retrieve a canonical product record</b> addressed by its unique CTIN, present on the provided request.
     *
     * <p><b>Non-blocking:</b> This method immediately returns a container for a future value, rather than blocking
     * until the server produces a response.</p>
     *
     * @param rpc RPC operation to retrieve an individual canonical product record.
     * @return Resulting product record, or {@link Optional#empty()} if it could not be found.
     */
    @Nonnull ListenableFuture<Optional<FinalProduct>> product(@Nonnull AsyncRPC<ProductRequest> rpc);

    // -- API: Product Sync -- //

    /**
     * <b>Given batch of product requests</b>, limited not-to-exceed the ceiling of products per request batch (`300` at
     * at the time of writing), stream a response of found product records, if any, matched from the central canonical
     * product database hosted by Cookies.
     *
     * <p>This method leverages server-side streaming, such that a single batch of fetch requests may produce more than
     * one response. The resulting stream of responses accounts for this batching and presents a smooth interface,
     * consuming product records until all queries have concluded and all records returned from the server.</p>
     *
     * <p>To efficiently fetch more than the limit (300 products), simply submit multiple requests, handle any errors
     * as responses resolve, and join the resulting streams.</p>
     *
     * @param rpc Batch of product records containing between one and `300` product records the invoking code wishes
     *            to validate or otherwise resolve from canonical catalog storage.
     * @return Stream of products resulting from the provided multi-product batch request.
     * @throws IllegalArgumentException If a request batch exceeds the batch size limit (currently `300` products).
     */
    @Nonnull ListenableFuture<Stream<CatalogProduct>> sync(@Nonnull AsyncRPC<MultiProductRequest> rpc);
}
