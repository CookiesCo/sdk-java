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
import cookies.schema.catalog.*;
import io.grpc.stub.StreamObserver;


/** Mock implementation of the Catalog API for local testing. */
public final class MockCatalogServiceImpl extends CatalogV1Grpc.CatalogV1ImplBase {
    private MockCatalogServiceImpl() { /* Private construction only. */ }

    /** @return Mock instance of the Catalog Service for testing, based on static responses. */
    public static MockCatalogServiceImpl acquire() {
        return new MockCatalogServiceImpl();
    }

    @Override
    public void brands(BrandsRequest request, StreamObserver<BrandsResponse> responseObserver) {
        if (request.getOptions().getInclusionMode() == CatalogQueryOptions.ContentInclusionMode.KEYS_ONLY) {
            responseObserver.onNext(ProtoLoader.loadTextFile(
                BrandsResponse.newBuilder(),
                "/brands_keysonly.prototxt"
            ));
        } else {
            responseObserver.onNext(ProtoLoader.loadTextFile(
                BrandsResponse.newBuilder(),
                "/brands_full.prototxt"
            ));
        }
        responseObserver.onCompleted();
    }

    @Override
    public void strains(StrainsRequest request, StreamObserver<StrainsResponse> responseObserver) {
        if (request.getOptions().getInclusionMode() == CatalogQueryOptions.ContentInclusionMode.KEYS_ONLY) {
            responseObserver.onNext(ProtoLoader.loadTextFile(
                StrainsResponse.newBuilder(),
                "/strains_keysonly.prototxt"
            ));
        } else {
            responseObserver.onNext(ProtoLoader.loadTextFile(
                StrainsResponse.newBuilder(),
                "/strains_full.prototxt"
            ));
        }
        responseObserver.onCompleted();
    }

    @Override
    public void product(ProductRequest request, StreamObserver<CatalogProduct> responseObserver) {
        responseObserver.onNext(ProtoLoader.loadTextFile(
            CatalogProduct.newBuilder(),
            "/product.prototxt"
        ));
        responseObserver.onCompleted();
    }

    @Override
    public void retrieve(CatalogRequest request, StreamObserver<CatalogResponse> responseObserver) {
        throw new IllegalStateException("deprecated");
    }

    @Override
    public void sync(MultiProductRequest request, StreamObserver<CatalogProductSet> responseObserver) {
        responseObserver.onNext(ProtoLoader.loadTextFile(
            CatalogProductSet.newBuilder(),
            "/sync_stanza1.prototxt"
        ));
        responseObserver.onNext(ProtoLoader.loadTextFile(
            CatalogProductSet.newBuilder(),
            "/sync_stanza2.prototxt"
        ));
        responseObserver.onCompleted();
    }
}
