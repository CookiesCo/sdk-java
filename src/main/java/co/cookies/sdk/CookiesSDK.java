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


import co.cookies.sdk.services.AsyncRPC;
import co.cookies.sdk.services.SyncRPC;
import com.google.protobuf.Message;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;


/**
 * Provides the main facade for acquiring a {@link CookiesSDKManager}, via {@link #builder()}.
 *
 * <p>Build SDK managers are immutable, and any bound services spawned from one adopt the settings provided by the user
 * to the original builder, as applicable.</p>
 */
@Immutable @ThreadSafe
public final class CookiesSDK {
    private CookiesSDK() { /* Disallow instantiation. */ }

    /**
     * Acquire a fresh builder which can be used to acquire an instance of {@link CookiesSDKManager}.
     *
     * <p>Using the builder methods at {@link CookiesSDKManager.Builder}, invoking developers can customize such things
     * as the executor, logging tools, credentials, and keys used across services. After building the manager, the
     * resulting object is immutable and may safely be used from multiple threads.</p>
     *
     * @return Fresh, defaults-only builder for an SDK manager.
     */
    public static @Nonnull CookiesSDKManager.Builder builder() {
        return new AutoValue_CookiesSDKManager.Builder();
    }

    /**
     * Wrap a request in a synchronous (blocking) RPC operation, where the response is returned directly to invoking
     * code, which waits until any activity completes before returning.
     *
     * @param request RPC request to wrap.
     * @param <R> Request message type.
     * @return Synchronous RPC wrapper, which can be passed to an RPC method.
     */
    public static @Nonnull <R extends Message> SyncRPC<R> sync(@Nonnull R request) {
        return SyncRPC.of(request);
    }

    /**
     * Wrap a request in an asynchronous (non-blocking) RPC operation, where a future value container is returned in
     * lieu of a response, which eventually either resolves to a return result or an error.
     *
     * @param request RPC request to wrap.
     * @param <R> Request message type.
     * @return Async RPC wrapper, which can be passed to an RPC method.
     */
    public static @Nonnull <R extends Message> AsyncRPC<R> async(@Nonnull R request) {
        return AsyncRPC.of(request);
    }
}
