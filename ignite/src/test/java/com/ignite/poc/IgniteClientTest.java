package com.ignite.poc;

import org.apache.ignite.Ignition;
import org.apache.ignite.client.IgniteClient;
import org.apache.ignite.configuration.ClientConfiguration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class IgniteClientTest
{

    @Test
    public void testClient()
    {
        String addressesString = System.getProperty("benchmark.ignite.addresses", "192.168.5.225:40100");
        ClientConfiguration cfg = new ClientConfiguration().setAddresses(addressesString);
        IgniteClient igniteClient = Ignition.startClient(cfg);
        Assertions.assertNotNull(igniteClient);

    }
}
