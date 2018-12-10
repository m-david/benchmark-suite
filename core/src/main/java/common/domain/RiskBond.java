package common.domain;

import java.util.Date;

/**
 * TSY Bond trades in Kraken Risk context. 
 */
public class RiskBond extends RiskProduct {
	
	private static final long serialVersionUID = 611224819451527483L;
	
	private String buySell = null;
    private double accruedInterest = 0;
    private String issuer = null;
    private Date maturityDate = null;
    private double principal = 0;     
    private String currency = null;    
    private String productType = null;
    private String superProductType = null;
    private Double coupon = null;
    private String id;
    
    public String getBuySell() {
		return buySell;
	}
	public void setBuySell(String buySell) {
		this.buySell = buySell;
	}
	public double getAccruedInterest() {
		return accruedInterest;
	}
	public void setAccruedInterest(double accruedInterest) {
		this.accruedInterest = accruedInterest;
	}
	public String getIssuer() {
		return issuer;
	}
	public void setIssuer(String issuer) {
		this.issuer = issuer;
	}
	public Date getMaturityDate() {
		return maturityDate;
	}
	public void setMaturityDate(Date maturityDate) {
		this.maturityDate = maturityDate;
	}
	public double getPrincipal() {
		return principal;
	}
	public void setPrincipal(double principal) {
		this.principal = principal;
	}
	public String getCurrency() {
		return currency;
	}
	public void setCurrency(String currency) {
		this.currency = currency;
	}

	@Override
	public String toString() {
		return "RiskBond ["
				+ (buySell != null ? "buySell=" + buySell + ", " : "")
				+ (getCusip() != null ? "cusip=" + getCusip() + ", " : "")
				+ "accruedInterest="
				+ accruedInterest
				+ ", "
				+ (issuer != null ? "issuer=" + issuer + ", " : "")
				+ (maturityDate != null ? "maturityDate=" + maturityDate + ", "
						: "")
				+ "principal="
				+ principal
				+ ", "
				+ (getSecurityDesc() != null ? "securityDesc=" + getSecurityDesc() + ", " : "")			
				+ (currency != null ? "currency=" + currency : "") + "]";
	}
	
	public String getProductType() {
		return productType;
	}
	public void setProductType(String productType) {
		this.productType = productType;
	}
	public String getSuperProductType() {
		return superProductType;
	}
	public void setSuperProductType(String superProductType) {
		this.superProductType = superProductType;
	}
	public Double getCoupon() {
		return coupon;
	}
	public void setCoupon(Double coupon) {
		this.coupon = coupon;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}

	public ProductType getRiskProductType() {
		return ProductType.BOND;
	} 

}
