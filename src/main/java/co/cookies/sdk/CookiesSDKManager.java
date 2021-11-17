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

import co.cookies.sdk.catalog.CatalogClient;
import co.cookies.sdk.catalog.v1.CatalogClientV1;
import co.cookies.sdk.services.BaseService;
import co.cookies.sdk.services.ServiceInfo;
import co.cookies.sdk.storefront.Storefront;
import co.cookies.sdk.storefront.v1.StorefrontClientV1;
import com.google.api.core.ApiFunction;
import com.google.api.gax.core.*;
import com.google.api.gax.grpc.InstantiatingGrpcChannelProvider;
import com.google.api.gax.rpc.HeaderProvider;
import com.google.api.gax.rpc.TransportChannelProvider;
import com.google.auth.Credentials;
import com.google.auto.value.AutoValue;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import io.grpc.ManagedChannelBuilder;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NegotiationType;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Duration;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;
import javax.net.ssl.SSLException;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.lang.String.format;


/**
 * Central manager for the Cookies SDK for Java; provides coordination via shared config and service support.
 *
 * <p>The central SDK manager should be a long-lived object (potentially a singleton in some applications), which is
 * used to acquire cached access to SDK service objects. Any services bound to a given {@link CookiesSDKManager} inherit
 * various components, such as:
 * <ul>
 *     <li><b>Execution:</b> All bound services share an executor, potentially customized.</li>
 *     <li><b>Credentials:</b> All bound services shared authorization materials, including API keys.</li>
 *     <li><b>Transport:</b> Cached access to transport services, including pooled channeling.</li>
 *     <li><b>Logging:</b> Services spawn SLF4J loggers at their client class name by default.</li>
 * </ul></p>
 *
 * <h2>How to acquire an SDK manager</h2>
 * <p>Developers should use the {@link CookiesSDK} facade class to acquire this manager, specifically by using the
 * builder methods on {@link CookiesSDKManager.Builder}. A new SDK builder object can be acquired with
 * {@link CookiesSDK#builder()}. For example:
 * <code>
 *     var builder = CookiesSDK.builder()
 *            .setApiKey("your-api-key-here")
 *            .build();
 *
 *     // or, for public service access which requires no configuration...
 *
 *     var sdk = CookiesSDK.builder().build();
 * </code>
 * </p>
 *
 * @see CookiesSDK#builder() Facade factory
 */
