package features.services.datastore

import framework.ReposeGlassfishLauncher
import framework.ReposeTest


class DistDatastoreGlassfishTest extends ReposeTest {

    def setupSpec() {
    }


    def "should startup in glassfish"() {
        given:

        when:
        repose.start()

        then:
        1 == 1

    }


}
