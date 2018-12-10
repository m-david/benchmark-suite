package com.hazelcast.poc.domain.portable;

import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import common.domain.ProductType;

import java.io.IOException;

public abstract class RiskProduct implements Portable {

    private String cusip;
    private String securityDesc;

    public RiskProduct() {
    }

    public String getCusip() {
        return cusip;
    }

    public void setCusip(String cusip) {
        this.cusip = cusip;
    }

    public String getSecurityDesc() {
        return securityDesc;
    }

    public void setSecurityDesc(String securityDesc) {
        this.securityDesc = securityDesc;
    }

    abstract ProductType getRiskProductType();

    @Override public abstract int getFactoryId();

    @Override public abstract int getClassId();

    @Override public abstract void writePortable(PortableWriter portableWriter) throws IOException;

    @Override public abstract void readPortable(PortableReader portableReader) throws IOException;
}
