package com.hazelcast.poc;

import com.hazelcast.config.Config;
import com.hazelcast.config.XmlConfigBuilder;
import com.hazelcast.core.Hazelcast;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class Member {

    public static void main(String[] args) throws FileNotFoundException {
//        InputStream resourceAsStream =
//            Member.class.getClassLoader().getResourceAsStream("hazelcast/hazelcast-server.xml");
//        Config config = new XmlConfigBuilder(resourceAsStream).build();
//
//        Hazelcast.newHazelcastInstance(config);
        Hazelcast.newHazelcastInstance();
    }
}
