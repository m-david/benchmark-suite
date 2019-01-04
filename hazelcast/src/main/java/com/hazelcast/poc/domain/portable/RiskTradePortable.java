package com.hazelcast.poc.domain.portable;


import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import common.domain.Action;
import common.domain.BuySell;
import common.domain.RiskTrade;
import common.domain.TradeSource;
import common.domain.TradeStatus;
import lombok.Data;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

/**
 * @author a445147
 * <p>
 * A simple trade for Kraken Risk.
 */
@SuppressWarnings("unchecked")
@Data
final public class RiskTradePortable extends RiskTrade implements Portable {

    public final static Integer RISK_TRADE_CLASS_ID = 1;
    public final static Integer RISK_TRADE_FACTORY_ID = 1;

    @Override
    public int getFactoryId() {
        return RISK_TRADE_FACTORY_ID;
    }

    @Override
    public int getClassId() {
        return RISK_TRADE_CLASS_ID;
    }

    @Override
    public void writePortable(PortableWriter portableWriter) throws IOException {
        portableWriter.writeInt("id", this.getId());
        portableWriter.writeUTF("ecnTradeId", this.getEcnTradeId());
        portableWriter.writeUTF("ecnLinkId", this.getEcnLinkId());
        // serialization of enum to string
        portableWriter.writeUTF("tradeSource", this.getTradeSource().toValue());

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

        rawDataOutput.writeObject(this.getProduct());

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

    @Override
    public void readPortable(PortableReader portableReader) throws IOException {
        this.id = portableReader.readInt("id");
        this.ecnTradeId = portableReader.readUTF("ecnTradeId");
        this.ecnLinkId = portableReader.readUTF("ecnLinkId");
        // serialization of enum to string
        this.tradeSource = TradeSource.fromValue(portableReader.readUTF("tradeSource"));

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
        this.product = rawDataInput.readObject();

        this.fees = new Hashtable(readMaps(rawDataInput));
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
