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

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;

import static java.lang.String.format;


/**
 * Provides an extension point for custom SDK implementations, along with a custom settings payload; additionally, acts
 * as a self-configuring manager which may be used to spawn services.
 *
 * @param <Settings> Object which designates available settings for this SDK implementation. Must implement at least all
 *                  required properties of {@link SDKConfiguration}.
 */
@Immutable @ThreadSafe
public abstract class ExtensibleCookiesSDK<Settings extends SDKConfiguration>
        implements SDKConfiguration {
    static final @Nonnull String DEFAULT_API_HOSTNAME = "api.cookies.co";
    static final @Nonnull String DEFAULT_API_SCHEME = "https";
    static final int DEFAULT_API_PORT = 443;

    /** Enumerates known/important headers for RPC communications. */
    enum Headers {
        /** Header that holds the user's designated API key. */
        API_KEY("x-api-key"),

        /** API client agent header. Injected by the SDK. */
        USER_AGENT("user-agent");

        // Name to use for the header under the hood.
        private final @Nonnull String headerName;

        // Private constructor for an enumerated header.
        Headers(@Nonnull String headerName) {
            this.headerName = headerName;
        }

        /** @return Name of the header to use in an HTTP request. */
        @Override
        public @Nonnull String toString() {
            return this.headerName;
        }
    }

    /** @return Default SDK endpoint hostname. */
    public static @Nonnull String getDefaultApiHostname() {
        return DEFAULT_API_HOSTNAME;
    }

    /** @return Default SDK endpoint port. */
    public static int getDefaultApiPort() {
        return DEFAULT_API_PORT;
    }

    /** @return Full endpoint to use by default with APIs. */
    static @Nonnull String getDefaultApiEndpoint() {
        return format(
            "%s:%s",
            getDefaultApiHostname(),
            getDefaultApiPort()
        );
    }

    /** @return Active settings for this SDK implementation. */
    abstract @Nonnull Settings getSettings();
}
