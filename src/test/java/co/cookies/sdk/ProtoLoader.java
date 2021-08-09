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


import com.google.protobuf.Message;
import com.google.protobuf.TextFormat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


/** Utility functions to load up text proto objects. */
public final class ProtoLoader {
    /**
     * Load a Protobuf object from some text source, expressed the text format defined as part of the Protocol Buffers
     * project and spec.
     *
     * @param builder Builder for the record we expect from this parsing.
     * @param source Source for the text content to parse.
     * @param <B> Builder type to build against.
     * @param <M> Message type expected from the builder.
     * @return Message instance pre-built from the builder.
     */
    public static <B extends Message.Builder, M extends Message> M loadText(
            B builder,
            InputStream source) {
        // open up the provided source, and merge it with the builder
        try (var buf = new BufferedReader(new InputStreamReader(source))) {
            TextFormat.merge(buf, builder);
            //noinspection unchecked
            return (M)builder.build();
        } catch (IOException ioe) {
            // this is only used in tests. just rethrow it.
            throw new RuntimeException(ioe);
        }
    }

    /**
     * Sugar for {@link #loadText(Message.Builder, InputStream)} which pulls a file off the classpath.
     *
     * @param builder Builder for the record we expect from this parsing.
     * @param file Name of the file to pull from the classpath.
     * @param <B> Builder type to build against.
     * @param <M> Message type expected from the builder.
     * @return Message instance pre-built from the builder.
     */
    public static <B extends Message.Builder, M extends Message> M loadTextFile(B builder, String file) {
        return loadText(
            builder,
            ProtoLoader.class.getResourceAsStream(file)
        );
    }
}
