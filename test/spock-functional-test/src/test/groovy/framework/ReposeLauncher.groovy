package framework

interface ReposeLauncher {

    void start(boolean waitOnStart, int waitTimeout);

    void stop();

    void applyConfigs(String[] configLocations)

    void updateConfigs(String[] configLocations)

    void addToClassPath(String path)
}
