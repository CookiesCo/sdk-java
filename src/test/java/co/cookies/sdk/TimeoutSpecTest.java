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


import co.cookies.sdk.services.Timeout;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;


/** Basic tests for {@link Timeout}. */
public final class TimeoutSpecTest {
    @Test void testTimeoutObject() {
        var timeout = Timeout.of(5, TimeUnit.SECONDS);
        assertEquals(5, timeout.value(), "timeout value should be correct");
        assertEquals(TimeUnit.SECONDS, timeout.unit(), "timeout unit should be correct");
    }
}
