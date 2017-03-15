package com.mengroba.scanneroption.utils.epc;


//TODO Identif. EG
public final class BeaconEpc extends Epc {

    private static final long serialVersionUID = -8989415180247453576L;

    public static final int VERSION = 9;

    private final int block;
    private final int number;
    private final boolean limit;
    private final boolean extreme;
    private final boolean right;

    protected BeaconEpc(GarmentEpc epc) {
        super((int) epc.first(), (int) epc.second(), (int) epc.serial(), (int) epc.fourth());

        GarmentCode code = epc.garmentCode();

        this.block = code.quality() * 100 + (code.color() / 10);
        this.number = code.size();
        this.limit = code.model() % 10 == 1;
        this.extreme = (code.model() / 10) % 10 == 1;
        this.right = code.color() % 10 == 2;
    }

    public static interface ExtremeStepBuilder {

        LimitStepBuilder extreme(boolean extreme);
    }

    public static interface LimitStepBuilder {

        BlockStepBuilder limit(boolean limit);
    }

    public static interface BlockStepBuilder {

        SideStepBuilder block(int block);
    }

    public static interface SideStepBuilder {

        PositionStepBuilder side(int side);
    }

    public static interface PositionStepBuilder {

        BeaconEpc position(int position);
    }

    private static class Builder implements ExtremeStepBuilder, LimitStepBuilder, BlockStepBuilder, SideStepBuilder,
            PositionStepBuilder {

        private int first;
        private int second;
        private final int serial;
        private int fourth;

        private long value;

        public Builder(Epc epc) {
            this.first = (int) epc.first();
            this.second = (int) epc.second();
            this.serial = (int) epc.serial();
            this.fourth = (int) epc.fourth();
            this.value = 999L;
        }

        @Override
        public LimitStepBuilder extreme(boolean extreme) {
            this.value = value * 10 + (extreme ? 1 : 0);

            return this;
        }

        @Override
        public BlockStepBuilder limit(boolean limit) {
            this.value = this.value * 10 + (limit ? 1 : 0);

            return this;
        }

        @Override
        public SideStepBuilder block(int block) {
            if (block < 0 || block > 99999) {
                throw new IllegalArgumentException("Invalid block " + block);
            }

            this.value = this.value * 100000 + block;

            return this;
        }

        @Override
        public PositionStepBuilder side(int side) {
            if (side < 1 || side > 2) {
                throw new IllegalArgumentException("Invalid side " + side);
            }

            this.value = this.value * 10 + side;

            return this;
        }

        @Override
        public BeaconEpc position(int position) {
            if (position < 0 || position > 99) {
                throw new IllegalArgumentException("Invalid position " + position);
            }

            this.value = this.value * 100 + position;

            return createNewBeacon();
        }

        private BeaconEpc createNewBeacon() {
            GarmentCode garmentCode = GarmentCode.of(this.value);

            long mcct = garmentCode.code() - garmentCode.product() * 1000000000000l;
            this.first = (this.first & 0xFFF80000)
                    | (garmentCode.product() << 15)
                    | (int) (mcct >> 25);
            this.second = (this.second & 0x7F)
                    | (int) (mcct << 7) | 0x40;

            return new BeaconEpc(new GarmentEpc(this.first, this.second, this.serial, this.fourth));
        }
    }

    public ExtremeStepBuilder with() {
        return new Builder(this);
    }

    public int block() {
        return this.block;
    }

    public int number() {
        return this.number;
    }

    public boolean limit() {
        return this.limit;
    }

    public boolean extreme() {
        return this.extreme;
    }

    public boolean right() {
        return this.right;
    }

    public int side() {
        return this.right ? 2 : 1;
    }

    public String info() {
        return this.block + (this.right ? "2" : "1") + this.number + "|" + (this.extreme ? "E" : "_")
                + (this.limit ? "L" : " ");
    }
}
