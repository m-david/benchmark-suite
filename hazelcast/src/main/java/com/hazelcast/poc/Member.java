package com.hazelcast.poc;

import com.hazelcast.core.Hazelcast;

import java.io.FileNotFoundException;

public class Member {

    public static void main(String[] args) throws FileNotFoundException {
        Hazelcast.newHazelcastInstance();
    }
}
