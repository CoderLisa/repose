package com.rackspace.papi.components.clientauth.openstack.v1_0;

import com.rackspace.auth.AuthGroups;
import com.rackspace.auth.AuthToken;
import com.rackspace.auth.openstack.AuthenticationService;
import com.rackspace.papi.components.clientauth.common.AuthenticationServiceWrapper;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Simple implementation of the AuthenticationServiceWrapper that uses Java Futures and
 * locally caches recently obtained auth tokens
 */
public class AuthenticationServiceWrapperImpl implements AuthenticationServiceWrapper {

    private AuthenticationService authenticationService;
    private Map<String, Future<AuthToken>> authTokenFutures = Collections.synchronizedMap(new HashMap());
    private Map<String, Future<AuthGroups>> authGroupsFutures = Collections.synchronizedMap(new HashMap());
    private Map<String, Future<String>> endpointFutures = Collections.synchronizedMap(new HashMap());

    private ExecutorService executorService;

    private int maxThreads = 10;

    public AuthenticationServiceWrapperImpl(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
        executorService = Executors.newFixedThreadPool(maxThreads);

    }

    @Override
    public Future<AuthToken> validateToken(String tenant, String userToken) {
        Future<AuthToken> future = authTokenFutures.get(userToken);
        if (future != null) {
            return future;
        }
        future = executorService.submit(new GetToken(tenant, userToken));
        authTokenFutures.put(tenant, future);
        return future;
    }

    @Override
    public Future<AuthGroups> getGroups(String userId) {
        Future<AuthGroups> future = authGroupsFutures.get(userId);
        if (future != null) {
            return future;
        }
        future = executorService.submit(new GetGroups(userId));
        authGroupsFutures.put(userId, future);
        return future;
    }

    @Override
    public Future<String> getBase64EndpointsStringForHeaders(String userToken, String format) {
        Future<String> future = endpointFutures.get(userToken);
        if (future != null) {
            return future;
        }
        future = executorService.submit(new GetEndpoints(userToken, format));
        endpointFutures.put(userToken, future);
        return future;
    }


    private class GetToken implements Callable<AuthToken> {

        private final String tenant;
        private final String userToken;

        public GetToken(String tenant, String userToken) {
            this.tenant = tenant;
            this.userToken = userToken;
        }


        @Override
        public AuthToken call() throws Exception {
            return authenticationService.validateToken(tenant, userToken);
        }
    }

    private class GetGroups implements Callable<AuthGroups> {
        private final String userId;

        public GetGroups(String userId) {
            this.userId = userId;
        }

        @Override
        public AuthGroups call() throws Exception {
            return authenticationService.getGroups(userId);
        }
    }

    private class GetEndpoints implements Callable<String> {
        private final String userToken;
        private final String format;

        public GetEndpoints(String userToken, String format) {
            this.userToken = userToken;
            this.format = format;
        }

        @Override
        public String call() throws Exception {
            return authenticationService.getBase64EndpointsStringForHeaders(userToken, format);
        }
    }
}
