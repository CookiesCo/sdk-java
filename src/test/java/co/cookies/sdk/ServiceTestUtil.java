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

import co.cookies.sdk.exceptions.CookiesSDKException;
import com.google.api.gax.grpc.GrpcTransportChannel;
import com.google.api.gax.rpc.FixedTransportChannelProvider;
import com.google.api.gax.rpc.TransportChannelProvider;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.protobuf.Message;
import io.grpc.BindableService;
import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


/** Utilities for testing a service mock. */
public final class ServiceTestUtil {
    private ServiceTestUtil() { /* Disallow construction. */ }

    /**
     * Block and resolve the provided listenable future within 2 minutes.
     *
     * @param future Future to resolve.
     * @param <T> Return type of the future.
     * @return The result of the future.
     */
    public static <T> T resolve(ListenableFuture<T> future) {
        try {
            return future.get(2, TimeUnit.MINUTES);
        } catch (ExecutionException rxe) {
            if (rxe.getCause() instanceof CookiesSDKException) {
                throw (CookiesSDKException) rxe.getCause();
            } else {
                throw new RuntimeException(rxe);
            }
        } catch (InterruptedException | TimeoutException rxe) {
            throw new RuntimeException(rxe);
        }
    }

    /**
     * Set up a fully mocked client and hand it to the provided client-side test; use the provided service supplier to
     * create the service mock one-off just for this test. The resulting server can dispatch only in-memory to avoid
     * network stack issues.
     *
     * @param clientTest Client test to execute against the resulting mocked client.
     * @param serviceProvider Service provider for the service we intend to stand up.
     * @param stubFactory Factory method to produce a stub from the provided channel.
     * @param clientFactory Factory method which can produce a client facade from a stub.
     * @param <Service> Service implementation to factory and test.
     * @param <Stub> Stub implementation to use when creating our testing client.
     * @param <Client> Client facade type we intend to exercise with the provided client test.
     */
    public static <Service extends BindableService, Stub, Client> void setupMockedClient(
            Consumer<Client> clientTest,
            Supplier<Service> serviceProvider,
            ThrowingBiFunction<Server, TransportChannelProvider, Stub, IOException> stubFactory,
            Function<Stub, Client> clientFactory) {
        standup(
                serviceProvider.get(),
                (server, channel) -> {
                    var channelProvider = FixedTransportChannelProvider
                            .create(GrpcTransportChannel.create(channel));
                    return stubFactory.accept(server, channelProvider);
                }, (stub) -> {
                    // using the provided stub, factory ourselves a client instance, and hand it to the test.
                    clientTest.accept(clientFactory.apply(stub));
                }
        );
    }

    /**
     * Stand-up the provided gRPC service in an in-memory server, hook it up to an in-memory channel to match, supply
     * both to a function that wraps them into a stub, and then supply the stub to the provided continuance function.
     *
     * @param service Service which we wish to stand-up in-memory.
     * @param stubFactory Factory for producing stubs from servers/channels.
     * @param continuance Callable designated to receive the resulting server and channel.
     * @param <Service> Service we are standing up.
     * @param <Stub> Stub we intend to test with.
     */
    public static <Service extends BindableService, Stub> void standup(
            Service service,
            ThrowingBiFunction<Server, ManagedChannel, Stub, IOException> stubFactory,
            Consumer<Stub> continuance) {
        standupService(service, (server, channel) -> {
            // create the stub
            try {
                continuance.accept(stubFactory.accept(server, channel));
            } catch (IOException ioe) {
                throw new RuntimeException(ioe);
            }
        });
    }

    /**
     * Stand-up the provided gRPC service in an in-memory server, hook it up to an in-memory channel to match, and then
     * provide both to the supplied continuance function.
     *
     * @param service Service which we wish to stand-up in-memory.
     * @param continuance Callable designated to receive the resulting server and channel.
     * @param <Service> Service type we are creating.
     */
    public static <Service extends BindableService> void standupService(
            Service service,
            BiConsumer<Server, ManagedChannel> continuance) {

        Server server = null;
        try {
            String serverName = InProcessServerBuilder.generateName();
            server = InProcessServerBuilder
                    .forName(serverName)
                    .directExecutor()
                    .addService(service)
                    .build()
                    .start();

            var channel = InProcessChannelBuilder
                    .forName(serverName)
                    .directExecutor()
                    .build();

            continuance.accept(server, channel);

        } catch (Exception thr) {
            throw new RuntimeException(thr);
        } finally {
            if (server != null) {
                server.shutdown();
                try {
                    server.awaitTermination();
                } catch (InterruptedException ixe) {
                    // shutdown interrupt
                }
            }
        }

    }

    /**
     * Safely acquire the first (and presumably, only) response for a given unary service method.
     *
     * @param request Request to pass the RPC method.
     * @param method Method to execute to produce a response.
     * @param <Request> Request payload we are testing with.
     * @param <Response> Response payload we are testing with.
     * @return Response object, buffered and acquired.
     */
    public static <Request extends Message, Response extends Message> Response acquireFirstResponse(
            Request request,
            BiConsumer<Request, StreamObserver<Response>> method) {
        // setup a semaphore
        Semaphore semaphore = new Semaphore(0);
        AtomicBoolean completed = new AtomicBoolean(false);

        // setup a response observer
        var observer = new StreamObserver<Response>() {
            Response firstResponse = null;
            Throwable err = null;

            @Override
            public void onNext(Response response) {
                firstResponse = response;
            }

            @Override
            public void onError(Throwable throwable) {
                err = throwable;
                completed.compareAndExchange(false, true);
                semaphore.release();
            }

            @Override
            public void onCompleted() {
                completed.compareAndExchange(false, true);
                semaphore.release();
            }
        };

        // prepare to block and wait.
        try {
            // actually execute the method.
            method.accept(request, observer);

            // if we can't acquire in 2 minutes, it's definitely time to fail.
            if (!semaphore.tryAcquire(2, TimeUnit.MINUTES)) {
                throw new TimeoutException("Timeout while dispatching mock method.");
            }

            // throw any errors that occur.
            if (observer.err != null) {
                throw observer.err;
            }

            // method should properly complete, and we should never get a `null` response.
            assertTrue(completed.get(), "mock method should properly complete");
            assertNotNull(observer.firstResponse, "should not get `null` for method response");
            return observer.firstResponse;

        } catch (Throwable ixe) {
            throw new RuntimeException(ixe);
        }
    }
}
