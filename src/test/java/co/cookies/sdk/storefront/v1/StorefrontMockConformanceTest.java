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
package co.cookies.sdk.storefront.v1;


import co.cookies.sdk.ProtoLoader;
import cookies.schema.store.MenuRequest;
import cookies.schema.store.MenuResponse;
import cookies.schema.store.MenuV1Grpc;
import org.junit.jupiter.api.Test;

import static co.cookies.sdk.ServiceTestUtil.acquireFirstResponse;
import static com.google.common.truth.extensions.proto.ProtoTruth.assertThat;
import static org.junit.jupiter.api.Assertions.*;


/** Make sure that Storefront services behave as expected. This smoke test should never fail. */
public final class StorefrontMockConformanceTest {
    private MenuV1Grpc.MenuV1ImplBase acquireMenuService() {
        return MockStorefrontMenuServiceImpl.acquire();
    }

    @Test void testAcquireMenuService() {
        assertNotNull(
            acquireMenuService(),
            "should be able to acquire a mock menu service implementation"
        );
    }

    @Test void testMenuGenericConformance() {
        assertThat(acquireFirstResponse(
            MenuRequest.newBuilder().build(),
            acquireMenuService()::menu
        )).ignoringRepeatedFieldOrder().isEqualTo(ProtoLoader.loadTextFile(
            MenuResponse.newBuilder(),
            "/store_menu_default.prototxt"
        ));
    }
}
