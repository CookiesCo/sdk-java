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

import com.google.protobuf.Empty;
import cookies.schema.store.*;
import cookies.schema.store.model.MenuSearchFacets;
import cookies.schema.store.model.ProductsList;
import cookies.schema.store.model.StoreUser;
import io.grpc.stub.StreamObserver;

import java.time.Duration;
import java.util.Optional;


/**
 * Storefront Profile API client wrapper which holds a mock service implementation that injects a given error for every
 * method call. Meant for testing error handling on the client side.
 */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public final class ErrorInjectingStorefrontProfileServiceImpl {
    private final Throwable injected;
    private final Optional<Duration> delay;

    private ErrorInjectingStorefrontProfileServiceImpl(Throwable injected, Optional<Duration> delay) {
        this.injected = injected;
        this.delay = delay;
    }

    public static WrappedProfileServiceImpl injectThrowable(Throwable error) {
        return new ErrorInjectingStorefrontProfileServiceImpl(error, Optional.empty()).getService();
    }

    public static WrappedProfileServiceImpl injectThrowable(Throwable error, Duration delay) {
        return new ErrorInjectingStorefrontProfileServiceImpl(error, Optional.of(delay)).getService();
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
    public WrappedProfileServiceImpl getService() {
        return new WrappedProfileServiceImpl();
    }

    // Wrapped service implementation.
    public final class WrappedProfileServiceImpl extends ProfileV1Grpc.ProfileV1ImplBase {
        @Override
        public void profileUsernameCheck(UsernameCheckRequest request, StreamObserver<Empty> responseObserver) {
            injectError(responseObserver);
        }

        @Override
        public void profile(ProfileRequest request, StreamObserver<ProfileResponse> responseObserver) {
            injectError(responseObserver);
        }

        @Override
        public void profileUpdate(ProfileUpdateRequest request, StreamObserver<StoreUser> responseObserver) {
            injectError(responseObserver);
        }

        @Override
        public void profileAvatarUpload(AvatarUploadRequest request,
                                        StreamObserver<AvatarUploadResponse> responseObserver) {
            injectError(responseObserver);
        }

        @Override
        public void profileAvatarFinalize(AvatarUploadFinalize request, StreamObserver<Empty> responseObserver) {
            injectError(responseObserver);
        }

        @Override
        public void profileAvatarClear(UsernameCheckRequest request, StreamObserver<Empty> responseObserver) {
            injectError(responseObserver);
        }
    }
}
