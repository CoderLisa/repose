package framework

class ReposeGlassfishLauncher extends AbstractReposeLauncher {

    def int shutdownPort

    def ReposeConfigurationProvider configurationProvider
    String glassfishJar

    ReposeGlassfishLauncher(String glassfishJar) {
        this.configurationProvider = configurationProvider
        this.glassfishJar = glassfishJar
    }

    @Override
    void start() {

        def cmd = "java -jar ${glassfishJar} -s ${shutdownPort}"
        if (!connFramework.isEmpty()) {
            cmd = cmd + " -cf ${connFramework}"
        }
        cmd = cmd + " start"
        println("Starting repose: ${cmd}")

        def th = new Thread({ cmd.execute() });

        th.run()
        th.join()
    }

    @Override
    void stop() {
        def cmd = "java -jar ${glassfishJar} -s ${shutdownPort} stop"
        println("Stopping Glassfish: ${cmd}")
    }
}
