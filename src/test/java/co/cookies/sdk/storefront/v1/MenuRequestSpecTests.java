package co.cookies.sdk.storefront.v1;


import cookies.schema.ProductLine;
import cookies.schema.StoreKey;
import cookies.schema.store.model.UserLocation;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.Locale;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;


/** Tests for the {@link MenuRequestSpec} class. */
public class MenuRequestSpecTests {
    private StoreKey storeKey(String code) {
        return StoreKey.newBuilder()
            .setCode(code)
            .build();
    }

    private void enforceLocale(MenuRequestSpec spec, Locale locale) {
        assertTrue(
            spec.getLocale().isPresent(),
            "request locale should be present when provided to spec builder"
        );
        assertEquals(
            locale.getCountry(),
            spec.getLocale().get().getCountry(),
            "country value should be expected result when locale is provided"
        );
        assertEquals(
            locale.getLanguage(),
            spec.getLocale().get().getLanguage(),
            "language value should be expected result when locale is provided"
        );
    }

    private void enforceStore(MenuRequestSpec spec, String code) {
        assertTrue(
            spec.getStoreKey().isPresent(),
            "request store should be present when provided to spec builder"
        );
        assertEquals(
            code,
            spec.getStoreKey().get().getCode(),
            "store code should be expected value when provided"
        );
    }

    @Test void testDefaultRequestSpec() {
        var request = MenuRequestSpec.defaults();
        assertNotNull(
            request,
            "should not get `null` for default menu request spec"
        );
        assertFalse(
            request.getStoreKey().isPresent(),
            "store should not be present by default"
        );
    }

    @Test void testMenuSpecCustomLocale() {
        var request = MenuRequestSpec.forLocale(Optional.of(Locale.FRANCE));
        assertNotNull(
            request,
            "should not get `null` for default menu request spec"
        );
        enforceLocale(
            request,
            Locale.FRANCE
        );
        assertFalse(
            request.getStoreKey().isPresent(),
            "store should not be present by default"
        );
    }

    @Test void testMenuSpecCustomStore() {
        var request = MenuRequestSpec.forStore(storeKey("CTL"));
        assertNotNull(
            request,
            "should not get `null` for default menu request spec"
        );
        enforceLocale(
            request,
            Locale.US
        );
        enforceStore(
            request,
            "CTL"
        );
    }

    @Test void testMenuSpecCustomStoreWithLocale() {
        var request = MenuRequestSpec.forStore(
            storeKey("BOH"),
            Locale.FRANCE);
        assertNotNull(
            request,
            "should not get `null` for default menu request spec"
        );
        enforceLocale(
            request,
            Locale.FRANCE
        );
        enforceStore(
            request,
            "BOH"
        );
    }

    @Test void testMenuSpecWithUserLocation() {
        var request = MenuRequestSpec.forUserLocation(UserLocation.newBuilder()
            .setMarket("testing")
            .build());
        enforceLocale(
            request,
            Locale.US
        );
        assertFalse(
            request.getStoreKey().isPresent(),
            "store should not be present by default"
        );
        assertTrue(
            request.getLocation().isPresent(),
            "user location should be present when provided"
        );
        assertEquals(
            "testing",
            request.getLocation().get().getMarket(),
            "market should be expected value when provided"
        );
    }

    @Test void testMenuSpecCustomLocaleWithUserLocation() {
        var request = MenuRequestSpec.forUserLocation(UserLocation.newBuilder()
                .setMarket("testing")
                .build(),
                Locale.FRANCE);
        enforceLocale(
            request,
            Locale.FRANCE
        );
        assertFalse(
            request.getStoreKey().isPresent(),
            "store should not be present by default"
        );
        assertTrue(
            request.getLocation().isPresent(),
            "user location should be present when provided"
        );
        assertEquals(
            "testing",
            request.getLocation().get().getMarket(),
            "market should be expected value when provided"
        );
    }

    @Test void testMenuSpecCustomStoreAndLocaleWithUserLocation() {
        var request = MenuRequestSpec.forUserLocation(UserLocation.newBuilder()
                        .setMarket("testing")
                        .build(),
                Locale.FRANCE,
                storeKey("CTL"));
        enforceLocale(
            request,
            Locale.FRANCE
        );
        enforceStore(
            request,
            "CTL"
        );
        assertTrue(
            request.getStoreKey().isPresent(),
            "store should be present by when provided"
        );
        assertTrue(
                request.getLocation().isPresent(),
                "user location should be present when provided"
        );
        assertEquals(
            "testing",
            request.getLocation().get().getMarket(),
            "market should be expected value when provided"
        );
    }

