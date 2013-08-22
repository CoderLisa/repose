package framework

import framework.client.jmx.JmxClient
import org.linkedin.util.clock.SystemClock

import static org.junit.Assert.fail
import static org.linkedin.groovy.util.concurrent.GroovyConcurrentUtils.waitForCondition

class ReposeGlassfishLauncher extends AbstractReposeLauncher {

    def clock = new SystemClock()
    def int shutdownPort
    def int debugPort = 8012

    def ReposeConfigurationProvider configurationProvider

    ReposeGlassfishLauncher(ReposeConfigurationProvider configurationProvider,
                        String reposeJar,
                        String reposeEndpoint,
                        String configDir,
                        int reposePort,
                        int shutdownPort,
                        String connFramework="jersey") {
        this.configurationProvider = configurationProvider
        this.reposeEndpoint = reposeEndpoint
        this.shutdownPort = shutdownPort
        this.configDir = configDir
    }

    @Override
    void start() {

        waitForCondition(clock, '5s', '1s', {
            killIfUp()
            !isUp()
        })

        def jmxprops = ""
        def debugProps = ""
        def classPath = ""

        if (debugEnabled) {
            debugProps = "-Xdebug -Xrunjdwp:transport=dt_socket,address=${debugPort},server=y,suspend=n"
        }

        int jmxPort = nextAvailablePort()
        jmxprops = "-Dspock=spocktest -Dcom.sun.management.jmxremote.port=${jmxPort} -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.local.only=true"

        if(!classPaths.isEmpty()){
            classPath = "-cp " + (classPaths as Set).join(";")

        }

        def cmd = "java ${classPath} ${debugProps} ${jmxprops} -jar ${reposeJar} -s ${shutdownPort} -c ${configDir}"
        if (!connFramework.isEmpty()) {
            cmd = cmd + " -cf ${connFramework}"
        }
        cmd = cmd + " start"
        println("Starting repose: ${cmd}")

        def th = new Thread({ cmd.execute() });

        th.run()
        th.join()

        def jmxUrl = "service:jmx:rmi:///jndi/rmi://localhost:${jmxPort}/jmxrmi"

        waitForCondition(clock, '60s', '1s') {
            connectViaJmxRemote(jmxUrl)
        }

        print("Waiting for repose to start")
        waitForCondition(clock, '60s', '1s', {
            isFilterChainInitialized()
        })

        // TODO: improve on this.  embedding a sleep for now, but how can we ensure Repose is up and
        // ready to receive requests without actually sending a request through (skews the metrics if we do)
        //sleep(10000)
    }


    @Override
    void stop() {
        def cmd = "java -jar ${reposeJar} -s ${shutdownPort} stop"
        println("Stopping repose: ${cmd}")

        cmd.execute();
        waitForCondition(clock, '45s', '1s', {
            !isUp()
        })
    }
}
