package com.mengroba.scanneroption.utils.epc;


//TODO Identif. EG + otros campos
public final class GarmentEpc extends Epc {

    private static final long serialVersionUID = -8989415180247453576L;
    
    private static final int SOFT_MASK = 0b1;
    
    private static final int ENABLED_MASK = 0b1000000;

    public static interface BrandStepBuilder {

        SectionTypeStepBuilder brand(int brand);
    }

    public static interface SectionTypeStepBuilder {

        GarmentCodeStepBuilder sectionType(int sectionType);
    }

    public static interface GarmentCodeStepBuilder {

        SoftTagStepBuilder garment(GarmentCode garmentCode);
    }
    
    public static interface SoftTagStepBuilder {

        GarmentEpc softTag();
        
        GarmentEpc hardTag();
    }
    
    private static class Builder implements BrandStepBuilder, SectionTypeStepBuilder, GarmentCodeStepBuilder,
            SoftTagStepBuilder {
        
        private int first;
        private int second;
        private final int serial;
        private int fourth;
        
        public Builder(Epc epc) {
            this.first = (int) epc.first();
            this.second = (int) epc.second();
            this.serial = (int) epc.serial();
            this.fourth = (int) epc.fourth();
        }

        @Override
        public SectionTypeStepBuilder brand(int brand) {
            if (brand < 1 || brand > 60) {
                throw new IllegalArgumentException("Invalid brand " + brand);
            }
            
            this.first = (this.first & 0b11111000000111111111111111111111) | (brand << 21);
            
            return this;
        }

        @Override
        public GarmentCodeStepBuilder sectionType(int sectionType) {
            if (sectionType < 0 || sectionType > 3) {
                throw new IllegalArgumentException("Invalid sectionType " + sectionType);
            }
            
            this.first = (this.first & 0b11111111111001111111111111111111) | (sectionType << 19);

            return this;
        }
        
        @Override
        public SoftTagStepBuilder garment(GarmentCode garmentCode) {
            long mcct = garmentCode.code() - garmentCode.product() * 1000000000000l;
            this.first = (this.first & 0b11111111111110000000000000000000)
                    | (garmentCode.product() << 15)
                    | (int) (mcct >> 25);
            this.second = (this.second & 0b00000000000000000000000001111111)
                    | (int) (mcct << 7) | 0b1000000;
            
            return this;
        }

        @Override
        public GarmentEpc softTag() {
            this.fourth = this.fourth | SOFT_MASK;
            
            return new GarmentEpc(this.first, this.second | ENABLED_MASK, this.serial, this.fourth);
        }

        @Override
        public GarmentEpc hardTag() {
            this.fourth = this.fourth & ~SOFT_MASK;

            return new GarmentEpc(this.first, this.second | ENABLED_MASK, this.serial, this.fourth);
        }
    }
    
    public BrandStepBuilder with() {
        return new Builder(this);
    }
    
    protected GarmentEpc(int first, int second, int serial, int fourth) {
        super(first, second, serial, fourth);
    }
    
    public int brand() {
        return (int) (first() >> 21) & 0b111111;
    }
    
    public int sectionType() {
        return (int) (first() >> 19) & 0b11;
    }

    public GarmentCode garmentCode() {
        int product = (int) (first() >> 15) & 0b1111;
        long mcct = ((first() & 0b111111111111111) << 25) | (second() >> 7);
        long code = product * 1000000000000l + mcct;
        
        return GarmentCode.of(code);
    }

    public boolean enabled() {
        return (second() & ENABLED_MASK) != 0;
    }
    
    public boolean softTag() {
        return (fourth() & SOFT_MASK) != 0;
    }
    
    public GarmentEpc disable() {
        if (enabled()) {
            return new GarmentEpc((int) first(), (int) (second() & ~ENABLED_MASK),
                    (int) serial(), (int) fourth());
        }
        
        return this;
    }
    
    public GarmentEpc enable() {
        if (enabled()) {
            return this;
        }
        
        return new GarmentEpc((int) first(), (int) (second() | ENABLED_MASK),
                (int) serial(), (int) fourth());
    }
}
