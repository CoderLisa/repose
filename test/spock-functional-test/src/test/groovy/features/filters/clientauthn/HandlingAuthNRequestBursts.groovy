package features.filters.clientauthn

import framework.ReposeValveTest
import org.apache.commons.lang.RandomStringUtils
import org.joda.time.DateTime
import org.joda.time.Period
import org.rackspace.gdeproxy.Deproxy
import org.rackspace.gdeproxy.MessageChain
import org.rackspace.gdeproxy.Request
import org.rackspace.gdeproxy.Response

import java.util.concurrent.ConcurrentHashMap

/**
 * This test is demonstrating that Repose behaves nicely when handling bursts of requests from a single auth
 * token that requires
 */
class HandlingAuthNRequestBursts extends ReposeValveTest {
    static DateTime lastCall
    static def identityEndpoint
    static def IdentityServiceResponseSimulator fauxIdentityService

    def setupSpec() {

        repose.applyConfigs("features/filters/clientauthn/cacheoffset")
        repose.start()
        deproxy = new Deproxy()
        deproxy.addEndpoint(properties.getProperty("target.port").toInteger())

        def clientToken = UUID.randomUUID().toString()
        fauxIdentityService = new IdentityServiceResponseSimulator()
        fauxIdentityService.client_token = clientToken
        fauxIdentityService.tokenExpiresAt = (new DateTime()).plusMinutes(1);


        def Closure rateLimitingHandler = { Request request ->

            if (! request.path.contains("tokens/"+clientToken)) {
                def response = fauxIdentityService.handler(request)
                return response
            }

            if (lastCall != null && lastCall.plusMinutes(1).isAfterNow()) {
                return new Response(413, 'ENTITY TOO LARGE')
            } else {
                lastCall = DateTime.now()
            }
            def response = fauxIdentityService.handler(request)
            return response
        }

        identityEndpoint = deproxy.addEndpoint(properties.getProperty("identity.port").toInteger(),
                'identity service', null, rateLimitingHandler)
    }

    def cleanupSpec() {
        deproxy.shutdown()
        repose.stop()
    }

    def "when a single client sends a burst of requests, repose should handle all requests with no 500s"() {

        when: "A user with an auth token submits a burst of requests"

        List<Thread> clientThreads = new ArrayList<Thread>()
        def token = fauxIdentityService.client_token
        Map<String,MessageChain> messageChainList = new ConcurrentHashMap<String, MessageChain>()

        for (int threadNumber in 1..50) {
            def Thread thread = Thread.start {
                def threadName = "Call_" + threadNumber
                try {
                    MessageChain mc = deproxy.makeRequest(reposeEndpoint, 'GET', ['X-Auth-Token': token, 'TEST_THREAD': threadName])
                    messageChainList.put(threadName, mc)
                } catch (Exception ex) {
                    // do nothing
                }
            }
            clientThreads.add(thread)
        }
        clientThreads*.join()

        Collection<String> codes = messageChainList.values().receivedResponse.code

        then:
        clientThreads.size() == 50
        codes.contains("500") == false
    }

}
