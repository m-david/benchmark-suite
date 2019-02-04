package common.domain;

import java.util.Date;
import java.util.Hashtable;

public interface IRiskTrade
{
    Integer getId();

    void setId(Integer id);


    String getEcnTradeId();

    void setEcnTradeId(String ecnTradeId);

    String getEcnLinkId();

    void setEcnLinkId(String ecnLinkId);

    TradeSource getTradeSource();

    void setTradeSource(TradeSource tradeSource);

    RiskProduct getProduct();

    void setProduct(RiskProduct product);

    Date getTradeDate();

    void setTradeDate(Date tradeDate);

    Date getSettleDate();

    void setSettleDate(Date settleDate);

    double getQuantity();

    void setQuantity(double quantity);

    double getTradePrice();

    void setTradePrice(double tradePrice);

    String getTradeCurrency();

    void setTradeCurrency(String tradeCurrency);

    String getSettleCurrency();

    void setSettleCurrency(String settleCurrency);

    String getBook();

    void setBook(String book);

    String getTraderName();

    void setTraderName(String traderName);

    String getCounterParty();

    void setCounterParty(String counterParty);

    Date getEnteredDate();

    void setEnteredDate(Date enteredDate);

    String getEnteredUser();

    void setEnteredUser(String enteredUser);

    String getModifiedUser();

    void setModifiedUser(String modifiedUser);

    String getComment();

    void setComment(String comment);

    TradeStatus getStatus();

    void setStatus(TradeStatus status);

    String getExchange();

    void setExchange(String exchange);

    double getAccrual();

    void setAccrual(double accrual);

    Date getUpdatedTime();

    void setUpdatedTime(Date updatedTime);

    Hashtable getFees();

    void setFees(Hashtable fees);

    Action getAction();

    void setAction(Action action);

    Hashtable getKeywords();

    void setKeywords(Hashtable keywords);

    Hashtable getTransientKeywords();

    void setTransientKeywords(Hashtable transientKeywords);

    String getSalesPerson();

    void setSalesPerson(String salesPerson);

    String getMarketType();

    void setMarketType(String marketType);

    BuySell getBuySell();

    void setBuySell(BuySell buySell);

    String getInventoryId();

    void setInventoryId(String inventoryId);

    String getProductFamily();

    void setProductFamily(String productFamily);


}
