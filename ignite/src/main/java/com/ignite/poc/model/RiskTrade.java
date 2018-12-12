package com.ignite.poc.model;


import common.domain.*;
import org.apache.ignite.cache.query.annotations.QuerySqlField;

import java.io.Serializable;
import java.util.Date;
import java.util.Hashtable;

/**
 * @author a445147
 *
 * A simple trade for Kraken Risk.
 */
final public class RiskTrade implements Serializable {

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

	@QuerySqlField
    protected String settleCurrency;

	@QuerySqlField(index = true)
    protected String book;

	@QuerySqlField
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

    public RiskTrade() {
    }

	public double getAccrual() {
		return accrual;
	}

	public void setAccrual(double accrual) {
		this.accrual = accrual;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public TradeSource getTradeSource() {
		return tradeSource;
	}

	public void setTradeSource(TradeSource tradeSource) {
		this.tradeSource = tradeSource;
	}

	public RiskProduct getProduct() {
		return product;
	}

	public void setProduct(RiskProduct product) {
		this.product = product;
	}

	public Date getTradeDate() {
		return tradeDate;
	}

	public void setTradeDate(Date tradeDate) {
		this.tradeDate = tradeDate;
	}

	public Date getSettleDate() {
		return settleDate;
	}

	public void setSettleDate(Date settleDate) {
		this.settleDate = settleDate;
	}

	public double getQuantity() {
		return quantity;
	}

	public void setQuantity(double quantity) {
		this.quantity = quantity;
	}

	public double getTradePrice() {
		return tradePrice;
	}

	public void setTradePrice(double tradePrice) {
		this.tradePrice = tradePrice;
	}

	public String getTradeCurrency() {
		return tradeCurrency;
	}

	public void setTradeCurrency(String tradeCurrency) {
		this.tradeCurrency = tradeCurrency;
	}

	public String getSettleCurrency() {
		return settleCurrency;
	}

	public void setSettleCurrency(String settleCurrency) {
		this.settleCurrency = settleCurrency;
	}

	public String getBook() {
		return book;
	}

	public void setBook(String book) {
		this.book = book;
	}

	public String getTraderName() {
		return traderName;
	}

	public void setTraderName(String traderName) {
		this.traderName = traderName;
	}

	public String getCounterParty() {
		return counterParty;
	}

	public void setCounterParty(String counterParty) {
		this.counterParty = counterParty;
	}

	public Date getEnteredDate() {
		return enteredDate;
	}

	public void setEnteredDate(Date enteredDate) {
		this.enteredDate = enteredDate;
	}

	public String getEnteredUser() {
		return enteredUser;
	}

	public void setEnteredUser(String enteredUser) {
		this.enteredUser = enteredUser;
	}

	public String getModifiedUser() {
		return modifiedUser;
	}

	public void setModifiedUser(String modifiedUser) {
		this.modifiedUser = modifiedUser;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public TradeStatus getStatus() {
		return status;
	}

	public void setStatus(TradeStatus status) {
		this.status = status;
	}

	public String getExchange() {
		return exchange;
	}

	public void setExchange(String exchange) {
		this.exchange = exchange;
	}

	public Date getUpdatedTime() {
		return updatedTime;
	}

	public void setUpdatedTime(Date updatedTime) {
		this.updatedTime = updatedTime;
	}

	public Hashtable getFees() {
		return fees;
	}

	public void setFees(Hashtable fees) {
		this.fees = fees;
	}

	public Action getAction() {
		return action;
	}

	public void setAction(Action action) {
		this.action = action;
	}

	public Hashtable getKeywords() {
		return keywords;
	}

	public void setKeywords(Hashtable keywords) {
		this.keywords = keywords;
	}

	public Hashtable getTransientKeywords() {
		return transientKeywords;
	}

	public void setTransientKeywords(Hashtable transientKeywords) {
		this.transientKeywords = transientKeywords;
	}

	public String getSalesPerson() {
		return salesPerson;
	}

	public void setSalesPerson(String salesPerson) {
		this.salesPerson = salesPerson;
	}

	public String getMarketType() {
		return marketType;
	}

	public void setMarketType(String marketType) {
		this.marketType = marketType;
	}
	
	public String getEcnTradeId() {
		return ecnTradeId;
	}

	public void setEcnTradeId(String ecnTradeId) {
		this.ecnTradeId = ecnTradeId;
	}

	public String getEcnLinkId() {
		return ecnLinkId;
	}

	public void setEcnLinkId(String ecnLinkId) {
		this.ecnLinkId = ecnLinkId;
	}
	
	public BuySell getBuySell() {
		return buySell;
	}
	
	public void setBuySell(BuySell buySell) {
		this.buySell = buySell;
	}
	
	public String getInventoryId() {
		return inventoryId;
	}
	
	public void setInventoryId(String inventoryId) {
		this.inventoryId = inventoryId;
	}
	
	public String getProductFamily() {
		return productFamily;
	}
	
	public void setProductFamily(String productFamily) {
		this.productFamily = productFamily;
	}

	@Override
	public String toString() {
		return "RiskTrade [ecnTradeId = "+ecnTradeId+ ", accrual=" + accrual + ", action=" + action
				+ ", book=" + book + ", comment=" + comment + ", counterParty="
				+ counterParty + ", enteredDate=" + enteredDate
				+ ", enteredUser=" + enteredUser + ", exchange=" + exchange + ", inventoryId=" + inventoryId
				+ ", fees=" + fees + ", id=" + id + ", ecnLinkId = "+ ecnLinkId +", keywords=" + keywords
				+ ", buySell=" + (buySell != null ? buySell : "null")
				+ ", marketType=" + marketType + ", modifiedUser="
				+ modifiedUser + ", product=" + product + ", productFamily=" + productFamily + ", quantity="
				+ quantity + ", salesPerson=" + salesPerson + ", tradeSource=" + tradeSource
				+ ", settleCurrency=" + settleCurrency + ", settleDate="
				+ settleDate + ", status=" + status + ", tradeCurrency="
				+ tradeCurrency + ", tradeDate=" + tradeDate + ", tradePrice="
				+ tradePrice + ", traderName=" + traderName
				+ ", transientKeywords=" + transientKeywords + ", updatedTime="
				+ updatedTime + "]";
	}
	
}
