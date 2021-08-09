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


import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;
import java.util.concurrent.TimeUnit;


/**
 * Small utility class which specifies timeout values for a given RPC operation.
 */
@Immutable @ThreadSafe
public class Timeout {
    /** Unit for the timeout. */
    private final @Nonnull TimeUnit unit;

    /** Value for the timeout. */
    private final long value;

    /**
     * Private constructor.
     *
     * @param unit Unit for the timeout.
     * @param value Value for the timeout.
     */
    private Timeout(@Nonnull TimeUnit unit, long value) {
        this.unit = unit;
        this.value = value;
    }

    /**
     * Create a timeout with the specified unit and value.
     *
     * @param value Value for the timeout.
     * @param unit Unit for the timeout.
     * @return Fabricated timeout specification.
     */
    public static @Nonnull Timeout of(long value, @Nonnull TimeUnit unit) {
        return new Timeout(unit, value);
    }

    // -- Getters -- //

    /** @return Unit specified for this timeout. */
    public @Nonnull TimeUnit unit() {
        return unit;
    }

    /** @return Value specified for this timeout. */
    public long value() {
        return value;
    }
}
