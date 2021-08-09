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
import java.util.Optional;
import java.util.concurrent.TimeUnit;


/**
 * Defines shared attributes between blocking and non-blocking SDK operations, i.e. a held request, a timeout, and error
 * handling and reporting facilities.
 *
 * @param <R> Request message wrapped by this RPC descriptor.
 */
@Immutable @ThreadSafe
abstract class BaseRPC<R extends Message> implements RPCOperation<R> {
    /** Default client-side timeout applied to all RPC operations unless otherwise specified. */
    protected final static @Nonnull Timeout DEFAULT_TIMEOUT = Timeout.of(60, TimeUnit.SECONDS);

    /** Request wrapped by this RPC operation. */
    private final @Nonnull R request;

    /** Timeout unit for this RPC operation. */
    private final Timeout timeout;

    /** Optional custom call context to apply during this operation. */
    private final @Nullable ApiCallContext callContext;

    /**
     * Construct a new baseline RPC object from a child implementation.
     *
     * @param request Wrapped request protocol buffer object.
     * @param timeout Timeout value to apply.
     * @param callContext Call context to apply during this operation.
     */
    protected BaseRPC(@Nonnull R request,
                      @Nonnull Timeout timeout,
                      @Nullable ApiCallContext callContext) {
        this.request = request;
        this.timeout = timeout;
        this.callContext = callContext;
    }

    /**
     * Return the RPC request which this operation relates to.
     *
     * @return RPC request for this operation.
     */
    @Override
    public final @Nonnull R request() {
        return this.request;
    }

    /**
     * Return the timeout value applied to this RPC operation; either a custom timeout assigned by the developer, or a
     * default assigned by the SDK.
     *
     * @return Timeout value for this operation.
     */
    @Override
    public final @Nonnull Timeout timeout() {
        return this.timeout;
    }

    /**
     * Return the custom call context applied to this operation, if any.
     *
     * @return Custom call context, if any.
     */
    @Override
    public final @Nonnull Optional<ApiCallContext> context() {
        return Optional.ofNullable(this.callContext);
    }
}
