package common.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public abstract class RiskProduct implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String cusip;
	private String securityDesc;

	abstract ProductType getRiskProductType();
	
}
