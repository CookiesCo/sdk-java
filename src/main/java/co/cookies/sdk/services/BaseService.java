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
package co.cookies.sdk.services;

import com.google.api.core.ApiFuture;
import com.google.api.gax.core.BackgroundResource;
import com.google.api.gax.rpc.ServerStream;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import com.google.protobuf.Message;
import io.grpc.MethodDescriptor;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Stream;

import static co.cookies.sdk.SDKUtil.applyAsync;
import static co.cookies.sdk.SDKUtil.applyStream;
import static java.lang.String.format;


/**
 * Defines a base class from which all Cookies service clients in Java inherit, with shared / basic functionality like
 * logging, debugging, and interface information access.
 *
 * @param <Stub> Service stub implemented by this service.
 */
@Immutable @ThreadSafe
public abstract class BaseService<Stub extends BackgroundResource> implements ServiceClient<Stub> {
    /** Information about this service implementation. */
    private final @Nonnull ServiceInfo serviceInfo;

    /** Holds the stub which implements the service. */
    private final @Nonnull Stub serviceStub;

    /** Holds a central logging pipe prepared for this service. */
    protected final @Nonnull Logger logging;

    /**
     * Child implementation construction entrypoint.
     *
     * @param serviceInfo Service info provided by the child implementation.
     * @param serviceStub Stub implementing the service.
     * @param loggerFactory Logger factory which we should use to acquire a logger.
     */
    protected BaseService(@Nonnull ServiceInfo serviceInfo,
                          @Nonnull Stub serviceStub,
                          @Nonnull ILoggerFactory loggerFactory) {
        this.serviceInfo = serviceInfo;
        this.serviceStub = serviceStub;
        this.logging = loggerFactory.getLogger(format(
            "co.cookies.services:%s:%s",
            serviceInfo.serviceName(),
            serviceInfo.serviceVersion()
        ));
    }

    /**
     * Return this service's initialized executor service, which typically just proxies to the executor provider mounted
     * on the client facade.
     *
     * @return Executor service.
     */
    public abstract @Nonnull ListeningScheduledExecutorService executorService();

    /**
     * Close this service down for any further interaction, by gracefully finishing any in-flight RPCs, shutting down
     * the stub's channel, and then shutting down the associated executor.
     */
    @Override
    public void close() {
        var svc = service();
        try {
            svc.shutdown();
            int totalWait = 0;
            if (!svc.awaitTermination(50, TimeUnit.MILLISECONDS)) {
                totalWait += 50;
                svc.shutdownNow();
                while (!svc.isTerminated()) {
                    svc.awaitTermination(25, TimeUnit.MILLISECONDS);
                    totalWait += 25;
                    if (svc.isShutdown() || totalWait > 150) {
                        throw new InterruptedException();  // bail due to timeout or termination
                    }
                }
            }
        } catch (InterruptedException ixe) {
            // we don't care
            svc.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /** @inheritDoc */
    @Override public @Nonnull Stub service() {
        return this.serviceStub;
    }

    /** @inheritDoc */
    @Override public @Nonnull Logger logger() {
        return this.logging;
    }

    /** @inheritDoc */
    @Override public @Nonnull ServiceInfo getServiceInfo() {
        return this.serviceInfo;
    }

    /** @inheritDoc */
    @Override public @Nonnull String getServiceName() {
        return this.serviceInfo.serviceName();
    }

    /** @inheritDoc */
    @Override public @Nonnull String getServiceVersion() {
        return this.serviceInfo.serviceVersion();
    }

    /**
     * Execute the provided async server-streaming RPC operation, and if one or more responses should be yielded, use
     * the provided transformer to convert each into final return values, and then flat-concatenate the resulting stream
     * and return to invoking code wrapped in a future value.
     *
     * @param rpc RPC request which we need to stream and transform.
     * @param descriptor gRPC method descriptor, mostly used for error logging.
     * @param method API method we wish to run to fulfill this request.
     * @param transformer Transformer function which knows how to stream-ify each response.
     * @param <Request> RPC request type (a {@link Message}) provided to the server to produce the stream.
     * @param <Response> Streaming RPC response type (a {@link Message}) expected to be yielded by the server.
     * @param <T> Final return type expected from the corresponding method facade.
     * @return Listenable future wrapping a {@link Stream} of transformed facade types {@link T}.
     */
    protected final @Nonnull <Request extends Message, Response extends Message, T> ListenableFuture<Stream<T>> stream(
            @Nonnull AsyncRPC<Request> rpc,
            @Nonnull MethodDescriptor<Request, Response> descriptor,
            @Nonnull Function<Request, ServerStream<Response>> method,
            @Nonnull Function<Response, Stream<T>> transformer) {
        return applyStream(
            logger(),
            rpc,
            method.apply(rpc.request()),
            descriptor,
            transformer,
            executorService()
        );
    }

    /**
     * Execute the provided asynchronous RPC operation, and if any response should be provided, return it directly
     * in a future value container upon which conclusion listeners may be affixed.
     *
     * <p>If a response needs to be transformed before being handed back to invoking code, see method variants of this
     * same name that accept a transformer argument.</p>
     *
     * @see #execute(AsyncRPC, MethodDescriptor, Function, Function) For the ability to transform the response.
     * @param rpc RPC request which we need to execute and transform.
     * @param method API method we wish to run to fulfill this request.
     * @param <Request> Request message type for this operation.
     * @param <Response> Response message type for this operation.
     * @return Future which wraps the operation to execute the RPC and transform it.
     */
    protected final @Nonnull <Request extends Message, Response extends Message> ListenableFuture<Response> execute(
        @Nonnull AsyncRPC<Request> rpc,
        @Nonnull MethodDescriptor<Request, Response> descriptor,
        @Nonnull Function<Request, ApiFuture<Response>> method) {
        return execute(
            rpc,
            descriptor,
            method,
            (response) -> response
        );
    }

    /**
     * Execute the provided asynchronous RPC operation, and if any response should be provided, transform it via the
     * provided transformer function, returning the entire operation in a future value container upon which conclusion
     * listeners may be affixed.
     *
     * @param rpc RPC request which we need to execute and transform.
     * @param method API method we wish to run to fulfill this request.
     * @param transformer Function which knows how to transform the response into the final return result.
     * @param <Request> Request message type for this operation.
     * @param <Response> Response message type for this operation.
     * @param <T> Ultimate (final) return type from the corresponding method facade.
     * @return Future which wraps the operation to execute the RPC and transform it.
     */
    protected final @Nonnull <Request extends Message, Response extends Message, T> ListenableFuture<T> execute(
            @Nonnull AsyncRPC<Request> rpc,
            @Nonnull MethodDescriptor<Request, Response> descriptor,
            @Nonnull Function<Request, ApiFuture<Response>> method,
            @Nonnull Function<Response, T> transformer) {
        return applyAsync(
            logger(),
            rpc,
            method.apply(rpc.request()),
            descriptor,
            transformer,
            executorService()
        );
    }
}
