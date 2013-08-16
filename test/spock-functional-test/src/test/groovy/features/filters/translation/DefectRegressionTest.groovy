package features.filters.translation
import framework.ReposeValveTest
import org.rackspace.gdeproxy.Deproxy
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
        MessageChain resp = deproxy.makeRequest(url: reposeEndpoint, method:"GET", headers:['Content-Type':'application/json'])

        then: "Response code should be 200"
        resp.receivedResponse.code == "200"

    }



}
