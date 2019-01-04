package common.domain;


import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.Hashtable;


@Data
public class RiskTrade implements Serializable {

	private static final long serialVersionUID = 5064782838786161065L;
	
	protected Integer id;
	protected String ecnTradeId;
	protected String ecnLinkId;
	protected TradeSource tradeSource;
    protected RiskProduct product;
    protected Date tradeDate;
    protected Date settleDate;
    protected double quantity;
    protected double tradePrice;
    protected String tradeCurrency;
    protected String settleCurrency;
    protected String book;
    protected String traderName;
    protected String counterParty;
    protected Date enteredDate;
    protected String enteredUser;
    protected String modifiedUser;
    protected String comment;
    protected TradeStatus status;
    protected String exchange;
    protected double accrual;
    protected Date updatedTime;
    protected Hashtable fees;
    protected Action action;
    protected Hashtable keywords;
    protected Hashtable transientKeywords;
    protected String salesPerson;
    protected String marketType;
    protected BuySell buySell;
    protected String inventoryId;
    protected String productFamily;
}
