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


import cookies.schema.ProductLine;
import cookies.schema.StoreKey;
import cookies.schema.store.model.UserLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import java.io.Serializable;
import java.util.*;


/**
 * Specifies information related to a request for menu data from the Menu Service, part of the Cookies Storefront API;
 * this class implements Version 1 of the API.
 *
 * <p>The following items are supported with regard to Menu API inputs:
 * <ul>
 *     <li><b>Content locale:</b> The content locale controls the copy language, currency, and time/date formats.</li>
 *     <li><b>User location:</b> If known, the user's location can be provided to generate a tailored menu payload.</li>
 *     <li><b>Store key:</b> If explicitly selected (or otherwise known), a store selection or hint can be sent.</li>
 * </ul></p>
 */
@NotThreadSafe
@SuppressWarnings("unused")
public final class MenuRequestSpec implements Serializable {
    private static final long serialVersionUID = 20211115L;
    private static final Locale DEFAULT_LOCALE = Locale.US;

    private final @Nonnull Locale locale;
    private final @Nullable UserLocation location;
    private final @Nullable StoreKey storeKey;
    private final @Nullable String userId;
    private final @Nonnull EnumSet<ProductLine> productLines = EnumSet.of(
        ProductLine.THC,
        ProductLine.CBD,
        ProductLine.MUSHROOMS,
        ProductLine.APPAREL,
        ProductLine.MERCHANDISE
    );

    private @Nonnull Boolean keysOnly = false;

    // Private constructor.
    private MenuRequestSpec(@Nonnull Locale locale,
                            @Nullable UserLocation location,
                            @Nullable StoreKey storeKey,
                            @Nullable String userId) {
        this.locale = locale;
        this.location = location;
        this.storeKey = storeKey;
        this.userId = userId;
    }

