package common.domain;


import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.Hashtable;
import java.util.Objects;


@Data
public class RiskTrade implements Serializable
{

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

    public Integer getId()
    {
        return id;
    }

    public void setId(Integer id)
    {
        this.id = id;
    }

    public String getEcnTradeId()
    {
        return ecnTradeId;
    }

    public void setEcnTradeId(String ecnTradeId)
    {
        this.ecnTradeId = ecnTradeId;
    }

    public String getEcnLinkId()
    {
        return ecnLinkId;
    }

    public void setEcnLinkId(String ecnLinkId)
    {
        this.ecnLinkId = ecnLinkId;
    }

    public TradeSource getTradeSource()
    {
        return tradeSource;
    }

    public void setTradeSource(TradeSource tradeSource)
    {
        this.tradeSource = tradeSource;
    }

    public RiskProduct getProduct()
    {
        return product;
    }

    public void setProduct(RiskProduct product)
    {
        this.product = product;
    }

    public Date getTradeDate()
    {
        return tradeDate;
    }

    public void setTradeDate(Date tradeDate)
    {
        this.tradeDate = tradeDate;
    }

    public Date getSettleDate()
    {
        return settleDate;
    }

    public void setSettleDate(Date settleDate)
    {
        this.settleDate = settleDate;
    }

    public double getQuantity()
    {
        return quantity;
    }

    public void setQuantity(double quantity)
    {
        this.quantity = quantity;
    }

    public double getTradePrice()
    {
        return tradePrice;
    }

    public void setTradePrice(double tradePrice)
    {
        this.tradePrice = tradePrice;
    }

    public String getTradeCurrency()
    {
        return tradeCurrency;
    }

    public void setTradeCurrency(String tradeCurrency)
    {
        this.tradeCurrency = tradeCurrency;
    }

    public String getSettleCurrency()
    {
        return settleCurrency;
    }

    public void setSettleCurrency(String settleCurrency)
    {
        this.settleCurrency = settleCurrency;
    }

    public String getBook()
    {
        return book;
    }

    public void setBook(String book)
    {
        this.book = book;
    }

    public String getTraderName()
    {
        return traderName;
    }

    public void setTraderName(String traderName)
    {
        this.traderName = traderName;
    }

    public String getCounterParty()
    {
        return counterParty;
    }

    public void setCounterParty(String counterParty)
    {
        this.counterParty = counterParty;
    }

    public Date getEnteredDate()
    {
        return enteredDate;
    }

    public void setEnteredDate(Date enteredDate)
    {
        this.enteredDate = enteredDate;
    }

    public String getEnteredUser()
    {
        return enteredUser;
    }

    public void setEnteredUser(String enteredUser)
    {
        this.enteredUser = enteredUser;
    }

    public String getModifiedUser()
    {
        return modifiedUser;
    }

    public void setModifiedUser(String modifiedUser)
    {
        this.modifiedUser = modifiedUser;
    }

    public String getComment()
    {
        return comment;
    }

    public void setComment(String comment)
    {
        this.comment = comment;
    }

    public TradeStatus getStatus()
    {
        return status;
    }

    public void setStatus(TradeStatus status)
    {
        this.status = status;
    }

    public String getExchange()
    {
        return exchange;
    }

    public void setExchange(String exchange)
    {
        this.exchange = exchange;
    }

    public double getAccrual()
    {
        return accrual;
    }

    public void setAccrual(double accrual)
    {
        this.accrual = accrual;
    }

    public Date getUpdatedTime()
    {
        return updatedTime;
    }

    public void setUpdatedTime(Date updatedTime)
    {
        this.updatedTime = updatedTime;
    }

    public Hashtable getFees()
    {
        return fees;
    }

    public void setFees(Hashtable fees)
    {
        this.fees = fees;
    }

    public Action getAction()
    {
        return action;
    }

    public void setAction(Action action)
    {
        this.action = action;
    }

    public Hashtable getKeywords()
    {
        return keywords;
    }

    public void setKeywords(Hashtable keywords)
    {
        this.keywords = keywords;
    }

    public Hashtable getTransientKeywords()
    {
        return transientKeywords;
    }

    public void setTransientKeywords(Hashtable transientKeywords)
    {
        this.transientKeywords = transientKeywords;
    }

    public String getSalesPerson()
    {
        return salesPerson;
    }

    public void setSalesPerson(String salesPerson)
    {
        this.salesPerson = salesPerson;
    }

    public String getMarketType()
    {
        return marketType;
    }

    public void setMarketType(String marketType)
    {
        this.marketType = marketType;
    }

    public BuySell getBuySell()
    {
        return buySell;
    }

    public void setBuySell(BuySell buySell)
    {
        this.buySell = buySell;
    }

    public String getInventoryId()
    {
        return inventoryId;
    }

    public void setInventoryId(String inventoryId)
    {
        this.inventoryId = inventoryId;
    }

    public String getProductFamily()
    {
        return productFamily;
    }

    public void setProductFamily(String productFamily)
    {
        this.productFamily = productFamily;
    }


    public static long getSerialVersionUID()
    {
        return serialVersionUID;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof RiskTrade)) return false;
        RiskTrade riskTrade = (RiskTrade) o;
        return Double.compare(riskTrade.quantity, quantity) == 0 &&
                Double.compare(riskTrade.tradePrice, tradePrice) == 0 &&
                Double.compare(riskTrade.accrual, accrual) == 0 &&
                Objects.equals(id, riskTrade.id) &&
                Objects.equals(ecnTradeId, riskTrade.ecnTradeId) &&
                Objects.equals(ecnLinkId, riskTrade.ecnLinkId) &&
                tradeSource == riskTrade.tradeSource &&
                Objects.equals(product, riskTrade.product) &&
                Objects.equals(tradeDate, riskTrade.tradeDate) &&
                Objects.equals(settleDate, riskTrade.settleDate) &&
                Objects.equals(tradeCurrency, riskTrade.tradeCurrency) &&
                Objects.equals(settleCurrency, riskTrade.settleCurrency) &&
                Objects.equals(book, riskTrade.book) &&
                Objects.equals(traderName, riskTrade.traderName) &&
                Objects.equals(counterParty, riskTrade.counterParty) &&
                Objects.equals(enteredDate, riskTrade.enteredDate) &&
                Objects.equals(enteredUser, riskTrade.enteredUser) &&
                Objects.equals(modifiedUser, riskTrade.modifiedUser) &&
                Objects.equals(comment, riskTrade.comment) &&
                status == riskTrade.status &&
                Objects.equals(exchange, riskTrade.exchange) &&
                Objects.equals(updatedTime, riskTrade.updatedTime) &&
                Objects.equals(fees, riskTrade.fees) &&
                action == riskTrade.action &&
                Objects.equals(keywords, riskTrade.keywords) &&
                Objects.equals(transientKeywords, riskTrade.transientKeywords) &&
                Objects.equals(salesPerson, riskTrade.salesPerson) &&
                Objects.equals(marketType, riskTrade.marketType) &&
                buySell == riskTrade.buySell &&
                Objects.equals(inventoryId, riskTrade.inventoryId) &&
                Objects.equals(productFamily, riskTrade.productFamily);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(id, ecnTradeId, ecnLinkId, tradeSource, product, tradeDate, settleDate, quantity,
                tradePrice, tradeCurrency, settleCurrency, book, traderName, counterParty, enteredDate,
                enteredUser, modifiedUser, comment, status, exchange, accrual, updatedTime, fees, action,
                keywords, transientKeywords, salesPerson, marketType, buySell, inventoryId, productFamily);
    }
}
