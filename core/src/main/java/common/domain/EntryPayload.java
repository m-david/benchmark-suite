package common.domain;

import java.io.Serializable;

public class EntryPayload implements Serializable {

    private byte[] payload;

    public EntryPayload(byte[] bytes)
    {
        this.payload = bytes;
    }

}
