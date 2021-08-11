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

import co.cookies.sdk.exceptions.CookiesRPCException;
import co.cookies.sdk.exceptions.CookiesSDKException;
import co.cookies.sdk.exceptions.RPCExecutionException;
import co.cookies.sdk.exceptions.RPCTimeoutException;
import co.cookies.sdk.services.AsyncRPC;
import co.cookies.sdk.services.SyncRPC;
import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutureToListenableFuture;
import com.google.api.gax.rpc.ServerStream;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import com.google.protobuf.Message;
import io.grpc.MethodDescriptor;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.google.common.util.concurrent.Futures.*;
import static java.lang.String.format;


/** Provides static utilities used internally by the Cookies SDK. */
@SuppressWarnings("UnstableApiUsage")
@ThreadSafe
public final class SDKUtil {
    private SDKUtil() { /* Disallow construction. */ }

    /**
     * "Protect" the supplied method by wrapping checked exceptions with {@link RuntimeException}.
     *
     * @param callable Callable to protect from checked exceptions.
     * @param <T> Return type of the callable.
     * @return Return value from the callable.
     */
    public static <T> T protect(@Nonnull ThrowingCallable<T, IOException> callable) {
        try {
            return callable.call();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * "Protect" the supplied method by wrapping checked exceptions with a user-supplied {@link RuntimeException}.
     *
     * @param rethrow Function which re-throws the supplied throwable with the appropriate custom exception. The
     *                re-thrower is only expected to construct the exception, not actually throw it.
     * @param callable Callable which should be wrapped in checked exception protection.
     * @param <T> Return type from the inner callable.
     * @param <E> Checked exception type we are intending to wrap.
     * @return Return value from the callable.
     * @throws RuntimeException If {@link E} is thrown by the inner callable.
     */
    public static <T, E extends Throwable> T protect(@Nonnull Function<Throwable, RuntimeException> rethrow,
                                                     @Nonnull ThrowingCallable<T, E> callable) {
        try {
            return callable.call();
        } catch (Throwable e) {
            throw rethrow.apply(e);
        }
    }

    /**
     * Synchronously execute the provided dispatch-able function, converting the wrapped RPC to an async operation as we
     * go, and then, after waiting for a maximum of the operation's timeout, returning directly any resulting value
     * (expected to be of type {@link R}), and throwing directly any exception that might arise.
     *
     * <p>Should any exception arise during processing of the operation, it will propagate out to invoking code and be
     * raised directly.</p>
     *
     * @param operation Operation which we need to execute synchronously.
     * @param logger Service logger, in case things go haywire. Receives all exceptions.
     * @param descriptor gRPC descriptor for the method we are executing.
     * @param method Callable API method, but in async form.
     * @param <R> Return type expected from the operation.
     * @return Return value from the operation, after waiting up to the timeout period.
     * @throws CookiesSDKException If any error is thrown, with the inner error assigned as the cause.
     */
    public static <R extends Message, T> T block(@Nonnull SyncRPC<R> operation,
                                                 @Nonnull Logger logger,
                                                 @Nonnull MethodDescriptor<R, ? extends Message> descriptor,
                                                 @Nonnull Function<AsyncRPC<R>, ListenableFuture<T>> method)
            throws CookiesSDKException {
        // resolve universal RPC timeout
        var timeout = Objects.requireNonNull(operation.timeout(), "cannot provide `null` timeout");
        ListenableFuture<T> future = null;

        try {
            future = method.apply(operation.unwrap());
            return future.get(timeout.value(), timeout.unit());

        } catch (TimeoutException txe) {
            // re-throw as timeout exception
            throw runtimeErr(
                logger,
                RPCExecutionException::new,
                txe,
                "Timeout (%s %s) error while processing RPC operation for method '%s'.",
                timeout.value(),
                timeout.unit().name(),
                descriptor.getFullMethodName()
            );

        } catch (InterruptedException interrupt) {
            // first, cancel the future if it is still running.
            var didCancel = false;
            if (!future.isCancelled()) {
                didCancel = future.cancel(true);
            }

            // re-throw as interrupt exception
            throw runtimeErr(
                logger,
                RPCExecutionException::new,
                interrupt,
                "Interrupted while processing RPC operation for method '%s'. %s",
                descriptor.getFullMethodName(),
                didCancel
                    ? "As a result, the operation was cancelled."
                    : "The underlying operation reported as cancelled."
            );

        } catch (ExecutionException exe) {
            // re-throw as interrupt exception
            throw runtimeErr(
                logger,
                RPCExecutionException::new,
                exe,
                "Execution failed while processing RPC operation for method '%s'.",
                descriptor.getFullMethodName()
            );
        }
    }

    /**
     * Handle a runtime error that surfaced while handling a background-executing RPC operation, converting it into an
     * appropriate SDK exception and emitting logs as we go.
     *
     * <p>This method returns the exception intentionally, so that we can raise it in the original call-site for more
     * meaningful tracebacks.</p>
     *
     * @param logger Logger where we should complain loudly about this error.
     * @param constructor Constructor for the concrete exception we should create and raise.
     * @param cause Error that caused this halt.
     * @param errorMessage Message for this error.
     * @param formatArgs Format arguments to apply to the error message.
     * @param <E> Expected exception type.
     * @param <T> Type of error intended to be raised.
     * @return Returns the exception prepared by this method, so that it may be raised at the call-site.
     */
    static @Nonnull <E extends Throwable, T extends CookiesRPCException> T runtimeErr(
            @Nonnull Logger logger,
            @Nonnull BiFunction<String, E, T> constructor,
            @Nonnull E cause,
            @Nonnull String errorMessage,
            @Nonnull Object... formatArgs) throws CookiesRPCException {
        // format the error message, and create the exception, enclosing the cause as we go
        var err = constructor.apply(format(
            errorMessage,
            formatArgs
        ), cause);

        // output to the logs and then return it to be thrown
        logger.error(err.getMessage(), err);
        return err;
    }

    /**
     * Wrap a future value represented by an API future object in a regular Guava {@link ListenableFuture} which holds
     * a value of the same type.
     *
     * @param apiFuture API future which we should adapt into a regular listenable future.
     * @param <R> Wrapped future value intended to be produced by the operation.
     * @return Listenable future, adapted to forward from the provided API future.
     */
    public static <R> ListenableFuture<R> wrap(@Nonnull ApiFuture<R> apiFuture) {
        return new ApiFutureToListenableFuture<>(apiFuture);
    }

    /**
     * Provided an RPC operation which is expected to produce a server-side stream of responses, wire up a transformer
     * which can convert each response into the final return value, and then setup a stream of such final responses.
     *
     * <p>Errors propagate via {@link CookiesRPCException} descendents, in particular {@link RPCExecutionException}. Any
     * timeout specified on the RPC, default or otherwise, is enforced, resulting in a {@link RPCTimeoutException} if
     * violated.</p>
     *
     * <p>The provided transformer is expected to produce a stream as a result, which is provided to invoking code in a
     * {@link ListenableFuture}. The future will not execute the streaming operation until a response is listener is
     * attached.</p>
     *
     * @param logger Main error logger to report runtime exceptions to.
     * @param operation RPC operation, with a request, timeout, and any call context that should be applied.
     * @param stream Stream of responses from the server, issued as a result of our async request.
     * @param descriptor gRPC method descriptor -- mostly used for error logging.
     * @param transformer Method transformer, which turns a response into a final facade result.
     * @param executor Background executor where we should prime and return the stream. Returns when the first item is
     *                 yielded from the response.
     * @param <R> Streaming {@link Message} response type expected from the server.
     * @param <T> Final method result type (usually also a {@link Message}), of which a {@link Stream} is produced by
     *            the provided transformer.
     * @return Future which resolves to a {@link Stream} of type {@link T}.
     */
    public static <R extends Message, T> ListenableFuture<Stream<T>> applyStream(
            @Nonnull Logger logger,
            @Nonnull AsyncRPC<? extends Message> operation,
            @Nonnull ServerStream<R> stream,
            @Nonnull MethodDescriptor<? extends Message, R> descriptor,
            @Nonnull Function<R, Stream<T>> transformer,
            @Nonnull ListeningScheduledExecutorService executor) {
        return withTimeout(executor.submit(() -> {
            try {
                // firstly, we convert the response stream into a Java iterator. iterators are naturally lazy, so call
                // execution will block once we try to produce an item.
                var iter = stream.iterator();

                // begin waiting for an item from the server. this fetches only the first response.
                if (iter.hasNext()) {
                    // take each item, which should be a fully-formed streaming response stanza. pass it to the
                    // transformer, which is responsible for converting it to a stream of `T`.
                    var item = iter.next();

                    // convert to a stream via the provided transformer. response streams produced by the server are
                    // joined into a single stream.
                    return Stream.iterate(
                        item,
                        (entry) -> iter.hasNext(),
                        (entry) -> iter.next()
                    ).flatMap(transformer);
                }

                // if we never have a next item, then it's an empty result stream.
                return Stream.empty();
            } catch (RuntimeException rxe) {
                throw runtimeErr(
                    logger,
                    RPCExecutionException::new,
                    rxe,
                    "Background error occurred while processing RPC stream '%s'.",
                    descriptor.getFullMethodName()
                );
            }
        }), operation.timeout().value(), operation.timeout().unit(), executor);
    }

    /**
     * Wrap a future value represented by an API future object in a regular Guava {@link ListenableFuture}, and, should
     * the operation succeed and produce a response of type {@link R}, pass it through a transformer function which
     * converts the response value to type {@link T}, the final return value of the method.
     *
     * @param logger Service logger, in case anything goes wrong.
     * @param operation RPC operation which we are fulfilling.
     * @param future API operation which intends to produce a result of type {@link R}.
     * @param transformer Transformer function which is capable of transforming between types {@link R} and {@link T},
     *                    the final return type for the function.
     * @param executor Background executor to use when running the transform method.
     * @param <R> Return type from the API operation.
     * @param <T> Return type from the API facade.
     * @return Listenable future which wraps and transforms the provided future.
     */
    public static <R extends Message, T> ListenableFuture<T> applyAsync(
            @Nonnull Logger logger,
            @Nonnull AsyncRPC<? extends Message> operation,
            @Nonnull ApiFuture<R> future,
            @Nonnull MethodDescriptor<? extends Message, R> descriptor,
            @Nonnull Function<R, T> transformer,
            @Nonnull ListeningScheduledExecutorService executor) {
        // note, these comments actually *execute* in reverse from how they are expressed here, because this code path
        // is planned out in non-blocking form and lazily dispatched once a value is ready. to begin with the conclusion
        // below: after the method executes, transform it into the expected return type.
        return transform(
            // apply the timeout value configured for the async operation, whether an explicit timeout was provided or
            // the SDK picked a sensible default.
            withTimeout(
                // if an error happens during processing of the RPC itself, wrap it in an expected SDK error type, and
                // make sure we log about it.
                catching(wrap(future), RuntimeException.class, (rxe) -> {
                    throw runtimeErr(
                        logger,
                        RPCExecutionException::new,
                        rxe,
                        "Background error occurred while processing method RPC '%s'.",
                        descriptor.getFullMethodName()
                    );
                }, executor),
                operation.timeout().value(),
                operation.timeout().unit(),
                executor
            ),
            transformer::apply,
            executor
        );
    }
}
