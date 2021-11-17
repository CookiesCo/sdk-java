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
import cookies.schema.store.*;
import io.grpc.stub.StreamObserver;


/** Mock implementation of the Storefront Menu API for local testing. */
public final class MockStorefrontMenuServiceImpl extends MenuV1Grpc.MenuV1ImplBase {
    private MockStorefrontMenuServiceImpl() { /* Please use static factory. */ }

    /** @return Mock instance of the Storefront Menu service for testing, based on static responses. */
    public static MockStorefrontMenuServiceImpl acquire() {
        return new MockStorefrontMenuServiceImpl();
    }

    @Override
    public void menu(MenuRequest request, StreamObserver<MenuResponse> responseObserver) {
        responseObserver.onNext(ProtoLoader.loadTextFile(
            MenuResponse.newBuilder(),
            request.getKeysOnly() ? "/store_menu_keysonly.prototxt" : "/store_menu_default.prototxt"
        ));
        responseObserver.onCompleted();
    }

    @Override
    public void menuSearch(MenuSearchRequest request, StreamObserver<MenuSearchResponse> responseObserver) {
        responseObserver.onNext(ProtoLoader.loadTextFile(
            MenuSearchResponse.newBuilder(),
            "/store_menu_search.prototxt"
        ));
        responseObserver.onCompleted();
    }
}
