package features.filters.apivalidator

import framework.ReposeValveTest
import org.gdeproxy.GDeproxy
import spock.lang.Unroll

class TestSspnn extends ReposeValveTest {

    GDeproxy deproxy
    String resourceUrl

    def setupSpec() {
        repose.applyConfigs("features/filters/apivalidator/common", "features/filters/apivalidator/multimatch/f4f4pf5f5")
        repose.start()

        deproxy = new GDeproxy(repose.getReposeEndpoint())
        resourceUrl = repose.getReposeEndpoint() + "/resource"
    }

    def cleanupSpec() {
        repose.stop()
    }

    // the following test_ * methods check how it responds to multiple roles in
    // different orders
    @Unroll("test #testName should pass for roles #roles")
    def "when multimatch is true and role matches passing validator, should send request to origin"() {
        when:
        def mc = deproxy.doGet(resourceUrl, ['X-Roles': roles])

        then:
        mc.receivedResponse.code == '200'
        mc.handlings.size == 1

        where:
        testName             | roles
        'sspnn'              | 'role-3'
        'pass_first_of_two'  | 'role-3,role-4'
        'pass_second_of_two' | 'role-4,role-3'
    }

    @Unroll("test #testName should fail for roles #roles")
    def "should not send request to origin"() {
        when:
        def mc = deproxy.doGet(resourceUrl, ['X-Roles': roles])

        then:
        mc.receivedResponse.code == '404'
        mc.handlings.size == 0

        where:
        testName             | roles
        'fail_first_of_two'  | 'role-2,role-3'
        'fail_second_of_two' | 'role-3,role-2'
    }
}
