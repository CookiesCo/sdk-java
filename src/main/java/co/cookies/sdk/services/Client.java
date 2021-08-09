package co.cookies.sdk.services;

import java.lang.annotation.*;


/** Specifies basic service information, including the name and version. */
@Documented
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.TYPE})
public @interface Client {
    /** @return Assigned short-name to a given API service client. */
    String name();

    /** @return API version for a given API service client. */
    String version();
}
