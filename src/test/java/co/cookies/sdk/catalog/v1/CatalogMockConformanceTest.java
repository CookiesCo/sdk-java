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
package co.cookies.sdk.catalog.v1;

import co.cookies.sdk.ProtoLoader;
import co.cookies.sdk.catalog.CatalogClient;
import cookies.schema.catalog.*;
import io.grpc.Status;
import org.junit.jupiter.api.Test;

import static co.cookies.sdk.ServiceTestUtil.acquireFirstResponse;
import static co.cookies.sdk.catalog.v1.ErrorInjectingCatalogServiceImpl.injectThrowable;
import static com.google.common.truth.extensions.proto.ProtoTruth.assertThat;
import static org.junit.jupiter.api.Assertions.*;


/** Make sure that the local mock service returns what we expect it to. This is a smoke test that should never fail. */
public final class CatalogMockConformanceTest {
    private CatalogV1Grpc.CatalogV1ImplBase acquireService() {
        return MockCatalogServiceImpl.acquire();
    }

    @Test void testBrandsConformance() {
        assertThat(acquireFirstResponse(
            BrandsRequest.newBuilder().build(),
            acquireService()::brands
        )).ignoringRepeatedFieldOrder().isEqualTo(ProtoLoader.loadTextFile(
            BrandsResponse.newBuilder(),
            "/brands_full.prototxt"
        ));
    }

    @Test void testStrainsConformance() {
        assertThat(acquireFirstResponse(
            StrainsRequest.newBuilder().build(),
            acquireService()::strains
        )).ignoringRepeatedFieldOrder().isEqualTo(ProtoLoader.loadTextFile(
            StrainsResponse.newBuilder(),
            "/strains_full.prototxt"
        ));
    }

    @Test void testProductFetchConformance() {
        assertThat(acquireFirstResponse(
            ProductRequest.newBuilder().build(),
            acquireService()::product
        )).ignoringRepeatedFieldOrder().isEqualTo(ProtoLoader.loadTextFile(
            CatalogProduct.newBuilder(),
            "/product.prototxt"
        ));
    }

    @Test void testErrorPropagation() {
        var svc = (
            injectThrowable(Status.UNIMPLEMENTED.asRuntimeException())
        );
        assertNotNull(svc, "should be able to acquire error-injected mock service");
        assertThrows(RuntimeException.class, () -> {
            acquireFirstResponse(
                BrandsRequest.newBuilder().build(),
                svc::brands
            );
        });
    }
}
