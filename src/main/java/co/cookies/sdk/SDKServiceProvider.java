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

import javax.annotation.Nonnull;
import java.io.Closeable;
import java.util.Optional;


/**
 * Specifies the API surface of an object which provides access to Cookies service clients.
 *
 * <p>Typically, invoking code would use {@link CookiesSDKManager} (acquired via {@link CookiesSDK#builder()}) to
 * access these methods.</p>.
 */
public interface SDKServiceProvider extends Closeable, AutoCloseable {
    /**
     * Access a pre-configured instance of the {@link CatalogClient}, which enables access to the Catalog API.
     *
     * <p>The Catalog API is a service meant for <b>trading partners</b>, <b>development partners</b>, and other
     * affiliated organizations, and calls an API backend which requires authentication with an API key, and, depending
     * on scope, authorized credentials.</p>
     *
     * <p>These credentials and keys can be acquired through the Tech@Cookies team. See main library docs for more
     * information.</p>
     *
     * @return Pre-configured Catalog API client.
     */
    default @Nonnull CatalogClient catalog() {
        return catalog(Optional.empty());
    }

    /**
     * Configure and return a customized instance of the {@link CatalogClient}, which enables access to the Catalog API.
     *
     * <p>The Catalog API is a service meant for <b>trading partners</b>, <b>development partners</b>, and other
     * affiliated organizations, and calls an API backend which requires authentication with an API key, and, depending
     * on scope, authorized credentials.</p>
     *
     * <p>These credentials and keys can be acquired through the Tech@Cookies team. See main library docs for more
     * information.</p>
     *
     * <p><b>Note:</b> Instead of configuring a custom service, invoking code should consider using the builder methods
     * for {@link CookiesSDK}, which keep configuration uniform across services.</p>
     *
     * @param serviceConfiguration Custom configuration to apply to the resulting catalog service.
     * @return Configured Catalog API client.
     */
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    @Nonnull CatalogClient catalog(@Nonnull Optional<SDKConfiguration> serviceConfiguration);
}
