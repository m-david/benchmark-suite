package common.domain;

import java.io.Serializable;

public enum TradeSource implements Serializable {

	TRADEWEB("TradeWeb", "TradeWeb"),
	GFI("GFI", "GFI"),
	TULLET("Tullet", "TradeWeb/Prebon"),
	BLOOMBERG("Bloomberg", "Bloomberg"),
	ICAP("iCAP", "iCAP"),
	BGC("BGC", "BGC"),
	JAVELIN("Javelin", "Javelin"),
	ERIS("Eris", "Eris"),
	TRUEEX("TrueEx", "TrueEx"),
	TRADX("Trad-X", "Trad-X"),
	WFC_VT("WfcVt", "Voice trade"),
	WFC_FT("WfcFt", "Futures trade"),
	DEALERWEB("DealerWeb", "DealerWeb"),
	// UNKNOWN
	UNKNOWN("UNKNOWN", "UNKNOWN");


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

	public String toValue() {
		return sourceId + ":" + sourceDesc;
	}

	public static TradeSource fromValue(String value) {
		if (value != null) {
			final String[] split = value.split(":");
			String sourceId = split[0];
			String sourceDesc = split[1];
			for (TradeSource tradeSource : values()) {
				if (sourceId.equals(tradeSource.sourceId) && sourceDesc
						.equals(tradeSource.sourceDesc)) {
					return tradeSource;
				}
			}
		}

		return getDefault();
	}

	private static TradeSource getDefault() {
		return UNKNOWN;
	}

}
