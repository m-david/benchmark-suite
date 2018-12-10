package common.domain;

import java.io.Serializable;

public abstract class RiskProduct implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String cusip;
	private String securityDesc;
	
	public RiskProduct() {}
	
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
	
}
