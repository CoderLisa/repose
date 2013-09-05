package com.rackspace.auth.openstack;

import com.rackspace.auth.AuthGroups;
import com.rackspace.auth.AuthToken;
import org.openstack.docs.identity.api.v2.Endpoint;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Simple implementation of the AuthenticationServiceWrapper that uses Java Futures and
 * locally caches recently obtained auth tokens
 */
public class AuthenticationServiceClientWrapper implements AuthenticationService {

    private AuthenticationService authenticationService;
    private Map<String, Future<AuthToken>> authTokenFutures = Collections.synchronizedMap(new HashMap());
    private Map<String, Future<AuthGroups>> authGroupsFutures = Collections.synchronizedMap(new HashMap());
    private Map<String, Future<String>> endpointFutures = Collections.synchronizedMap(new HashMap());

    private ExecutorService executorService;

    private int maxThreads = 10;

    public AuthenticationServiceClientWrapper(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
        executorService = Executors.newFixedThreadPool(maxThreads);

    }

    @Override
    public AuthToken validateToken(String tenant, String userToken) {
//        Future<AuthToken> future = authTokenFutures.get(userToken);
//        if (future == null) {
//            future = executorService.submit(new GetToken(tenant, userToken));
//            authTokenFutures.put(userToken, future);
//        }
//        AuthToken authToken = null;
//        try {
//            authToken = future.get(5, TimeUnit.SECONDS);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        } catch (ExecutionException e) {
//            e.printStackTrace();
//        } catch (TimeoutException e) {
//
//        }

        return authenticationService.validateToken(tenant, userToken);
    }

    @Override
    public List<Endpoint> getEndpointsForToken(String userToken) {
        return authenticationService.getEndpointsForToken(userToken);
    }

    @Override
    public AuthGroups getGroups(String userId) {
        return authenticationService.getGroups(userId);
    }

    @Override
    public String getBase64EndpointsStringForHeaders(String userToken, String format) {
        return authenticationService.getBase64EndpointsStringForHeaders(userToken, format);
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
