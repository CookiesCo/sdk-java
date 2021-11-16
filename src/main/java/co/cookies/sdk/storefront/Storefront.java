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
package co.cookies.sdk.storefront;


import co.cookies.sdk.CookiesSDK;
import co.cookies.sdk.SDKConfiguration;
import co.cookies.sdk.SDKServiceProvider;
import co.cookies.sdk.services.AsyncRPC;
import co.cookies.sdk.services.ServiceClient;
import co.cookies.sdk.services.SyncRPC;
import co.cookies.sdk.storefront.v1.*;
import co.cookies.sdk.storefront.v1.err.*;
import com.google.common.util.concurrent.ListenableFuture;
import cookies.schema.StoreKey;
import cookies.schema.store.*;
import cookies.schema.store.model.MenuSearchResultset;
import cookies.schema.store.model.ProductContext;
import cookies.schema.store.model.StoreUser;

import javax.annotation.Nonnull;
import java.util.Locale;
import java.util.Optional;

import static co.cookies.sdk.SDKUtil.block;


/**
 * Defines the API surface for the Java client facade implementing the Cookies Storefront API, which provides business
 * logic for headless cannabis commerce powered by the Cookies Platform.
 *
 * <p>The Storefront API is quite large, and so it is split into functional sub-services which combine on this API
 * interface. Each sub-interface is defined as its own nested block.</p>
 *
 * <p>Instances of the Storefront API Client may be acquired via the {@link CookiesSDK#builder()} method, which can be
 * built into an SDK manager that provides a client via {@link SDKServiceProvider#storefront()}. Customizations can be
 * applied for this service's construction via the methods offered on the builder.</p>
 *
 * <p><b>Note:</b> The Cookies Storefront API is a <b>private API service</b> designed for use by Cookies Engineering,
 * partners, contractors, and vendors. To invoke methods on this API, one must provide a valid API key and set of
 * credentials with sufficient authorization scope. <b>Most Storefront API methods require both an API key and a valid
 * user access token.</b></p>
 */
@SuppressWarnings({"OptionalUsedAsFieldOrParameterType", "unused"})
public interface Storefront {
    /**
     * Specifies the API surface for the <b>Menu Service</b>, a constituent part of the Cookies Storefront API, which is
     * responsible for traversing catalog and inventory data to produce relevant localized product menus.
     *
     * <p>The Menu API is aware of jurisdictional restrictions and support. For example, CBD products will not be shown
     * in the United States unless an age gate parameter is passed that allows such menu items; additionally, user
     * geo-location occurs to determine relevant retail locations nearby, unless an explicit location is provided.</p>
     *
     * <p>Whereas inventory operates on the individual item level, menus typically operate at the level of <i>abstract
     * products</i>, meaning a single product may have multiple variants, as is the case with item types like apparel
     * and flower (for example, apparel typically has sizes/colors and flower typically is sold at weight-driven size
     * tiers). Via the Menu API, a "product" encompasses all variants and inventory for a given product group.</p>
     */
    interface MenuClient extends ServiceClient<MenuV1Client> {
        /**
         * Generate a default menu payload for the detected context for this request; this will use server-side geo-
         * location to determine the user's menu relevance, and then merge the resulting payload with globally-available
         * inventory and catalog data.
         *
         * <p>This default implementation route always opts to include menu content. See other menu method options to
         * specify things like the content locale, user's location or ID, and store hints.</p>
         *
         * @see #menu(Optional) To specify a content locale.
         * @return Rendered menu payload across relevant inventory channels, decorated with global content.
         */
        default @Nonnull ListenableFuture<MenuResponse> menu() {
            return menu(
                MenuRequestSpec.defaults()
            );
        }

