package com.benchmark.test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.*;

public class AddressListTest
{

    @Test
    public void testAddressList()
    {
        String addressesString = "127.0.0.1,127.0.0.2";
        String portsString = "47500..47509";

        String[] addresses = addressesString.split(",");
        List<String> list = new ArrayList<>();
        Set<String> ultimateAddressList = new HashSet<>();
        Collections.addAll(list, addresses);
        list.forEach(s -> ultimateAddressList.add(s + ":" + portsString));

        List<String> expectedList = new ArrayList<>();
        expectedList.add("127.0.0.1:47500..47509");
        expectedList.add("127.0.0.2:47500..47509");

        expectedList.forEach(a -> Assertions.assertTrue(ultimateAddressList.contains(a)));

    }
}
