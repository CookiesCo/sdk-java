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

import javax.annotation.Nonnull;

import static java.lang.String.format;


/**
 * Describes the API interface for a simple descriptor which carries static service information.
 *
 * <p>An implementation of {@link ServiceInfo} is expected to be prepared by a service implementation, which passes it
 * in to its abstract base during construction.</p>
 */
public interface ServiceInfo extends Comparable<ServiceInfo> {
    /**
     * Official short name for this service.
     *
     * @return Service name.
     */
    @Nonnull String serviceName();

    /**
     * Implementation version for this service.
     *
     * @return Service version.
     */
    @Nonnull String serviceVersion();

    /**
     * Whether this service requires an API key.
     *
     * @return API key requirement status.
     */
    default boolean apiKeyRequired() {
        return true;
    }

    /**
     * Whether this service requires authorization.
     *
     * @return Authorization requirement status.
     */
    default boolean authorizationRequired() {
        return false;
    }

    /**
     * Return the composed service tag for this service (for example, "catalog:v1").
     *
     * @return Composed tag for this service (name and version).
     */
    default String serviceTag() {
        return format("%s:%s", serviceName(), serviceVersion());
    }

    /**
     * Compare two service info payloads to determine if they are the same.
     *
     * @param other Other payload.
     * @return Standard integer comparison result.
     */
    @Override
    default int compareTo(ServiceInfo other) {
        return (
            serviceTag().compareTo(other.serviceTag())
        );
    }
}