        /**
         * Generate a default menu payload for the detected context for this request within the specified content locale
         * and with server-side geo-location, used to determine the user's menu relevance, then merge the resulting
         * payload with globally-available inventory and catalog data.
         *
         * <p>This default implementation route always opts to include menu content. See other menu method options to
         * specify things like the user's location or ID, and store hints.</p>
         *
         * @see #menu(Optional, StoreKey) To specify a content locale and retail store.
         * @param locale Content locale for the resulting menu payload. If the specified locale is unsupported, an error
         *               is returned to the invoking user.
         * @return Rendered menu payload across relevant inventory channels, decorated with global content.
         */
        default @Nonnull ListenableFuture<MenuResponse> menu(@Nonnull Optional<Locale> locale) {
            return menu(
                MenuRequestSpec.forLocale(locale)
            );
        }

        /**
         * Generate a store-specific payload for the detected context for this request within the specified content
         * locale, and with server-side geo-location, used to determine the user's menu relevance, then merge the
         * resulting payload with globally-available inventory and catalog data.
         *
         * <p>This default implementation route always opts to include menu content. See other menu method options to
         * specify things like the user's location or ID, and store hints.</p>
         *
         * @see #menu(MenuRequestSpec) To specify advanced menu request options.
         * @param locale Content locale for the resulting menu payload. If the specified locale is unsupported, an error
         *               is returned to the invoking user.
         * @param store Store for which the desired menu should consider inventory, and include entries (with content)
         *              for matching products.
         * @return Rendered menu payload across relevant inventory channels, decorated with global content.
         */
        default @Nonnull ListenableFuture<MenuResponse> menu(@Nonnull Optional<Locale> locale,
                                                             @Nonnull StoreKey store) {
            return menu(locale
                .map(value -> MenuRequestSpec.forStore(store, value))
                .orElseGet(() -> MenuRequestSpec.forStore(store))
            );
        }

        /**
         * Generate an advanced menu request specification based on a developer-crafted request spec, which considers
         * the specified inventory channels, user location and account ID, content locale, and query settings; this
         * method allows full control of the menu generation engine.
         *
         * <p>Simpler invocations exist on this interface which work based on a store key, or a content locale (which
         * uses user location detection features).</p>
         *
         * @see #menu(Optional) Convenience method if you just need to set the content locale.
         * @see #menu(Optional, StoreKey) Convenience method if you just need to set the content locale and store key.
         * @param options Crafted RPC request spec object, which specifies the desired menu.
         * @return Rendered menu payload across relevant inventory channels, decorated with global content.
         */
        default @Nonnull ListenableFuture<MenuResponse> menu(@Nonnull MenuRequestSpec options) {
            var request = MenuRequest.newBuilder();
            var context = ProductContext.newBuilder();

            // copy in product line filters
            if (!options.getProductLines().isEmpty())
                request.addAllLine(options.getProductLines());

            // copy in user information
            if (options.getLocation().isPresent())
                request.setLocation(options.getLocation().get());

            // copy in product context and keys-only state
            if (options.isKeysOnly())
                request.setKeysOnly(true);
            if (options.getLocale().isPresent()) {
                var locale = options.getLocale().get();
                context.setLocale(String.format(
                    "%s-%s",
                    locale.getLanguage(),
                    locale.getCountry()
                ));
            }
            return menu(AsyncRPC.of(
                request.setContext(context).build()
            ));
        }

        /**
         * Synchronously generate a custom menu request based on a developer-crafted protocol buffer, which considers
         * the specified inventory channels, user location and account ID, content locale, and query settings; this
         * method allows full control of the menu generation engine.
         *
         * <p>Simpler invocations exist on this interface which work based on a store key, or a content locale (which
         * uses user location detection features). An async variant of this method is also available, and should
         * generally be preferred unless dispatching from a background thread.</p>
         *
         * @see #menu(AsyncRPC) Asynchronous variant of this method.
         * @see #menu(Optional) Convenience method if you just need to set the content locale.
         * @see #menu(Optional, StoreKey) Convenience method if you just need to set the content locale and store key.
         * @param rpc Crafted RPC request payload, which specifies the desired menu, and is wrapped to execute and
         *            return asynchronously.
         * @return Rendered menu payload across relevant inventory channels, decorated with global content.
         */
        default @Nonnull MenuResponse menu(@Nonnull SyncRPC<MenuRequest> rpc) {
            return block(
                rpc,
                logger(),
                MenuV1Grpc.getMenuMethod(),
                this::menu
            );
        }

