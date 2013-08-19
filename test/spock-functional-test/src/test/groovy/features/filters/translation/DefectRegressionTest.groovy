package features.filters.translation
import framework.ReposeValveTest
import org.rackspace.gdeproxy.Deproxy
import org.rackspace.gdeproxy.Handling
import org.rackspace.gdeproxy.MessageChain

/**
 * Regression test for all known Translation defects
 */
class DefectRegressionTest extends ReposeValveTest {

    def setupSpec() {

        repose.applyConfigs(
                "features/filters/translation/common",
                "features/filters/translation/defects/400onGets"
        )
        repose.start()

        deproxy = new Deproxy()
        deproxy.addEndpoint(properties.getProperty("target.port").toInteger())
    }

    def cleanupSpec() {
        deproxy.shutdown()
        repose.stop()
    }

    def "when sending Content-Type on GET request with no body, translation should not fail"() {

        when: "User sends requests through repose"
        MessageChain resp = deproxy.makeRequest(url: reposeEndpoint, method:"GET", headers:['Content-Type':'application/json', 'x-rax-roles':'jawsome'])

        then: "Response code should be 200"
        resp.receivedResponse.code == "200"
    }

    def "when sending Content-Type on GET request with a body, translation should not fail"() {

        when: "User sends requests through repose"
        MessageChain resp = deproxy.makeRequest(url: reposeEndpoint, method:"GET", headers:['Content-Type':'application/xml', 'x-rax-roles':'jawsome'], requestBody:"<foo></foo>")

        then: "Response code should be 200"
        resp.receivedResponse.code == "200"
        Handling sentRequest = ((MessageChain) resp).getHandlings()[0]
        sentRequest.getRequest().headers.contains("x-roles")
    }

    def "when sending GET request with no body, translation should not fail"() {

        when: "User sends requests through repose"
        MessageChain resp = deproxy.makeRequest(url: reposeEndpoint, method:"GET", headers:['x-rax-roles':'jawsome'])

        then: "Response code should be 200"
        resp.receivedResponse.code == "200"
        Handling sentRequest = ((MessageChain) resp).getHandlings()[0]
        sentRequest.getRequest().headers.contains("x-roles")
    }


}
