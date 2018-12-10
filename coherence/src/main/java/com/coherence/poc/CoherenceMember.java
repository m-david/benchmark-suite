package com.coherence.poc;

import com.tangosol.net.DefaultCacheServer;

/**
 * TODO
 *
 * @author Viktor Gamov on 8/12/15.
 *         Twitter: @gamussa
 * @since 0.0.1
 */
public class CoherenceMember {
    public static void main(String[] args) {
        System.setProperty("java.net.preferIPv4Stack", "true");
        System.setProperty("tangosol.coherence.override", "coherence/tangosol-coherence-override.xml");
        System.setProperty("tangosol.coherence.cacheconfig", "coherence/cache-configuration.xml");
        System.setProperty("tangosol.pof.config", "coherence/my-custom-pof-config.xml");

        DefaultCacheServer.main(args);
    }
}