        /**
         * Generate a custom menu request based on a developer-crafted protocol buffer, which considers the specified
         * inventory channels, user location and account ID, content locale, and query settings; this method allows full
         * control of the menu generation engine.
         *
         * <p>Simpler invocations exist on this interface which work based on a store key, or a content locale (which
         * uses user location detection features).</p>
         *
         * <p><b>Note:</b> This method operates synchronously. Async variants exist and should generally be preferred
         * unless dispatching from a background thread.</p>
         *
         * @see #menu(SyncRPC) Sychronous version of this method, for dispatch in a background thread.
         * @see #menu(Optional) Convenience method if you just need to set the content locale.
         * @see #menu(Optional, StoreKey) Convenience method if you just need to set the content locale and store key.
         * @param rpc Crafted RPC request payload, which specifies the desired menu, and is wrapped to execute and
         *            return asynchronously.
         * @return Rendered menu payload across relevant inventory channels, decorated with global content.
         */
        @Nonnull ListenableFuture<MenuResponse> menu(@Nonnull AsyncRPC<MenuRequest> rpc);

        /**
         * Synchronously perform a search across available menu items for a given context, based on arbitrary term input
         * and any facet filters or regular filters applied by the user (or underlying code).
         *
         * <p>Search results typically reference items via their CPIDs -- i.e. abstract product groups, which contain
         * variants. When using this method, results are filtered based on live inventory signals.</p>
         *
         * @see #search(AsyncRPC) Asynchronous variant of this method.
         * @param rpc Crafted RPC request payload for a menu search operation which is wrapped to execute synchronously.
         * @return Rendered menu search results, based on the input parameters in the subject RPC request payload.
         */
        default @Nonnull MenuSearchResultset search(@Nonnull SyncRPC<MenuSearchRequest> rpc) {
            return block(
                rpc,
                logger(),
                MenuV1Grpc.getMenuSearchMethod(),
                this::search
            );
        }

        /**
         * Asynchronously perform a search across available menu items for a given context, based on arbitrary term
         * input and any facet filters or regular filters applied by the user (or underlying code).
         *
         * <p>Search results typically reference items via their CPIDs -- i.e. abstract product groups, which contain
         * variants. When using this method, results are filtered based on live inventory signals.</p>
         *
         * @see #search(AsyncRPC) Asynchronous variant of this method.
         * @param rpc Crafted RPC request payload for a menu search operation which is wrapped to execute synchronously.
         * @return Rendered menu search results, based on the input parameters in the subject RPC request payload.
         */
        @Nonnull ListenableFuture<MenuSearchResultset> search(@Nonnull AsyncRPC<MenuSearchRequest> rpc);
    }

    /**
     * Specifies the API surface for the <b>Profile Service</b>, a constituent part of the Cookies Storefront, which is
     * responsible for managing user profile information, and exposing tools to edit or update such information in
     * different circumstances.
     *
     * <p>The Profile API works in tandem with Cookies Identity Services, available at `accounts.cookies.co`. After a
     * user authenticates via OAuth or SAML, an access token is yielded which allows them access to view other user
     * profiles (where allowed via privacy settings) and view and edit their own user profile, all via this service.</p>
     */
    interface ProfileClient extends ServiceClient<ProfileV1Client> {
        /**
         * Synchronously check the availability of the provided username; if the decoded response indicates the name is
         * globally available, return `true`, otherwise `false`.
         *
         * @see #usernameCheck(AsyncRPC) Async version of this method.
         * @param username Username to check for global availability.
         * @return Whether the provided username is available to be claimed.
         * @throws UsernameInvalidError If the supplied username is blocked for policy reasons.
         * @throws UsernameIneligibleError If the active user is not eligible to claim a username.
         */
        default @Nonnull Boolean usernameCheck(@Nonnull String username) {
            return block(
                SyncRPC.of(UsernameCheckRequest.newBuilder().setUsername(username).build()),
                logger(),
                ProfileV1Grpc.getProfileUsernameCheckMethod(),
                this::usernameCheck
            );
        }

