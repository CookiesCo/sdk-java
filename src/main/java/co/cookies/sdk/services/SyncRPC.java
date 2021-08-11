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
 * Defines a convenience wrapper for a synchronous RPC operation, which can carry with it a timeout and an optional set
 * of call-level context to apply when executed.
 *
 * <p>Synchronous RPCs are <b>blocking</b>, meaning a synchronous call will block the thread it is issued from until a
 * response is received from the server. For asynchronous (non-blocking) RPC operations, check out {@link AsyncRPC}.</p>
 *
 * @param <R> Request type which is expected to be submitted with this RPC.
 */
@Immutable @ThreadSafe
public final class SyncRPC<R extends Message> extends BaseRPC<R> {
    /**
     * Private constructor.
     *
     * @param request Wrapped async request.
     * @param timeout Wrapped operation timeout value.
     * @param context Custom call context, as applicable.
     */
    private SyncRPC(@Nonnull R request, @Nonnull Timeout timeout, @Nullable ApiCallContext context) {
        super(request, timeout, context);
    }

    /**
     * Wrap the provided request in a synchronous RPC operation, which intends to block until a result is ready from the
     * remote service; any resulting message is returned directly to invoking code, and any thrown exception is raised
     * directly within invoking code.
     *
     * <p>As the simplest method variant, acquiring an RPC operation wrapper through this method will set a default
     * timeout and use default call context on behalf of the developer. See other method options to control these
     * aspects of execution.</p>
     *
     * @see #sync(Message, Timeout) Specify a custom RPC timeout
     * @see #sync(Message, Timeout, ApiCallContext) Create a sync RPC with context
     * @param request Request which we intend to send to the server synchronously.
     * @param <R> Message type for the request payload.
     * @return Synchronous RPC operation.
     */
    public static @Nonnull <R extends Message> SyncRPC<R> of(@Nonnull R request) {
        return sync(request, DEFAULT_TIMEOUT);
    }

    /**
     * Wrap the provided request in a synchronous RPC operation, which intends to block until a result is ready from the
     * remote service; any resulting message is returned directly to invoking code, and any thrown exception is raised
     * directly within invoking code.
     *
     * <p>This method variant allows specification of a custom timeout value, which is enforced no matter what happens
     * after the remote request begins execution. To additionally control call-specific call context, see the "See
     * Other" section for this method.</p>
     *
     * @see #sync(Message, Timeout, ApiCallContext) Create a sync RPC with context
     * @param request Request operation to wrap in a synchronous RPC container.
     * @param timeout Timeout value to apply to this operation.
     * @param <R> Request message type.
     * @return Synchronous RPC container.
     */
    public static @Nonnull <R extends Message> SyncRPC<R> sync(@Nonnull R request, @Nonnull Timeout timeout) {
        return sync(request, timeout, null);
    }

    /**
     * Wrap the provided request in a synchronous RPC operation, which intends to block until a result is ready from the
     * remote service; any resulting message is returned directly to invoking code, and any thrown exception is raised
     * directly within invoking code.
     *
     * <p>This method variant allows full customization of the synchronous operation, including the timeout applied, and
     * any call-specific context.</p>
     *
     * @param request Request operation to wrap in a synchronous RPC container.
     * @param timeout Timeout value to apply to this operation.
     * @param <R> Request message type.
     * @return Synchronous RPC container.
     */
    public static @Nonnull <R extends Message> SyncRPC<R> sync(@Nonnull R request,
                                                               @Nonnull Timeout timeout,
                                                               @Nullable ApiCallContext context) {
        return new SyncRPC<>(request, timeout, context);
    }

    /**
     * Convert a synchronous RPC operation into an asynchronous RPC operation, preserving the request, timeout, and any
     * call context assigned.
     *
     * <p>Synchronous methods are implemented via async with a simple wrapper that blocks until a result is ready. Thus,
     * <i>every</i> synchronous RPC travels through this method before execution. The distinction between synchronous
     * and asynchronous operations also allows clean method signatures without triggering type erasure problems.</p>
     *
     * @return Asynchronous RPC operation corresponding to this synchronous operation.
     */
    public @Nonnull AsyncRPC<R> unwrap() {
        return AsyncRPC.async(
            request(),
            timeout(),
            context().orElse(null)
        );
    }
}
