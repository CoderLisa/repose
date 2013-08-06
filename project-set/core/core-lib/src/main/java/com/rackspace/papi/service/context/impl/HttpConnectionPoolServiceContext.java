package com.rackspace.papi.service.context.impl;

import com.rackspace.papi.commons.config.manager.UpdateListener;
import com.rackspace.papi.service.ServiceRegistry;
import com.rackspace.papi.service.config.ConfigurationService;
import com.rackspace.papi.service.context.ServiceContext;
import com.rackspace.papi.service.reporting.metrics.MetricsService;
import com.rackspace.papi.service.reporting.metrics.config.GraphiteServer;
import com.rackspace.papi.service.reporting.metrics.config.MetricsConfiguration;
import com.rackspace.repose.service.httpconnectionpool.HttpConnectionPoolService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.servlet.ServletContextEvent;
import java.io.IOException;
import java.net.URL;

/**
 * Manages the {@link com.rackspace.repose.service.httpconnectionpool} instance and subscribes to the
 * http-connection-pool.cfg.xml configuration file.
 */
@Component("httpConnectionPoolServiceContext")
public class HttpConnectionPoolServiceContext implements ServiceContext<HttpConnectionPoolService> {

    public static final String SERVICE_NAME = "HttpConnectionPoolService";
    public static final String DEFAULT_CONFIG_NAME = "http-connection-pool.cfg.xml";

    private static final Logger LOG = LoggerFactory.getLogger(HttpConnectionPoolService.class);
    private static final String prefix = "Error with the HttpConnectionPoolService";


    private final HttpConnectionPoolService connectionPoolService;
    private final ServiceRegistry registry;
    private final ConfigurationService configurationService;
    private final ConfigurationListener configurationListener;

    @Autowired
    public HttpConnectionPoolServiceContext(@Qualifier("serviceRegistry") ServiceRegistry registry,
                                 @Qualifier("configurationManager") ConfigurationService configurationService,
                                 @Qualifier("connectionPoolService") HttpConnectionPoolService connectionPoolService) {

        this.registry = registry;
        this.configurationService = configurationService;
        this.connectionPoolService = connectionPoolService;
        configurationListener = new ConfigurationListener();
    }

    private void register() {
        if (registry != null) {
            registry.addService(this);
        }
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }

    @Override
    public HttpConnectionPoolService getService() {
        return connectionPoolService;
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {

        URL xsdURL = getClass().getResource("/META-INF/schema/config/http-connection-pool.xsd");
        configurationService.subscribeTo(DEFAULT_CONFIG_NAME, xsdURL, configurationListener, MetricsConfiguration.class);

        register();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {

        connectionPoolService.destroy();
        configurationService.unsubscribeFrom(DEFAULT_CONFIG_NAME, configurationListener);
    }

    private class ConfigurationListener implements UpdateListener<MetricsConfiguration> {

        private boolean initialized = false;

        @Override
        public void configurationUpdated(MetricsConfiguration metricsC) {

            // we are reinitializing the graphite servers
            connectionPoolService.shutdownGraphite();

            if (metricsC.getGraphite() != null) {

                try {

                    for (GraphiteServer gs : metricsC.getGraphite().getServer()) {

                        connectionPoolService.addGraphiteServer(gs.getHost(),
                                gs.getPort().intValue(),
                                gs.getPeriod(),
                                gs.getPrefix());
                    }
                } catch (IOException e) {

                    LOG.debug(prefix, e);
                }
            }

            initialized = true;
        }

        @Override
        public boolean isInitialized() {
            return initialized;
        }
    }
}