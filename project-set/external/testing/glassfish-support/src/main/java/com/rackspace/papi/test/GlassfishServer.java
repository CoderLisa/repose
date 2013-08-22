package com.rackspace.papi.test;


import org.glassfish.internal.embedded.EmbeddedFileSystem;
import org.glassfish.internal.embedded.Server;

import java.io.IOException;

/**
 * Create an embedded glassfish server
 */
public class GlassfishServer {

    static Server server;

    public static void main(String[] args) {

        EmbeddedFileSystem.Builder builder = new EmbeddedFileSystem.Builder();

        builder.autoDelete(true);
    }

}
