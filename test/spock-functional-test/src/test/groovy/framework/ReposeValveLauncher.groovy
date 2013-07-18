package framework

import framework.client.http.SimpleHttpClient
import framework.client.http.SimpleHttpResponse
import framework.client.jmx.JmxClient
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpUriRequest
import org.linkedin.util.clock.SystemClock
import sun.net.www.http.HttpClient

import static org.junit.Assert.fail
import static org.linkedin.groovy.util.concurrent.GroovyConcurrentUtils.waitForCondition

class ReposeValveLauncher implements ReposeLauncher {

    def boolean debugEnabled
    def boolean waitOnStart
    def String reposeJar
    def String configDir

    def clock = new SystemClock()

    def reposeEndpoint
    def int shutdownPort
    def int reposePort

    def JmxClient jmx
    int jmxPort

    def int debugPort = 8011
    def classPaths =[]

    def ReposeConfigurationProvider configurationProvider

    ReposeValveLauncher(ReposeConfigurationProvider configurationProvider,
                        String reposeJar,
                        String reposeEndpoint,
                        String configDir,
                        int reposePort,
                        int shutdownPort,
                        boolean debugEnabled = true
    ) {
        this.configurationProvider = configurationProvider
        this.reposeJar = reposeJar
        this.reposeEndpoint = reposeEndpoint
        this.shutdownPort = shutdownPort
        this.configDir = configDir
        this.debugEnabled = debugEnabled
    }

    @Override
    void applyConfigs(String[] configLocations) {
        configurationProvider.applyConfigs(configLocations)
    }

    @Override
    void updateConfigs(String[] configLocations) {
        configurationProvider.updateConfigs(configLocations)
    }

    @Override
    void start(boolean waitOnStart = true, int waitTimeout = 30) {

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
        jmxprops = "-Dcom.sun.management.jmxremote.port=${jmxPort} -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.local.only=true"

        if(!classPaths.isEmpty()){
            classPath = "-cp " + (classPaths as Set).join(";")

        }

        def cmd = "java ${classPath} ${debugProps} ${jmxprops} -jar ${reposeJar} -s ${shutdownPort} -c ${configDir} start"
        println("Starting repose: ${cmd}")

        def th = new Thread({ cmd.execute() });

        th.run()
        th.join()

        if (waitOnStart) {
            getWaitTilNon500(reposeEndpoint, waitTimeout)
        }
    }

    def getWaitTilNon500(String endpoint = reposeEndpoint, int waitTimeout = 30) {
        SimpleHttpClient httpClient = new SimpleHttpClient(endpoint)

        waitForCondition(clock, "${waitTimeout}s", '1s') {
            SimpleHttpResponse response = httpClient.doGet()
            response.statusCode != 500
        }
    }

    def waitTilFiltersInitialized() {
        def jmxUrl = "service:jmx:rmi:///jndi/rmi://localhost:${jmxPort}/jmxrmi"

        waitForCondition(clock, '60s', '1s') {
            connectViaJmxRemote(jmxUrl)
        }

        print("Waiting for repose to start")
        waitForCondition(clock, '60s', '1s', {
            isFilterChainInitialized()
        })
    }

    def nextAvailablePort() {

        def socket
        int port
        try {
            socket = new ServerSocket(0);
            port = socket.getLocalPort()
        } catch (IOException e) {
            fail("Failed to find an open port")
        } finally {
            if (socket != null && !socket.isClosed()) {
                socket.close()
            }
        }
        return port
    }

    def connectViaJmxRemote(jmxUrl) {
        try {
            jmx = new JmxClient(jmxUrl)
            return true
        } catch (Exception ex) {
            return false
        }
    }


    @Override
    void stop() {
        def cmd = "java -jar ${reposeJar} -s ${shutdownPort} stop"
        println("Stopping repose: ${cmd}")

        cmd.execute();
        waitForCondition(clock, '25s', '1s', {
            !isUp()
        })
    }

    @Override
    void addToClassPath(String path){
        classPaths.add(path)
    }

     /**
     * TODO: introspect the system model for expected filters in filter chain and validate that they
     * are all present and accounted for
     * @return
     */
    private boolean isFilterChainInitialized() {
        print('.')

        // First query for the mbean.  The name of the mbean is partially configurable, so search for a match.
        def HashSet cfgBean = jmx.getMBeans("*com.rackspace.papi.jmx:type=ConfigurationInformation")
        if (cfgBean == null || cfgBean.isEmpty()) {
            return false
        }

        def String beanName = cfgBean.iterator().next().name.toString()

        def ArrayList filterchain = jmx.getMBeanAttribute(beanName, "FilterChain")


        if (filterchain == null || filterchain.size() == 0) {
            return beanName.contains("nofilters")
        }

        def initialized = true

        filterchain.each { data ->
            if (data."successfully initialized" == false) {
                initialized = false
            }
        }

        return initialized

    }

    private String getJvmProcesses() {
        def runningJvms = "jps".execute()
        runningJvms.waitFor()

        return runningJvms.in.text
    }

    public boolean isUp() {
        return getJvmProcesses().contains("repose-valve.jar")
    }

    private void killIfUp() {
        String processes = getJvmProcesses()
        def regex = /(\d*) repose-valve.jar/
        def matcher = (processes =~ regex)
        if (matcher.size() > 0) {
            String pid = matcher[0][1]

            if (!pid.isEmpty()) {
                println("Killing running repose-valve process: " + pid)
                Runtime rt = Runtime.getRuntime();
                if (System.getProperty("os.name").toLowerCase().indexOf("windows") > -1)
                    rt.exec("taskkill " + pid.toInteger());
                else
                    rt.exec("kill -9 " + pid.toInteger());
            }
        }
    }
}
