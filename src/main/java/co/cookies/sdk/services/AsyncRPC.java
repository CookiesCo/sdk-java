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
package co.cookies.sdk.services;

import com.google.api.gax.rpc.ApiCallContext;
import com.google.protobuf.Message;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;


/**
 *
 *
 * @param <R>
 */
@Immutable @ThreadSafe
public final class AsyncRPC<R extends Message> extends BaseRPC<R> {
    /**
     * Private constructor.
     *
     * @param request Wrapped async request.
     * @param timeout Wrapped operation timeout value.
     * @param context Custom call context, as applicable.
     */
    private AsyncRPC(@Nonnull R request, @Nonnull Timeout timeout, @Nullable ApiCallContext context) {
        super(request, timeout, context);
    }

    /**
     *
     *
     * @param request
     * @param <R>
     * @return
     */
    public static @Nonnull <R extends Message> AsyncRPC<R> of(@Nonnull R request) {
        return async(request, DEFAULT_TIMEOUT);
    }

    /**
     *
     *
     * @param request
     * @param timeout
     * @param <R>
     * @return
     */
    public static @Nonnull <R extends Message> AsyncRPC<R> async(@Nonnull R request, @Nonnull Timeout timeout) {
        return async(request, timeout, null);
    }

    /**
     *
     *
     * @param request
     * @param timeout
     * @param <R>
     * @return
     */
    public static @Nonnull <R extends Message> AsyncRPC<R> async(@Nonnull R request,
                                                                 @Nonnull Timeout timeout,
                                                                 @Nullable ApiCallContext context) {
        return new AsyncRPC<>(request, timeout, context);
    }
}