    @Test void testMenuSpecAnnotatedWithUserAccount() {
        var request = MenuRequestSpec.forUser(
            "abc123",
            Optional.empty()
        );
        enforceLocale(
            request,
            Locale.US
        );
        assertFalse(
            request.getStoreKey().isPresent(),
            "store should not be present by default"
        );
        assertFalse(
            request.getLocation().isPresent(),
            "user location should not be present when omitted"
        );
        assertTrue(
            request.getUserId().isPresent(),
            "user ID should be present when provided"
        );
        assertEquals(
            "abc123",
            request.getUserId().get(),
            "user ID should be copied in as expected"
        );

        var request2 = MenuRequestSpec.forUser(
            "abc123",
            Optional.of(Locale.FRANCE)
        );
        enforceLocale(
            request2,
            Locale.FRANCE
        );
        assertFalse(
            request2.getStoreKey().isPresent(),
            "store should not be present by default"
        );
        assertFalse(
            request2.getLocation().isPresent(),
            "user location should not be present when omitted"
        );
        assertTrue(
            request2.getUserId().isPresent(),
            "user ID should be present when provided"
        );
        assertEquals(
                "abc123",
            request2.getUserId().get(),
            "user ID should be copied in as expected"
        );

        var request3 = MenuRequestSpec.forUser(
            "abc123",
            UserLocation.newBuilder()
                .setMarket("testing")
                .build(),
            Optional.of(Locale.FRANCE)
        );
        enforceLocale(
            request3,
            Locale.FRANCE
        );
        assertFalse(
            request3.getStoreKey().isPresent(),
            "store should not be present by default"
        );
        assertTrue(
            request3.getLocation().isPresent(),
            "user location should be present when provided"
        );
        assertEquals(
            "testing",
            request3.getLocation().get().getMarket(),
            "market for user location should be present when provided"
        );
        assertTrue(
            request3.getUserId().isPresent(),
            "user ID should be present when provided"
        );
        assertEquals(
            "abc123",
            request3.getUserId().get(),
            "user ID should be copied in as expected"
        );

        var request4 = MenuRequestSpec.forUser(
            "abc123",
            UserLocation.newBuilder()
                .setMarket("testing")
                .build(),
            storeKey("CTL"),
            Optional.of(Locale.FRANCE)
        );
        enforceLocale(
            request4,
            Locale.FRANCE
        );
        assertTrue(
            request4.getStoreKey().isPresent(),
            "store should be present when provided"
        );
        assertEquals(
            "CTL",
            request4.getStoreKey().get().getCode(),
            "store code should be present, accurately, when provided"
        );
        assertTrue(
            request4.getLocation().isPresent(),
            "user location should not be present when omitted"
        );
        assertEquals(
            "testing",
            request3.getLocation().get().getMarket(),
            "market for user location should be present when provided"
        );
        assertTrue(
            request3.getUserId().isPresent(),
            "user ID should be present when provided"
        );
        assertEquals(
            "abc123",
            request3.getUserId().get(),
            "user ID should be copied in as expected"
        );
    }

    @Test public void testMenuSpecProductLines() {
        var spec = MenuRequestSpec.defaults();
        assertNotNull(
            spec,
            "should not get `null` from menu request spec defaults factory"
        );
        assertFalse(
            spec.getProductLines().isEmpty(),
            "product lines should not be empty by default"
        );

        var defaultTypes = EnumSet.of(
            ProductLine.APPAREL,
            ProductLine.CBD,
            ProductLine.THC,
            ProductLine.MERCHANDISE
        );

        for (var type : defaultTypes) {
            assertTrue(
                spec.getProductLines().contains(type),
                String.format("default product types should contain %s", type.name())
            );
        }

        var spec2 = MenuRequestSpec
                .defaults()
                .clearProductLines()
                .addProductLines(ProductLine.CBD);

        assertTrue(
            spec2.getProductLines().contains(ProductLine.CBD),
            "custom product lines should reflect added values"
        );
        assertFalse(
            spec2.getProductLines().contains(ProductLine.THC),
            "custom product lines should omit values that aren't provided"
        );
    }

    @Test public void testMenuSpecKeysOnly() {
        var spec = MenuRequestSpec.defaults();
        assertNotNull(
            spec,
            "should not get `null` from menu request spec defaults factory"
        );
        assertFalse(
            spec.isKeysOnly(),
            "request should default to `false` for `keysOnly`"
        );

        var spec2 = MenuRequestSpec.defaults().setKeysOnly(true);
        assertNotNull(
            spec2,
            "should not get `null` from menu request spec defaults factory"
        );
        assertTrue(
            spec2.isKeysOnly(),
            "request should reflect `true` for `keysOnly` when set"
        );
    }

    @Test public void testMenuSpecEquals() {
        var spec = MenuRequestSpec.defaults();
        assertEquals(
            spec,
            spec,
            "two same-object specs should equal each other"
        );
        assertEquals(
            spec.hashCode(),
            spec.hashCode(),
            "hash code value should be stable for a single request spec"
        );

        var spec2 = MenuRequestSpec.defaults();
        assertEquals(
            spec2,
            spec2,
            "two identical specs should equal each other"
        );
        assertEquals(
            spec2.hashCode(),
            spec2.hashCode(),
            "hash code value should be stable for a single request spec"
        );
        assertEquals(
            spec.hashCode(),
            spec2.hashCode(),
            "hash code should not differ for identical specs"
        );

        var spec3 = MenuRequestSpec.defaults().setKeysOnly(true);
        assertEquals(
            spec3,
            spec3,
            "two identical specs should equal each other"
        );
        assertEquals(
            spec3.hashCode(),
            spec3.hashCode(),
            "hash code value should be stable for a single request spec"
        );
        assertNotEquals(
            spec,
            spec3,
            "two different specs should not equal each other"
        );
        assertNotEquals(
            spec2,
            spec3,
            "two different specs should not equal each other"
        );
        assertNotEquals(
            spec.hashCode(),
            spec3.hashCode(),
            "hash code should differ for different specs"
        );
        assertNotEquals(
            spec2.hashCode(),
            spec3.hashCode(),
            "hash code should differ for different specs"
        );
    }
}
