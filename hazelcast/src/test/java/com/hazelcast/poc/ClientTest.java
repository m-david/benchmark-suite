package com.hazelcast.poc;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.config.Config;
import com.hazelcast.config.XmlConfigBuilder;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import common.BenchmarkConstants;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.InputStream;

public class ClientTest
{

    private static HazelcastInstance server1;
    private static HazelcastInstance server2;

    @BeforeAll
    public static void init()
    {
        InputStream resourceAsStream =
                Member.class.getClassLoader().getResourceAsStream("hazelcast-server-test.xml");
        Config config = new XmlConfigBuilder(resourceAsStream).build();

        server1 = Hazelcast.newHazelcastInstance(config);
        server2 = Hazelcast.newHazelcastInstance(config);

    }

    @AfterAll
    public static void shutdown()
    {
        server1.shutdown();
        server2.shutdown();
    }

    @Test
    public void testClient()
    {
        System.setProperty("hazelcast.client.config", "./src/test/resources/hazelcast-client-test.xml");
        HazelcastInstance hazelcastClient = HazelcastClient.newHazelcastClient();
        Assertions.assertNotNull(hazelcastClient.getMap(BenchmarkConstants.TRADE_READ_MAP));
    }
}
