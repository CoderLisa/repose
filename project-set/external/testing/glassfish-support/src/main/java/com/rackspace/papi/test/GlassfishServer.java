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

        final CmdLineParser cmdLineParser = new CmdLineParser(commandLineArgs);

        try {
            cmdLineParser.parseArgument(args);
        } catch (CmdLineException e) {

            displayUsage(cmdLineParser, e);
            return;
        }

        if (!validPorts(commandLineArgs)) {
            return;
        }

        try{
            validateConfigDirectory(commandLineArgs);
        }catch(IOException e){
            System.err.println(e.getMessage());
            cmdLineParser.printUsage(System.err);
            return;
        }

        final PowerApiValveServerControl serverControl = new PowerApiValveServerControl(commandLineArgs);

        if (commandLineArgs.getAction().equalsIgnoreCase(CommandLineArguments.ACTION_START)) {
            serverControl.startPowerApiValve();
        }
        if (commandLineArgs.getAction().equalsIgnoreCase(CommandLineArguments.ACTION_STOP)) {
            serverControl.stopPowerApiValve();
        }

        EmbeddedFileSystem.Builder builder = new EmbeddedFileSystem.Builder();

        builder.autoDelete(true);


        server = builder.
    }

}
