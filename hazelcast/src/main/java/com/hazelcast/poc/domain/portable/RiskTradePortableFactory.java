package com.hazelcast.poc.domain.portable;

import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableFactory;

/**
 * TODO
 *
 * @author Viktor Gamov on 8/16/15.
 * Twitter: @gamussa
 * @since 0.0.1
 */
public class RiskTradePortableFactory implements PortableFactory {
    public Portable create(int classId) {
        switch (classId) {
            case 1:
                return new RiskTradePortable();
        }
        return null;
    }
}
