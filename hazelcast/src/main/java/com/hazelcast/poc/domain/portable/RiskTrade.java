package com.hazelcast.poc.domain.portable;


import common.domain.BuySell;
import common.domain.TradeStatus;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import common.domain.Action;

import java.io.IOException;
import java.util.*;

/**
 * @author a445147
 *         <p>
 *         A simple trade for Kraken Risk.
 */
@SuppressWarnings("unchecked")
final public class RiskTrade implements Portable {

    public final static Integer RISK_TRADE_CLASS_ID = 1;
    public final static Integer RISK_TRADE_FACTORY_ID = 1;

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
        return "RiskTrade [ecnTradeId = " + ecnTradeId + ", accrual=" + accrual + ", action="
            + action
            + ", book=" + book + ", comment=" + comment + ", counterParty="
            + counterParty + ", enteredDate=" + enteredDate
            + ", enteredUser=" + enteredUser + ", exchange=" + exchange + ", inventoryId="
            + inventoryId
            + ", fees=" + fees + ", id=" + id + ", ecnLinkId = " + ecnLinkId + ", keywords="
            + keywords
            + ", buySell=" + (buySell != null ? buySell : "null")
            + ", marketType=" + marketType + ", modifiedUser="
            + modifiedUser + ", product=" + product + ", productFamily=" + productFamily
            + ", quantity="
            + quantity + ", salesPerson=" + salesPerson + ", tradeSource=" + tradeSource
            + ", settleCurrency=" + settleCurrency + ", settleDate="
            + settleDate + ", status=" + status + ", tradeCurrency="
            + tradeCurrency + ", tradeDate=" + tradeDate + ", tradePrice="
            + tradePrice + ", traderName=" + traderName
            + ", transientKeywords=" + transientKeywords + ", updatedTime="
            + updatedTime + "]";
    }

    @Override public int getFactoryId() {
        return RISK_TRADE_FACTORY_ID;
    }

    @Override public int getClassId() {
        return RISK_TRADE_CLASS_ID;
    }

    @Override public void writePortable(PortableWriter portableWriter) throws IOException {
        portableWriter.writeInt("id", this.getId());
        portableWriter.writeUTF("ecnTradeId", this.getEcnTradeId());
        portableWriter.writeUTF("ecnLinkId", this.getEcnLinkId());
        // serialization of enum to string
        portableWriter.writeUTF("tradeSource", this.getTradeSource().toValue());
        portableWriter.writePortable("product", this.getProduct());

        portableWriter.writeLong("tradeDate", this.getTradeDate().getTime());
        portableWriter.writeLong("settleDate", this.getSettleDate().getTime());
        portableWriter.writeDouble("quantity", this.getQuantity());
        portableWriter.writeDouble("tradePrice", this.getTradePrice());
        portableWriter.writeUTF("tradeCurrency", this.getTradeCurrency());

        portableWriter.writeUTF("settleCurrency", this.getSettleCurrency());
        portableWriter.writeUTF("book", this.getBook());
        portableWriter.writeUTF("traderName", this.getTraderName());
        portableWriter.writeUTF("counterParty", this.getCounterParty());
        portableWriter.writeLong("enteredDate", this.getEnteredDate().getTime());

        portableWriter.writeUTF("enteredUser", this.getEnteredUser());
        portableWriter.writeUTF("modifiedUser", this.getModifiedUser());
        portableWriter.writeUTF("comment", this.getComment());

        // enum
        portableWriter.writeUTF("tradeStatus", this.getStatus().name());
        portableWriter.writeUTF("exchange", this.getExchange());

        portableWriter.writeDouble("accrual", this.getAccrual());
        portableWriter.writeLong("updatedTime", this.getUpdatedTime().getTime());

        // enum
        portableWriter.writeUTF("riskAction", this.getAction().name());

        portableWriter.writeUTF("salePerson", this.getSalesPerson());
        portableWriter.writeUTF("marketType", this.getMarketType());
        portableWriter.writeUTF("buySell", this.getBuySell().name());
        portableWriter.writeUTF("inventoryId", this.getInventoryId());
        portableWriter.writeUTF("productFamily", this.getProductFamily());

        final ObjectDataOutput rawDataOutput = portableWriter.getRawDataOutput();

        // portableWriter.writeMap(22, fees);
        writeMap(this.getFees(), rawDataOutput);

        //portableWriter.writeMap(24, this.getKeywords());
        writeMap(getKeywords(), rawDataOutput);

        //portableWriter.writeMap("transientKeywords", this.getTransientKeywords());
        writeMap(getTransientKeywords(), rawDataOutput);

    }

    private void writeMap(Map map, ObjectDataOutput rawDataOutput) throws IOException {
        int size = map.size();
        rawDataOutput.writeInt(size);
        final Set<Map.Entry> set = map.entrySet();
        for (Map.Entry entry : set) {
            final Object key = entry.getKey();
            final Object value = entry.getValue();
            rawDataOutput.writeObject(key);
            rawDataOutput.writeObject(value);
        }
    }

    @Override public void readPortable(PortableReader portableReader) throws IOException {
        this.id = portableReader.readInt("id");
        this.ecnTradeId = portableReader.readUTF("ecnTradeId");
        this.ecnLinkId = portableReader.readUTF("ecnLinkId");
        // serialization of enum to string
        this.tradeSource = TradeSource.fromValue(portableReader.readUTF("tradeSource"));
        this.product = portableReader.readPortable("product");

        this.tradeDate = new Date(portableReader.readLong("tradeDate"));
        this.settleDate = new Date(portableReader.readLong("settleDate"));
        this.quantity = portableReader.readDouble("quantity");
        this.tradePrice = portableReader.readDouble("tradePrice");
        this.tradeCurrency = portableReader.readUTF("tradeCurrency");

        this.settleCurrency = portableReader.readUTF("settleCurrency");
        this.book = portableReader.readUTF("book");
        this.traderName = portableReader.readUTF("traderName");
        this.counterParty = portableReader.readUTF("counterParty");
        this.enteredDate = new Date(portableReader.readLong("enteredDate"));

        this.enteredUser = portableReader.readUTF("enteredUser");
        this.modifiedUser = portableReader.readUTF("modifiedUser");
        this.comment = portableReader.readUTF("comment");

        // enum
        this.status = TradeStatus.valueOf(portableReader.readUTF("tradeStatus"));
        this.exchange = portableReader.readUTF("exchange");

        this.accrual = portableReader.readDouble("accrual");
        this.updatedTime = new Date(portableReader.readLong("updatedTime"));

        // enum
        this.action = Action.valueOf(portableReader.readUTF("riskAction"));

        this.salesPerson = portableReader.readUTF("salePerson");
        this.marketType = portableReader.readUTF("marketType");
        this.buySell = BuySell.valueOf(portableReader.readUTF("buySell"));
        this.inventoryId = portableReader.readUTF("inventoryId");
        this.productFamily = portableReader.readUTF("productFamily");

        // TODO read maps
        final ObjectDataInput rawDataInput = portableReader.getRawDataInput();
        this.fees = new Hashtable(readMaps(rawDataInput)) ;
        this.keywords = new Hashtable(readMaps(rawDataInput));
        this.transientKeywords = new Hashtable(readMaps(rawDataInput));
    }

    private Map readMaps(ObjectDataInput rawData) throws IOException {
        final int size = rawData.readInt();
        Map map = new HashMap();
        for (int i = 0; i < size; i++) {
            map.put(rawData.readObject(), rawData.readObject());
        }
        return map;
    }
}
