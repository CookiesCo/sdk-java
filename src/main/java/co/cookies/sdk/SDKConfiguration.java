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

import co.cookies.sdk.catalog.v1.CatalogClientV1;
import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.core.ExecutorProvider;
import com.google.api.gax.rpc.HeaderProvider;
import com.google.api.gax.rpc.TransportChannelProvider;
import org.slf4j.ILoggerFactory;

import javax.annotation.Nonnull;
import java.util.Optional;


/**
 * Defines the API surface for the main collection of configurable components that compose together to enable gRPC
 * services hosted by Cookies.
 *
 * <p>To customize low-level access to service interfaces, acquire your services with a customized implementation of
 * this interface. {@link CookiesSDKManager} is the default implementation.</p>
 *
 * <p>For example, to acquire an instance of the {@link CatalogClientV1} with custom configuration:
 * <code>
 *     CatalogClient.configure(new SDKConfiguration() {
 *         // ... your implementation here
 *     });
 * </code>
 * </p>
 */
public interface SDKConfiguration {
    /** @return Full endpoint to use by default with APIs. */
    @Nonnull String endpoint();

    /** @return Factory to use for spawning service loggers. */
    @Nonnull ILoggerFactory loggerFactory();

    /** @return Executor provider to use for mounted services. */
    @Nonnull ExecutorProvider executorProvider();

    /** @return Header provider to use for mounted services. */
    @Nonnull HeaderProvider headerProvider();

    /** @return Transport channel provider to use for mounted services. */
    @Nonnull TransportChannelProvider transportChannelProvider();

    /** @return Credentials provider to use for mounted services. */
    @Nonnull CredentialsProvider credentialsProvider();

    /** @return API key to use by default, if any. */
    @Nonnull Optional<String> getApiKey();
}