    // -- Object Copy -- //

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MenuRequestSpec)) return false;
        MenuRequestSpec that = (MenuRequestSpec) o;
        return getLocale().equals(
            that.getLocale()
        ) && Objects.equals(
            getLocation(),
            that.getLocation()
        ) && Objects.equals(
            getStoreKey(),
            that.getStoreKey()
        ) && Objects.equals(
            getUserId(),
            that.getUserId()
        ) && Objects.equals(
            isKeysOnly(),
            that.isKeysOnly()
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            getLocale(),
            getLocation(),
            getStoreKey(),
            getUserId()
        );
    }

    // -- Getters -- //

    /** @return Locale attached to this request, if any. */
    public @Nonnull Optional<Locale> getLocale() {
        return Optional.of(locale);
    }

    /** @return Store key attached to this request, if any. */
    public @Nonnull Optional<StoreKey> getStoreKey() {
        return Optional.ofNullable(storeKey);
    }

    /** @return User's location info, if known. */
    public @Nonnull Optional<UserLocation> getLocation() {
        return Optional.ofNullable(location);
    }

    /** @return User's account ID, if known. */
    public @Nonnull Optional<String> getUserId() {
        return Optional.ofNullable(userId);
    }

    /** @return Whether this request should return matching keys, or full records. */
    public boolean isKeysOnly() {
        return keysOnly;
    }

    /** @return Set of product lines eligible to be included in the response. */
    public @Nonnull EnumSet<ProductLine> getProductLines() {
        return productLines;
    }

    // -- Setters -- //

    /**
     * Mutate the current menu request specification to use the specified `keysOnly` setting.
     *
     * @param keysOnly Whether the response should just include keys, or full records.
     * @return Mutated menu request spec.
     */
    public @Nonnull MenuRequestSpec setKeysOnly(@Nonnull Boolean keysOnly) {
        this.keysOnly = keysOnly;
        return this;
    }

    /**
     * Mutate the current menu request specification to clear the set of current product lines; after calling this,
     * product lines should be added with {@link #addProductLines(ProductLine...)}, or the request will default to
     * including all product lines anyway under the hood.
     *
     * @return Mutated menu request spec.
     */
    public @Nonnull MenuRequestSpec clearProductLines() {
        this.productLines.clear();
        return this;
    }

    /**
     * Mutate the current menu request specification to include the specified product lines; if the product lines are
     * already included, this is a no-op.
     *
     * @param productLines Product lines to add to this request.
     * @return Mutated menu request spec.
     */
    public @Nonnull MenuRequestSpec addProductLines(@Nonnull ProductLine... productLines) {
        this.productLines.addAll(Arrays.asList(productLines));
        return this;
    }

    // -- Factories -- //

    /** @return Default instance of a menu request spec, with the default locale and no store. */
    public static @Nonnull MenuRequestSpec defaults() {
        return new MenuRequestSpec(
            DEFAULT_LOCALE,
            null,
            null,
            null
        );
    }

    /**
     * Return a menu request specification for the provided with the provided content locale; this method does not
     * provide a facility to customize a menu based on a user's location.
     *
     * @see #forStore(StoreKey, Locale) to generate a store menu within a locale.
     * @param locale Content locale for the desired menu.
     * @return Menu request specification for the specified content locale.
     */
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public static @Nonnull MenuRequestSpec forLocale(@Nonnull Optional<Locale> locale) {
        return new MenuRequestSpec(
            locale.orElse(DEFAULT_LOCALE),
            null,
            null,
            null
        );
    }

    /**
     * Return a menu request specification for the provided store, with the default locale; this method does not provide
     * a facility to customize a menu based on a user's location.
     *
     * @see #forStore(StoreKey, Locale) for control over content locale.
     * @param key Store to generate a menu for.
     * @return Menu request specification for the specified store, and the default locale.
     */
    public static @Nonnull MenuRequestSpec forStore(@Nonnull StoreKey key) {
        return forStore(
            key,
            DEFAULT_LOCALE
        );
    }

    /**
     * Return a menu request specification for the provided store, with an explicit locale; this method does not provide
     * a facility to customize a menu based on a user's location.
     *
     * @param key Store to generate a menu for.
     * @param locale Content locale to generate a menu for.
     * @return Menu request specification for the specified store, and the specified locale.
     */
    public static @Nonnull MenuRequestSpec forStore(@Nonnull StoreKey key, @Nonnull Locale locale) {
        return new MenuRequestSpec(
            locale,
            null,
            key,
            null
        );
    }

    /**
     * Return a menu request specification for the provided user's location, within the default content locale, and with
     * no specified store.
     *
     * @param userLocation Known (or estimated) user location.
     * @return Menu request specification containing the specified location.
     */
    public static @Nonnull MenuRequestSpec forUserLocation(@Nonnull UserLocation userLocation) {
        return new MenuRequestSpec(
            DEFAULT_LOCALE,
            userLocation,
            null,
            null
        );
    }

    /**
     * Return a menu request specification for the provided user's location, with the specified content locale, and with
     * no specified store.
     *
     * @param userLocation Known (or estimated) user location.
     * @param locale Content locale for the menu to be generated.
     * @return Menu request specification containing the specified location.
     */
    public static @Nonnull MenuRequestSpec forUserLocation(@Nonnull UserLocation userLocation, @Nonnull Locale locale) {
        return new MenuRequestSpec(
            locale,
            userLocation,
            null,
            null
        );
    }

    /**
     * Return a menu request specification for the provided user's location, with the specified content locale, and with
     * the specified store as a selection.
     *
     * @param userLocation Known (or estimated) user location.
     * @param locale Content locale for the menu to be generated.
     * @param storeKey Store hint or explicit store selection.
     * @return Menu request specification containing the specified location.
     */
    public static @Nonnull MenuRequestSpec forUserLocation(@Nonnull UserLocation userLocation,
                                                           @Nonnull Locale locale,
                                                           @Nonnull StoreKey storeKey) {
        return new MenuRequestSpec(
            locale,
            userLocation,
            storeKey,
            null
        );
    }

    /**
     * Return a menu request specification for the provided user, addressed by their account ID, within the default
     * content locale, and with no specified store.
     *
     * @param userId Known user account ID.
     * @return Menu request specification containing the specified ID.
     */
    public static @Nonnull MenuRequestSpec forUser(@Nonnull String userId) {
        return new MenuRequestSpec(
            DEFAULT_LOCALE,
            null,
            null,
            userId
        );
    }

    /**
     * Return a menu request specification for the provided user, addressed by their account ID, within the default
     * content locale, and with no specified store, but with a known or estimated usr location.
     *
     * @param userId Known user account ID.
     * @param location Known or estimated user location.
     * @return Menu request specification containing the specified ID.
     */
    public static @Nonnull MenuRequestSpec forUser(@Nonnull String userId, @Nonnull UserLocation location) {
        return new MenuRequestSpec(
            DEFAULT_LOCALE,
            location,
            null,
            userId
        );
    }

    /**
     * Return a menu request specification for the provided user, addressed by their account ID, within the default
     * content locale, and with no specified store, but with a known or estimated usr location.
     *
     * @param userId Known user account ID.
     * @param location Known or estimated user location.
     * @param storeKey Selected or hinted store for this user.
     * @return Menu request specification containing the specified ID.
     */
    public static @Nonnull MenuRequestSpec forUser(@Nonnull String userId,
                                                   @Nonnull UserLocation location,
                                                   @Nonnull StoreKey storeKey) {
        return new MenuRequestSpec(
            DEFAULT_LOCALE,
            location,
            storeKey,
            userId
        );
    }
}
