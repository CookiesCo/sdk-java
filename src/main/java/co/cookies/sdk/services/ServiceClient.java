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

import com.google.api.gax.core.BackgroundResource;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import java.io.Closeable;


/**
 * Describes the API surface of an object which manages interaction with a specific Cookies API, via an underlying
 * generated GAPIC and gRPC client set.
 *
 * @param <Stub> Service stub which the specified client implements.
 */
public interface ServiceClient<Stub extends BackgroundResource> extends Closeable, AutoCloseable {
    /**
     * Retrieve the stub in use by this service client, which describes the implemented API interface in raw form.
     *
     * <p>Stubs are generated code, and so it is not advised that developers use stubs directly. As generated APIs
     * change, there are no compatibility guarantees provided at the stub level.</p>
     *
     * @return Stub instance implementing this service client.
     */
    @Nonnull Stub service();

    /**
     * Return the logger dedicated to this specific service client, spawned via the logger factory provided at SDK
     * construction time.
     *
     * @return Logger for this service client.
     */
    @Nonnull Logger logger();

    /**
     * Retrieve a packaged data class which describes basic information about this service, including the service's name
     * and version, and whether API keys or authorization are required.
     *
     * @return Specification info for a given service implementation.
     */
    @Nonnull ServiceInfo getServiceInfo();

    /**
     * Retrieve the short "name" for a service, which is generally a handful of characters long and represents the API
     * service uniquely with a one-word label.
     *
     * <p>API service names are used in routing, and generally identify services from one another.</p>
     *
     * @return Assigned name of a given service.
     */
    @Nonnull String getServiceName();

    /**
     * Retrieve the version token for a service, which is generally a handful of characters long and represents the
     * current major and minor API interface revision implemented by this client.
     *
     * <p>API service versions indicate breaking API changes.</p>
     *
     * @return Assigned version of a given service.
     */
    @Nonnull String getServiceVersion();
}
