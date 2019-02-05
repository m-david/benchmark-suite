package com.coherence.poc.serializer;

import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofSerializer;
import com.tangosol.io.pof.PofWriter;
import common.domain.RiskBond;

import java.io.IOException;
import java.util.Date;

public class RiskBondSerializer implements PofSerializer<RiskBond>
{
    public RiskBond deserialize(PofReader in)
            throws IOException {
        RiskBond bond = new RiskBond();

        String id = in.readString(0);
        String cusip = in.readString(1);
        String securityDesc = in.readString(2);
        String buySell = in.readString(3);
        Double accruedInterest = in.readDouble(4);
        String issuer = in.readString(5);
        Date maturityDate = in.readDate(6);
        Double principal = in.readDouble(7);
        String currency = in.readString(8);
        String productType = in.readString(9);
        String superProductType = in.readString(10);
        Double coupon = in.readDouble(11);

        // mark that reading the object is done
        in.readRemainder();

        return bond;
    }

    public void serialize(PofWriter out, RiskBond bond)
            throws IOException
    {
        out.writeString(0, bond.getId());
        out.writeString(1, bond.getCusip());
        out.writeString(2, bond.getSecurityDesc());
        out.writeString(3, bond.getBuySell());
        out.writeDouble(4, bond.getAccruedInterest());
        out.writeString(5, bond.getIssuer());
        out.writeDate(6, bond.getMaturityDate());
        out.writeDouble(7, bond.getPrincipal());
        out.writeString(8, bond.getCurrency());
        out.writeString(9, bond.getProductType());
        out.writeString(10, bond.getSuperProductType());
        out.writeDouble(11, bond.getCoupon());

        out.writeRemainder(null);
    }
}