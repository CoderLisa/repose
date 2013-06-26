package features

import framework.ReposeValveTest
import org.rackspace.gdeproxy.Deproxy

class MultiNodeInOneProcessTest extends ReposeValveTest {


    def cleanup() {
        if (repose)
            repose.stop()
    }


    def "multiple nodes with versioning should work"() {
        given:
        repose.applyConfigs("features/multinode")

        when:
        repose.start()

        then:
        repose.isFilterChainInitialized() == true
    }


}
