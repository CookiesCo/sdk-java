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
 * Defines a convenience wrapper for an asynchronous RPC operation, which can carry with it a timeout and an optional
 * set of call-level context to apply when executed.
 *
 * <p>Asynchronous RPCs are <b>non-blocking</b>, meaning an asynchronous call will never block the thread it is issued
 * from, instead returning immediately with a future container for a result value.</p>
 *
 * @param <R> Request type which is expected to be submitted with this RPC.
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
     * Wrap the provided protocol buffer request in an asynchronous RPC container.
     *
     * @param request Request which we should wrap.
     * @param <R> Request type we are wrapping.
     * @return Wrapped request as an asynchronous RPC.
     */
    public static @Nonnull <R extends Message> AsyncRPC<R> of(@Nonnull R request) {
        return async(request, DEFAULT_TIMEOUT);
    }

    /**
     * Wrap the provided protocol buffer request in an asynchronous RPC container, specifying a timeout to be enforced
     * when the operation executes.
     *
     * @param request Request which we should wrap.
     * @param timeout Timeout to enforce.
     * @param <R> Request type we are wrapping.
     * @return Wrapped request as an asynchronous RPC.
     */
    public static @Nonnull <R extends Message> AsyncRPC<R> async(@Nonnull R request, @Nonnull Timeout timeout) {
        return async(request, timeout, null);
    }

    /**
     * Wrap the provided protocol buffer request in an asynchronous RPC container, specifying a timeout to be enforced
     * when the operation executes, and a set of call-level context to apply.
     *
     * @param request Request which we should wrap.
     * @param timeout Timeout to enforce.
     * @param context Context to apply to this call only.
     * @param <R> Request type we are wrapping.
     * @return Wrapped request as an asynchronous RPC.
     */
    public static @Nonnull <R extends Message> AsyncRPC<R> async(@Nonnull R request,
                                                                 @Nonnull Timeout timeout,
                                                                 @Nullable ApiCallContext context) {
        return new AsyncRPC<>(request, timeout, context);
    }
}
