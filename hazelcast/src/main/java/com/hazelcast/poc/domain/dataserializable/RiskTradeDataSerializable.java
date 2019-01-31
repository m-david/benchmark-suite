package com.hazelcast.poc.domain.dataserializable;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;
import common.domain.*;

import java.io.IOException;
import java.util.*;

public class RiskTradeDataSerializable extends RiskTrade implements DataSerializable
{
    @Override
    public void writeData(ObjectDataOutput objectDataOutput) throws IOException
    {
        objectDataOutput.writeInt(this.getId());
        objectDataOutput.writeUTF(this.getEcnTradeId());
        objectDataOutput.writeUTF(this.getEcnLinkId());
        // serialization of enum to string
        objectDataOutput.writeUTF(this.getTradeSource().toValue());

        objectDataOutput.writeLong(this.getTradeDate().getTime());
        objectDataOutput.writeLong(this.getSettleDate().getTime());
        objectDataOutput.writeDouble(this.getQuantity());
        objectDataOutput.writeDouble(this.getTradePrice());
        objectDataOutput.writeUTF(this.getTradeCurrency());

        objectDataOutput.writeUTF(this.getSettleCurrency());
        objectDataOutput.writeUTF(this.getBook());
        objectDataOutput.writeUTF(this.getTraderName());
        objectDataOutput.writeUTF(this.getCounterParty());
        objectDataOutput.writeLong(this.getEnteredDate().getTime());

        objectDataOutput.writeUTF(this.getEnteredUser());
        objectDataOutput.writeUTF(this.getModifiedUser());
        objectDataOutput.writeUTF(this.getComment());

        // enum
        objectDataOutput.writeUTF(this.getStatus().name());
        objectDataOutput.writeUTF(this.getExchange());

        objectDataOutput.writeDouble(this.getAccrual());
        objectDataOutput.writeLong(this.getUpdatedTime().getTime());

        // enum
        objectDataOutput.writeUTF(this.getAction().name());

        objectDataOutput.writeUTF(this.getSalesPerson());
        objectDataOutput.writeUTF(this.getMarketType());
        objectDataOutput.writeUTF(this.getBuySell().name());
        objectDataOutput.writeUTF(this.getInventoryId());
        objectDataOutput.writeUTF(this.getProductFamily());

        objectDataOutput.writeObject(this.getProduct());

        // objectDataOutput.writeMap(22, fees);
        writeMap(this.getFees(), objectDataOutput);

        //objectDataOutput.writeMap(24, this.getKeywords());
        writeMap(getKeywords(), objectDataOutput);

        //objectDataOutput.writeMap(this.getTransientKeywords());
        writeMap(getTransientKeywords(), objectDataOutput);
    }

    @Override
    public void readData(ObjectDataInput objectDataInput) throws IOException
    {
        {
            this.id = objectDataInput.readInt();
            this.ecnTradeId = objectDataInput.readUTF();
            this.ecnLinkId = objectDataInput.readUTF();
            // serialization of enum to string
            this.tradeSource = TradeSource.fromValue(objectDataInput.readUTF());

            this.tradeDate = new Date(objectDataInput.readLong());
            this.settleDate = new Date(objectDataInput.readLong());
            this.quantity = objectDataInput.readDouble();
            this.tradePrice = objectDataInput.readDouble();
            this.tradeCurrency = objectDataInput.readUTF();

            this.settleCurrency = objectDataInput.readUTF();
            this.book = objectDataInput.readUTF();
            this.traderName = objectDataInput.readUTF();
            this.counterParty = objectDataInput.readUTF();
            this.enteredDate = new Date(objectDataInput.readLong());

            this.enteredUser = objectDataInput.readUTF();
            this.modifiedUser = objectDataInput.readUTF();
            this.comment = objectDataInput.readUTF();

            // enum
            this.status = TradeStatus.valueOf(objectDataInput.readUTF());
            this.exchange = objectDataInput.readUTF();

            this.accrual = objectDataInput.readDouble();
            this.updatedTime = new Date(objectDataInput.readLong());

            // enum
            this.action = Action.valueOf(objectDataInput.readUTF());

            this.salesPerson = objectDataInput.readUTF();
            this.marketType = objectDataInput.readUTF();
            this.buySell = BuySell.valueOf(objectDataInput.readUTF());
            this.inventoryId = objectDataInput.readUTF();
            this.productFamily = objectDataInput.readUTF();

            // TODO read maps
            this.product = objectDataInput.readObject();

            this.fees = new Hashtable(readMaps(objectDataInput));
            this.keywords = new Hashtable(readMaps(objectDataInput));
            this.transientKeywords = new Hashtable(readMaps(objectDataInput));
        }
    }

    private void writeMap(Map map, ObjectDataOutput rawDataOutput) throws IOException
    {
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

    private Map readMaps(ObjectDataInput rawData) throws IOException {
        final int size = rawData.readInt();
        Map map = new HashMap();
        for (int i = 0; i < size; i++) {
            map.put(rawData.readObject(), rawData.readObject());
        }
        return map;
    }

}
