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
package co.cookies.sdk.storefront.v1.err;


import javax.annotation.Nonnull;

/**
 * Error specifically thrown when checking validity of a desired username, and the result of the check is that the user
 * is not, themselves, eligible to claim the username, until they complete activation of their account.
 */
public final class UsernameIneligibleError extends UsernameCheckError {
    // Private constructor.
    private UsernameIneligibleError() {
        super("USERNAME_INELIGIBLE");
    }

    /**
     * Create a well-formed `USERNAME_INVALID` error in response to a username validity check request.
     *
     * @return Username-taken-error.
     */
    public static @Nonnull UsernameIneligibleError create() {
        return new UsernameIneligibleError();
    }
}
