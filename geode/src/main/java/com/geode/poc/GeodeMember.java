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
        String logFile = System.getProperty("gemfire.log-file", "server.log");
        String jmxPort = System.getProperty("gemfire.jmx-manager-port", "1099");
        ServerLauncher serverLauncher  = new ServerLauncher.Builder()
                .setMemberName(serverName)
                .setServerPort(serverPort)
//                .set("jmx-manager-port", jmxPort)
//                .set("jmx-manager", "true")
//                .set("jmx-manager-start", "true")
                .setServerBindAddress(bindAddress)
                .set("cache-xml-file", cacheXMLPath)
                .set("locators", locators)
                .set("off-heap-memory-size", offHeapMemorySize)
                .set("log-file", logFile)
                .setCriticalOffHeapPercentage(Float.valueOf(criticalOffHeapPercentage))
                .setEvictionOffHeapPercentage(Float.valueOf(evictionOffHeapPercentage))
                .build();


        try
        {
            serverLauncher.start();
            System.out.println("Cache server successfully started");
            Object lock = new Object();
            synchronized (lock)
            {
                while (true)
                {
                    lock.wait();
                }
            }
        } catch (InterruptedException ex) {
        }

    }
}
