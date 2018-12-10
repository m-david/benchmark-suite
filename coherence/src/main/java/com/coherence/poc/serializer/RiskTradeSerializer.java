package com.coherence.poc.serializer;

import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofSerializer;
import com.tangosol.io.pof.PofWriter;
import common.domain.*;

import java.io.IOException;
import java.util.Date;
import java.util.Hashtable;


public class RiskTradeSerializer  implements PofSerializer  {
	
		
	public Object deserialize(PofReader in) 
			   throws IOException 
			   {
				
				RiskTrade trade = new RiskTrade();
				
				 Integer id = null;
				 String ecnTradeId = null;
				 String ecnLinkId = null;
				 TradeSource tradeSource = null;
			     RiskProduct product = null;
			     Date tradeDate = null;
			     Date settleDate = null;
			     double quantity =0.0;
			     double tradePrice =0.0;
			     String tradeCurrency = null;
			     String settleCurrency = null;
			     String book = null;
			     String traderName = null;
			     String counterParty = null;
			     Date enteredDate = null;
			     String enteredUser = null;
			     String modifiedUser = null;
			     String comment = null;
			     TradeStatus status = null;
			     String exchange = null;
			     double accrual =0.0;
			     Date updatedTime = null;
			     Hashtable fees = null;
			     Action action = null;
			     Hashtable keywords = null;
			     Hashtable transientKeywords = null;
			     String salesPerson = null;
			     String marketType = null;
			     BuySell buySell = null;
			     String inventoryId = null;
			     String productFamily = null;
				
				id = in.readInt(0);
				ecnTradeId = in.readString(1);
				ecnLinkId = in.readString(2);
				tradeSource = (TradeSource)in.readObject(3);
				product = (RiskProduct)in.readObject(4);
				
				tradeDate = in.readDate(5);
				settleDate = in.readDate(6);
				quantity = in.readDouble(7);
				tradePrice = in.readDouble(8);
				tradeCurrency=in.readString(9);
				
				settleCurrency=in.readString(10);
				book=in.readString(11);
				traderName = in.readString(12);
				counterParty =in.readString(13);
				enteredDate = in.readDate(14);
				
				enteredUser = in.readString(15);
				modifiedUser =in.readString(16);
				comment =in.readString(17);
				status = (TradeStatus)in.readObject(18);
				exchange =in.readString(19);
				
				accrual = in.readDouble(20);
				updatedTime = in.readDate(21);
				fees = (Hashtable) in.readMap(22, new Hashtable());
			
				keywords =  (Hashtable) in.readMap(24, new Hashtable());
				
//				transientKeywords =  (Hashtable) in.readMap(25, null);
				transientKeywords =  (Hashtable) in.readMap(25, new Hashtable());
				salesPerson = in.readString(26);
				marketType = in.readString(27);
				buySell = (BuySell)in.readObject(28);
				inventoryId = in.readString(29);
				productFamily = in.readString(30);
				// mark that reading the object is done
			   in.readRemainder();
			   
			   trade.setId(id);
			   trade.setEcnTradeId(ecnTradeId);
			   trade.setEcnLinkId(ecnLinkId);
			   trade.setTradeSource(tradeSource);
			   trade.setProduct(product);
			   trade.setTradeDate(tradeDate);
			   trade.setSettleDate(settleDate);
			   trade.setQuantity(quantity);
			   trade.setTradePrice(tradePrice);
			   trade.setTradeCurrency(tradeCurrency);
			   trade.setSettleCurrency(settleCurrency);
			   trade.setBook(book);
			   trade.setTraderName(traderName);
			   trade.setCounterParty(counterParty);
			   trade.setEnteredDate(enteredDate);
			   trade.setEnteredUser(enteredUser);
			   trade.setModifiedUser(modifiedUser);
			   trade.setComment(comment);
			   trade.setStatus(status);
			   trade.setExchange(exchange);
			   trade.setAccrual(accrual);
			   trade.setUpdatedTime(updatedTime);
			   trade.setFees(fees);
			   trade.setAction(action);
			   trade.setKeywords(keywords);
			   trade.setTransientKeywords(transientKeywords);
			   trade.setSalesPerson(salesPerson);
			   trade.setMarketType(marketType);
			   trade.setBuySell(buySell);
			   trade.setInventoryId(inventoryId);
			   trade.setProductFamily(productFamily);
			 
			   return trade;
			   }
			 
	
	public void serialize(PofWriter out, Object o) 
			   throws IOException 
			   {
				RiskTrade  trade = (RiskTrade) o;
				
				try {
					out.writeInt(0, trade.getId());
					out.writeString(1, trade.getEcnTradeId());
					out.writeString(2, trade.getEcnLinkId());
					out.writeObject(3, trade.getTradeSource());
					out.writeObject(4, trade.getProduct());
					
					out.writeDateTime(5, trade.getTradeDate());
					out.writeDateTime(6, trade.getSettleDate());
					out.writeDouble(7, trade.getQuantity());
					out.writeDouble(8, trade.getTradePrice());
					out.writeString(9, trade.getTradeCurrency());
					
					out.writeString(10, trade.getSettleCurrency());
					out.writeString(11, trade.getBook());
					out.writeString(12, trade.getTraderName());
					out.writeString(13, trade.getCounterParty());
					out.writeDateTime(14, trade.getEnteredDate());
					
					out.writeString(15, trade.getEnteredUser());
					out.writeString(16, trade.getModifiedUser());
					out.writeString(17, trade.getComment());
					out.writeObject(18, trade.getStatus());
					out.writeString(19, trade.getExchange());
					
					out.writeDouble(20, trade.getAccrual());
					out.writeDateTime(21, trade.getUpdatedTime());
					out.writeMap(22, trade.getFees());
					out.writeObject(23, trade.getAction());
					out.writeMap(24, trade.getKeywords());
					
					out.writeMap(25, trade.getTransientKeywords());
					out.writeString(26, trade.getSalesPerson());
					out.writeString(27, trade.getMarketType());
					out.writeObject(28, trade.getBuySell());
					out.writeString(29, trade.getInventoryId());
					out.writeString(30, trade.getProductFamily());
					
       
   out.writeRemainder(null);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			   }
			
			
}
