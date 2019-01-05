package com.hazelcast.poc;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class BatchTest
{

    @Test
    public void testBatchIncrementOdd()
    {
        int batchSize = 9;
        int maxSize = 100;

        List<Integer> sourceList = new ArrayList<>();
        for(int i = 0; i < maxSize; i++)
        {
            sourceList.add(i);
        }


        for(int i = 0; i < sourceList.size();)
        {
            testIter(sourceList, i, batchSize);
            i += batchSize;
        }
    }

    @Test
    public void testBatchIncrementEven()
    {
        int batchSize = 10;
        int maxSize = 100;

        List<Integer> sourceList = new ArrayList<>();
        for(int i = 0; i < maxSize; i++)
        {
            sourceList.add(i);
        }


        for(int i = 0; i < sourceList.size();)
        {
            testIter(sourceList, i, batchSize);
            i += batchSize;
        }
    }

    @Test
    public void testBatchIncrementOddSize()
    {
        int batchSize = 3;
        int maxSize = 11;

        List<Integer> sourceList = new ArrayList<>();
        for(int i = 0; i < maxSize; i++)
        {
            sourceList.add(i);
        }


        for(int i = 0; i < sourceList.size();)
        {
            testIter(sourceList, i, batchSize);
            i += batchSize;
        }
    }

    @Test
    public void testBatchIncrementEvenSize()
    {
        int batchSize = 4;
        int maxSize = 12;

        List<Integer> sourceList = new ArrayList<>();
        for(int i = 0; i < maxSize; i++)
        {
            sourceList.add(i);
        }


        for(int i = 0; i < sourceList.size();)
        {
            testIter(sourceList, i, batchSize);
            i += batchSize;
        }
    }



    private void testIter(List<Integer> list, int start, int bounds)
    {
        int limit = Math.min(list.size(), start+bounds);
        for(int i = start; i < limit; i++)
        {
            System.out.println(list.get(i));
        }
    }
}
