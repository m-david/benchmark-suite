package common.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;
import java.util.Objects;

/**
 * TSY Bond trades in Kraken Risk context. 
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RiskBond extends RiskProduct {
	
	private static final long serialVersionUID = 611224819451527483L;
	
	private String buySell = "";
    private Double accruedInterest = Double.valueOf(0);
    private String issuer = "";
    private Date maturityDate = new Date();
    private Double principal = Double.valueOf(0);
    private String currency = "";
    private String productType = "";
    private String superProductType = "";
    private Double coupon = Double.valueOf(0);
    private String id = "";

	public ProductType getRiskProductType() {
		return ProductType.BOND;
	}

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof RiskBond)) return false;
        RiskBond riskBond = (RiskBond) o;
        return Double.compare(riskBond.accruedInterest, accruedInterest) == 0 &&
                Double.compare(riskBond.principal, principal) == 0 &&
                Objects.equals(buySell, riskBond.buySell) &&
                Objects.equals(issuer, riskBond.issuer) &&
                Objects.equals(maturityDate, riskBond.maturityDate) &&
                Objects.equals(currency, riskBond.currency) &&
                Objects.equals(productType, riskBond.productType) &&
                Objects.equals(superProductType, riskBond.superProductType) &&
                Objects.equals(coupon, riskBond.coupon) &&
                Objects.equals(id, riskBond.id);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(buySell, accruedInterest, issuer, maturityDate, principal, currency, productType, superProductType, coupon, id);
    }

    public String getBuySell()
    {
        return buySell;
    }

    public void setBuySell(String buySell)
    {
        this.buySell = buySell;
    }

    public double getAccruedInterest()
    {
        return accruedInterest;
    }

    public void setAccruedInterest(double accruedInterest)
    {
        this.accruedInterest = accruedInterest;
    }

    public String getIssuer()
    {
        return issuer;
    }

    public void setIssuer(String issuer)
    {
        this.issuer = issuer;
    }

    public Date getMaturityDate()
    {
        return maturityDate;
    }

    public void setMaturityDate(Date maturityDate)
    {
        this.maturityDate = maturityDate;
    }

    public double getPrincipal()
    {
        return principal;
    }

    public void setPrincipal(double principal)
    {
        this.principal = principal;
    }

    public String getCurrency()
    {
        return currency;
    }

    public void setCurrency(String currency)
    {
        this.currency = currency;
    }

    public String getProductType()
    {
        return productType;
    }

    public void setProductType(String productType)
    {
        this.productType = productType;
    }

    public String getSuperProductType()
    {
        return superProductType;
    }

    public void setSuperProductType(String superProductType)
    {
        this.superProductType = superProductType;
    }

    public Double getCoupon()
    {
        return coupon;
    }

    public void setCoupon(Double coupon)
    {
        this.coupon = coupon;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }
}
