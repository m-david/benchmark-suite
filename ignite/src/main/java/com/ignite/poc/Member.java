package com.ignite.poc;

import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;

public class Member
{
    public static void main(String[] args)
    {
        String xmlLocation = System.getProperty("benchmark.ignite.config.xml", "conf/ignite-cache.xml");
        Ignite ignite = Ignition.start(xmlLocation);

    }
}