        /**
         * Asynchronously check the availability of the provided username; if the decoded response indicates the name is
         * globally available, return `true`, otherwise `false`.
         *
         * @see #usernameCheck(String) Synchronous version of this method.
         * @param rpc RPC describing the username to check for uniqueness.
         * @return Listenable future which resolve to a boolean which indicates whether the provided username is
         *         available to be claimed.
         * @throws UsernameInvalidError If the supplied username is blocked for policy reasons.
         * @throws UsernameIneligibleError If the active user is not eligible to claim a username.
         */
        @Nonnull ListenableFuture<Boolean> usernameCheck(@Nonnull AsyncRPC<UsernameCheckRequest> rpc);

        /**
         * Synchronously fetch the specified user profile, specified either via their username or their user ID; if the
         * user could not be found, or the invoking user does not have access and the requested user's profile is set to
         * private, return {@link Optional#empty()}.
         *
         * <p>Requesting a profile with the special username/user ID `me` requests the currently logged-in user's
         * profile. Accessing this special path requires an active and identified user session.</p>
         *
         * @see #fetch(AsyncRPC) Async version of this method.
         * @param rpc RPC which we should submit to fetch a user profile.
         * @return {@link Optional} containing the requested store user profile, or else {@link Optional#empty()} if the
         *         profile does not exist, or the invoking user does not have access to see it.
         */
        default @Nonnull Optional<ProfileResponse> fetch(@Nonnull SyncRPC<ProfileRequest> rpc) {
            return block(
                rpc,
                logger(),
                ProfileV1Grpc.getProfileMethod(),
                this::fetch
            );
        }

        /**
         * Asynchronously fetch the specified user profile, specified either via their username or their user ID; if the
         * user could not be found, or the invoking user does not have access and the requested user's profile is set to
         * private, return {@link Optional#empty()}.
         *
         * <p>Requesting a profile with the special username/user ID `me` requests the currently logged-in user's
         * profile. Accessing this special path requires an active and identified user session.</p>
         *
         * @see #fetch(SyncRPC) Synchronous version of this method.
         * @param rpc RPC which we should submit to fetch a user profile.
         * @return Future which resolves to an {@link Optional} containing the requested store user profile, or else
         *         {@link Optional#empty()} if the profile does not exist, or the invoking user does not have access to
         *         see it.
         */
        @Nonnull ListenableFuture<Optional<ProfileResponse>> fetch(@Nonnull AsyncRPC<ProfileRequest> rpc);

        /**
         * Synchronously apply the specified update to a user's profile, and then return the updated profile with any
         * requested changes applied; if changes fail to apply because of concurrent mutations (or any other reason),
         * this method will throw an exception.
         *
         * <p>Requests to update user profiles must, of course, be affixed with user authorization which matches the
         * profile under edit.</p>
         *
         * @see #update(AsyncRPC) Async version of this method.
         * @param rpc RPC which we should submit to edit the current user's profile.
         * @return The updated user profile, with any requested changes applied.
         */
        default @Nonnull StoreUser update(@Nonnull SyncRPC<ProfileUpdateRequest> rpc) {
            return block(
                rpc,
                logger(),
                ProfileV1Grpc.getProfileUpdateMethod(),
                this::update
            );
        }

