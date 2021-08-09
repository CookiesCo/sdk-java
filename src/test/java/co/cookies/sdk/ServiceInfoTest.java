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


import co.cookies.sdk.catalog.v1.CatalogClientV1;
import co.cookies.sdk.services.BaseServiceInfo;
import co.cookies.sdk.services.ServiceInfo;
import org.junit.jupiter.api.Test;

import javax.annotation.Nonnull;

import static org.junit.jupiter.api.Assertions.*;


/** Tests for static service info descriptors. */
public class ServiceInfoTest {
    @Test void testServiceInfoCompare() {
        var left = CatalogClientV1.INFO;
        var right = CatalogClientV1.INFO;
        assertEquals(left, right, "equivalent service info should be equivalent");
        var foreign = new BaseServiceInfo() {
            @Override
            public @Nonnull String serviceName() {
                return "sample";
            }

            @Override
            public @Nonnull String serviceVersion() {
                return "v1";
            }
        };
        assertFalse(left.equals(foreign), "non-equivalent service info should not report equal");
        assertEquals(left.hashCode(), right.hashCode(), "service info hash code should be predictable");
        assertNotEquals(right.hashCode(), foreign.hashCode(), "service info hash code should not bleed");
        var synth = new BaseServiceInfo() {
            @Override
            public @Nonnull String serviceName() {
                return "sample";
            }

            @Override
            public @Nonnull String serviceVersion() {
                return "v1";
            }
        };
        assertEquals(synth.hashCode(), foreign.hashCode(), "service info hash code should be predictable");
        assertEquals(0, synth.compareTo(foreign), "service info sort order should work correctly");
    }

    @Test void testServiceInfoOrderable() {
        var left = CatalogClientV1.INFO;
        var right = CatalogClientV1.INFO;
        assertEquals(0, left.compareTo(right), "comparing two equal service infos should equal 0");
    }

    @Test void testServiceInfoDefaults() {
        var defaultInfo = new ServiceInfo() {
            @Nonnull
            @Override
            public String serviceName() {
                return "sample";
            }

            @Nonnull
            @Override
            public String serviceVersion() {
                return "v1";
            }
        };

        assertEquals(
            "sample",
            defaultInfo.serviceName(),
            "service name should report correctly"
        );
        assertEquals(
            "v1",
            defaultInfo.serviceVersion(),
            "service name should report correctly"
        );
        assertEquals(
            "sample:v1",
            defaultInfo.serviceTag(),
            "service tag should report correctly"
        );
        assertTrue(
            defaultInfo.apiKeyRequired(),
            "API keys should be required by default"
        );
        assertFalse(
            defaultInfo.authorizationRequired(),
            "auth keys should not be required by default"
        );
    }
}
