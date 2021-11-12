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
import co.cookies.sdk.SDKServiceProvider;
import co.cookies.sdk.services.ServiceClient;
import co.cookies.sdk.storefront.v1.CheckoutV1Client;
import co.cookies.sdk.storefront.v1.MenuV1Client;
import co.cookies.sdk.storefront.v1.ProfileV1Client;
import co.cookies.sdk.storefront.v1.StorefrontV1Client;


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
        /* */
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
        /* */
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
}
