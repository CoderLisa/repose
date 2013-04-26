package org.openrepose.components.apivalidator.filter;

import com.rackspace.com.papi.components.checker.Validator;
import com.rackspace.com.papi.components.checker.step.ErrorResult;
import com.rackspace.com.papi.components.checker.step.MultiFailResult;
import com.rackspace.com.papi.components.checker.step.Result;
import com.rackspace.papi.commons.util.http.HttpStatusCode;
import com.rackspace.papi.commons.util.http.OpenStackServiceHeader;
import com.rackspace.papi.commons.util.http.header.HeaderValue;
import com.rackspace.papi.commons.util.http.header.HeaderValueImpl;
import com.rackspace.papi.commons.util.servlet.http.MutableHttpServletRequest;
import com.rackspace.papi.commons.util.servlet.http.MutableHttpServletResponse;
import com.rackspace.papi.filter.logic.FilterDirector;
import com.rackspace.papi.model.Filter;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(Enclosed.class)
public class ApiValidatorHandlerTest {

    public static class WhenApplyingValidators {
        private ValidatorInfo defaultValidatorInfo;
        private ValidatorInfo role1ValidatorInfo;
        private ValidatorInfo role2ValidatorInfo;
        private Validator defaultValidator;
        private Validator role1Validator;
        private Validator role2Validator;
        private ApiValidatorHandler instance;
        private FilterChain chain;
        private MutableHttpServletRequest request;
        private MutableHttpServletResponse response;
        private ValidatorInfo nullValidatorInfo;
        private ValidatorInfo blowupValidatorInfo;
        private Validator blowupValidator;

        @Before
        public void setup() {
            chain = mock(FilterChain.class);
            request = mock(MutableHttpServletRequest.class);
            response = mock(MutableHttpServletResponse.class);

            defaultValidator = mock(Validator.class);
            defaultValidatorInfo = new ValidatorInfo("defaultrole", "defaultwadl", null, null);
            defaultValidatorInfo.setValidator(defaultValidator);
            
            role1Validator = mock(Validator.class);
            role1ValidatorInfo = new ValidatorInfo("role1", "role1wadl", null, null);
            role1ValidatorInfo.setValidator(role1Validator);
            
            role2Validator = mock(Validator.class);
            role2ValidatorInfo = new ValidatorInfo("role2", "role2wadl", null, null);
            role2ValidatorInfo.setValidator(role2Validator);
            
            nullValidatorInfo = mock(ValidatorInfo.class);
            when(nullValidatorInfo.getRole()).thenReturn("nullValidator");
            when(nullValidatorInfo.getValidator()).thenReturn(null);
            
            blowupValidator = mock(Validator.class);
            when(blowupValidator.validate(request, response, chain)).thenThrow(new RuntimeException("Test"));
            blowupValidatorInfo = new ValidatorInfo("blowupValidator", "blowupWadl", null, null);
            blowupValidatorInfo.setValidator(blowupValidator);

            List<ValidatorInfo> validators = new ArrayList<ValidatorInfo>();
            validators.add(defaultValidatorInfo);
            validators.add(role1ValidatorInfo);
            validators.add(role2ValidatorInfo);
            validators.add(nullValidatorInfo);
            validators.add(blowupValidatorInfo);
            
            instance = new ApiValidatorHandler(defaultValidatorInfo, validators, false);
            instance.setFilterChain(chain);

        }
        
        @Test
        public void shouldCallDefaultValidatorWhenNoRoleMatch() {
            
            instance.handleRequest(request, response);
            verify(defaultValidator).validate(request, response, chain);
        }
        
        @Test
        public void shouldCallValidatorForRole() {
            List<HeaderValue> roles = new ArrayList<HeaderValue>();
            roles.add(new HeaderValueImpl("role1"));
            
            when(request.getPreferredHeaderValues(eq(OpenStackServiceHeader.ROLES.toString()), any(HeaderValueImpl.class))).thenReturn(roles);
            
            instance.handleRequest(request, response);
            verify(role1Validator).validate(request, response, chain);
        }

        @Test
        public void shouldHandleNullValidators() {
            List<HeaderValue> roles = new ArrayList<HeaderValue>();
            roles.add(new HeaderValueImpl("nullValidator"));
            
            when(request.getPreferredHeaderValues(eq(OpenStackServiceHeader.ROLES.toString()), any(HeaderValueImpl.class))).thenReturn(roles);
            FilterDirector director = instance.handleRequest(request, response);
            verify(nullValidatorInfo).getValidator();
            assertEquals(HttpStatusCode.BAD_GATEWAY, director.getResponseStatus());
        }

        @Test
        public void shouldHandleExceptionsInValidators() {
            List<HeaderValue> roles = new ArrayList<HeaderValue>();
            roles.add(new HeaderValueImpl("blowupValidator"));
            when(request.getPreferredHeaderValues(eq(OpenStackServiceHeader.ROLES.toString()), any(HeaderValueImpl.class))).thenReturn(roles);
            
            FilterDirector director = instance.handleRequest(request, response);
            verify(blowupValidator).validate(request, response, chain);
            assertEquals(HttpStatusCode.BAD_GATEWAY, director.getResponseStatus());
        }

        @Test
        public void shouldProvideHighestPrivilegeErrorMessageWhenMultiRoleMatch() {
            List<ValidatorInfo> validators = new ArrayList<ValidatorInfo>();
            validators.add(role1ValidatorInfo);
            validators.add(role2ValidatorInfo);
            validators.add(defaultValidatorInfo);

            instance = new ApiValidatorHandler(defaultValidatorInfo, validators, true);
            instance.setFilterChain(chain);

            List<HeaderValue> roles = new ArrayList<HeaderValue>();
            roles.add(new HeaderValueImpl("defaultrole"));
            roles.add(new HeaderValueImpl("role1"));
            roles.add(new HeaderValueImpl("role2"));

            ErrorResult result = mock(ErrorResult.class);
            when(result.valid()).thenReturn(false);
            when(result.message()).thenReturn("DEFAULT");
            when(result.code()).thenReturn(400);

            ErrorResult resultRole1 = mock(ErrorResult.class);
            when(resultRole1.valid()).thenReturn(false);
            when(resultRole1.message()).thenReturn("ROLE1");
            when(resultRole1.code()).thenReturn(401);

            ErrorResult resultRole2 = mock(ErrorResult.class);
            when(resultRole2.valid()).thenReturn(false);
            when(resultRole2.message()).thenReturn("ROLE2");
            when(resultRole2.code()).thenReturn(402);

            when(defaultValidator.validate(any(HttpServletRequest.class), any(HttpServletResponse.class), any(FilterChain.class))).thenReturn(result);
            when(role1Validator.validate(any(HttpServletRequest.class), any(HttpServletResponse.class), any(FilterChain.class))).thenReturn(resultRole1);
            when(role2Validator.validate(any(HttpServletRequest.class), any(HttpServletResponse.class), any(FilterChain.class))).thenReturn(resultRole2);

            when(request.getPreferredHeaderValues(eq(OpenStackServiceHeader.ROLES.toString()), any(HeaderValueImpl.class))).thenReturn(roles);

            FilterDirector director = instance.handleRequest(request, response);

            assertEquals(402, director.getResponseStatusCode());
            assertEquals("ROLE2", director.getResponseMessageBody());
        }



    }
}
