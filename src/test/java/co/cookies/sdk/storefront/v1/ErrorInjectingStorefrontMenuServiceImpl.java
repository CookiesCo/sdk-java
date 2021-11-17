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

import cookies.schema.store.*;
import cookies.schema.store.model.MenuSearchFacets;
import cookies.schema.store.model.ProductsList;
import io.grpc.stub.StreamObserver;

import java.time.Duration;
import java.util.Optional;


/**
 * Storefront Menu API client wrapper which holds a mock service implementation that injects a given error for every
 * method call. Meant for testing error handling on the client side.
 */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public final class ErrorInjectingStorefrontMenuServiceImpl {
    private final Throwable injected;
    private final Optional<Duration> delay;

    private ErrorInjectingStorefrontMenuServiceImpl(Throwable injected, Optional<Duration> delay) {
        this.injected = injected;
        this.delay = delay;
    }

    public static WrappedMenuServiceImpl injectThrowable(Throwable error) {
        return new ErrorInjectingStorefrontMenuServiceImpl(error, Optional.empty()).getService();
    }

    public static WrappedMenuServiceImpl injectThrowable(Throwable error, Duration delay) {
        return new ErrorInjectingStorefrontMenuServiceImpl(error, Optional.of(delay)).getService();
    }

    private void injectError(StreamObserver<?> observer) {
        if (delay.isPresent()) {
            try {
                Thread.sleep(delay.get().toMillis());
            } catch (InterruptedException ixe) {
                // no-op
            }
        }
        observer.onError(injected);
    }

    // Get an injected service instance.
    public WrappedMenuServiceImpl getService() {
        return new WrappedMenuServiceImpl();
    }

    // Wrapped service implementation.
    public final class WrappedMenuServiceImpl extends MenuV1Grpc.MenuV1ImplBase {
        @Override
        public void products(ProductsRequest request, StreamObserver<ProductsList> responseObserver) {
            injectError(responseObserver);
        }

        @Override
        public void menu(MenuRequest request, StreamObserver<MenuResponse> responseObserver) {
            injectError(responseObserver);
        }

        @Override
        public void menuFacets(MenuFacetsRequest request, StreamObserver<MenuSearchFacets> responseObserver) {
            injectError(responseObserver);
        }

        @Override
        public void menuSearch(MenuSearchRequest request, StreamObserver<MenuSearchResponse> responseObserver) {
            injectError(responseObserver);
        }
    }
}
