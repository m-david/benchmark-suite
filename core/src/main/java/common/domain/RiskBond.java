package common.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * TSY Bond trades in Kraken Risk context. 
 */
@Data
@EqualsAndHashCode(callSuper = true)
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

	public ProductType getRiskProductType() {
		return ProductType.BOND;
	} 

}
