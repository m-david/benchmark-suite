package com.geode.poc;

import org.apache.geode.distributed.LocatorLauncher;
import org.apache.geode.distributed.ServerLauncher;

public class GeodeLocator
{
    public static void main(String[] args)
    {
        String serverName = System.getProperty("gemfire.name", "locator_" + Thread.currentThread().getName());
        int locatorPort = Integer.valueOf(System.getProperty("gemfire.locator-port", "10680"));
        String bindAddress = System.getProperty("gemfire.bind-address", "127.0.0.1");
        String locators = System.getProperty("gemfire.locators", "127.0.0.1[10680]");

        String logFile = System.getProperty("gemfire.log-file", "locator.log");

        String jmxPort = System.getProperty("gemfire.jmx-manager-port", "1099");
        String jmxHttpPort = System.getProperty("gemfire.jmx-manager-http-port", "8099");

//        String locator = String.format("%s[%d]", bindAddress, locatorPort);
        LocatorLauncher locatorLauncher = new LocatorLauncher.Builder()
                .setMemberName(serverName)
                .setPort(locatorPort)
                .setBindAddress(bindAddress)
                .set("locators", locators)
                .set("log-file", logFile)
                .set("mcast-port", "0")
                .set("jmx-manager-http-port", jmxHttpPort)
                .set("jmx-manager-port", jmxPort)
                .set("jmx-manager", "true")
                .set("jmx-manager-start", "true")

                .build();

        try
        {
            locatorLauncher.start();
            System.out.println("Locator successfully started");
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
