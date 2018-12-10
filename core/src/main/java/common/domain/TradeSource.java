package common.domain;

import java.io.Serializable;

public enum TradeSource implements Serializable {

	TRADEWEB 	("TradeWeb", "TradeWeb"),
    GFI			("GFI", "GFI"),
    TULLET 		("Tullet", "TradeWeb/Prebon"),
    BLOOMBERG 	("Bloomberg", "Bloomberg"),
    ICAP	 	("iCAP", "iCAP"),
    BGC		 	("BGC", "BGC"),
    JAVELIN 	("Javelin", "Javelin"),
    ERIS		("Eris", "Eris"),
    TRUEEX		("TrueEx", "TrueEx"),
    TRADX		("Trad-X", "Trad-X"),
    WFC_VT	 	("WfcVt", "Voice trade"),
    WFC_FT		("WfcFt", "Futures trade"),
    DEALERWEB 	("DealerWeb", "DealerWeb");
	
	
	private final String sourceId;
	private final String sourceDesc;
	
	TradeSource(String sourceId, String sourceDesc) {
		this.sourceId = sourceId;
		this.sourceDesc = sourceDesc;
	}
	
	public String sourceId() {
		return sourceId;
	}
	
	public String sourceDesc() {
		return sourceDesc;
	}
	
}
