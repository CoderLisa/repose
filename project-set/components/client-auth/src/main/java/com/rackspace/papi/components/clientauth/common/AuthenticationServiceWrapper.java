package com.rackspace.papi.components.clientauth.common;

import com.rackspace.auth.AuthGroups;
import com.rackspace.auth.AuthToken;
import org.openstack.docs.identity.api.v2.Endpoint;

import java.util.List;
import java.util.concurrent.Future;

/**
 * Rename me
 */
public interface AuthenticationServiceWrapper {

    Future<AuthToken> validateToken(String tenant, String userToken);

    Future<AuthGroups> getGroups(String userId);

    Future<String> getBase64EndpointsStringForHeaders(String userToken, String format);

}
