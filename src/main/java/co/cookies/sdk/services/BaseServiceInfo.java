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


/**
 * Base service info implementation class, providing a hash code implementation for service info objects based on the
 * name and version of a given service.
 */
public abstract class BaseServiceInfo implements ServiceInfo {
    /**
     * Delegate to the `hashCode` result of this service's composed tag.
     *
     * @return Hash code of this info object's composed tag.
     */
    @Override
    public int hashCode() {
        return serviceTag().hashCode();
    }

    /**
     * Two service information objects are equal if they represent the same service name and version.
     *
     * @param other Other object to compare.
     * @return Whether the two are considered equal.
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof BaseServiceInfo)) return false;
        BaseServiceInfo that = (BaseServiceInfo) other;
        return serviceTag().equals(that.serviceTag());
    }

    @Override
    public String toString() {
        return "BaseServiceInfo{" +
            "serviceName='" + serviceName() + '\'' +
            ", serviceVersion='" + serviceVersion() + '\'' +
        '}';
    }
}
