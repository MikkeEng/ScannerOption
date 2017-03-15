package com.mengroba.scanneroption.utils.epc;


import com.mengroba.scanneroption.utils.Row;

public class TagInfo implements Row {

    private final Epc epc;
    private final Long time;
    private final int rssi;

    public TagInfo(String epc, Long time, int rssi) {
        this.epc = Epc.of(epc);
        this.time = time;
        this.rssi = rssi;
    }

    public Epc getEpc() {
        return this.epc;
    }

    public Long getTime() {
        return this.time;
    }

    public int getRssi() {
        return this.rssi;
    }

    public boolean isBeacon() {
        return this.epc instanceof BeaconEpc;
    }

    @Override
    public String[] getFieldsRow() {
        return new String[]{epc.toString(), String.valueOf(epc.serial()), this.time.toString(), String.valueOf(this.rssi)};
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("TagInfo[");
        sb.append("serial = ").append(this.epc.serial());
        sb.append(", epc = ").append(this.epc);

        if (this.epc instanceof GarmentEpc) {
            sb.append(", garment = ").append(((GarmentEpc) this.epc).garmentCode());
        } else {
            sb.append(", beacon = ").append(((BeaconEpc) this.epc).info());
        }

        sb.append(", rssi = ").append(this.rssi);
        sb.append(", time = ").append(this.time);
        sb.append("]");

        return sb.toString();
    }
}
