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
import java.util.function.BiFunction;


/**
 * Defines the interface for a callable two-parameter function which declares itself the thrower of some checked
 * exception.
 *
 * @param <I1> Input type 1.
 * @param <I2> Input type 2.
 * @param <R> Return value for the function.
 * @param <E> Checked exception.
 */
@FunctionalInterface
public interface ThrowingBiFunction<I1, I2, R, E extends Throwable> {
    /**
     * Invoke the function specified by this interface.
     *
     * @param inputOne First input value.
     * @param inputTwo Second input value.
     * @return Return value from the function's computation.
     * @throws E Throwable which may occur.
     */
    @Nonnull R accept(I1 inputOne, I2 inputTwo) throws E;

    /**
     * Wrap the specified callable in another callable which converts the provided checked exception type {@link E} to
     * an instance of {@link RuntimeException}.
     *
     * @param inner Inner callable function which may yield the checked error {@link E}, and which returns {@link R}.
     * @param <R> Return type for the inner function.
     * @param <E> Checked exception which may occur.
     * @param <I1> Input type 1.
     * @param <I2> Input type 2.
     * @return Wrapped callable.
     */
    static @Nonnull <I1, I2, R, E extends Throwable> BiFunction<I1, I2, R> unchecked(
            @Nonnull ThrowingBiFunction<I1, I2, R, E> inner) {
        return (one, two) -> {
            try {
                return inner.accept(one, two);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        };
    }
}
