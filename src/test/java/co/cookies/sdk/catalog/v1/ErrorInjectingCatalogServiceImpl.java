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

import cookies.schema.catalog.*;
import io.grpc.stub.StreamObserver;

import java.time.Duration;
import java.util.Optional;


/**
 * Wrapper which holds a mock service implementation that injects a given error for every method call. Meant for testing
 * error handling on the client side.
 */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public final class ErrorInjectingCatalogServiceImpl {
    private final Throwable injected;
    private final Optional<Duration> delay;

    private ErrorInjectingCatalogServiceImpl(Throwable injected, Optional<Duration> delay) {
        this.injected = injected;
        this.delay = delay;
    }

    public static WrappedCatalogServiceImpl injectThrowable(Throwable error) {
        return new ErrorInjectingCatalogServiceImpl(error, Optional.empty()).getService();
    }

    public static WrappedCatalogServiceImpl injectThrowable(Throwable error, Duration delay) {
        return new ErrorInjectingCatalogServiceImpl(error, Optional.of(delay)).getService();
    }

    private void injectError(StreamObserver<?> observer) {
        if (delay.isPresent()) {
            try {
                // wait a delay, maybe
                Thread.sleep(delay.get().toMillis());
            } catch (InterruptedException ixe) {
                // no-op
            }
        }

        // inject the error
        observer.onError(injected);
    }

    // Get an injected service instance.
    public WrappedCatalogServiceImpl getService() {
        return new WrappedCatalogServiceImpl();
    }

    // Wrapped service implementation.
    public final class WrappedCatalogServiceImpl extends CatalogV1Grpc.CatalogV1ImplBase {
        @Override
        public void brands(BrandsRequest request, StreamObserver<BrandsResponse> responseObserver) {
            injectError(responseObserver);
        }

        @Override
        public void strains(StrainsRequest request, StreamObserver<StrainsResponse> responseObserver) {
            injectError(responseObserver);
        }

        @Override
        public void product(ProductRequest request, StreamObserver<CatalogProduct> responseObserver) {
            injectError(responseObserver);
        }

        @Override
        public void retrieve(CatalogRequest request, StreamObserver<CatalogResponse> responseObserver) {
            injectError(responseObserver);
        }

        @Override
        public void sync(MultiProductRequest request, StreamObserver<CatalogProductSet> responseObserver) {
            injectError(responseObserver);
        }
    }
}