@AutoValue @Immutable @ThreadSafe
@SuppressWarnings({"OptionalUsedAsFieldOrParameterType", "rawtypes"})
public abstract class CookiesSDKManager
        extends ExtensibleCookiesSDK<SDKConfiguration>
        implements SDKConfiguration, SDKServiceProvider {
    private static final @Nonnull String SDK_USER_AGENT = "cookies-sdk-java-v1";
    private static final @Nonnull Logger logging = LoggerFactory.getLogger(CookiesSDKManager.class);
    private static volatile @Nonnull Optional<ListeningScheduledExecutorService> DEFAULT_EXECUTOR_SERVICE = (
            Optional.empty());

    /** Builder object for preparing an immutable {@link CookiesSDKManager} manager. */
    @AutoValue.Builder
    @SuppressWarnings({"UnusedReturnValue", "unused"})
    public abstract static class Builder {
        /**
         * Set the default executor service to use with Cookies API services.
         *
         * <p>If an executor service is not provided via this method, a default executor is booted up by the SDK and
         * shared across all services.</p>
         *
         * @param service Executor service to use.
         * @return Builder, for chainability.
         */
        public abstract Builder setExecutorService(Optional<ListeningScheduledExecutorService> service);

        /**
         * Return the executor service currently configured with this builder, if any.
         *
         * @return Executor service, or {@link Optional#empty()}.
         */
        public abstract Optional<ListeningScheduledExecutorService> getExecutorService();

        /**
         * Set a custom API endpoint.
         *
         * <p>This is expected to be in full URL prefix format, i.e. `example.cool.com:443`.</p>
         *
         * @param endpoint Custom RPC endpoint to use.
         * @return Builder, for chainability.
         */
        public abstract Builder setEndpoint(Optional<String> endpoint);

        /**
         * Return the configured endpoint to use for RPC traffic.
         *
         * @return Custom RPC endpoint, or {@link Optional#empty()}.
         */
        public abstract Optional<String> getEndpoint();

        /**
         * Set whether to enable private access.
         *
         * <p>Private access must be enabled when running in private cluster circumstances.<br /><b>Caution:</b> only
         * enable this if you know what you're doing.</p>
         *
         * @param privateAccess Whether to enable private access.
         * @return Builder, for chainability.
         */
        public abstract Builder setPrivateAccess(Optional<Boolean> privateAccess);

        /**
         * Return whether private API access is enabled.
         *
         * @return Private API access indication.
         */
        public abstract Optional<Boolean> getPrivateAccess();

        /**
         * Set a channel configurator, which is called each time a new managed channel is instantiated with a filled-out
         * builder; user-land code can opt to customize the builder before it is put to use, or return one just-in-time.
         *
         * @param fn Function to use when customizing the managed channel instantiation process. To clear the current
         *           function, if any, pass {@link Optional#empty()}.
         * @return Builder, for chainability.
         */
        public abstract Builder setChannelConfigurator(
                Optional<Function<ManagedChannelBuilder, ManagedChannelBuilder>> fn);

        /**
         * Retrieve the current channel configurator, if any, or {@link Optional#empty()} if none if currently installed
         * at the time this method is called.
         *
         * @return Current channel configurator, if any.
         */
        public abstract Optional<Function<ManagedChannelBuilder, ManagedChannelBuilder>> getChannelConfigurator();

        /**
         * Set a custom executor provider (overrides any set custom executor).
         *
         * @param executorProvider Executor provider to set.
         * @return Builder, for chainability.
         */
        public abstract Builder setExecutorProvider(Optional<ExecutorProvider> executorProvider);

        /**
         * Return the executor provider currently configured with this builder, if any.
         *
         * @return Executor provider, or {@link Optional#empty()}.
         */
        public abstract Optional<ExecutorProvider> getExecutorProvider();

        /**
         * Set the default channel provider to use for communication with the API.
         *
         * <p>If no explicit channel provider is set, a sensible default one will be used (over gRPC/TLS).</p>
         *
         * @param provider Channel provider to use.
         * @return Builder, for chainability.
         */
        public abstract Builder setTransportChannelProvider(Optional<TransportChannelProvider> provider);

        /**
         * Return the RPC transport provider currently configured with this builder, if any.
         *
         * @return Transport provider, or {@link Optional#empty()}.
         */
        public abstract Optional<TransportChannelProvider> getTransportChannelProvider();

        /**
         * Set the default credentials information to use for authorization when communicating with the API.
         *
         * <p>If no explicit credentials payload is set, a sensible default one will be used (usually empty).</p>
         *
         * @param credentials Credential set to use.
         * @return Builder, for chainability.
         */
        public abstract Builder setCredentials(Optional<Credentials> credentials);

        /**
         * Return the fixed set of credentials currently configured with this builder, if any.
         *
         * @return Fixed credentials, or {@link Optional#empty()}.
         */
        public abstract Optional<Credentials> getCredentials();

        /**
         * Set the default credentials provider to use for authorization when communicating with the API.
         *
         * <p>If no explicit credentials provider is set, a sensible default one will be used (usually empty).</p>
         *
         * @param provider Credentials provider to use.
         * @return Builder, for chainability.
         */
        public abstract Builder setCredentialsProvider(Optional<CredentialsProvider> provider);

        /**
         * Return the credentials provider currently configured with this builder, if any.
         *
         * @return Credentials provider, or {@link Optional#empty()}.
         */
        public abstract Optional<CredentialsProvider> getCredentialsProvider();

        /**
         * Set the default API key to use when communicating with Cookies API services.
         *
         * @param apiKey API key to use for all services.
         * @return Builder, for chainability.
         */
        public abstract Builder setApiKey(Optional<String> apiKey);

        /**
         * Return the API key currently configured with this builder, if any.
         *
         * @return Service API key issued by Cookies, or {@link Optional#empty()}.
         */
        public abstract Optional<String> getApiKey();

        /**
         * Set a custom logger factory, which has an opportunity to control logging for each spawned service.
         *
         * @param loggerFactory Logger factory for services.
         * @return Builder, for chainability.
         */
        public abstract Builder setLoggerFactory(Optional<ILoggerFactory> loggerFactory);

        /**
         * Return the logger factory currently configured with this builder, if any.
         *
         * @return Logger factory to use for services, or {@link Optional#empty()}.
         */
        public abstract Optional<ILoggerFactory> getLoggerFactory();

        /**
         * Build these settings into an immutable {@link CookiesSDKManager} configuration manager instance.
         *
         * @return Configured and sealed Cookies SDK manager.
         */
        public abstract CookiesSDKManager build();
    }

    /** Keeps track of the open/closed state of the underlying executor. */
    private final AtomicBoolean closed = new AtomicBoolean(false);

    /** Map of services loaded or initialized as part of this SDK manager. */
    private final ConcurrentMap<ServiceInfo, BaseService<?>> serviceMap = new ConcurrentSkipListMap<>();

    /**
     * Register a stubbed service with the SDK manager, so that it may be cleanly shut down when requested.
     *
     * @param service Lazy-instantiated service which we wish to register.
     * @param <Stub> Type of stub attached to this service.
     * @return Service instance.
     * @throws IllegalStateException If a service is registered after closing an SDK manager.
     */
    private @Nonnull <Service extends BaseService<Stub>, Stub extends BackgroundResource> Service
            register(@Nonnull Service service) {
        var closed = this.closed.get();

        // can't register after closing
        if (closed) {
            if (logging.isErrorEnabled())
                logging.error(format("Cannot register service '%s': closed.", service.getServiceInfo().serviceTag()));
            throw new IllegalStateException(format(
                "Cannot register service ('%s') after closing an SDK manager.",
                service.getServiceInfo().serviceTag()
            ));
        }

        // put it, we're good to go
        serviceMap.put(service.getServiceInfo(), service);
        return service;
    }

    /**
     * Either resolve a cached copy of, or create a new copy of, the provided service, addressed by the provided service
     * info; if we create an instance, write it to the service map.
     *
     * <p>This method makes sure to recycle services, which are thread-safe, where possible. We need to register weak
     * references to services so that we may close them when the developer asks us to.</p>
     *
     * @param info Service info for the service we want.
     * @param service Supplier which can create the service if we need it to.
     * @param <Stub> Stub type for the service we're creating.
     * @return Service implementation, resolved (and potentially cached) from the registered service map.
     */
    @SuppressWarnings("SameParameterValue")
    private @Nonnull <Service extends BaseService<Stub>, Stub extends BackgroundResource> Service resolve(
            @Nonnull ServiceInfo info,
            @Nonnull Supplier<Service> service) {
        // check if we have it first
        if (serviceMap.containsKey(info)) {
            if (logging.isDebugEnabled())
                logging.debug(format("Using cached service '%s'.", info.serviceTag()));
            //noinspection unchecked
            return (Service)serviceMap.get(info);
        }

        // otherwise, set up the service, cache it, and return
        if (logging.isDebugEnabled())
            logging.debug(format("Creating registered service '%s'.", info.serviceTag()));
        return register(service.get());
    }

    // Method stub to return a configured custom API endpoint.
    public abstract @Nonnull Optional<String> getEndpoint();

    // Method stub to return an indication of private access state.
    public abstract @Nonnull Optional<Boolean> getPrivateAccess();

    /**
     * Return the configured endpoint to use for RPC traffic.
     *
     * @return Custom RPC endpoint, or {@link Optional#empty()}.
     */
    @Override
    public @Nonnull String endpoint() {
        return getEndpoint().orElseGet(ExtensibleCookiesSDK::getDefaultApiEndpoint);
    }

    // Method stub to return a custom executor to wrap with a provider, as applicable.
    abstract @Nonnull Optional<ListeningScheduledExecutorService> getExecutorService();

    // Method stub to return the executor provider used to acquire/spawn executors for services.
    abstract @Nonnull Optional<ExecutorProvider> getExecutorProvider();

    /**
     * Returns an executor provider implementing execution services for the SDK.
     *
     * <p>If a custom executor is set via the SDK settings, this will use that executor. If no explicit executor is set,
     * the default executor generated for the SDK is returned by the provider.</p>
     *
     * @see Builder#setExecutorService(Optional)  Custom executor services
     * @return Executor provider for SDK use.
     */
    @Override
    public @Nonnull ExecutorProvider executorProvider() {
        return getExecutorProvider().orElseGet(() -> new ExecutorProvider() {
            @Override
            public boolean shouldAutoClose() {
                return false;
            }

            @Override
            public ScheduledExecutorService getExecutor() {
                // first up: if any custom executor is set, use that.
                var customExecutor = getExecutorService();
                if (customExecutor.isPresent()) {
                    return customExecutor.get();
                }

                // otherwise, fall back to any current default executor.
                var exec = DEFAULT_EXECUTOR_SERVICE;
                if (exec.isPresent()) {
                    return exec.get();
                }

                // we don't have a default executor yet, so spawn a default that we can keep around.
                synchronized (this) {
                    var spawned = MoreExecutors.listeningDecorator(
                            Executors.newScheduledThreadPool(3)
                    );
                    DEFAULT_EXECUTOR_SERVICE = Optional.of(spawned);
                    return spawned;
                }
            }
        });
    }

    // Method stub to return the logger factory to use for services.
    abstract @Nonnull Optional<ILoggerFactory> getLoggerFactory();

    /**
     * Returns a logger factory implemented via SLF4J that can be used to spawn loggers for services.
     *
     * @return Logger factory to use for services.
     */
    @Override
    public @Nonnull ILoggerFactory loggerFactory() {
        return getLoggerFactory().orElseGet(LoggerFactory::getILoggerFactory);
    }

    /**
     * Indicate whether private access is enabled for this SDK instance; in private access mode, the SDK will use
     * endpoints that are only available within the Cookies Cloud.
     *
     * @return Whether private access is enabled.
     */
    public @Nonnull Boolean privateAccess() {
        return getPrivateAccess().orElse(false);
    }

    // Method stub to return any higher-order channel configurator configured for this SDK manager.
    public abstract @Nonnull Optional<Function<ManagedChannelBuilder, ManagedChannelBuilder>> getChannelConfigurator();

    // Utility function to return a channel configurator, based on the provided high-order channel configurator, or any
    // defaults that should be applied.
    @VisibleForTesting
    ApiFunction<ManagedChannelBuilder, ManagedChannelBuilder> channelConfigurator() {
        return input -> {
            // apply base customizations
            input.useTransportSecurity();
            input.offloadExecutor(executorProvider().getExecutor());
            input.keepAliveWithoutCalls(true);
            input.keepAliveTime(1, TimeUnit.MINUTES);
            input.keepAliveTimeout(10, TimeUnit.MINUTES);
            input.enableRetry();
            input.maxRetryAttempts(3);
            input.userAgent("Cookies SDK/J v1");

            var nettyBuilder = (NettyChannelBuilder)input;
            nettyBuilder.negotiationType(NegotiationType.TLS);

            // apply any requisite private access settings
            if (privateAccess()) {
                var endpoint = endpoint();
                if (logging.isInfoEnabled())
                    logging.info("Enabling private Cookies API access (endpoint: '{}')", endpoint);
                input.overrideAuthority(endpoint);
                try {
                    nettyBuilder.sslContext(GrpcSslContexts.forClient()
                        .trustManager(InsecureTrustManagerFactory.INSTANCE)
                        .build());
                } catch (SSLException e) {
                    if (logging.isWarnEnabled())
                        logging.warn("Failed to enable private SSL settings. Access may fail.", e);
                }
            }

            var configurator = getChannelConfigurator();
            if (configurator.isPresent()) {
                return configurator.get().apply(input);
            }
            return input;
        };
    }

    // Method stub to return an immutable transport provider.
    abstract @Nonnull Optional<TransportChannelProvider> getTransportChannelProvider();

    /**
     * Returns a configured {@link TransportChannelProvider} which provides network transit to SDK services.
     *
     * <p>Invoking users will generally want to leave this un-customized, instead opting to specify an executor or body
     * of method settings. The default transport provider ({@link InstantiatingGrpcChannelProvider}) intelligently
     * manages a pool of channels on top of the configured executor.</p>
     *
     * @return Provider for transport channel instances.
     */
    @Override
    public @Nonnull TransportChannelProvider transportChannelProvider() {
        return getTransportChannelProvider().orElseGet(() ->
            InstantiatingGrpcChannelProvider.newBuilder()
                    .setEndpoint(endpoint())
                    .setKeepAliveTime(Duration.ofMinutes(1))
                    .setKeepAliveTimeout(Duration.ofMinutes(10))
                    .setKeepAliveWithoutCalls(true)
                    .setExecutor(executorProvider().getExecutor())
                    .setChannelConfigurator(channelConfigurator())
                    .build()
        );
    }

    // Method stub to return immutable explicit credentials.
    abstract @Nonnull Optional<Credentials> getCredentials();

    // Method stub to return immutable explicit credentials.
    abstract @Nonnull Optional<CredentialsProvider> getCredentialsProvider();

    /**
     * Returns a configured {@link CredentialsProvider} which provides authorization to SDK services.
     *
     * <p>By default, authorization is left empty by the SDK. If accessing a protected or otherwise authorized service,
     * the developer must enclose credentials using this property. Providers exist for fixed credentials, OAuth2-based
     * credentials, and more.</p>
     *
     * @return Credentials provider for SDK use.
     */
    @Override
    public @Nonnull CredentialsProvider credentialsProvider() {
        // if the user has provided an explicit set of credentials, use those, by wrapping them in a fixed credential
        // provider. otherwise, check if the user has provided their own provider. if they have, use that. if they have
        // not provided a provider or explicit set of credentials, create a `NoCredentialsProvider`.
        return getCredentials()
            .map((creds) -> (CredentialsProvider)FixedCredentialsProvider.create(creds))
            .orElseGet(() ->
                getCredentialsProvider().orElseGet(NoCredentialsProvider::create)
            );
    }

    /**
     * Returns a configured {@link HeaderProvider} which applies active SDK settings.
     *
     * <p>API keys and credentials are automatically handled by the SDK with regard to headers, although a developer may
     * elect to provide their own header provider as well. For custom header providers, the developer must account for
     * <b>all headers</b> including API key or authorization headers.</p>
     *
     * @return Header provider for SDK use.
     */
    @Override
    public @Nonnull HeaderProvider headerProvider() {
        return () -> {
            var headers = new TreeMap<String, String>();

            // fill in user agent
            headers.put(Headers.USER_AGENT.toString(), SDK_USER_AGENT);

            // fill in API key
            var apiKey = getApiKey();
            apiKey.ifPresent(s -> headers.put(Headers.API_KEY.toString(), s));
            return headers;
        };
    }

    /** @return Self, as the active settings configuration. */
    @Override
    public @Nonnull SDKConfiguration getSettings() {
        return this;
    }

    // -- Interface: Closeable -- //

    /**
     * Close this SDK by gracefully ending any in-flight RPCs, and then shutting down the managed channel and any
     * backing executor.
     */
    @Override
    public void close() {
        if (!closed.get()) {
            closed.compareAndSet(false, true);
            var services = serviceMap.values();
            if (!services.isEmpty()) {
                services.forEach(BaseService::close);
                serviceMap.clear();
            }
        }
    }

    // -- Interface: Services -- //

    /**
     * Acquire access to the Cookies Catalog API, via the main Java service facade.
     *
     * @param serviceConfiguration Custom service configuration for the Catalog API, if any.
     * @return Implementation of a Catalog API client.
     * @throws IllegalStateException If the SDK manager is already closed.
     */
    @Override
    public @Nonnull CatalogClient catalog(@Nonnull Optional<SDKConfiguration> serviceConfiguration) {
        return resolve(CatalogClientV1.INFO, () -> CatalogClientV1.configure(this));
    }

    /**
     * Acquire access to the Cookies Storefront API, via the main Java service facade.
     *
     * @param serviceConfiguration Custom service configuration for the Storefront API, if any.
     * @return Implementation of a Storefront API facade.
     * @throws IllegalStateException If the SDK manager is already closed.
     */
    @Override
    public @Nonnull Storefront storefront(@Nonnull Optional<SDKConfiguration> serviceConfiguration) {
        return StorefrontClientV1.withServices(
            resolve(StorefrontClientV1.MenuClientV1.INFO, () -> StorefrontClientV1.MenuClientV1.configure(this)),
            resolve(StorefrontClientV1.ProfileClientV1.INFO, () -> StorefrontClientV1.ProfileClientV1.configure(this))
        );
    }
}
