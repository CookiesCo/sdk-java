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
import java.util.Optional;


/**
 * Defines the public API surface for an object describing a single RPC operation, regardless of operating mode (async
 * or sync).
 */
public interface RPCOperation<R extends Message> {
    /**
     * Return the request which this RPC operation wraps; requests are implemented just like responses, via Protocol
     * Buffers objects.
     *
     * @return Request which this operation wraps.
     */
    @Nonnull R request();

    /**
     * Return the timeout value applied to this RPC operation; either a custom timeout assigned by the developer, or a
     * default assigned by the SDK.
     *
     * @return Timeout value for this operation.
     */
    @Nonnull Timeout timeout();

    /**
     * Optional custom call context to apply to this operation; custom call contexts can apply things like credentials
     * or timeouts on a per-RPC basis.
     *
     * @return Call context assigned to this operation, if any.
     */
    @Nonnull Optional<ApiCallContext> context();
}
