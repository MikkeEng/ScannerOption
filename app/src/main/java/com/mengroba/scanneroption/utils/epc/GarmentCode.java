package com.mengroba.scanneroption.utils.epc;

import java.io.Serializable;

/**
 * Clase que representa un código de prenda del tipo PMMMMQQQCCCTT (producto/modelo/calidad/color/talla).
 * 
 * Para pasear GarmentCode.of("0123412312312")
 * 
 * Para construir GarmentCode.product(0).model(1234).quality(123).color(123).size(12)
 * 
 * @author jfreire
 */
public final class GarmentCode implements Serializable {

    private static final long serialVersionUID = -2884768032493384328L;

    private static final char[] COMPACT_PATTERN = "0000000000000".toCharArray();

    private static final char[] PRETTY_PATTERN = "0/0000/000/000/00".toCharArray();

    public static interface ModelStepBuilder {

        QualityStepBuilder model(int model);
    }

    public static interface QualityStepBuilder {

        ColorStepBuilder quality(int quality);
    }

    public static interface ColorStepBuilder {

        SizeStepBuilder color(int color);
    }

    public static interface SizeStepBuilder {

        GarmentCode size(int size);
    }

    private static class Builder implements ModelStepBuilder, QualityStepBuilder, ColorStepBuilder, SizeStepBuilder {

        private long value;

        private Builder(int product) {
            if (product < 0 || product > 9) {
                throw new IllegalArgumentException("Invalid product " + product);
            }
            
            this.value = product;
        }

        public QualityStepBuilder model(int model) {
            if (model < 0 || model > 9999) {
                throw new IllegalArgumentException("Invalid model " + model);
            }
            
            this.value = this.value * 10000 + model;

            return this;
        }

        public ColorStepBuilder quality(int quality) {
            if (quality < 0 || quality > 999) {
                throw new IllegalArgumentException("Invalid quality " + quality);
            }
            
            this.value = this.value * 1000 + quality;

            return this;
        }

        public SizeStepBuilder color(int color) {
            if (color < 0 || color > 999) {
                throw new IllegalArgumentException("Invalid color " + color);
            }
            
            this.value = this.value * 1000 + color;

            return this;
        }

        public GarmentCode size(int size) {
            if (size < 0 || size > 99) {
                throw new IllegalArgumentException("Invalid size " + size);
            }
            
            return new GarmentCode(this.value * 100 + size);
        }
    }

    public static ModelStepBuilder product(int product) {
        return new Builder(product);
    }

    public static GarmentCode of(long code) {
        if (code > 99999999999999l) {
            throw new IllegalArgumentException("Invalid GarmentCode " + code);
        }

        if(code > 9999999999999l && code < 99999999999999l) {
            code = code / 10;
        }

        return new GarmentCode(code);
    }

    public static GarmentCode of(String text) {
        int length = text.length();

        if(length == COMPACT_PATTERN.length + 1) {
            text = text.substring(0, COMPACT_PATTERN.length);
            length = text.length();
        }

        if (length != PRETTY_PATTERN.length && length != COMPACT_PATTERN.length) {
            throw new IllegalArgumentException("Invalid GarmentCode " + text);
        }

        long value = 0;

        for (int i = 0; i < length; i++) {
            final char ch = text.charAt(i);

            if (ch >= '0' && ch <= '9') {
                value = value * 10 + (ch - '0');
            } else if (PRETTY_PATTERN[i] != ch) {
                throw new IllegalArgumentException("Invalid GarmentCode " + text);
            }
        }

        return new GarmentCode(value);
    }

    private final long code;

    private GarmentCode(long code) {
        this.code = code;
    }

    public int product() {
        return (int) (this.code / 1000000000000l);
    }

    public int model() {
        long subCode = this.code / 100000000;

        return (int) (subCode - (subCode / 10000) * 10000);
    }

    public int quality() {
        long subCode = this.code / 100000;

        return (int) (subCode - (subCode / 1000) * 1000);
    }

    public int color() {
        long subCode = this.code / 100;

        return (int) (subCode - (subCode / 1000) * 1000);
    }

    public int size() {
        return (int) (this.code - (this.code / 100) * 100);
    }

    public long code() {
        return this.code;
    }

    @Override
    public int hashCode() {
        return (int) this.code;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof GarmentCode && ((GarmentCode) obj).code == code;
    }

    @Override
    public String toString() {
        return toString(COMPACT_PATTERN);
        //return toString(PRETTY_PATTERN);
    }

    public String barcode() {
        return toString(COMPACT_PATTERN);
    }

    private String toString(final char[] pattern) {
        char[] chars = new char[pattern.length];
        long value = this.code;

        for (int i = pattern.length - 1; i >= 0; i--) {
            if (pattern[i] == '0') {
                final long newValue = value / 10;
                chars[i] = (char) ('0' + (value - (newValue * 10)));
                value = newValue;
            } else {
                chars[i] = pattern[i];
            }
        }

        return new String(chars);
    }
}
