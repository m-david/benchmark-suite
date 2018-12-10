package com.geode.poc;

import org.apache.geode.distributed.ServerLauncher;

public class GeodeMember
{
    public static void main(String[] args) {
        ServerLauncher serverLauncher  = new ServerLauncher.Builder()
                .setMemberName("server1")
                .setServerPort(40405)
                .set("jmx-manager", "true")
                .set("jmx-manager-start", "true")
                .build();

        serverLauncher.start();

        System.out.println("Cache server successfully started");

    }
}