        /**
         * Asynchronously apply the specified update to a user's profile, and then return the updated profile with any
         * requested changes applied; if changes fail to apply because of concurrent mutations (or any other reason),
         * this method will throw an exception.
         *
         * <p>Requests to update user profiles must, of course, be affixed with user authorization which matches the
         * profile under edit.</p>
         *
         * @see #update(SyncRPC) Synchronous version of this method.
         * @param rpc RPC which we should submit to edit the current user's profile.
         * @return Future which resolves to the updated user profile, with any requested changes applied.
         */
        @Nonnull ListenableFuture<StoreUser> update(@Nonnull AsyncRPC<ProfileUpdateRequest> rpc);
    }

    /**
     * Specifies the API surface for the <b>Checkout Service</b>, a constituent part of the Cookies Storefront, which is
     * responsible for preparing user checkouts, validating carts, performing final inventory checks, and executing
     * payment to transition a checkout to an <i>Order</i>.
     *
     * <p>The Checkout API works with the Menu API and Profile API on top of the Cookies Identity platform. Users can be
     * identified or anonymous, but must always carry a valid access token. Checkout for regulated products (shipped CBD
     * and THC pickup/delivery) always requires identity verification (see also: <i>Draft OCP15</i>, the standard behind
     * ID verification in this API).</p>
     */
    interface CheckoutClient extends ServiceClient<CheckoutV1Client> {
        /* */
    }

    /**
     * Specifies the API surface for the main <b>Storefront Service</b>, which holds a collection of top-level methods
     * that are shared across constituent sub-services (e.g. Menu, Profile, and Checkout).
     *
     * <p>The Storefront API works in tandem with the Cookies Identity platform. User tokens are typically required
     * unless noted otherwise. OAuth2-based scopes identify authorization for each method, and a user token may be used
     * for an anonymous or identified session.</p>
     *
     * <p>Most interactions with the Storefront Service occur through the constituent services, which supply logic for
     * menus, collections, profiles, checkouts, payment, and so on.</p>
     *
     * @see MenuClient for menu-related logic
     * @see ProfileClient for user profile-related logic
     * @see CheckoutClient for user checkout-related logic
     */
    interface StorefrontClient extends ServiceClient<StorefrontV1Client> {
        /* */
    }

    /**
     * Acquire an instance of the Storefront Menu API client, configured with the same SDK configuration provided to the
     * main Storefront facade.
     *
     * <p>Optionally, this method's partner can be used to override the active SDK configuration for just this instance
     * of the Menu API client.</p>
     *
     * @see #menu(Optional) Option to override configuration for just this menu client.
     * @return Instance of the Menu API client.
     */
    default @Nonnull MenuClient menu() {
        return menu(Optional.empty());
    }

    /**
     * Acquire an instance of the Storefront Menu API client, configured with the provided SDK configuration, overriding
     * any active SDK configuration provided to the facade.
     *
     * <p>Optionally, this method's partner can be used to leverage the default SDK configuration attached to the facade
     * which hosts the resulting instance.</p>
     *
     * @see #menu() Option to leverage default configuration.
     * @return Instance of the Menu API client.
     */
    @Nonnull MenuClient menu(@Nonnull Optional<SDKConfiguration> configuration);

    /**
     * Acquire an instance of the Storefront Profile API client, configured with the same SDK configuration provided to
     * the main Storefront facade.
     *
     * <p>Optionally, this method's partner can be used to override the active SDK configuration for just this instance
     * of the Profile API client.</p>
     *
     * @see #menu(Optional) Option to override configuration for just this profile client.
     * @return Instance of the Profile API client.
     */
    default @Nonnull ProfileClient profile() {
        return profile(Optional.empty());
    }

    /**
     * Acquire an instance of the Storefront Profile API client, configured with the provided SDK configuration,
     * overriding any active SDK configuration provided to the facade.
     *
     * <p>Optionally, this method's partner can be used to leverage the default SDK configuration attached to the facade
     * which hosts the resulting instance.</p>
     *
     * @see #menu() Option to leverage default configuration.
     * @return Instance of the Profile API client.
     */
    @Nonnull ProfileClient profile(@Nonnull Optional<SDKConfiguration> configuration);
}
