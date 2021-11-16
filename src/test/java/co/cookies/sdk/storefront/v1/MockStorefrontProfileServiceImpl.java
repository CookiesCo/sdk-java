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
import com.google.protobuf.Empty;
import cookies.schema.store.*;
import cookies.schema.store.model.StoreUser;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;


/** Mock implementation of the Storefront Profile API for local testing. */
public final class MockStorefrontProfileServiceImpl extends ProfileV1Grpc.ProfileV1ImplBase {
    private MockStorefrontProfileServiceImpl() { /* Please use static factory. */ }

    /** @return Mock instance of the Storefront Profile service for testing, based on static responses. */
    public static MockStorefrontProfileServiceImpl acquire() {
        return new MockStorefrontProfileServiceImpl();
    }

    @Override
    public void profileUsernameCheck(UsernameCheckRequest request, StreamObserver<Empty> responseObserver) {
        if (request.getUsername().startsWith("failure")) {
            // simulate failure
            var user = request.getUsername();
            if (user.contains("taken")) {
                // simulate the username being taken
                responseObserver.onError(Status.ALREADY_EXISTS.asRuntimeException());
            } else if (user.contains("policy")) {
                // simulate the username being blocked for policy reasons
                responseObserver.onError(Status.INVALID_ARGUMENT.asRuntimeException());
            } else if (user.contains("ineligible")) {
                // simulate the username being blocked because the account is ineligible
                responseObserver.onError(Status.FAILED_PRECONDITION.asRuntimeException());
            } else {
                // simulate a standard failure
                responseObserver.onError(Status.UNIMPLEMENTED.asRuntimeException());
            }
        } else {
            // simulate success
            responseObserver.onNext(Empty.newBuilder().build());
            responseObserver.onCompleted();
        }
    }

    @Override
    public void profile(ProfileRequest request, StreamObserver<ProfileResponse> responseObserver) {
        responseObserver.onNext(ProtoLoader.loadTextFile(
            ProfileResponse.newBuilder(),
            "/store_profile_fetch.prototxt"
        ));
        responseObserver.onCompleted();
    }

    @Override
    public void profileUpdate(ProfileUpdateRequest request, StreamObserver<StoreUser> responseObserver) {
        responseObserver.onNext(ProtoLoader.loadTextFile(
            StoreUser.newBuilder(),
            "/store_profile_update.prototxt"
        ));
        responseObserver.onCompleted();
    }
}
