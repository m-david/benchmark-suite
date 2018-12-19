package com.geode.poc;

import org.apache.geode.distributed.ServerLauncher;

public class GeodeMember
{
    public static void main(String[] args)
    {
        String serverName = System.getProperty("gemfire.name", "server_" + Thread.currentThread().getName());
        int serverPort = Integer.valueOf(System.getProperty("gemfire.server-port", "40405"));
        String bindAddress = System.getProperty("gemfire.bind-address", "127.0.0.1");
        String cacheXMLPath = System.getProperty("gemfire.cache-xml-file", "geode-server.xml");
        String locators = System.getProperty("gemfire.locators", "localhost[10080]");
        String offHeapMemorySize = System.getProperty("gemfire.off-heap-memory-size", "128M");
        String criticalOffHeapPercentage = System.getProperty("gemfire.critical-off-heap-percentage", "90");
        String evictionOffHeapPercentage = System.getProperty("gemfire.eviction-off-heap-percentage", "80");

//        String maxHeap = System.getProperty("gemfire.max-heap", "256M");
//        String initialHeap = System.getProperty("gemfire.initial-heap", "256M");
//        String appHomeDir = System.getProperty("gemfire.dir", "./working");

        ServerLauncher serverLauncher  = new ServerLauncher.Builder()
                .setMemberName(serverName)
                .setServerPort(serverPort)
                .set("jmx-manager", "true")
                .set("jmx-manager-start", "true")
                .setServerBindAddress(bindAddress)
                .set("cache-xml-file", cacheXMLPath)
                .set("locators", locators)
                .set("off-heap-memory-size", offHeapMemorySize)
                .setCriticalOffHeapPercentage(Float.valueOf(criticalOffHeapPercentage))
                .setEvictionOffHeapPercentage(Float.valueOf(evictionOffHeapPercentage))
//                .set("deploy-working-dir", appHomeDir)
//                .set("critical-off-heap-percentage", criticalOffHeapPercentage)
//                .set("eviction-off-heap-percentage", evictionOffHeapPercentage)
//                .set("max-heap", maxHeap)
//                .set("initial-heap", initialHeap)
                .build();

        serverLauncher.start();

        System.out.println("Cache server successfully started");

    }

    /**
     --dir=$APP_HOME/server1 --locators=10.212.1.116[10680] \
     --classpath=$CLASS_PATH \
     --properties-file=$APP_HOME/conf/geode-server.properties \
     --cache-xml-file=$APP_HOME/conf/geode-server.xml \
     --server-port=40405 \
     --bind-address=10.212.1.117 \
     --name=server1 \
     --initial-heap=512M \
     --max-heap=512M \
     --off-heap-memory-size=512M \
     --critical-off-heap-percentage=90 --eviction-off-heap-percentage=80
     */
}
